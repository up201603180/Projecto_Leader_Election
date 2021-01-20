import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class NodeReceiver implements Runnable{

    private Node node;
    private int port;
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

    private void printMessage(int senderID, String messageType){
        if(messageType.equals("election")){
            System.out.println("Received from Node " + senderID + ": " + messageType);
        }

    }

    int nodeCandidateToCompare = 0;
    int candidateValueToCompare = 0;

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
                String messageType = null;

                //Format ID, election
                // Standby State - waiting for first election message
                if (node.getMachineState() == 0) {
                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring(pos + 1, receivePacket.getLength());

                    if (messageType.equals("election") && !node.getInElection()) {
                        printMessage(senderID, messageType);
                        node.setInElection(true);
                        node.setWaitACK(true);
                        node.setNodeParent(senderID);
                        node.setMachineState(1);
                    }
                }

                // ACK Format: immediate ack -> ID, ack, 0, 0  real ack-> ID, ack, node, nodeValue
                // Wait for ACK state //Election State next
                else if ( node.getMachineState() == 1 ) {
                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring(pos + 1, pos + 1 + 3);
                    System.out.println(messageType);
                    if(messageType.equals("ack")){
                        nodeCandidateToCompare = Integer.parseInt(receivedData.split(",")[2]);
                        System.out.println(nodeCandidateToCompare);
                        candidateValueToCompare = Integer.parseInt(receivedData.split(",")[3]);
                        System.out.println(candidateValueToCompare);
                    }

                    if(node.getNodeCandidate() != 0){ // If ack message is coming from child
                        node.setAckCounter(node.getAckCounter() + 1);
                        if(candidateValueToCompare > node.getNodeCandidateValue()){

                        }

                        if(node.getAckCounter() == neighbours.size()){

                            node.setMachineState(2);
                        }
                    }

                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring( pos + 1, receivePacket.getLength());
                    System.out.println("Node " + senderID + ": " + messageType);
                }

            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

}
