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

    private void startAck ( boolean immediate_ack ) throws Exception {
        if ( immediate_ack ) {
            // Format immediate ack -> ID, ack, DEST_ID
            byte[] packetData =  (uniqueID + ",iack," + node.getIackID() ).getBytes();
            transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
            transmitSocket.send(transmitPacket);
            Thread.sleep(1);
            node.setIackID( -1 );
            System.out.println("Message sent: " + new String(transmitPacket.getData()));
        }
        else {
            // Format real ack-> ID, ack, node, nodeValue
            byte[] packetData =  (uniqueID + "," + "ack," + node.getNodeCandidate() + "," + node.getNodeCandidateValue() ).getBytes();
            transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
            transmitSocket.send(transmitPacket);
            System.out.println("Message sent: " + new String(transmitPacket.getData()));
        }
    }

    public void run(){

        try {

            initializeSockets();

            while( true ) {

                // StandBy state
                if(node.getMachineState() == 0){

                    // Race Condition
                    Thread.sleep(1);

                    if( node.getInElection() ){
                        System.out.println("Election started, waiting ack...");
                        startElection();
                        node.setWaitACK( true );
                        node.setMachineState(1);
                    }
                }

                // Wait ACK State
                else if( node.getMachineState() == 1 ) {

                    // Race Condition
                    Thread.sleep(1);

                    if ( node.getIackID() != -1 ) {
                        startAck( true );
                    }

                    // Race Condition
                    Thread.sleep(1);

                    // Se já recebeu ack dos filhos, envia ack para parent
                    if( !node.getWaitACK() && node.getNodeCandidate() != -1 ) {
                        node.setMachineState( 2 );
                        // send real ack to parent with info from childs
                        System.out.println("Acknowledgements received, informing parent...");
                        startAck( false );
                    }
                    // Como nó é uma folha "== -1", deve enviar ack ao parent com o seu id,value
                    else if ( !node.getWaitACK() && node.getNodeCandidate() == -1 ) {
                        node.setNodeCandidate( node.getUniqueID() );
                        node.setNodeCandidateValue( node.getValue() );
                        node.setMachineState( 2 );
                        // send real ack to parent with own info
                        System.out.println("Acknowledgements received, informing parent...");
                        startAck( false );
                    }

                }
                // Leader state
                else if( node.getMachineState() == 2 ){

                    // Race Condition
                    Thread.sleep(1);

                    if ( node.getLeaderID() > 0 ) {
                        node.setMachineState( 0 );
                        node.setInElection( false );
                        System.out.println("New Leader Found! It's Node " + node.getLeaderID() );
                        startLeader( node.getLeaderID() );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        transmitSocket.close();

    }

}
