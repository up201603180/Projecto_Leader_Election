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

    public void initializeSockets() throws IOException {

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

            byte[] packetData = new byte[1024];
            String receivedData;
            receivePacket = new DatagramPacket(packetData, packetData.length);
            while(true){
                //System.out.println("Waiting for message");
                receiveSocket.receive(receivePacket);
                receivedData = new String(receivePacket.getData());
                int sender_id = Integer.parseInt(receivedData.split(",")[0]);
                String cmd = receivedData.split(",")[1];

                System.out.println("Node " + sender_id + ": " + cmd);
                if ( cmd.equals("election") && !node.getInElection()) {
                    node.setInElection( true );
                    System.out.println("STARTED ELECTION");
                }

            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        //String receivedData = new String(receivePacket.getData());
        //System.out.println(receivedData);
        //receiveSocket.close();
    }

}
