import java.net.*;
import java.util.*;

public class Node {

    private int machineState;
    private int value;
    private int uniqueID;
    private int leaderID;
    private int leaderValue;
    private int iackID;
    private int ack_counter;
    private boolean in_election;
    private boolean waitACK;
    private int nodeCandidate;
    private int nodeCandidateValue;
    private static ArrayList<Integer> neighbours;

    public Node(int value, int uniqueID, boolean in_election ){
        this.value = value;
        this.uniqueID = uniqueID;
        this.in_election = in_election;
    }

    public int getLeaderValue() {
        return leaderValue;
    }

    public void setLeaderValue(int leaderValue) {
        this.leaderValue = leaderValue;
    }

    public int getIackID() {
        return iackID;
    }

    public void setIackID(int iackID) {
        this.iackID = iackID;
    }

    public int getNodeCandidateValue() {
        return nodeCandidateValue;
    }

    public void setNodeCandidateValue(int nodeCandidateValue) {
        this.nodeCandidateValue = nodeCandidateValue;
    }

    public int getNodeCandidate() {
        return nodeCandidate;
    }

    public void setNodeCandidate(int nodeCandidate) {
        this.nodeCandidate = nodeCandidate;
    }

    public int getValue() {
        return value;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public boolean getInElection() {
        return in_election;
    }

    public void setInElection(boolean in_election) {
        this.in_election = in_election;
    }

    public int getLeaderID() {
        return leaderID;
    }

    public void setLeaderID(int leaderID) {
        this.leaderID = leaderID;
    }

    public int getMachineState() {
        return machineState;
    }

    public void setMachineState(int machineState) {
        this.machineState = machineState;
    }

    public boolean getWaitACK() {
        return waitACK;
    }

    public void setWaitACK(boolean waitACK) {
        this.waitACK = waitACK;
    }

    public int getAckCounter() {
        return ack_counter;
    }

    public void setAckCounter(int ack_counter) {
        this.ack_counter = ack_counter;
    }

    public ArrayList<Integer> getNeighbours() {
        return neighbours;
    }

    public static void main(String[] args) throws Exception{

        if(args.length != 3){
            System.out.println("Usage: <port> <value> <uniqueID>");
            System.exit(0);
        }

        neighbours = new ArrayList<>();

        // Node object
        Node node = new Node(Integer.parseInt(args[1]), Integer.parseInt(args[2]), false );


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

        if ( node.uniqueID == 1 ) {
            //TransmitterOrigin object
            NodeTransmitterOrigin transmitter = new NodeTransmitterOrigin( node, Integer.parseInt(args[2]), Integer.parseInt(args[0]), group);
            Thread threadTransmitter = new Thread(transmitter);
            threadTransmitter.start();
        }
        else {
            //Transmitter object
            NodeTransmitter transmitter = new NodeTransmitter( node, Integer.parseInt(args[2]), Integer.parseInt(args[0]), group);
            Thread threadTransmitter = new Thread(transmitter);
            threadTransmitter.start();
        }
    }

}
