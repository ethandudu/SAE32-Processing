package rt.sae32.processing;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Index {
    /**
     * Creates a JSON object for the index
     * @param jsonLength Length of the JSON data
     * @param testName Name of the test
     * @return JSON object
     */
    public static JSONObject createIndexPacket(Integer jsonLength, String testName){
        JSONObject indexpacket = new JSONObject();
        indexpacket.put("name", testName);
        indexpacket.put("numberframe", jsonLength.toString());
        indexpacket.put("datetime",createDateTime());
        return indexpacket;
    }

    /**
     * Creates the date and time for the index with current date and time
     * @return Date and time in YYYY-MM-DD HH:MM:SS format
     */
    private static String createDateTime(){
        //get current date time
        Date date = new Date();
        //process date to YYYY-MM-DD HH:MM:SS
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
