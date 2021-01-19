import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class NodeReceiver implements Runnable{

    private int value;
    private int uniqueID;
    private int port;
    private InetAddress group;
    private MulticastSocket receiveSocket;
    private DatagramPacket receivePacket;
    private ArrayList<Integer> neighbours;

    public NodeReceiver(int value, int uniqueID, int port, InetAddress group, ArrayList<Integer> neighbours){
        this.value = value;
        this.uniqueID = uniqueID;
        this.port = port;
        this.group = group;
        this.neighbours = neighbours;
    }

    public void initializeSockets() throws IOException {

        receivePacket = null;
        try {
            receiveSocket = new MulticastSocket(port);
        } catch (Exception e) {
            System.out.println("Failed to create socket.");
            //e.printStackTrace();
        }

        System.out.println(group);
        receiveSocket.joinGroup( group );
    }


    public void run() {
        try {
            initializeSockets();
        } catch (IOException e) {
            System.out.println("Failed to initialize sockets");
            e.printStackTrace();
            System.exit(1);
        }

        byte[] packetData = new byte[1024];
        String receivedData = null;
        receivePacket = new DatagramPacket(packetData, packetData.length);
        while(true){
            try {
                System.out.println("Waiting for message");
                receiveSocket.receive(receivePacket);
                receivedData = new String(receivePacket.getData());
                System.out.println(receivedData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //String receivedData = new String(receivePacket.getData());
        //System.out.println(receivedData);
        //receiveSocket.close();
    }

}
