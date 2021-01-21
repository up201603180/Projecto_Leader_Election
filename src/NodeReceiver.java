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

    public NodeReceiver( Node node, int port, InetAddress group, ArrayList<Integer> neighbours){
        this.node = node;
        this.port = port;
        this.group = group;
    }

    private void initializeSockets() throws IOException {

        receivePacket = null;
        try {
            receiveSocket = new MulticastSocket( port );
            for ( int i = 0; i < node.getNeighbours().size(); i++ ) {
                group = InetAddress.getByName( "230.0.0." + node.getNeighbours().get(i));
                receiveSocket.joinGroup( group );
            }
        } catch (Exception e) {
            System.out.println("Failed to create socket.");
            //e.printStackTrace();
        }

    }

    // Inteiros para guardar nó e valor durante ack state
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
            node.setLeaderValue( -1 );
            node.setMachineState( 0 );
            node.setNodeCandidate( node.getUniqueID() );
            node.setNodeCandidateValue( node.getValue() );
            node.setIackID( -1 );

            byte[] packetData = new byte[1024];
            String receivedData;
            receivePacket = new DatagramPacket(packetData, packetData.length);

            // Receiver Cycle
            while( true ) {

                System.out.println("Waiting for message");

                receiveSocket.receive(receivePacket);
                receivedData = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength() );

                int senderID, pos;
                String messageType;

                //Race Conditions
                Thread.sleep(1);
                System.out.println(receivedData);

                // Format ID, election
                // Standby State - waiting for first election message
                if ( node.getMachineState() == 0 ) {
                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring(pos + 1, receivePacket.getLength());

                    if (messageType.equals("election") && !node.getInElection()) {
                        System.out.println("Node " + senderID + ": " + messageType);
                        node.setInElection(true);
                        node.setWaitACK(true);
                    }
                }

                // ACK Format: immediate ack -> ID, ack, iackID ||  real ack-> ID, ack, node, nodeValue
                // Wait for ACK state //Election State next
                else if ( node.getMachineState() == 1 ) {

                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring(pos + 1, pos + 1 + 3);

                    // Immediate ack, incrementa ackCounter sem alterar nodeCandidate/nodeCandidateValue
                    switch ( messageType ) {

                        case "iac":
                            int iackID = Integer.parseInt( receivedData.split(",")[2] );
                            if ( node.getUniqueID() == iackID ) {
                                node.setAckCounter(node.getAckCounter() + 1);
                                System.out.println("Received immediate ACK from " + senderID);
                            }

                            if( node.getUniqueID() == 1 && node.getAckCounter() >= node.getNeighbours().size() ) {
                                node.setWaitACK( false );
                            }
                            else if ( node.getUniqueID() > 1 && node.getAckCounter() >= node.getNeighbours().size() - 1 ) {
                                node.setWaitACK( false );
                            }
                            break;

                        case "ack":
                            // recebe informação do filho acerca do nó que ele diz ser o melhor dele para baixo
                            nodeCandidateToCompare = Integer.parseInt(receivedData.split(",")[2]);
                            System.out.println(nodeCandidateToCompare);
                            candidateValueToCompare = Integer.parseInt(receivedData.split(",")[3]);
                            System.out.println(candidateValueToCompare);

                            // If ack message is coming from child, update leader values
                            if ( candidateValueToCompare > node.getNodeCandidateValue()) {
                                node.setNodeCandidateValue( candidateValueToCompare );
                                node.setNodeCandidate( nodeCandidateToCompare );
                            }
                            node.setAckCounter(node.getAckCounter() + 1);

                            if( node.getUniqueID() == 1 && node.getAckCounter() >= node.getNeighbours().size() ) {
                                node.setWaitACK( false );
                            }
                            else if ( node.getUniqueID() > 1 && node.getAckCounter() >= node.getNeighbours().size() - 1 ) {
                                node.setWaitACK( false );
                            }
                            break;

                        case "ele":
                            // Se receber election de outros nós enquanto espera ack's, devolve immediate ack
                            node.setIackID( senderID );
                            break;

                    }

                }
                // Format: ID, leader, nodeLeader
                // Leader State
                else if ( node.getMachineState() == 2) {

                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring(pos + 1, pos + 1 + 3);

                    // If message received is leader, change his leader and propagate message
                    if ( messageType.equals("lea") ) {
                        int leaderID = Integer.parseInt( receivedData.split(",")[2] );
                        node.setLeaderID( leaderID );
                    }
                }
            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

}
