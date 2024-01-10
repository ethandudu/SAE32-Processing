package rt.sae32.processing;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private String serverUrl;
    private String token;

    /**
     * Config constructor
     */
    public Config() {
        JSONObject config = getSavedConfig();
        if (config != null){
            this.serverUrl = config.getString("serverUrl");
            this.token = config.getString("token");
        } else {
            this.serverUrl = null;
            this.token = null;
        }
    }

    /**
     * Set the server url
     * @param serverUrl The server url to set
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Set the token
     * @param token The token of the API to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Get the server url
     * @return The server url
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Get the token
     * @return The token
     */
    public String getToken() {
        return token;
    }
    /**
     * Detect the OS of the computer
     * @return the OS name
     */
    private static String detectOS(){
        if (System.getProperty("os.name").startsWith("Windows")){
            return "Windows";
        } else if (System.getProperty("os.name").startsWith("Linux")){
            return "Linux";
        } else {
            return "Other";
        }
    }

    /**
     * get the config file and return it
     * @return the config file as a JSONObject
     */
    public JSONObject getSavedConfig(){
        JSONObject config = new JSONObject();
        try {
            String os = detectOS();
            if (!checkConfigExists(os)){
                return null;
            }
            if (os.equals("Windows")){
                config = new JSONObject(Files.readString(Paths.get(System.getenv("APPDATA") + "/sae32/config.json")));
            } else if (os.equals("Linux")){
                config = new JSONObject(Files.readString(Paths.get(System.getProperty("user.home") + "/.config/sae32/config.json")));
            }

        } catch (Exception e){
            System.out.println("Error reading config file");
        }
        return config;
    }

    /**
     * Check if the config file exists
     * @param OS The OS of the computer
     * @return true if the config file exists, false if not
     */
    private boolean checkConfigExists(String OS){
        if (OS.equals("Windows")){
            return Files.exists(Paths.get(System.getenv("APPDATA") + "/sae32/config.json"));

        } else if (OS.equals("Linux")){
            return Files.exists(Paths.get(System.getProperty("user.home") + "/.config/sae32/config.json"));
        } else {
            return false;
        }
    }

    /**
     * Save the config file
     */
    public void saveConfig(){
        String os = detectOS();
        if (os.equals("Windows")){
            Path path = Paths.get(System.getenv("APPDATA") + "/sae32/config.json");
            if (!checkConfigExists(os)){
                System.out.println("Creating config file ...");
                try {
                    if (!new File(path.getParent().toString()).exists()){
                        System.out.println("Creating config directory ...");
                        if (!new File(path.getParent().toString()).mkdirs()){
                            System.out.println("Error creating config directory");
                        }
                    }
                    JSONObject config = createJsonConfig();
                    if (config == null){
                        System.out.println("No config to save");
                        return;
                    }
                    Files.writeString(Paths.get(path.toUri()), config.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Writing config file ...");
                try {
                    JSONObject config = createJsonConfig();
                    if (config == null){
                        System.out.println("No config to save");
                        return;
                    }
                    Files.writeString(Paths.get(path.toUri()), config.toString());
                    System.out.println("Config file written");
                } catch (Exception e){
                    System.out.println("Error writing config file");
                }
            }
        } else if (os.equals("Linux")){
            Path path = Paths.get(System.getProperty("user.home") + "/.config/sae32/config.json");
            if (!checkConfigExists(os)){
                System.out.println("Creating config file ...");
                try {
                    if (!new File(path.getParent().toString()).exists()){
                        System.out.println("Creating config directory ...");
                        if (!new File(path.getParent().toString()).mkdirs()){
                            System.out.println("Error creating config directory");
                        }
                    }
                    JSONObject config = createJsonConfig();
                    if (config == null){
                        System.out.println("No config to save");
                        return;
                    }
                    Files.writeString(Paths.get(path.toUri()), config.toString());
                } catch (IOException e) {
                    System.out.println("Error creating config file");
                }
            } else {
                System.out.println("Writing config file ...");
                try {
                    JSONObject config = createJsonConfig();
                    if (config == null){
                        System.out.println("No config to save");
                        return;
                    }
                    Files.writeString(Paths.get(path.toUri()), config.toString());
                    System.out.println("Config file written");
                } catch (Exception e){
                    System.out.println("Error writing config file");
                }
            }
        } else {
            System.out.println("Error saving config file");
        }
    }

    /**
     * Create the config with the server url and the token
     * @return the config as a JSONObject to save
     */
    private JSONObject createJsonConfig(){
        if (this.serverUrl == null || this.token == null){
            return null;
        }
        JSONObject config = new JSONObject();
        config.put("serverUrl", this.serverUrl);
        config.put("token", this.token);
        return config;
    }
}
