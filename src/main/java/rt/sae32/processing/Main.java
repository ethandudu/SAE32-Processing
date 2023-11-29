package rt.sae32.processing;
import org.json.JSONArray;
import org.json.JSONObject;

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
            FileReader lireFichier = new FileReader(fichierUtilise);

            // 
            JSONArray fichierArray = new JSONArray(fichierUtilise);

        for (int i = 0; i < fichierArray.length(); i++) {
            JSONObject objetATrouver = fichierArray.getJSONObject(i);
            System.out.println("IP Source : " + objetATrouver.get("ip.src"));
        }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}