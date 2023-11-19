package rt.sae32.processing;

import io.pkts.Pcap;
import io.pkts.packet.*;
import io.pkts.protocol.Protocol;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {
        ArrayList<Packet> packets = new ArrayList<Packet>();
        Date datepacket = null;

        JSONObject jo = new JSONObject();

        try {
            final Pcap pcap = Pcap.openStream("src/main/resources/pcap.pcap");

            pcap.loop(packet -> {
                packets.add(packet);
                return true;
            });

            pcap.close();

            Integer i = 0;
            for (Packet packet : packets) {
                if (i == 0) {
                    //convert timestamp to date
                    datepacket = new Date(TimeUnit.MICROSECONDS.toMillis(packet.getArrivalTime()));
                    jo.put("name", "Test java");
                    jo.put("numberframe", packets.size());
                    jo.put("datetime", datepacket.toString());
                    jo.put("packets", new JSONArray());
                }

                ArrayList<String> protocols = new ArrayList<String>();
                //read protocol for each packet
                Packet tempPacket = packet;
                try {
                    while (tempPacket.getNextPacket() != null) {
                        protocols.add(tempPacket.getProtocol().getName());
                        tempPacket = tempPacket.getNextPacket();
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }


                MACPacket macPacket = null;
                //check if packet has ethernet or sll protocol
                if (packet.hasProtocol(Protocol.ETHERNET_II)) {
                    macPacket = (MACPacket) packet.getPacket(Protocol.ETHERNET_II);
                } else if (packet.hasProtocol(Protocol.SLL)) {
                    macPacket = (MACPacket) packet.getPacket(Protocol.SLL);
                }

                JSONObject datajson = new JSONObject();

                /*
                //check if packet has ip protocol
                if (packet.hasProtocol(Protocol.IPv4)) {
                    IPPacket ipPacket = (IPPacket) packet.getPacket(Protocol.IPv4);
                    datajson.put("ipsrc", ipPacket.getSourceIP());
                    datajson.put("ipdst", ipPacket.getDestinationIP());
                    protocols.add("IPv4");
                } else if (packet.hasProtocol(Protocol.IPv6)) {
                    IPPacket ipPacket = (IPPacket) packet.getPacket(Protocol.IPv6);
                    datajson.put("ipsrc", ipPacket.getSourceIP());
                    datajson.put("ipdst", ipPacket.getDestinationIP());
                    protocols.add("IPv6");
                }

                //check if packet has tcp protocol
                if (packet.hasProtocol(Protocol.TCP)) {
                    TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                    datajson.put("portsrc", tcpPacket.getSourcePort());
                    datajson.put("portdst", tcpPacket.getDestinationPort());
                    protocols.add("TCP");
                } else if (packet.hasProtocol(Protocol.UDP)) {
                    UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                    datajson.put("portsrc", udpPacket.getSourcePort());
                    datajson.put("portdst", udpPacket.getDestinationPort());
                    protocols.add("UDP");
                }*/

                JSONObject packetjson = new JSONObject();
                packetjson.put("packetid", i);
                packetjson.put("protocols", protocols);
                packetjson.put("macsrc", macPacket.getSourceMacAddress());
                packetjson.put("macdst", macPacket.getDestinationMacAddress());
                packetjson.put("data", new JSONArray());

                //merge packet data to json
                packetjson.put("data", packetjson.getJSONArray("data").put(datajson));
                jo.put("packets", jo.getJSONArray("packets").put(packetjson));
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(jo.toString());
    }
}