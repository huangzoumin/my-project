import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
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

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

/**
 * How to send a request via proxy.--可用！！
 *
 * @since 4.0
 */
public class Example {

    public static void main(String[] args)throws Exception {

        try {
            HttpHost target = new HttpHost("youtube.com", 443, "https");
            HttpHost proxy = new HttpHost("23.111.166.114", 8080, "http");

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

            CloseableHttpClient client = null;

            client = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .setConnectionManager(cm)
                    .build();
            //确保没有异常抛出end

            //给get请求设置代理地址-begin
            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            HttpGet request = new HttpGet("/");
            request.setConfig(config);
            //给get请求设置代理地址-end

            System.out.println("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy);
            //发送请求
            CloseableHttpResponse response = client.execute(target, request);

            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println("响应内容:" + response.toString());
            } finally {
                response.close();
                client.close();
            }
        } finally {

        }
    }

}