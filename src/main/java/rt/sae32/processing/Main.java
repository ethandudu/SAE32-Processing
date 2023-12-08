package rt.sae32.processing;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        try {
            // Spécifiez le chemin du fichier JSON avec les clés dupliquées
            String fichierDuplique = "/nas2/users/etudiant/b/by310239/Documents/GitHub/wireshark.json";
            
            // Spécifiez le chemin du fichier JSON sans les clés dupliquées
            String fichierUtilise = "/nas2/users/etudiant/b/by310239/Documents/GitHub/wireshark_modifie.json";
            
            // Fonction qui supprime les clés dupliquées
            RemoveDuplicateKeys.main(fichierDuplique, fichierUtilise);
            
            // Créez un objet FileReader
            //FileReader lireFichier = new FileReader(fichierUtilise);

            // 
            

            String json = FileUtils.readFileToString(new File(fichierUtilise), "UTF-8");
            JSONArray array = new JSONArray(json);

            // JSONArray fichierArray = new JSONArray(fichierUtilise);

            for (int i = 0; i < array.length(); i++) {
                JSONObject objetATrouver = array.getJSONObject(i);
                //System.out.println("Source : " + sourceATrouver.get("_source"));
                //search in the object for the value of the key [_source][layers][frame][frame.protocols]
                JSONObject source = objetATrouver.getJSONObject("_source");
                JSONObject layers = source.getJSONObject("layers");
                JSONObject frame = layers.getJSONObject("frame");
                String protocols = frame.getString("frame.protocols");
                System.out.println("Protocols : " + protocols);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}