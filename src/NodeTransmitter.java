import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class NodeTransmitter implements Runnable{

    private int value;
    private int uniqueID;
    private int port;
    private InetAddress group;
    private DatagramSocket transmitSocket;
    private DatagramPacket transmitPacket;

    public NodeTransmitter(int value, int uniqueID, int port, InetAddress group){
        this.value = value;
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
        System.out.println("Message sent");
    }

    public void waitForChildren(){

    }

    public void run(){
        initializeSockets();
        BufferedReader startInput = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            try {
                if(startInput != null && startInput.readLine().equals("start")){
                    startElection();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //waitForChildren();
        //transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);

        transmitSocket.close();
    }

}
