import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTime {
    public static void main(String[] args) {
        for (int i=0;i<10;i++){
        Date date = new Date();
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

            str = df.format(date);
            System.out.println("************"+str+"**************");
            try {
                Thread.sleep(1000);//等待1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
