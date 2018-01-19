import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        parse();
    }

    public static void parse() {

        // blogBody("");
        List<String> list = null;
        try {
            list = getHtml();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        String path = "/Users/tianjia/Documents/article";
        String path = "D:\\testData\\data.txt";
        List<String> articles = FileUtil.getListFromFile(path);
        ExecutorService executorService = Executors.newCachedThreadPool();
        int len_article = articles.size();
        for (int i = 0; i < len_article; i++) {
            executorService.execute(new MyRun(articles.get(i), list));
        }
    }

    private static List<String> getHtml() throws IOException {
        Document doc = null;
        try {
            // doc = Jsoup.connect("http://www.baidu.com")
            doc = Jsoup.connect("http://www.xicidaili.com/nt")
                    // .data("query", "Java")
                    .userAgent("Mozilla")
                    // .cookie("auth", "token")
                    // .timeout(3000)
                    .get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> list = new ArrayList<String>();
        Elements elements = doc.select("tr.odd");
        int len = elements.size();
        Element element = null;
        for (int i = 0; i < len; i++) {
            element = elements.get(i);
            StringBuilder sBuilder = new StringBuilder(20);
            sBuilder.append(element.child(1).text());
            sBuilder.append(":");
            sBuilder.append(element.child(2).text());
            list.add(sBuilder.toString());
        }
        // System.out.println(doc.html());
        doc = null;
        elements.clear();
        elements = null;
        return list;
    }

    public static void visit(String ip, String url){
        // prop.setProperty("http.proxyHost", "183.45.78.31");
        // 设置http访问要使用的代理服务器的端口
        // prop.setProperty("http.proxyPort", "8080");
        String[] r = ip.split(":");
        System.getProperties().setProperty("http.proxyHost", r[0]);
        System.getProperties().setProperty("http.proxyPort", r[1]);
        try {
            // doc = Jsoup.connect("http://www.baidu.com")
            Jsoup.connect(url)
                    // .data("query", "Java")
                    .userAgent("Mozilla")
                    // .cookie("auth", "token")
                    // .timeout(3000)
                    .get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}