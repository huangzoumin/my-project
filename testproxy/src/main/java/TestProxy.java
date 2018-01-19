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

public class TestProxy {

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

        for(String key1 : propsIP.stringPropertyNames()){
            System.setProperty("proxyType", "4");
            System.setProperty("proxyHost", propsIP.getProperty(key1));
            System.setProperty("proxySet", "true");
            System.out.println("********************开始测试代理:ip: "+ propsIP.getProperty(key1)+" *********************");
            for (String key2 : propsURI.stringPropertyNames())
            {

                CloseableHttpClient client= HttpClients.createDefault();     //定义一个默认的请求客户端

                String tmp3 = propsURI.getProperty(key2);

                HttpGet get=new HttpGet(tmp3);    // 定义一个get请求

                CloseableHttpResponse response=null;        //定义一个响应

                int statusCode =0;

                //设置http代理


//                System.setProperty("http.proxySet", "true");
//                String tmp1 = propsIP.getProperty(key1);
//                System.setProperty("http.proxyHost", tmp1);

//                //设置https代理
//                System.setProperty("https.proxySet", "true");
//                String tmp2 = propsIP.getProperty(key1);
//                System.setProperty("https.proxyHost", tmp2);

                try {
                    //发送请求
                    response=client.execute(get);
                    statusCode = response.getStatusLine().getStatusCode();

                    //测试http/https代理
                    System.out.println(propsURI.getProperty(key2)+" 返回码:    ");
                    System.out.println(statusCode);//打印响应状态码，200表示成功

                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //判断返回的结果是否包含期望的关键字
                Assert.assertEquals(statusCode,200);
            }
        }

    }
}
