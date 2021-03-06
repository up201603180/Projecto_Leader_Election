import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;

public class NodeReceiver implements Runnable{

    private Node node;
    private int port;
    private InetAddress group;
    private MulticastSocket receiveSocket;
    private DatagramPacket receivePacket;

    public NodeReceiver( Node node, int port, InetAddress group ){
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
            // Join the broadcast group
            receiveSocket.joinGroup( node.getBroadcast() );
        } catch (Exception e) {
            System.out.println("Failed to create socket.");
            //e.printStackTrace();
        }

    }

    // Inteiros para guardar nó e valor durante ack state
    int nodeCandidateToCompare = 0;
    int candidateValueToCompare = 0;
    int parent = 0;

    private void checkProbe() {

        for ( int i = 0; i < node.getNeighboursProbe().size(); i++ ) {
            if ( !node.getNeighboursProbe().get(i) ) {
                // Não recebeu reply de um dos vizinhos e deve remover da lista neighbours
                System.out.println( "Lost connection with Node: " + node.getNeighbours().get(i) );
                node.getNeighbours().remove(i);
                node.getNeighboursProbe().remove(i);
            }
            else {
                node.getNeighboursProbe().set(i, false);
            }
        }

    }

    private void checkHeartbeat(){
        if( !node.getHasHeartbeat() ) {
            System.out.println("A new election should be started");
            node.setHasLeader( false );
            for ( int i = 0; i < node.getNeighbours().size(); i++ ) {
                if ( node.getNeighbours().get(i) == node.getLeaderID() ) {
                    node.getNeighbours().remove(i);
                    node.getNeighboursProbe().remove(i);
                }
            }
            node.setLeaderID( -1 );
            node.setLeaderValue( -1 );
            node.setAckCounter( 0 );
            node.setInElection( true );
        }
        else{
            System.out.println("I have heartbeat");
            node.setHasHeartbeat(false);
        }
    }

    public void run() {

        try {

            initializeSockets();

            // Initialize Algorithm Variables
            node.setComputationIndex( 0 );
            node.setAckCounter( 0 );
            node.setLeaderID( -1 );
            node.setLeaderValue( -1 );
            node.setMachineState( 0 );
            node.setNodeCandidate( node.getUniqueID() );
            node.setNodeCandidateValue( node.getValue() );
            node.setIackID( -1 );
            node.setInElection( false );

            // Heartbeat Thread
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {

                            checkProbe();
                            node.setHasProbe( false );

                            if ( node.getMachineState() == 0 ) {
                                // If this node is the current leader, send HEARTBEAT
                                if ( node.getHasLeader() && (node.getLeaderID() != node.getUniqueID()) ) {
                                    checkHeartbeat();
                                    node.setHasHeartbeat( false );
                                }
                            }

                        }
                    },
                    15000, 15000  //aumentei em relacao a 5s para nao ser tao apertado o check
            );

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

                senderID = Integer.parseInt(receivedData.split(",")[0]);
                pos = receivedData.indexOf(",");
                messageType = receivedData.substring(pos + 1, pos + 1 + 5 );

                // Format: senderID, probe
                // Probe Message
                if ( messageType.equals( "probe" ) ) {
                    node.setReplyID( senderID );
                }
                else if ( messageType.equals( "reply" ) ) {

                    // Save the index of the probe sender to
                    for ( int i = 0; i < node.getNeighbours().size(); i++ ) {
                        if ( node.getNeighbours().get(i) == senderID ) {
                            node.getNeighboursProbe().set(i, true);
                            break;
                        }
                    }
                }
                else if ( messageType.equals("heart") ) {
                    node.setHasHeartbeat( true );
                }

                // Format ID, election
                // Standby State - waiting for first election message
                if ( node.getMachineState() == 0 ) {
                    senderID = Integer.parseInt(receivedData.split(",")[0]);
                    pos = receivedData.indexOf(",");
                    messageType = receivedData.substring(pos + 1, pos + 1 + 3);


                    if ( messageType.equals("ele") && !node.getInElection() ) {
                        System.out.println("Node " + senderID + ": " + messageType);
                        node.setInElection(true);
                        node.setNodeParent( senderID );
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
                            parent = Integer.parseInt(receivedData.split(",")[2]);
                            nodeCandidateToCompare = Integer.parseInt(receivedData.split(",")[3]);
                            candidateValueToCompare = Integer.parseInt(receivedData.split(",")[4]);

                            // Se o ACK era para o nó que o recebeu trata a mensagem, senão descarta
                            if ( parent == node.getUniqueID() ) {
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
                            }

                        case "ele":
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
