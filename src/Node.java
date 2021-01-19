import java.net.*;
import java.util.*;

public class Node {

    private int value;
    private int uniqueID;
    private boolean in_election;
    private boolean ack_ready;

    public Node(int value, int uniqueID, boolean in_election){
        this.value = value;
        this.uniqueID = uniqueID;
        this.in_election = in_election;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public boolean getInElection() {
        return in_election;
    }

    public void setInElection(boolean in_election) {
        this.in_election = in_election;
    }

    public static void main(String[] args) throws Exception{

        if(args.length != 3){
            System.out.println("Usage: <port> <value> <uniqueID>");
            System.exit(0);
        }

        // Node object
        Node node = new Node(Integer.parseInt(args[1]), Integer.parseInt(args[2]), false);

        // Neighbours initialization
        ArrayList<Integer> neighbours = new ArrayList<>();

        String address = null;
        switch( node.uniqueID ) {
            case 1:
                neighbours.add(2);
                neighbours.add(3);
                address = "230.0.0.1";
                break;
            case 2:
                neighbours.add(1);
                neighbours.add(3);
                neighbours.add(4);
                address = "230.0.0.2";
                break;
            case 3:
                neighbours.add(1);
                neighbours.add(2);
                neighbours.add(5);
                address = "230.0.0.3";
                break;
            case 4:
                neighbours.add(2);
                neighbours.add(5);
                address = "230.0.0.4";
                break;
            case 5:
                neighbours.add(3);
                neighbours.add(4);
                address = "230.0.0.5";
                break;
        }

        //Group that node belongs to
        InetAddress group = InetAddress.getByName( address );

        //Receiver object
        NodeReceiver receiver = new NodeReceiver( node, Integer.parseInt(args[0]), group, neighbours);
        Thread threadReceiver = new Thread(receiver);
        threadReceiver.start();

        Thread.sleep(1000);

        //Transmitter object
        NodeTransmitter transmitter = new NodeTransmitter( node, Integer.parseInt(args[2]), Integer.parseInt(args[0]), group);
        Thread threadTransmitter = new Thread(transmitter);
        threadTransmitter.start();
    }

}
