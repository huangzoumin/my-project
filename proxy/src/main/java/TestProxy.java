import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestProxy {

    public static void main(String[] args) {
        for(int i=0;i<ips.length;i++){
           get("http://blcpt.6blc.com",ips[i]);
        }
    }

    private static String[] ips = new String[]{
            "118.184.87.229"
            , "118.184.87.228"
            , "118.184.87.227"
            , "118.184.87.224"
            , "118.184.87.23"
            , "104.217.128.66"
            , "172.106.73.130"
            , "104.217.128.114"
            , "104.216.108.154"
            , "183.111.66.246"
            , "183.111.66.214"
            , "183.111.66.209"
            , "183.111.66.208"
            , "183.111.66.218"
            , "115.144.122.189"
            , "115.144.122.190"
            , "115.144.122.191"
            , "103.86.84.57"
            , "103.86.84.236"
            , "45.77.183.37"
            , "45.32.44.195"
            , "45.32.26.194"
            , "104.238.150.107"
            , "45.76.221.248"
            , "139.162.2.247"
            , "172.104.164.8"
            , "173.234.25.10"
            , "216.105.168.26"
            , "216.105.168.34"
            , "216.105.168.42"
            , "103.44.61.81"
            , "103.44.61.84"
            , "103.44.61.133"
            , "103.44.61.42"
            , "103.44.61.67"
            , "45.124.112.194"
            , "45.124.112.195"
            , "45.124.112.196"
            , "45.124.112.200"
            , "45.124.112.202"
            , "103.44.61.56"
            , "103.44.61.167"
            , "103.44.61.230"
            , "103.44.61.55"
            , "103.44.61.134"
    };
    private static String port = "8080";

    public static String get(String uri,String ip){

        System.setProperty("http.proxySet", "true");
        System.getProperties().put("http.proxyHost", ip);
        System.getProperties().put("http.proxyPort", port);

        Document doc = null;
        String  agent="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko)"
                + "  Chrome/56.0.2924.87 Safari/537.36" ;
        try {
            doc = Jsoup.connect(uri).ignoreContentType(true)
                    .userAgent(agent)
                    // ignoreHttpErrors
                    //否则会报HTTP error fetching URL. Status=404
                    .ignoreHttpErrors(true)
                    .timeout(3000).get();
        } catch (IOException e) {
            System.out.println(e.getMessage()+"  **************** get");
        }
        if (doc!=null) {
            return doc.body().text();
        }
        return null;
    }

}
