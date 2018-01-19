import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;

public class HttpClientTrustingAllCertsTest {

    @Test
    public void shouldAcceptUnsafeCerts() throws Exception {
        DefaultHttpClient httpclient = httpClientTrustingAllSSLCerts();
        // 依次是代理地址，代理端口号，协议类型
        HttpHost proxys = new HttpHost("216.105.168.34", 443, "https");
        RequestConfig configs = RequestConfig.custom().setProxy(proxys).build();

        // 请求地址
//                HttpHead request = new HttpHead(propsHttpsURI.getProperty(key2));
        HttpGet request = new HttpGet("https://7747a.com");
        request.setConfig(configs);
//        HttpGet httpGet = new HttpGet("https://7747a.com");
        HttpResponse response = httpclient.execute( request );
        HttpEntity httpEntitys = response.getEntity();
        System.out.println(response.toString());
        assertEquals("HTTP/1.1 200 OK", response.getStatusLine().toString());
    }

    private DefaultHttpClient httpClientTrustingAllSSLCerts() throws NoSuchAlgorithmException, KeyManagementException {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustingManager(), new java.security.SecureRandom());

        SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
        Scheme sch = new Scheme("https", 443, socketFactory);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        return httpclient;
    }

    private TrustManager[] getTrustingManager() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

        } };
        return trustAllCerts;
    }
}