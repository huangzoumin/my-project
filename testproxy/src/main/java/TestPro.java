import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Listeners({AssertionListener.class})
public class TestPro {

    //端口
    private static String port = "80";

    //期望返回码
    private static int expectedCode = 200;

    //记录连接失败的Http代理
    private  static ArrayList listHttp = new ArrayList();
    //记录连接失败的Https代理
    private  static ArrayList listHttps = new ArrayList();

    @Test
    public static void get() {

        Resource resIP = new ClassPathResource("/conf/ipData");
        Properties propsIP = null;
        try {
            propsIP = PropertiesLoaderUtils.loadProperties(resIP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //HTTP测试
        Resource resHttpURI = new ClassPathResource("/conf/httpData");
        Properties propsHttpURI = null;
        try {
            propsHttpURI = PropertiesLoaderUtils.loadProperties(resHttpURI);
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (String key1 : propsIP.stringPropertyNames()) {
            System.out.println("********************开始测试HTTP代理:ip: " + propsIP.getProperty(key1) + " *********************");
            for (String key2 : propsHttpURI.stringPropertyNames()) {

                // 创建HttpClientBuilder
                HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
                // HttpClient
                CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
                // 依次是目标请求地址，端口号,协议类型
                HttpHost target = new HttpHost(propsIP.getProperty(key1), 80,
                        "http");
                // 依次是代理地址，代理端口号，协议类型
                HttpHost proxy = new HttpHost(propsIP.getProperty(key1), 80, "http");
                RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

                // 请求地址
                HttpGet httpGet = new HttpGet(propsHttpURI.getProperty(key2));
                httpGet.setConfig(config);
                // 创建参数队列
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                // 参数名为pid，值是2
                formparams.add(new BasicNameValuePair("pid", "2"));

                UrlEncodedFormEntity entity;
                int statusCode = 0;
                try {
                    try {
                        entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                    } catch (Exception e) {
                        throw e;
                    }

                    CloseableHttpResponse response = null;
                    try {
                        response = closeableHttpClient.execute(
                                target, httpGet);
                    } catch (Exception e) {
                        Assertion.verifyEquals(1,2);
                        listHttp.add(propsIP.getProperty(key1));
                        continue;
                    }
                    // getEntity()
                    HttpEntity httpEntity = response.getEntity();
                    statusCode = response.getStatusLine().getStatusCode();
                    if (httpEntity != null) {
                        // 打印响应内容
                        System.out.println("statusCode:"
                                + statusCode);
                    }
                    // 释放资源
                    try {
                        closeableHttpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assertion.verifyEquals(statusCode, expectedCode);
                if(Assertion.flag!=true){
                    listHttp.add(propsIP.getProperty(key1));
                }
            }
        }

        //HTTPS测试
        Resource resHttpsURI = new ClassPathResource("/conf/httpsData");
        Properties propsHttpsURI = null;
        try {
            propsHttpsURI = PropertiesLoaderUtils.loadProperties(resHttpsURI);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String key1 : propsIP.stringPropertyNames()) {
            System.out.println("********************开始测试HTTPS代理:ip: " + propsIP.getProperty(key1) + " *********************");
            for (String key2 : propsHttpsURI.stringPropertyNames()) {

                // 创建HttpClientBuilder
                HttpClientBuilder httpsClientBuilder = HttpClientBuilder.create();
                // HttpClient
                CloseableHttpClient closeableHttpsClient = httpsClientBuilder.build();
                // 依次是目标请求地址，端口号,协议类型
                HttpHost target = new HttpHost(propsIP.getProperty(key1), 443,
                        "https");
                // 依次是代理地址，代理端口号，协议类型
                HttpHost proxys = new HttpHost(propsIP.getProperty(key1), 443, "https");
                RequestConfig configs = RequestConfig.custom().setProxy(proxys).build();

                // 请求地址
                HttpGet httpsGet = new HttpGet(propsHttpsURI.getProperty(key2));
                httpsGet.setConfig(configs);
                // 创建参数队列
                List<NameValuePair> formparamss = new ArrayList<NameValuePair>();
                // 参数名为pid，值是2
                formparamss.add(new BasicNameValuePair("pid", "2"));

                UrlEncodedFormEntity entitys;
                int statusCodes = 0;
                try {
                    try {
                        entitys = new UrlEncodedFormEntity(formparamss, "UTF-8");
                    } catch (Exception e) {
                        throw e;
                    }

                    CloseableHttpResponse response = null;
                    try {
                        response = closeableHttpsClient.execute(
                                target, httpsGet);
                    } catch (Exception e) {
                        Assertion.verifyEquals(1,2);
                        listHttps.add(propsIP.getProperty(key1));
                        continue;
                    }
                    // getEntity()
                    HttpEntity httpEntitys = response.getEntity();
                    statusCodes = response.getStatusLine().getStatusCode();
                    if (httpEntitys != null) {
                        // 打印响应内容
                        System.out.println("statusCode:"
                                + statusCodes);
                    }
                    // 释放资源
                    try {
                        closeableHttpsClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assertion.verifyEquals(statusCodes, expectedCode);
                if(Assertion.flag!=true){
                    listHttps.add(propsIP.getProperty(key1));
                }
            }
        }
        System.out.println("HTTP未连通代理： "+listHttp.toString());
        System.out.println("HTTPS未连通代理： "+listHttps.toString());
    }
}
