package rt.sae32.processing;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Index {
    public static JSONObject createIndexPacket(Integer jsonLength, String testName){
        JSONObject indexpacket = new JSONObject();
        indexpacket.put("name", testName);
        indexpacket.put("numberframe", jsonLength.toString());
        indexpacket.put("datetime",createDateTime());
        return indexpacket;
    }

    private static String createDateTime(){
        //get current date time
        Date date = new Date();
        //process date to YYYY-MM-DD HH:MM:SS
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
