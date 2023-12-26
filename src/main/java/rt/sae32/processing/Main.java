package rt.sae32.processing;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        String[] parsedArgs = parseArgs(args);
        String fileName = parsedArgs[0];
        String testName = parsedArgs[1];
        JSONArray array = loadFile(fileName);

        if (array == null) {
            System.out.println("Erreur lors de la lecture du fichier");
            return;
        }

        try {

            // Crée un JSONObject pour stocker les les objets
            JSONObject packet = new JSONObject();

            // Crée un JSONObject pour stocker les valeurs
            JSONObject object = new JSONObject();

            for (int i = 0; i < array.length(); i++) {
                String macSource = null, macDestination = null;
                String ipSource = null, ipDestination = null;
                String portSource = null, portDestination = null;

                JSONObject jsonObject = new JSONObject();

                JSONObject objetATrouver = array.getJSONObject(i);

                jsonObject.put("packetid", i);
                // Search in the object for the value of the key [_source][layers][frame][frame.protocols]
                JSONObject source = objetATrouver.getJSONObject("_source");
                JSONObject layers = source.getJSONObject("layers");
                JSONObject frame = layers.getJSONObject("frame");

                // Découpe protocols en fonction des ":" et met les valeurs dans un JSONArray
                String[] protocolsArrayString = frame.getString("frame.protocols").split(":");
                JSONArray protocols= new JSONArray();
                for (String s : protocolsArrayString) {
                    protocols.put(s);
                }
                jsonObject.put("protocols", protocols);
                
                JSONObject data = new JSONObject();

                //Cherche dans le tableau protocols s'il y a les protocoles suivants
                for (int j = 0; j < protocols.length(); j++) {
                    if (protocols.getString(j).equals("eth")) {
                        macSource = layers.getJSONObject("eth").getString("eth.src");
                        macDestination = layers.getJSONObject("eth").getString("eth.dst");
                    } else if (protocols.getString(j).equals("sll")) {
                        if (layers.getJSONObject("sll").has("sll.src.eth")) {
                            macSource = layers.getJSONObject("sll").getString("sll.src.eth");
                        }
                        if (layers.getJSONObject("sll").has("sll.dst.eth")) {
                            macDestination = layers.getJSONObject("sll").getString("sll.dst.eth");
                        }
                    }
                    jsonObject.put("macsrc", macSource).put("macdst", macDestination);

                    if (protocols.getString(j).equals("ip")) {
                        ipSource = layers.getJSONObject("ip").getString("ip.src");
                        ipDestination = layers.getJSONObject("ip").getString("ip.dst");
                    } else if (protocols.getString(j).equals("ipv6")) {
                        ipSource = layers.getJSONObject("ipv6").getString("ipv6.src");
                        ipDestination = layers.getJSONObject("ipv6").getString("ipv6.dst");
                    }
                    jsonObject.put("srcip", ipSource).put("dstip", ipDestination);

                    if (protocols.getString(j).equals("arp")) {
                        JSONObject arp = new JSONObject();
                        String arpMacSource = null, arpMacDestination = null, arpIPSource = null, arpIPDestination = null;
                        arpMacSource = layers.getJSONObject("arp").getString("arp.src.hw_mac");
                        arpMacDestination = layers.getJSONObject("arp").getString("arp.dst.hw_mac");
                        arpIPSource = layers.getJSONObject("arp").getString("arp.src.proto_ipv4");
                        arpIPDestination = layers.getJSONObject("arp").getString("arp.dst.proto_ipv4");
                        data.put("arp", arp.put("srcmac", arpMacSource).put("dstmac", arpMacDestination).put("srcip", arpIPSource).put("dstip", arpIPDestination));                        
                    }
                    
                    /*
                    if (protocols.getString(j).equals("DHCP")) {
                        JSONObject dhcp = new JSONObject();
                        String dhcpMacSource = null, dhcpIPSource = null, dhcpIPDestination = null dhcpIPClient = null, dhcpIPServer = null;
                        dhcpMacSource = layers.getJSONObject("dhcp").getString("dhcp.hw.mac");
                        dhcpIPSource = layers.getJSONObject("dhcp").getString("dhcp.ip.src");
                        dhcpIPDestination = layers.getJSONObject("dhcp").getString("dhcp.ip.dst");
                        data.put("dhcp", dhcp.put("srcmac", dhcpMacSource).put("dstmac", dhcpMacDestination).put("srcip", dhcpIPSource).put("dstip", dhcpIPDestination));
                    }
                    if (protocols.getString(j).equals("DNS")) {

                    }
                    if (protocols.getString(j).equals("http")) {}
                    if (protocols.getString(j).equals("MPLS")) {}
                    if (protocols.getString(j).equals("TLS")) {}
                    */
                    
                    if (protocols.getString(j).equals("tcp")) {
                        JSONObject tcp = new JSONObject();
                        portSource = layers.getJSONObject("tcp").getString("tcp.srcport");
                        portDestination = layers.getJSONObject("tcp").getString("tcp.dstport");
                        data.put("tcp", tcp.put("srcport", portSource).put("dstport", portDestination));
                    } else if (protocols.getString(j).equals("udp")) {
                        JSONObject udp = new JSONObject();
                        portSource = layers.getJSONObject("udp").getString("udp.srcport");
                        portDestination = layers.getJSONObject("udp").getString("udp.dstport");
                        data.put("udp", udp.put("srcport", portSource).put("dstport", portDestination));
                    }
                }
                
                jsonObject.put("datapackets", data);

                // Affiche les valeurs

                System.out.println("--------------------------------------------------");
                System.out.println("packetid : " + i);
                System.out.println("protocols : " + protocols);                
                System.out.println("MAC Source : " + macSource);
                System.out.println("MAC Destination : " + macDestination);
                System.out.println("IP Source : " + ipSource);
                System.out.println("IP Destination : " + ipDestination);
                System.out.println("Data : " + data);
                System.out.println("--------------------------------------------------");
                
                // Ajouter les objets à un autre JSONObject
                object.put(Integer.toString(i),jsonObject);
            }
            // System.out.println(array.getJSONObject(0).getJSONObject("_source").getJSONObject("layers").getJSONObject("frame").getString("frame.protocols"));
            
            packet.put("packets", object);

            // Affiche le JSONObject contenant toutes les valeurs
            System.out.println(packet);

            // Envoi des données
            SendData(createIndexPacket(object.length(), testName), packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONArray loadFile(String InputFile) {
        //check if the file exists
        if (Files.exists(Paths.get(InputFile))) {
            return RemoveDuplicateKeys.main(InputFile);
        } else {
            System.out.println("Le fichier n'existe pas");
            return null;
        }
    }

    private static void SendData(JSONObject dataindex, JSONObject datapackets){
        String url = "https://api.sae32.ethanduault.fr/insert.php";
        String response = HttpRequest.main(url
, dataindex.toString(), datapackets.toString());
        System.out.println(response);
        assert response != null;
        JSONObject responseJson = new JSONObject(response);
        if (responseJson.getString("responsecode").equals("200")){
            System.out.println("Envoi des données réussi");
        } else {
            System.out.println("Erreur lors de l'envoi des données");
        }
    }

    private static JSONObject createIndexPacket(Integer jsonLength, String testName){
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

    private static void printHelp(){
        System.out.println("Usage :");
        System.out.println("java -jar processing.jar -f <file> -n <testname>");
    }

    private static String[] parseArgs(String[] args){
        if (args.length < 4){
            printHelp();
            System.exit(0);
        }
        String fileName = null, testName = null;
        for (int i=0; i<args.length; i+=2){
            String key = args[i];
            String value = args[i+1];

            switch (key){
                case "-f" : fileName = value;
                case "-n" : testName = value;
            }
        }
        if (fileName == null || testName == null){
            printHelp();
            System.exit(0);
        }
        return new String[]{fileName, testName};
    }
}
