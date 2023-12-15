package rt.sae32.processing;


import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class HttpRequest {
    public static String main(String urlstring, String dataIndex, String dataPackets) {
        try {
            URL url = new URL(urlstring);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Request settings
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
            conn.setRequestProperty("User-Agent", "Java/SAE32-Processing");
            conn.setRequestProperty("Authorization", "wJb~9E@ttr#{%?K7");

            // Form data
            String formData = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                    "Content-Disposition: form-data; name=\"dataindex\"\r\n" +
                    "\r\n" +
                    dataIndex + "\r\n" +
                    "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                    "Content-Disposition: form-data; name=\"datapackets\"\r\n" +
                    "\r\n" +
                    dataPackets + "\r\n" +
                    "------WebKitFormBoundary7MA4YWxkTrZu0gW--";

            // Send request
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(formData);
            out.flush();
            out.close();

            // Fetch response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
