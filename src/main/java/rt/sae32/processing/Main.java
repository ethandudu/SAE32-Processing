package rt.sae32.processing;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {
        String fichierDuplique = "~/Documents/GitHub/wireshark.json";
        String fichierUtilise = "~/Documents/GitHub/wireshark_reel.json";
        RemoveDuplicateKeys.main(fichierDuplique, fichierUtilise);
        
        JSONArray fichier = new JSONArray(fichierUtilise);
        for (int i = 0; i < fichier.length(); i++) {
            
        }
    }
}