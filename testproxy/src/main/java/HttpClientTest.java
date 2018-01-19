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
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class HttpClientTest {
    @Test
    public static void get(){
        // 创建HttpClientBuilder  
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // HttpClient  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        // 依次是目标请求地址，端口号,协议类型  
        HttpHost target = new HttpHost("http://blcpt.6blc.com", 80,
                "http");
        // 依次是代理地址，代理端口号，协议类型  
        HttpHost proxy = new HttpHost("103.86.84.236", 80, "http");
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

        // 请求地址  
        HttpGet httpGet = new HttpGet("http://blcpt.6blc.com");
        httpGet.setConfig(config);
        // 创建参数队列  
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        // 参数名为pid，值是2  
        formparams.add(new BasicNameValuePair("pid", "2"));

        UrlEncodedFormEntity entity;
        int statusCode=0;
        try {
            entity = new UrlEncodedFormEntity(formparams, "UTF-8");

            CloseableHttpResponse response = closeableHttpClient.execute(
                    target, httpGet);
            // getEntity()  
            HttpEntity httpEntity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            if (httpEntity != null) {
                // 打印响应内容  
                System.out.println("response:"
                        + EntityUtils.toString(httpEntity, "UTF-8"));
            }
            // 释放资源  
            closeableHttpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //判断返回的结果是否包含期望的关键字
        Assert.assertEquals(statusCode,200);
    }
}  