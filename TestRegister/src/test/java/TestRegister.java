import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TestRegister {

    public static void main(String[] args){

        int num = 1;

        CloseableHttpClient httpclient = HttpClients.createDefault();

//        HttpHost target = new HttpHost("121.58.234.210", 19093, "http");

//        HttpPost httpPost = new HttpPost("/uaa/apid/data/member/checkOrCreateMemberBcbaochi");
        HttpPost httpPost = new HttpPost("http://121.58.234.210:19093/uaa/apid/data/member/checkOrCreateMemberBcbaochi");
//        httpPost.setHeader("Host","blcpt.6blc.com");
//        httpPost.setHeader("Proxy-Connection","close");
//        httpPost.setHeader("Connection","close");
        httpPost.setHeader("Content-Type","application/json; charset=UTF-8");
//        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("clientId","dDDTkatPN8MkWbfqb6c_MqTSSi9e5UT9FTDH5qMC");
//        httpPost.setHeader("Referer","http://cp2.bccp.co/reg");
        httpPost.setHeader("Origin","http://cp2.bccp.co");
//        httpPost.setHeader("Authorization","Basic d2ViX2FwcDo=");

//        HttpPost httpPost = new HttpPost("http://121.58.234.210:19093/uaa/apid/data/member/checkOrCreateMemberBcbaochi");

        for (int i = 0;i < num; i++){
            String userName = "autoTest" + i;
            ArrayList <NameValuePair> nvps = new ArrayList <NameValuePair>();

            nvps.add(new BasicNameValuePair("acType", "1"));
            nvps.add(new BasicNameValuePair("appid", "bcappid02"));
            nvps.add(new BasicNameValuePair("curType", "CNY"));
            nvps.add(new BasicNameValuePair("referrals", ""));
            nvps.add(new BasicNameValuePair("login", userName));
            nvps.add(new BasicNameValuePair("method", "mc"));
            nvps.add(new BasicNameValuePair("oddType", "a"));
            nvps.add(new BasicNameValuePair("password", "123456"));
            nvps.add(new BasicNameValuePair("realName", "测试"));
            nvps.add(new BasicNameValuePair("mobile", "15200000000"));
            nvps.add(new BasicNameValuePair("passwordPay", "1234"));
            nvps.add(new BasicNameValuePair("bankId", "1"));
            nvps.add(new BasicNameValuePair("code", "000000"));
            nvps.add(new BasicNameValuePair("bankCode", "ICBC"));
            nvps.add(new BasicNameValuePair("bankAddress", "测试"));
            nvps.add(new BasicNameValuePair("bankCard", "6221003811111111"));
            nvps.add(new BasicNameValuePair("source", "2"));


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
//                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//                System.out.println("执行请求： " + httpPost.getRequestLine() + " to " + target);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            CloseableHttpResponse response2 = null;
            try {
//                response2 = httpclient.execute(target,httpPost);
                response2 = httpclient.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                System.out.println(response2.getStatusLine());

                HttpEntity entity2 = response2.getEntity();

                try {
                    //打印接口返回值！！！
                    System.out.println("Response content: " + EntityUtils.toString(entity2, "UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    EntityUtils.consume(entity2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    response2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}