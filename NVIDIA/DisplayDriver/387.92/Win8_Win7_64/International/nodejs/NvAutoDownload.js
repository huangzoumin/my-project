/* Copyright (c) 2016-2017, NVIDIA CORPORATION.  All rights reserved.
 *
 * NVIDIA CORPORATION and its licensors retain all intellectual property
 * and proprietary rights in and to this software, related documentation
 * and any modifications thereto.  Any use, reproduction, disclosure or
 * distribution of this software and related documentation without an express
 * license agreement from NVIDIA CORPORATION is strictly prohibited.
 */

(function(){

    'use strict'
    var downloaderAPI     = require('./Downloader.node'),
        https             = require("https"),
        _                 = require("underscore"),
        store             = require('nv-localstore'),
        fs                = require('fs');

    var NvAutoGFEDownloadVersion = '1.0.2';
    var downloadQueue = []; //temp queue
    var DOWNLOAD_STATUS  = {
		UNDEFINED: -1,
		DOWNLOADING: 0,
		PAUSED: 1,
		COMPLETED: 2,
		RETRYING: 3,
		PAUSED_FOR_FAILED: 4,
		STOPPED_FOR_FAILED: 5,
		CHECKSUM_VERIFY_FAILS: 6,
		SIGNATURE_VERIFICATION_FAILS: 7,
		DISK_WRITE_FAIL: 8,
		DOWNLOAD_ERROR: 9,
		OPERATION_IN_PROGRESS: 10
	};
	var DOWNLOAD_TYPE = {
		OTHER: 0,
		DRIVER_DOWNLOAD: 1,
		AUTO_DRIVER_DOWNLOAD: 2,
		GFE_SELF_UPDATE: 3,
		GFE_SELF_UPDATE_BETA: 4
	};
	var NV_ERRORCODES = {
		CHECKSUM_VERIFY_FAILS: 1001,
		SIGNATURE_VERIFICATION_FAILS: 1002,
		DISK_WRITE_FAIL: 1003
    };
    var CHECKFORUPDATE_STATUS = {
        STARTED: 0,
        FINISHED: 1,
        FAILED: 2
    };
	var CURLE_OK = 0;
    var nvIO;
    var WSUrlVersion = 'v1.0';
    var WSHost = 'https://services.gfe.nvidia.com/GFE/';
    var WSURLTimeout = 30 * 1000; //30sec
    var WSChannel;
    var pending_rest_responses = [];
    var clientVersion;
	var taskList = [];
    var nvAppDataPath = "";
    var GFEWSConfigFile = "config.json";
    var NvidiaPath = "NVIDIA GeForce Experience";
    var NvidiaWwwPath = NvidiaPath + "/www";
    var OSVersion = "";
    var OSArch64Bit = false;
    var Logger = undefined;
    var nvUtil = undefined;
    var exitCriteria = false;
    var isBeta = "0";

    function NativeCallbackToPromise(resolve, reject) {
        return function (err, data) {
            if (data) {
                //TODO: log
                setImmediate((function () { resolve(data); }));
            }
            else {
                setImmediate((function () { reject(err); }));
            }
        }
    };

    function statusUpdate(status){
        if(!status) return;

        if(status.status == DOWNLOAD_STATUS.COMPLETED && _.contains(taskList, status.taskId)){
            extractInstaller(status);
            killRequested(); //request for exit if download is completed or previously downloaded.
        }
    };
	
    var deleteFolderRecursive = function (path) {
        if (fs.existsSync(path)) {
            fs.readdirSync(path).forEach(function (file, index) {
                var curPath = path + "/" + file;
                if (fs.lstatSync(curPath).isDirectory()) { // recurse
                    deleteFolderRecursive(curPath);
                } else { // delete file
                    fs.unlinkSync(curPath);
                }
            });
            fs.rmdirSync(path);
        }
    };

    function extractInstaller(status) {
        var downloadLoc = status.downloadedLocation;
        var path = downloadLoc.substring(0, downloadLoc.lastIndexOf('\\'));
        path = path.substring(0, path.lastIndexOf('\\'));

        if (process.platform === 'win32') {
            var execFileSync = require('child_process').execFileSync;
            try {
                var extractPath = path + '\\latest';
                deleteFolderRecursive(extractPath);
                var buffer = execFileSync('../' + NvidiaPath + '/7z.exe',
                                          ['x', '-o' + extractPath, '-y', downloadLoc], { encoding: 'utf8' });
                if (buffer.toString().indexOf('Everything is Ok') != -1) {
                    status.downloadedLocation = path + '\\latest\\setup.exe';
                } else {
                    Logger.error('Extraction didnt finish with success');
                }
            } catch(e) {
                 Logger.error('Extraction failed: ' + e);
            }
        } else {
            //res.status(400);
        }
        createUpdateFile(status);
    };

    function createUpdateFile(status){
        //access appdata and create the gfeupdate.json file with url.
        var payload = {};
        var downloadLoc = status.downloadedLocation;
        payload.url = status.downloadedLocation;
        var path = downloadLoc.substring(0, downloadLoc.lastIndexOf('\\'));
        path = path.substring(0, path.lastIndexOf('\\'));
        fs.writeFileSync(nvAppDataPath + "\\gfeupdate.json", JSON.stringify(payload));
        fs.writeFileSync(path + "\\gfeupdate.json", JSON.stringify(payload));
    }

    function clearPendingRequests(data){
        data = data || {};
        if(pending_rest_responses.length > 0){
             _.each(pending_rest_responses, function(res, key){
                if(data.status == DOWNLOAD_STATUS.COMPLETED && data.percentComplete == 100){
                    res.send(data.downloadedLocation); //HTTP end()
                } else{
                    res.status(404).send('');
                }
            });
        }
        pending_rest_responses = [];
    };

    function initiateGFEDownload(val){
        return new Promise(function(resolve, reject){
			try{
				var betaOption = store.getValue('autoGFEbeta');
				var downloadType = betaOption === "1" ? DOWNLOAD_TYPE.GFE_SELF_UPDATE_BETA : DOWNLOAD_TYPE.GFE_SELF_UPDATE;
				downloaderAPI.startDownload(NativeCallbackToPromise(resolve, reject), val.version, val.url, downloadType);
                downloadQueue.push({url: val.url, ver: val.version});
			} catch (err){
				Logger.error(err);
				reject(err);
			}
		});
    };

    function onGFEUpdateFound(data, isBetaRequested){

        try {
            var retData = {};
            retData.isBetaRequested = isBetaRequested;
            data = JSON.parse(data);
        }catch(e){
            Logger.error("Update Service error: " + data);
            killRequested();
            clearPendingRequests();
            retData.returnCode = CHECKFORUPDATE_STATUS.FAILED;
            nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);
            return;
        }
        if(data.url == undefined) {
            Logger.error('Invalid/Empty update Url');
            killRequested();
            clearPendingRequests();
            retData.returnCode = CHECKFORUPDATE_STATUS.FINISHED;
            retData.url = "";
            nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);
            return;
        }
        retData.returnCode = CHECKFORUPDATE_STATUS.FINISHED;
        retData.isBeta = data.isBeta;
        retData.url = data.url;
        retData.version = data.version;
        nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);
        var url = data.url;
        var ver = data.version;
        var filen = url.match(/([^\/.]+)$|([^\/]+)(\.[^\/.]+)$/)[0];
        initiateGFEDownload({filename: filen, url: url, version: ver})
            .then(function(data){
                taskList.push(data.taskId);
                clearPendingRequests(data); //Dont block the REST requests
                if(data.status == DOWNLOAD_STATUS.COMPLETED){
                    createUpdateFile(data);
                    killRequested();
                }
            })
            .catch(function(d){
                Logger.error('initiateGFEDownload: ' + d);
            });

        /* GFE update response is not XML anymore */
    };

    function getWSSelfUpdateUrl() {
        var betaOption = store.getValue('autoGFEbeta');
		var betaStr = "Official";
		if(betaOption === "") {
			betaOption = isBeta;
		}
		betaStr = betaOption === "1" ? "Beta" : "Official";
		store.updateValue('autoGFEbeta', betaOption);

        if(WSChannel){
            betaStr = WSChannel;
        }
        var url = "";
        if (clientVersion) {
            //version query is under discussion, using 3.0.0.0
            var params = {};
            params.version = clientVersion;
            params.channel = betaStr;
            params.osVersion = OSVersion;
            params.is64bit = OSArch64Bit;
            //'{"version": "3.0.0.0", "channel": "' + betaStr +'"}';
            url = WSHost + WSUrlVersion + '/self-update/?' + JSON.stringify(params);
        }
        return { updateUrl: url, isBetaRequested: betaOption === "1" ? true : false };
    };

    function checkForGFEUpdates(){

        var auto_download = store.getValue('autoGFE');
        if(auto_download == "") auto_download = "1";
        if(auto_download !== "1"){
			clearPendingRequests();
			return; //return if check for updates is disabled
		}
        if(OSVersion === ""){
            clearPendingRequests();
           	return;
        }
        
        var retSelfUpdateUrl = getWSSelfUpdateUrl();
        var updateCheckUrl = retSelfUpdateUrl.updateUrl;
        Logger.info('checking for GFE Update... ' + updateCheckUrl);

        var retData = {};
        retData.returnCode = CHECKFORUPDATE_STATUS.STARTED;
        retData.isBetaRequested = retSelfUpdateUrl.isBetaRequested;
        nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);

        var req = https.get(updateCheckUrl,
            (res) => {    
                    var data = "";
                    res.setEncoding('binary');
                    res.on('data', (chunk)=>{ data += chunk; });
                    res.on('end', () => {
                        onGFEUpdateFound(data, retSelfUpdateUrl.isBetaRequested);
                                        }
                          );
                    res.on('error', (e) => {
                                                Logger.error(e);
                                           });
                    Logger.info(' OTA response statusCode: ' + res.statusCode);
                    Logger.info(' OTA response headers: ' + JSON.stringify(res.headers));
                });
        req.on('error', function(err){
            Logger.error('Update Check Http Error ' + err);
            killRequested();
            clearPendingRequests();
            retData.returnCode = CHECKFORUPDATE_STATUS.FAILED;
            nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);
         });
        req.setTimeout(WSURLTimeout, function(){
            Logger.error('Update Service URL timedout');
            //exit if request has timedout
            //TODO: do a retry for 2-3 times before exiting
            killRequested();
            clearPendingRequests();
            retData.returnCode = CHECKFORUPDATE_STATUS.FAILED;
            nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);
        });
    };
    
    function checkForDriverUpdates(){
        //has the user disabled auto check for drivers?

    };

    function setupDownloadCompleteCallback(){

        downloaderAPI.setDownloadStatusCallback(statusUpdate);
    };

    function promiseChainCheckForUpdates() {
		Promise.all([new Promise(getClientVersionPromise), new Promise(getGFEBetaPromise)]).then(function () {
            initDownloadURLAndConfig();
            checkForGFEUpdates();
        }).catch(function (err) {
            var retData = {};
            retData.returnCode = CHECKFORUPDATE_STATUS.FAILED;
            nvIO.emit('/download/v.0.1/checkforGFEUpdate', retData);
            Logger.error("Failed to perform update check " + err);
        });
    };

    function setupDownloaderEndpoints(app){
        app.get('/gfeupdate/autoGFEDownload/:key', function(req, res){
            res.end(store.getValue(req.params.key));
        });
        app.post('/gfeupdate/autoGFEDownload/:key', function (req, res) {
            if (req.is('text/*')) {
                req.setEncoding('utf8');
                req.text = '';
                req.on('data', function (chunk) { req.text += chunk });
                req.on('end', function () {
                    store.updateValue(req.params.key, req.text);
                    setImmediate(function () {
                        Logger.info('Emitting ' + req.url);
                        nvIO.emit(req.url, req.text);
                        if ((req.params.key === "autoGFEbeta") && (req.text === "1"))
                        {
                            promiseChainCheckForUpdates();
                        }
                    });
                    res.end(req.text);
                });
            }
        });

        //POST - Begin installation of GFE
        app.post('/gfeupdate/autoGFEInstall/', function (req, res) {
            if (req.is('text/*')) {
                req.setEncoding('utf8');
                req.text = '';
                req.on('data', function (chunk) { req.text += chunk });
                req.on('end', function () {
                    if (process.platform === 'win32') {
                        var childProc = require('child_process').exec;
                        //double quote to make up for the spaces in command/folder
                        childProc("\"" + req.text + "\"", function (err, data) {
                            //data.toString();
                        });
                        res.end();
                    } else {
                        //res.status(400);
                    }
                });
            }
        });

        //GET new GFE version (explicit)
        app.get('/gfeupdate/autoGFENewVersion/', function(req, res){
            pending_rest_responses.push(res);
            exitCriteria = false;
            checkForGFEUpdates();
        });
    };

    function killRequested(data){
        exitCriteria = true;
    };

    function getClientVersionPromise(resolve, reject) {
        try {
            clientVersion = nvUtil.GetGFEVersionSync();
            Logger.info("Read GFExperience version from registry: " + clientVersion);
            
        }
        catch (e) {
            Logger.error("Cannot read GFExperience version value from registry" + e);
            reject(e);
            return;
        }

        try {
            var arch = nvUtil.GetGFEArchSync();
            Logger.info("Read GFExperience architecture value from registry: " + arch);
            if (arch.toLowerCase() === 'x64') {
                OSArch64Bit = true;
            }
        }
        catch (e) {
            Logger.error("Cannot read GFExperience architecture value from registry" + e);
        }

        resolve();
    }

    function getGFEBetaPromise(resolve, reject) {
        try {
            var gfe3beta = nvUtil.GetGFE3BetaFlagSync();
            Logger.info("Read GFE 3 BETA flag from registry: " + gfe3beta);
            //if current gfe build is beta, we don't carry forward perisistence, so don't bother GFE2
            if (gfe3beta == 1) {
                isBeta = "1";
            }
        }
        catch (e) {
            Logger.error("Cannot read GFE 3 BETA flag from registry" + e);
            readGFE2Betaflag();
            // Not a really hard error to DIE.
        }
        //remove GFE2Beta regkey unconditionally
        try {
            nvUtil.DeleteGFE2BetaFlagSync();
        }
        catch (e) {
            Logger.error("Cannot delete GFE 2 BETA flag from registry" + e);
        }
        resolve();
    }

    function readGFE2Betaflag()
    {
        try {
            var gfe2beta = nvUtil.GetGFE2BetaFlagSync();
            Logger.info("Read GFE 2 BETA flag from registry: " + gfe2beta);
            if (gfe2beta == 1) {
                isBeta = "1";
            }
        }
        catch (e) {
            Logger.error("Cannot read GFE 2 BETA flag from registry" + e);
            // Not a really hard error to DIE.
        }
    }

    function initDownloadURLAndConfig() {
        var os = require('os');

        var rel = os.release().split('.');
        OSVersion = rel[0] + "." + (rel[1] || "");
        try{
            if(OSArch64Bit){
                NvidiaWwwPath = "../../Program Files/NVIDIA Corporation/" + NvidiaWwwPath;
                NvidiaPath = "../../Program Files/NVIDIA Corporation/" + NvidiaPath;
            }
            var config = require("./"+GFEWSConfigFile);
        }catch(e){
            Logger.error(e);
            return;
        }
        if(config.gfservices){
            WSHost = config.gfservices.server;
            WSUrlVersion = config.gfservices.version;
            if(config.gfservices.selfupdate){
                WSChannel = config.gfservices.selfupdate.channel || WSChannel;
                clientVersion = config.gfservices.selfupdate.verOverride || clientVersion;
            }
        }
    };

    module.exports = {
        initialize: function (app, io, logger, util, NvCommonTasks) {
            return new Promise(
                function (resolve, reject) {
                    setImmediate(function init() {
                        nvIO = io;
                        Logger = logger;
                        nvUtil = util;

                        if (NvCommonTasks.setIntervalCallback) {
                            NvCommonTasks.setIntervalCallback(checkForGFEUpdates);
                        };

                        setupDownloaderEndpoints(app);

                        // Check for updates after startup.
                        promiseChainCheckForUpdates();

                        setupDownloadCompleteCallback();
                        resolve();
                    });
                });
        },

        stop: function () {
            _.each(downloadQueue, function (elem) {
                downloaderAPI.stopDownload(function () { }, elem.ver);
            });
        },

        version: function () {
            return NvAutoGFEDownloadVersion;
        },

        setAppDataPath: function (path) {
            nvAppDataPath = path;
        },

        canNodeExitNow: function () {
            return exitCriteria;
        }
    }
}());