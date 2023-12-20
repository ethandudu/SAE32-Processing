package rt.sae32.processing;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        JSONArray array = loadFile("/nas2/users/etudiant/b/by310239/Documents/GitHub/Wireshark/ether.json");
        if (array == null) {
            System.out.println("Erreur lors de la lecture du fichier");
            return;
        }
        try {

            // Crée un JSONObject pour stocker les les objets
            JSONObject packet = new JSONObject();

            // Crée un JSONObject pour stocker les valeurs
            JSONObject object = new JSONObject();

            System.out.println("--------------------------------------------------");

            for (int i = 0; i < array.length(); i++) {
                String macSource = null;
                String macDestination = null;
                String ipSource = null;
                String ipDestination = null;
                String portSource = null;
                String portDestination = null;

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
                System.out.println("protocols : " + protocols);
                jsonObject.put("protocols", protocols);
                
                if (layers.has("sll")) {
                    JSONObject sll = layers.getJSONObject("sll");
                    if (sll.has("sll.src.eth")) {
                        macSource = sll.getString("sll.src.eth");
                    }
                    if (sll.has("sll.dst.eth")) {
                        macDestination = sll.getString("sll.dst.eth");
                    }
                } else if (layers.has("eth")) {
                    JSONObject eth = layers.getJSONObject("eth");
                    macSource = eth.getString("eth.src");
                    macDestination = eth.getString("eth.dst");                    
                }
                System.out.println("MAC Source : " + macSource);
                System.out.println("MAC Destination : " + macDestination);
                jsonObject.put("macsrc", macSource);
                jsonObject.put("macdst", macDestination);

                if (layers.has("ip")) {
                    JSONObject ip = layers.getJSONObject("ip");
                    ipSource = ip.getString("ip.src");
                    ipDestination = ip.getString("ip.dst");
                } else if (layers.has("ipv6")) {
                    JSONObject ipv6 = layers.getJSONObject("ipv6");
                    ipSource = ipv6.getString("ipv6.src");
                    ipDestination = ipv6.getString("ipv6.dst");

                }
                System.out.println("IP Source : " + ipSource);
                System.out.println("IP Destination : " + ipDestination);
                jsonObject.put("ipsrc", ipSource);
                jsonObject.put("ipdst", ipDestination);

                JSONObject data = new JSONObject();

                //Cherche dans le tableau protocols s'il y a udp ou tcp
                for (int j = 0; j < protocols.length(); j++) {
                    if (protocols.getString(j).equals("udp")) {
                        JSONObject udp = new JSONObject();
                        portSource = layers.getJSONObject("udp").getString("udp.srcport");
                        portDestination = layers.getJSONObject("udp").getString("udp.dstport");
                        udp.put("srcport", portSource);
                        udp.put("dstport", portDestination);
                        data.put("udp", udp);
                    } else if (protocols.getString(j).equals("tcp")) {
                        JSONObject tcp = layers.getJSONObject("tcp");
                        data.put("tcp", tcp);
                    }
                }
                
                /*
                if (layers.has("udp")) {
                    JSONObject udp = layers.getJSONObject("udp");
                    data.put("udp", udp);
                    
                } else if (layers.has("tcp")) {
                    JSONObject tcp = layers.getJSONObject("tcp");
                    data.put("tcp", tcp);
                }

                if (layers.has("http")) {
                    JSONObject http = layers.getJSONObject("http");
                    data.put("http", http);
                }
                */
                
                jsonObject.put("datapackets", data);
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
            //SendData(createIndexPacket(object.length()), packet);
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

    /*private static void SendData(JSONObject dataindex, JSONObject datapackets){
        String url = "https://api.sae32.ethanduault.fr/insert.php";
        String response = HttpRequest.main(url, dataindex.toString(), datapackets.toString());
        System.out.println(response);
        assert response != null;
        JSONObject responseJson = new JSONObject(response);
        if (responseJson.getString("responsecode").equals("200")){
            System.out.println("Envoi des données réussi");
        } else {
            System.out.println("Erreur lors de l'envoi des données");
        }
    }

    private static JSONObject createIndexPacket(Integer jsonLength){
        JSONObject indexpacket = new JSONObject();
        indexpacket.put("name", "testname");
        indexpacket.put("numberframe", jsonLength.toString());
        indexpacket.put("datetime","2023-10-25 08:42:51");
        return indexpacket;
    }*/
}
