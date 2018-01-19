import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Properties;

public class FinalTest {

    //期望返回码
    private static int expectedCode = 200;

    //记录连接失败的Http代理
    private  static ArrayList listHttp = new ArrayList();
    //记录连接失败的Https代理
    private  static ArrayList listHttps = new ArrayList();

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactoryEx sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            System.out.println("新建httpclient没有报错！！！！！！！！！！！！！！！！");

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultHttpClient();
        }
    }

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
                Assertion.flag = true;
                int statusCode=0;
                CloseableHttpResponse response = null;

                HttpClientBuilder build = HttpClients.custom();
                HttpHost proxy = new HttpHost(propsIP.getProperty(key1));
                CloseableHttpClient client = build.setProxy(proxy).build();
                HttpGet request = new HttpGet(propsHttpURI.getProperty(key2));

                try {
                        try {
                        response = client.execute(request);
                    } catch (Exception e) {
                        Assertion.verifyEquals(1,2);
                        listHttp.add(propsIP.getProperty(key1));
                        e.printStackTrace();
                        continue;
                    }
                    // getEntity()
                    HttpEntity httpEntity = response.getEntity();
                    statusCode = response.getStatusLine().getStatusCode();
                    if (httpEntity != null) {
                        // 打印响应内容
                        System.out.println("statusCode:" + statusCode);
                    }
                    // 释放资源
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assertion.verifyEquals(statusCode, expectedCode);
                if(!Assertion.flag){
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
                Assertion.flag = true;
                int statusCode1=0;
                HttpResponse response = null;
//                HttpClientBuilder build = HttpClients.custom();
//                HttpHost proxy = new HttpHost(propsIP.getProperty(key1));
//                CloseableHttpClient client = build.setProxy(proxy).build();
//                HttpGet request = new HttpGet(propsHttpsURI.getProperty(key2));

//                CloseableHttpClient client = createSSLClientDefault();
                HttpClient client = getNewHttpClient();

                // HttpClient
//                CloseableHttpClient client = HttpClients.createDefault();

                // 依次是代理地址，代理端口号，协议类型
                HttpHost proxys = new HttpHost(propsIP.getProperty(key1), 443, "http");
                RequestConfig configs = RequestConfig.custom().setProxy(proxys).build();

                // 请求地址
//                HttpHead request = new HttpHead(propsHttpsURI.getProperty(key2));
                HttpGet request = new HttpGet(propsHttpsURI.getProperty(key2));
                request.setConfig(configs);

                try {
                    try {
                        response = client.execute(request);
                    } catch (Exception e) {
                        Assertion.verifyEquals(1,2);
                        listHttps.add(propsIP.getProperty(key1));
                        e.printStackTrace();
                        continue;
                    }
                    // getEntity()
                    HttpEntity httpEntitys = response.getEntity();
                    statusCode1 = response.getStatusLine().getStatusCode();
                    if (httpEntitys != null) {
                        // 打印响应内容
                        System.out.println("响应内容:" + response.toString());
                        System.out.println("statusCode:" + statusCode1);
                    }
                    // 释放资源
//                    try {
//                        client.;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assertion.verifyEquals(statusCode1, expectedCode);
                if(!Assertion.flag){
                    listHttps.add(propsIP.getProperty(key1));
                }
            }
        }
        System.out.println("HTTP未连通代理： "+listHttp.toString());
        System.out.println("HTTPS未连通代理： "+listHttps.toString());
    }
}
