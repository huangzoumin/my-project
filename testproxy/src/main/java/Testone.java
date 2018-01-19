import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Properties;

public class Testone {

    //端口
    private static String port = "80";

    //期望返回码
    private static int expectedCode = 200;

    @Test
    public static void get(){

        Resource resIP = new ClassPathResource("/conf/ipData");
        Properties propsIP = null;
        try {
            propsIP = PropertiesLoaderUtils.loadProperties(resIP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Resource resURI = new ClassPathResource("/conf/httpData");
        Properties propsURI = null;
        try {
            propsURI = PropertiesLoaderUtils.loadProperties(resURI);
        } catch (IOException e) {
            e.printStackTrace();
        }


            System.out.println("********************开始测试代理:ip: "+ "103.86.84.236"+"*********************");

                CloseableHttpClient client= HttpClients.createDefault();     //定义一个默认的请求客户端


                HttpGet get=new HttpGet("http://blcpt.6blc.com");    // 定义一个get请求

                CloseableHttpResponse response=null;        //定义一个响应

                int statusCode =0;

                //设置http代理
                System.setProperty("http.proxySet", "true");

                System.setProperty("http.proxyHost", "103.86.84.236");

                //设置https代理
                System.setProperty("https.proxySet", "true");

                System.setProperty("https.proxyHost", "103.86.84.236");

                try {
                    //发送请求
                    response=client.execute(get);
                    statusCode = response.getStatusLine().getStatusCode();

                    //测试http/https代理
                    System.out.println(""+" 返回码:    ");
                    System.out.println(statusCode);//打印响应状态码，200表示成功

                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assert.assertEquals(statusCode,expectedCode);



    }
}
