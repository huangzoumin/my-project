import java.util.List;

public class MyRun implements Runnable{

    private List<String> list;
    private String urlString;
    public MyRun(String url,List<String> list) {
        this.list =  list;
        this.urlString = url;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        int len = list.size();
        for (int i = 0; i < len; i++) {
            Test.visit(list.get(i), urlString);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}