import java.net.*;
import java.util.*;

public class Node {

    private int value;
    private int uniqueID;

    public Node(int value, int uniqueID){
        this.value = value;
        this.uniqueID = uniqueID;
    }

    public static void main(String args[]) throws Exception{

        if(args.length != 3){
            System.out.println("Usage: <port> <value> <uniqueID>");
            System.exit(0);
        }

        //Node object
        Node node = new Node(Integer.parseInt(args[1]), Integer.parseInt(args[2]));

        String address = null;
        //Neighbours initialization
        ArrayList<Integer> neighbours = new ArrayList<>();
        switch(node.uniqueID) {
            case 1:
                neighbours.add(2);
                neighbours.add(3);
                address = "230.0.0.1";
            case 2:
                neighbours.add(1);
                neighbours.add(3);
                neighbours.add(4);
                address = "230.0.0.2";
            case 3:
                neighbours.add(1);
                neighbours.add(2);
                neighbours.add(5);
                address = "230.0.0.3";
            case 4:
                neighbours.add(2);
                neighbours.add(5);
                address = "230.0.0.4";
            case 5:
                neighbours.add(3);
                neighbours.add(4);
                address = "230.0.0.5";
        }

        //Group that node belongs to
        InetAddress group = InetAddress.getByName(address);

        //Receiver object
        NodeReceiver receiver = new NodeReceiver(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[0]), group, neighbours);
        Thread threadReceiver = new Thread(receiver);
        threadReceiver.start();

        Thread.sleep(1000);
        //Transmitter object
        NodeTransmitter transmitter = new NodeTransmitter(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[0]), group);
        Thread threadTransmitter = new Thread(transmitter);
        threadTransmitter.start();
    }

}
