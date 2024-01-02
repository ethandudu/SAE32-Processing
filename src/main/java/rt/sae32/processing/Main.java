package rt.sae32.processing;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                    jsonObject.put("ipsrc", ipSource).put("ipdst", ipDestination);

                    if (protocols.getString(j).equals("arp")) {
                        JSONObject arp = new JSONObject();
                        String arpMacSource = null, arpMacDestination = null, arpIPSource = null, arpIPDestination = null;
                        arpMacSource = layers.getJSONObject("arp").getString("arp.src.hw_mac");
                        arpMacDestination = layers.getJSONObject("arp").getString("arp.dst.hw_mac");
                        arpIPSource = layers.getJSONObject("arp").getString("arp.src.proto_ipv4");
                        arpIPDestination = layers.getJSONObject("arp").getString("arp.dst.proto_ipv4");
                        data.put("arp", arp.put("srcmac", arpMacSource).put("dstmac", arpMacDestination).put("srcip", arpIPSource).put("dstip", arpIPDestination));                        
                    }


                    if (protocols.getString(j).equals("dhcp")) {
                        JSONObject dhcp = new JSONObject();
                        JSONArray dhcpOptions;
                        dhcpOptions = layers.getJSONObject("dhcp").getJSONArray("dhcp.option.type");
                        for (int k = 0; k < dhcpOptions.length(); k++) {
                            if (dhcpOptions.getString(k).equals("53")) {
                                JSONArray dhcpOptionTree;
                                dhcpOptionTree = layers.getJSONObject("dhcp").getJSONArray("dhcp.option.type_tree");
                                String dhcpOptionType = dhcpOptionTree.getJSONObject(k).getString("dhcp.option.dhcp");
                                data.put("dhcp", dhcp.put("type", dhcpOptionType));
                            }
                        }
                    }

                    if (protocols.getString(j).equals("dns")) {
                        JSONObject dns = new JSONObject();
                        JSONObject dnsFlags = layers.getJSONObject("dns").getJSONObject("dns.flags_tree");

                        //Query or response
                        String type = switch (dnsFlags.getString("dns.flags.response")) {
                            case "0" -> "Query";
                            case "1" -> "Response";
                            default -> "Unknown";
                        };
                        JSONObject queries = layers.getJSONObject("dns").getJSONObject("Queries");
                        JSONObject queriesTree = new JSONObject();


                        for (String key : queries.keySet()){
                            JSONObject query = queries.getJSONObject(key);
                            String name = query.getString("dns.qry.name");
                            String typeQuery = switch (query.getString("dns.qry.type")){
                                case "1" -> "A";
                                case "2" -> "NS";
                                case "5" -> "CNAME";
                                case "6" -> "SOA";
                                case "15" -> "MX";
                                case "16" -> "TXT";
                                case "28" -> "AAAA";
                                default -> query.getString("dns.qry.type");
                            };
                            queriesTree.put("name", name).put("type", typeQuery);
                        }

                        if (type.equals("Response")){
                            JSONObject answersTree = new JSONObject();
                            //check if it's a response or a authority
                            if (layers.getJSONObject("dns").has("Answers")) {
                                JSONObject answers = layers.getJSONObject("dns").getJSONObject("Answers");


                                for (String key : answers.keySet()) {
                                    JSONObject answer = answers.getJSONObject(key);
                                    String name = answer.getString("dns.resp.name");
                                    String typeAnswer = switch (answer.getString("dns.resp.type")) {
                                        case "1" -> "A";
                                        case "2" -> "NS";
                                        case "5" -> "CNAME";
                                        case "6" -> "SOA";
                                        case "15" -> "MX";
                                        case "16" -> "TXT";
                                        case "28" -> "AAAA";
                                        default -> answer.getString("dns.resp.type");
                                    };
                                    if (typeAnswer.equals("A")) {
                                        answersTree.put("name", name).put("type", typeAnswer).put("ip", answer.getString("dns.a"));
                                    } else if (typeAnswer.equals("AAAA")) {
                                        answersTree.put("name", name).put("type", typeAnswer).put("ip", answer.getString("dns.aaaa"));
                                    }
                                }
                                data.put("dns", dns.put("type", type).put("queries", queriesTree).put("answers", answersTree));
                            } else if (layers.getJSONObject("dns").has("Authoritative nameservers")){
                                JSONObject answers = layers.getJSONObject("dns").getJSONObject("Authoritative nameservers");

                                for (String key : answers.keySet()) {
                                    String name = answers.getJSONObject(key).getString("dns.resp.name");
                                    String typeAnswer = switch (answers.getJSONObject(key).getString("dns.resp.type")) {
                                        case "1" -> "A";
                                        case "2" -> "NS";
                                        case "5" -> "CNAME";
                                        case "6" -> "SOA";
                                        case "15" -> "MX";
                                        case "16" -> "TXT";
                                        case "28" -> "AAAA";
                                        default -> answers.getJSONObject(key).getString("dns.resp.type");
                                    };
                                    String mname = answers.getJSONObject(key).getString("dns.soa.mname");
                                    String rname = answers.getJSONObject(key).getString("dns.soa.rname");
                                    answersTree.put("name", name).put("type", typeAnswer).put("mname", mname).put("rname", rname);
                                }
                            }
                            data.put("dns", dns.put("type", type).put("queries", queriesTree).put("answers", answersTree));
                        } else {
                            data.put("dns", dns.put("type", type).put("queries", queriesTree));
                        }

                    }

                    
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

                
                // Ajouter les objets à un autre JSONObject
                object.put(Integer.toString(i),jsonObject);
            }
            // System.out.println(array.getJSONObject(0).getJSONObject("_source").getJSONObject("layers").getJSONObject("frame").getString("frame.protocols"));
            
            packet.put("packets", object);


            // Envoi des données

            // switch on HTTP response code
            switch (SendData(Index.createIndexPacket(object.length(), testName), packet))
            {
                case 200 -> System.out.println("Data inserted");

                case 410 -> System.out.println("No data provided");

                case 411 -> System.out.println("No index provided");

                case 412 -> System.out.println("No packets data provided");

                case 400 -> System.out.println("Not Authorized");

                case 504 -> System.out.println("Server did not respond");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a file
     * @param InputFile path to the file
     * @return A JsonArray representation of the file, null if the file does not exist
     */
    private static JSONArray loadFile(String InputFile) {
        if (!Files.exists(Paths.get(InputFile)))
            return null;

        return RemoveDuplicateKeys.main(InputFile);
    }

    /**
     * Make a webrequest to the API to insert the test
     * @param dataindex Data of the index packet
     * @param datapackets Data of the packets
     * @return The response code of the server
     */
    private static int SendData(JSONObject dataindex, JSONObject datapackets){
        final String url = "https://api.sae32.ethanduault.fr/insert.php";

        String response = HttpRequest.send(url, dataindex.toString(), datapackets.toString());

        if (response == null)
            return 504; // server did not respond

        JSONObject responseJson = new JSONObject(response);

        return (responseJson.getInt("responsecode"));
    }

    /**
     * Display the help
     */
    private static void printHelp(){
        System.out.println("Usage :");
        System.out.println("java -jar processing.jar -f <file> -n <testname>");
    }

    /**
     * Parses command line arguments.
     * @param args The command line arguments.
     * @return An array containing the file name and test name.
     */
    private static String[] parseArgs(String[] args){
        if (args.length < 4){
            printHelp();
            System.exit(1);
        }
        String fileName = null, testName = null;
        for (int i=0; i<args.length; i+=2){
            final String key = args[i];
            final String value = args[i+1];

            switch (key){
                case "-f" : //noinspection UnusedAssignment
                    fileName = value;
                case "-file" : fileName = value;
                case "-n" : //noinspection UnusedAssignment
                    testName = value;
                case "-name" : testName = value;
            }
        }
        if (fileName == null || testName == null){
            printHelp();
            System.exit(1);
        }
        return new String[]{fileName, testName};
    }
}
