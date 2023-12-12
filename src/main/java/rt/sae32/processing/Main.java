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
            String fichierDuplique = "/nas2/users/etudiant/b/by310239/Documents/GitHub/Wireshark/test_Win.json";
            
            // Spécifiez le chemin du fichier JSON à créer sans les clés dupliquées
            String fichierUtilise = "/nas2/users/etudiant/b/by310239/Documents/GitHub/Wireshark/test_Win_modifie.json";
            
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
                JSONObject jsonObject = new JSONObject();
                
                String packets = "Packet " + i + " { ";
                System.out.println(packets);
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
                        System.out.println("MAC Source : " + macSource);
                    }
                    if (sll.has("sll.dst.eth")) {
                        macDestination = sll.getString("sll.dst.eth");
                        System.out.println("MAC Destination : " + macDestination);
                    } else {
                        System.out.println("MAC Destination : null");
                    }
                } else if (layers.has("eth")) {
                    JSONObject eth = layers.getJSONObject("eth");
                    macSource = eth.getString("eth.src");
                    macDestination = eth.getString("eth.dst");
                    System.out.println("MAC Source : " + macSource);
                    System.out.println("MAC Destination : " + macDestination);                    
                }
                jsonObject.put("MAC Source", macSource);
                jsonObject.put("MAC Destination", macDestination);

                if (layers.has("ip")) {
                    JSONObject ip = layers.getJSONObject("ip");
                    ipSource = ip.getString("ip.src");
                    ipDestination = ip.getString("ip.dst");
                    //System.out.println("IP Source : " + ipSource);
                    //System.out.println("IP Destination : " + ipDestination);
                }
                jsonObject.put("IP Source", ipSource);
                jsonObject.put("IP Destination", ipDestination);
                System.out.println("--------------------------------------------------");
                
                // Ajouter le JSONObject au JSONArray
                jsonArray.put(jsonObject);
            }
            // System.out.println(array.getJSONObject(0).getJSONObject("_source").getJSONObject("layers").getJSONObject("frame").getString("frame.protocols"));
            // Affiche le JSONArray contenant tous les String
            System.out.println("Tous les JSONObject : " + jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}