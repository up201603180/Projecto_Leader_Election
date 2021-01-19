import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class NodeReceiver implements Runnable{

    private Node node;
    private int port;
    private int ack_counter;
    private InetAddress group;
    private MulticastSocket receiveSocket;
    private DatagramPacket receivePacket;
    private ArrayList<Integer> neighbours;

    public NodeReceiver( Node node, int port, InetAddress group, ArrayList<Integer> neighbours){
        this.node = node;
        this.port = port;
        this.group = group;
        this.neighbours = neighbours;
    }

    private void initializeSockets() throws IOException {

        receivePacket = null;
        try {
            receiveSocket = new MulticastSocket( port );
            for ( int i = 0; i < neighbours.size(); i++ ) {
                group = InetAddress.getByName( "230.0.0." + neighbours.get(i));
                receiveSocket.joinGroup( group );
            }
        } catch (Exception e) {
            System.out.println("Failed to create socket.");
            //e.printStackTrace();
        }

    }


    public void run() {

        try {
            initializeSockets();
        } catch (IOException e) {
            System.out.println("Failed to initialize sockets");
            e.printStackTrace();
            System.exit(1);
        }
        try {

            // Initialize Algorithm Variables
            node.setAckCounter( 0 );
            node.setLeaderID( -1 );
            node.setMachineState( 0 );

            byte[] packetData = new byte[1024];
            String receivedData;
            receivePacket = new DatagramPacket(packetData, packetData.length);

            // Receiver Cycle
            while( true ) {

                System.out.println("Waiting for message");
                receiveSocket.receive(receivePacket);
                receivedData = new String(receivePacket.getData());

                int senderID = 0, leaderID, pos;
                String cmd = null;

                // Election State
                if ( node.getMachineState() == 0 ) {
                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    cmd = receivedData.substring( pos + 1, receivePacket.getLength());
                    System.out.println("Node " + senderID + ": " + cmd);
                }
                // Leader State
                else if ( node.getMachineState() == 1 ) {
                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    cmd = receivedData.substring( pos + 1, receivePacket.getLength());
                    leaderID = Integer.parseInt(receivedData.split(",")[1]);
                    System.out.println("Node " + senderID + ": " + cmd + ": " + leaderID);
                }

                // ELECTION
                if ( cmd.equals("election") && !node.getInElection() ) {
                    node.setInElection( true );
                    node.setWaitACK( true );
                    node.setNodeParent(senderID);
                }
                // IMMEDIATE ACKNOWLEDGEMENT
                else if ( cmd.equals("election") && node.getInElection() && ( senderID != node.getNodeParent() ) ) {
                    // enviar ack imediatamente
                    // msg = nodeID,ack
                }
                // ACKNOWLEDGEMENT
                else if ( cmd.equals("ack") ) {
                    node.setAckCounter( node.getAckCounter() + 1 );
                    if ( node.getAckCounter() == neighbours.size()) {
                        // -1 durante eleição
                        node.setLeaderID( 4 /* mais tarde usar o nó com maior valor */ );
                        node.setAckCounter(0);
                    }
                }
                // LEADER
                else if ( cmd.equals("leader") ) {
                    System.out.println("RECEBI LEADER VOTE");
                }

            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

}
