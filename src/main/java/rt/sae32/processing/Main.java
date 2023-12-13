package rt.sae32.processing;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
//import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        try {
            // Spécifiez le chemin du fichier JSON avec les clés dupliquées
            String fichierDuplique = "/nas2/users/etudiant/b/by310239/Documents/GitHub/Wireshark/ether.json";
            
            // Spécifiez le chemin du fichier JSON à créer sans les clés dupliquées
            String fichierUtilise = "/nas2/users/etudiant/b/by310239/Documents/GitHub/Wireshark/ether_modifie.json";
            
            // Fonction qui supprime les clés dupliquées
            RemoveDuplicateKeys.main(fichierDuplique, fichierUtilise);
            
            // Créer un objet lisant le fichier JSON
            //FileReader lireFichier = new FileReader(fichierUtilise);
            String json = FileUtils.readFileToString(new File(fichierUtilise), "UTF-8");
            JSONArray array = new JSONArray(json);

            // JSONArray fichierArray = new JSONArray(fichierUtilise);

            // Créer un JSONArray pour stocker les String
            JSONArray jsonArray = new JSONArray();
            
            System.out.println("--------------------------------------------------");

            for (int i = 0; i < array.length(); i++) {
                String protocols = null;
                String macSource = null;
                String macDestination = null;
                String ipSource = null;
                String ipDestination = null;

                // Créer un JSONObject pour stocker les valeurs
                JSONObject packet = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                
                String id = "ID " + i + " { ";
                System.out.println(id);
                JSONObject objetATrouver = array.getJSONObject(i);
                //System.out.println("Source : " + sourceATrouver.get("_source"));
                //search in the object for the value of the key [_source][layers][frame][frame.protocols]
                JSONObject source = objetATrouver.getJSONObject("_source");
                JSONObject layers = source.getJSONObject("layers");
                JSONObject frame = layers.getJSONObject("frame");
                protocols = frame.getString("frame.protocols");
                System.out.println("Protocols : " + protocols);
                jsonObject.put("Protocols", protocols);
                
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
                jsonObject.put("MACSrc", macSource);
                jsonObject.put("MACDst", macDestination);

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
                jsonObject.put("IPSrc", ipSource);
                jsonObject.put("IPDst", ipDestination);
                System.out.println("--------------------------------------------------");
                
                // Ajouter le JSONObject au JSONArray
                jsonArray.put(packet.put(id, jsonObject));
            }
            // System.out.println(array.getJSONObject(0).getJSONObject("_source").getJSONObject("layers").getJSONObject("frame").getString("frame.protocols"));
            // Affiche le JSONArray contenant tous les JSonObject
            System.out.println(jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}