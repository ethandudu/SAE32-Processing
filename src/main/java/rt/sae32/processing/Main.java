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
            
            // Spécifiez le chemin du fichier JSON sans les clés dupliquées
            String fichierUtilise = "/nas2/users/etudiant/b/by310239/Documents/GitHub/Wireshark/ether_modifie.json";
            
            // Fonction qui supprime les clés dupliquées
            RemoveDuplicateKeys.main(fichierDuplique, fichierUtilise);
            
            // Créer un objet lisant le fichier JSON
            //FileReader lireFichier = new FileReader(fichierUtilise);
            String json = FileUtils.readFileToString(new File(fichierUtilise), "UTF-8");
            JSONArray array = new JSONArray(json);

            // JSONArray fichierArray = new JSONArray(fichierUtilise);

            for (int i = 0; i < array.length(); i++) {
                System.out.println("Packet " + i);
                JSONObject objetATrouver = array.getJSONObject(i);
                //System.out.println("Source : " + sourceATrouver.get("_source"));
                //search in the object for the value of the key [_source][layers][frame][frame.protocols]
                JSONObject source = objetATrouver.getJSONObject("_source");
                JSONObject layers = source.getJSONObject("layers");
                JSONObject frame = layers.getJSONObject("frame");
                String protocols = frame.getString("frame.protocols");
                System.out.println("Protocols : " + protocols);
                if (layers.has("sll")) {
                    JSONObject sll = layers.getJSONObject("sll");
                    if (sll.has("sll.src.eth")) {
                        String macSource = sll.getString("sll.src.eth");
                        System.out.println("MAC Source : " + macSource);
                    }
                    if (sll.has("sll.dst.eth")) {
                        String macDestination = sll.getString("sll.dst.eth");
                        System.out.println("MAC Destination : " + macDestination);
                    } else {
                        System.out.println("MAC Destination : null");
                    }
                } else if (layers.has("eth")) {
                    JSONObject eth = layers.getJSONObject("eth");
                    String macSource = eth.getString("eth.src");
                    String macDestination = eth.getString("eth.dst");
                    System.out.println("MAC Source : " + macSource);
                    System.out.println("MAC Destination : " + macDestination);                    
                }
                if (layers.has("ip")) {
                    JSONObject ip = layers.getJSONObject("ip");
                    String ipSource = ip.getString("ip.src");
                    String ipDestination = ip.getString("ip.dst");
                    System.out.println("IP Source : " + ipSource);
                    System.out.println("IP Destination : " + ipDestination);
                }
                System.out.println("--------------------------------------------------");
            }
            // System.out.println(array.getJSONObject(0).getJSONObject("_source").getJSONObject("layers").getJSONObject("frame").getString("frame.protocols"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}