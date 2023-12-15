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

            // Créer un JSONArray pour stocker les String
            JSONObject object = new JSONObject();

            // Créer un JSONObject pour stocker les valeurs
            JSONObject packet = new JSONObject();

            System.out.println("--------------------------------------------------");

            for (int i = 0; i < array.length(); i++) {
                String macSource = null;
                String macDestination = null;
                String ipSource = null;
                String ipDestination = null;

                JSONObject jsonObject = new JSONObject();

                JSONObject objetATrouver = array.getJSONObject(i);

                // Search in the object for the value of the key [_source][layers][frame][frame.protocols]
                JSONObject source = objetATrouver.getJSONObject("_source");
                JSONObject layers = source.getJSONObject("layers");
                JSONObject frame = layers.getJSONObject("frame");

                // Découpe protocols en fonction des ":" et met les valeurs dans un JSONArray
                String[] protocolsArrayString = frame.getString("frame.protocols").split(":");
                JSONArray protocols= new JSONArray();
                for (int j = 0; j < protocolsArrayString.length; j++) {
                    protocols.put(protocolsArrayString[j]);
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
                jsonObject.put("data", data);
                System.out.println("Data : " + data);

                System.out.println("--------------------------------------------------");
                
                // Ajouter le JSONObject au JSONArray
                object.put(Integer.toString(i),jsonObject);
            }
            // System.out.println(array.getJSONObject(0).getJSONObject("_source").getJSONObject("layers").getJSONObject("frame").getString("frame.protocols"));
            
            packet.put("packets", object);

            // Affiche le JSONObject contenant toutes les valeurs
            System.out.println(packet);
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
}
