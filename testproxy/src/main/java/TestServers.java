import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.testng.annotations.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;

public class TestServers {

    //期望返回码
    private static int expectedCode = 200;

    //记录连接失败的Http代理
    private  static ArrayList<String> listHttp = new ArrayList<String>();
    //记录连接失败的Https代理
    private  static ArrayList<String> listHttps = new ArrayList<String>();

    @Test
    public static void get() {

        Resource resIP = new ClassPathResource("/conf/ipData");
        Properties propsIP = null;
        try {
            propsIP = PropertiesLoaderUtils.loadProperties(resIP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String key1 : propsIP.stringPropertyNames()) {
            System.out.println("********************开始测试HTTP代理:ip: " + propsIP.getProperty(key1) + " *********************");

                Assertion.flag = true;
                int statusCode=0;
                CloseableHttpResponse response = null;

                CloseableHttpClient client = HttpClients.createDefault();

                HttpHost target = new HttpHost(propsIP.getProperty(key1), -1, "http");

                HttpGet request = new HttpGet("/");
                request.setHeader("Host","blcpt.6blc.com");
                request.setHeader("Proxy-Connection","close");
                request.setHeader("Connection","close");

                System.out.println("Executing request " + request.getRequestLine() + " to " + target);

                try {
                    try {
                        response = client.execute(target,request);
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

                        System.out.println("响应内容:" + response.toString());
                        System.out.println("statusCode:" + statusCode);
                    }
                    // 释放资源
                    try {
                        client.close();
                        response.close();
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

                Assertion.flag = true;
                CloseableHttpResponse response1 = null;
                int statusCode1 = 0;

                //确保没有异常抛出begin
                final SSLConnectionSocketFactory sslsf;
                try {
                    sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(),
                            NoopHostnameVerifier.INSTANCE);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new PlainConnectionSocketFactory())
                        .register("https", sslsf)
                        .build();

                final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
                cm.setMaxTotal(100);

                CloseableHttpClient client1 = null;

                client1 = HttpClients.custom()
                        .setSSLSocketFactory(sslsf)
                        .setConnectionManager(cm)
                        .build();
                //确保没有异常抛出end

                HttpHost target1 = new HttpHost(propsIP.getProperty(key1), -1, "https");
                HttpGet httpGet = new HttpGet("/");
                httpGet.setHeader("Host","7747a.com");
                //给get请求设置代理地址--end

                //打印目标地址和代理ip
                System.out.println("Executing request " + httpGet.getRequestLine() + " to " + target1);

                try {
                    try {
                        //执行get请求
                        response1 = client1.execute(target1,httpGet);
                    } catch (Exception e) {
                        Assertion.verifyEquals(1,2);
                        listHttps.add(propsIP.getProperty(key1));
                        e.printStackTrace();
                        continue;
                    }
                    // getEntity()
                    HttpEntity httpEntity1 = response1.getEntity();
                    statusCode1 = response1.getStatusLine().getStatusCode();
                    if (httpEntity1 != null) {
                        // 打印响应内容
                        System.out.println("响应内容:" + response1.toString());
                        System.out.println("statusCode:" + statusCode1);
                    }
                    // 释放资源
                    try {
                        client1.close();
                        response1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assertion.verifyEquals(statusCode1, expectedCode);
                if(!Assertion.flag){
                    listHttps.add(propsIP.getProperty(key1));
                }

        }
        System.out.println("HTTP未连通代理： "+listHttp.toString());
        System.out.println("HTTPS未连通代理： "+listHttps.toString());
    }
}
