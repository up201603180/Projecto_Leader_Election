import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class NodeTransmitterOrigin implements Runnable{

    private Node node;
    private int uniqueID;
    private int port;
    private InetAddress group;
    private DatagramSocket transmitSocket;
    private DatagramPacket transmitPacket;

    public NodeTransmitterOrigin(Node node, int uniqueID, int port, InetAddress group){
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
            BufferedReader startInput = new BufferedReader( new InputStreamReader( System.in ) );

            while( true ) {
                if( startInput.readLine().equals("start") ) {
                    startElection();
                    node.setInElection( true );
                    node.setWaitACK( true );
                    break;
                }
            }

            while( true ) {

                // Race Condition
                Thread.sleep(1);

                if( node.getLeaderID() > 0 ) {
                    node.setWaitACK( false );
                    System.out.println("ANNOUNCE LEADER");
                    startLeader( node.getLeaderID() );
                }

            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        transmitSocket.close();
    }

}
