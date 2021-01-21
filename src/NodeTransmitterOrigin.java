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
        byte[] packetData =  (uniqueID + ",election").getBytes();
        transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
        transmitSocket.send(transmitPacket);
        System.out.println("Message sent: " + new String(transmitPacket.getData()));
    }

    private void startLeader( int leaderID ) throws IOException {
        byte[] packetData =  (uniqueID + ",leader," + leaderID).getBytes();
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
                    node.setInElection( true );
                    node.setWaitACK( true );
                    node.setMachineState( 1 );
                    System.out.println("Election started, waiting ack...");
                    startElection();
                    break;
                }
            }

            while( true ) {


                // Wait ACK State
                if( node.getMachineState() == 1 ) {

                    // Race Condition
                    Thread.sleep(1);

                    // Se a raíz já não está à espera de ack's, já tem leader para anunciar
                    // se recebeu ack mas candidate == -1, deve escolher-se a si próprio como lider
                    // NOTA: Na nossa tipologia isto não deve acontecer porque o nó 1 tem de facto filhos
                    if( !node.getWaitACK() && node.getNodeCandidate() == -1 ) {
                        node.setNodeCandidate( node.getUniqueID() );
                        node.setNodeCandidateValue( node.getValue() );
                        node.setMachineState( 2 );
                        System.out.println("Acknowledgements received...");
                    }
                    else if ( !node.getWaitACK() && node.getNodeCandidate() != -1 ) {
                        node.setMachineState( 2 );
                        node.setLeaderID( node.getNodeCandidate() );
                        node.setLeaderValue( node.getNodeCandidateValue() );
                        System.out.println("Acknowledgements received...");

                    }

                }
                // Leader State
                else if ( node.getMachineState() == 2 ) {

                    // Race Condition
                    Thread.sleep(1);

                    if( node.getLeaderID() > 0 ) {
                        node.setMachineState( 0 );
                        node.setInElection( false );
                        System.out.println("New Leader Found! It's Node " + node.getLeaderID() );
                        startLeader( node.getLeaderID() );
                    }
                }
            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        transmitSocket.close();

    }

}
