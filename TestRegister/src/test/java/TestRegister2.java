import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;

public class TestRegister2 {

    public static void main(String[] args){

        int numOfAccount = 1;
        String hostName = "121.58.234.210";
        int port = 19093;
        String scheme = "http";
        String uriOfAPI = "/uaa/apid/data/member/checkOrCreateMemberBcbaochi";
        String source = "2";

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpHost target = new HttpHost(hostName, port, scheme);

        HttpPost httpPost = new HttpPost(uriOfAPI);

        httpPost.setHeader("Content-Type","application/json; charset=UTF-8");
        httpPost.setHeader("clientId","dDDTkatPN8MkWbfqb6c_MqTSSi9e5UT9FTDH5qMC");
        httpPost.setHeader("Origin","http://cp2.bccp.co");
        httpPost.setHeader("Authorization","Basic d2ViX2FwcDo=");

        for (int i = 0;i < numOfAccount; i++){
            String userName = "autoTest" + System.currentTimeMillis();
            String json = "";

            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("acType", "1");
            jsonObject.put("appid", "bcappid02");
            jsonObject.put("curType", "CNY");

            jsonObject.put("referrals", "");
            jsonObject.put("login", userName);
            jsonObject.put("method", "mc");

            jsonObject.put("oddType", "a");
            jsonObject.put("password", "123456");
            jsonObject.put("realName", "测试");

            jsonObject.put("mobile", "15200000000");
            jsonObject.put("passwordPay", "1234");
            jsonObject.put("bankId", "1");

            jsonObject.put("code", "000000");
            jsonObject.put("bankCode", "ICBC");
            jsonObject.put("bankAddress", "测试");

            jsonObject.put("bankCard", "6221003811111111");
            jsonObject.put("source", source);
            jsonObject.put("Authorization", "Basic d2ViX2FwcDo=");

            //convert JSONObject to JSON to String
            json = jsonObject.toString();

            StringEntity entity = null;
            entity = new StringEntity(json,"UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            CloseableHttpResponse response2 = null;

            //打印目标地址
            System.out.println("Executing request: " + httpPost.getRequestLine() + " to " + target);
            try {
                response2 = httpclient.execute(target,httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                System.out.println("返回状态码: " + response2.getStatusLine());

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