import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class NodeTransmitter implements Runnable{

    private Node node;
    private int uniqueID;
    private int port;
    private InetAddress group;
    private DatagramSocket transmitSocket;
    private DatagramPacket transmitPacket;

    public NodeTransmitter(Node node, int uniqueID, int port, InetAddress group){
        this.node = node;
        this.uniqueID = uniqueID;
        this.port = port;
        this.group = group;
    }

    public void initializeSockets() {

        transmitPacket = null;
        try {
            transmitSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Failed to create socket.");
            //e.printStackTrace();
        }
    }

    public void startElection() throws IOException {
        byte[] packetData =  (uniqueID + "," + "election").getBytes();
        transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
        transmitSocket.send(transmitPacket);
        System.out.println("Message sent: " + new String(transmitPacket.getData()));
    }

    public void run(){

        try {

            initializeSockets();
            BufferedReader startInput = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                if( startInput.readLine().equals("start") ) {
                    startElection();
                    node.setInElection(true);
                    break;
                }
                if ( node.getInElection() ) {
                    System.out.println("STARTED ELECTION");
                    startElection();
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        transmitSocket.close();
    }

}
