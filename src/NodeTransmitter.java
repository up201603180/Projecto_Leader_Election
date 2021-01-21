import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class NodeTransmitter implements Runnable{

    private Node node;
    private int uniqueID;
    private int port;
    private InetAddress group;
    private DatagramSocket transmitSocket;
    private DatagramPacket transmitPacket;
    Instant start;
    Instant finish;

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

    private void reply( int nodeID ) throws IOException {
        byte[] packetData =  ( uniqueID + ",reply," + nodeID ).getBytes();
        transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
        transmitSocket.send(transmitPacket);
        System.out.println("Message sent: " + new String(transmitPacket.getData()));
    }

    private void probe() throws IOException {
        // change  this uniqueID to a node ID not to send probe
        if ( uniqueID != -1 ) {

            byte[] packetData =  ( uniqueID + ",probe").getBytes();
            transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
            transmitSocket.send(transmitPacket);
            System.out.println("Message sent: " + new String(transmitPacket.getData()));
        }
    }

    private void heartbeat() throws IOException {
        byte[] packetData =  ( uniqueID + ",heartbeat").getBytes();
        transmitPacket = new DatagramPacket(packetData, packetData.length, node.getBroadcast(), port);
        transmitSocket.send(transmitPacket);
        System.out.println("Message sent: " + new String(transmitPacket.getData()));
    }

    private void startElection() throws IOException {
        byte[] packetData =  (uniqueID + ",election," + node.getComputationIndex() ).getBytes();
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
            node.setIackID( -1 );
            System.out.println("Message sent: " + new String(transmitPacket.getData()));
        }
        else {
            // Format real ack-> ID, ack, DEST_ID, node, nodeValue
            byte[] packetData =  (uniqueID + "," + "ack," + node.getNodeParent() + "," + node.getNodeCandidate() + "," + node.getNodeCandidateValue() ).getBytes();
            transmitPacket = new DatagramPacket(packetData, packetData.length, group, port);
            transmitSocket.send(transmitPacket);
            System.out.println("Message sent: " + new String(transmitPacket.getData()));
        }
    }

    public void run(){

        try {

            initializeSockets();

            // Heartbeat Thread
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                // Send probe periodically
                                probe();
                                // If this node is the current leader, send HEARTBEAT
                                if ( node.getHasLeader() && (node.getLeaderID() == node.getUniqueID()) ) {
                                    heartbeat();
                                }
                            } catch ( Exception e ) {
                                e.printStackTrace();
                            }
                        }
                    }, 10000, 10000
            );

            if ( node.getUniqueID() == 1 ) {
                BufferedReader startInput = new BufferedReader(new InputStreamReader(System.in));

                while (true) {
                    if (startInput.readLine().equals("start")) {
                        node.setInElection(true);
                        node.setWaitACK(true);
                        node.setMachineState(1);
                        node.setComputationIndex( node.getComputationIndex() + 1 );
                        start = Instant.now();
                        System.out.println("Election started, waiting ack...");
                        startElection();
                        break;
                    }
                }
            }
            while( true ) {

                // If has node to reply to
                if ( node.getReplyID() > 0 ) {
                    reply( node.getReplyID() );
                    node.setReplyID( -1 );
                }

                // StandBy state
                if ( node.getMachineState() == 0 ) {
                    // Race Condition
                    Thread.sleep(1);
                    if ( !node.getHasLeader() && node.getInElection() ){
                        node.setWaitACK(true);
                        node.setComputationIndex( node.getComputationIndex() + 1 );
                        node.setMachineState(1);
                        System.out.println("Election started, waiting ack...");
                        startElection();
                    }
                }
                // Wait ACK State
                else if( node.getMachineState() == 1 ) {

                    // Race Condition
                    Thread.sleep(1);

                    if ( node.getUniqueID() == 1 ) {
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
                    else {

                        // Race Condition
                        Thread.sleep(1);

                        if ( node.getIackID() != -1 ) {
                            startAck( true );
                        }

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
                }
                // Leader State
                else if ( node.getMachineState() == 2 ) {
                    if( node.getLeaderID() > 0 ) {
                        node.setMachineState( 0 );
                        node.setInElection( false );
                        node.setHasLeader( true );
                        finish = Instant.now();
                        System.out.println("New Leader Found! It's Node " + node.getLeaderID() );
                        long timeElapsed = Duration.between(start, finish).toMillis();
                        System.out.println("Election time = " + timeElapsed + " ms");
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
