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

    private void initializeSockets() {

        transmitPacket = null;
        try {
            transmitSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Failed to create socket.");
            //e.printStackTrace();
        }
    }

    private void startElection() throws IOException {
        byte[] packetData =  (uniqueID + "," + "election").getBytes();
        transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
        transmitSocket.send(transmitPacket);
        System.out.println("Message sent: " + new String(transmitPacket.getData()));
    }

    private void startLeader( int leaderID ) throws IOException {
        byte[] packetData =  (uniqueID + "," + "leader:" + leaderID).getBytes();
        transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
        transmitSocket.send(transmitPacket);
        System.out.println("Message sent: " + new String(transmitPacket.getData()));
    }

    public void run(){

        try {

            initializeSockets();

            while( true ){

                // StandBy state
                if(node.getMachineState() == 0){

                    // Race Condition
                    Thread.sleep(1);

                    if(node.getInElection()){
                        System.out.println("Election started");
                        startElection();
                        node.setWaitACK( true );
                        node.setMachineState(1);
                    }
                }

                // Race Condition
                Thread.sleep(1);

                if(node.getMachineState() == 1){

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        transmitSocket.close();
    }

}
