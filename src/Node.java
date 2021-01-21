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
    private boolean hasLeader;
    private boolean waitACK;
    private int nodeCandidate;
    private int nodeCandidateValue;
    private int nodeParent;
    private int rootNode;
    private boolean hasHeartbeat;
    private boolean hasProbe;
    private int replyID;
    private int computationIndex;
    private ArrayList<Integer> neighbours;
    private ArrayList<Boolean> neighboursProbe;
    private InetAddress broadcast;

    public Node ( int value, int uniqueID, boolean in_election ) throws UnknownHostException {
        this.value = value;
        this.uniqueID = uniqueID;
        this.in_election = in_election;
        this.broadcast = InetAddress.getByName("230.0.0.255");
    }

    public int getComputationIndex() {
        return computationIndex;
    }

    public void setComputationIndex(int computationIndex) {
        this.computationIndex = computationIndex;
    }

    public int getReplyID() {
        return replyID;
    }

    public void setReplyID(int replyID) {
        this.replyID = replyID;
    }

    public ArrayList<Boolean> getNeighboursProbe() {
        return neighboursProbe;
    }

    public void setNeighboursProbe(ArrayList<Boolean> neighboursProbe) {
        this.neighboursProbe = neighboursProbe;
    }

    public boolean getHasHeartbeat() {
        return hasHeartbeat;
    }

    public void setHasHeartbeat(boolean hasHeartbeat) {
        this.hasHeartbeat = hasHeartbeat;
    }

    public boolean getHasProbe() {
        return hasProbe;
    }

    public void setHasProbe(boolean hasProbe) {
        this.hasProbe = hasProbe;
    }

    public int getRootNode() {
        return rootNode;
    }

    public void setRootNode(int rootNode) {
        this.rootNode = rootNode;
    }

    public InetAddress getBroadcast() {
        return broadcast;
    }

    public boolean getHasLeader() {
        return hasLeader;
    }

    public void setHasLeader(boolean hasLeader) {
        this.hasLeader = hasLeader;
    }

    public int getNodeParent() {
        return nodeParent;
    }

    public void setNodeParent(int nodeParent) {
        this.nodeParent = nodeParent;
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

    public void setNeighbours(ArrayList<Integer> neighbours) {
        this.neighbours = neighbours;
    }

    public static void main(String[] args) throws Exception{

        if(args.length != 3){
            System.out.println("Usage: <port> <value> <uniqueID>");
            System.exit(0);
        }

        // Node object
        Node node = new Node(Integer.parseInt(args[1]), Integer.parseInt(args[2]), false );

        ArrayList<Integer> neighbours_1 = new ArrayList<>();
        ArrayList<Integer> neighbours_2 = new ArrayList<>();
        ArrayList<Integer> neighbours_3 = new ArrayList<>();
        ArrayList<Integer> neighbours_4 = new ArrayList<>();
        ArrayList<Integer> neighbours_5 = new ArrayList<>();

        ArrayList<Boolean> neighboursProbe_1 = new ArrayList<>();
        ArrayList<Boolean> neighboursProbe_2 = new ArrayList<>();
        ArrayList<Boolean> neighboursProbe_3 = new ArrayList<>();
        ArrayList<Boolean> neighboursProbe_4 = new ArrayList<>();
        ArrayList<Boolean> neighboursProbe_5 = new ArrayList<>();

        String address = null;
        switch( node.getUniqueID() ) {
            case 1:
                neighbours_1.add(2);
                neighbours_1.add(3);
                node.setNeighbours(neighbours_1);
                neighboursProbe_1.add(false);
                neighboursProbe_1.add(false);
                node.setNeighboursProbe(neighboursProbe_1);
                address = "230.0.0.1";
                break;
            case 2:
                neighbours_2.add(1);
                neighbours_2.add(3);
                neighbours_2.add(4);
                node.setNeighbours(neighbours_2);
                neighboursProbe_2.add(false);
                neighboursProbe_2.add(false);
                neighboursProbe_2.add(false);
                node.setNeighboursProbe(neighboursProbe_2);
                address = "230.0.0.2";
                break;
            case 3:
                neighbours_3.add(1);
                neighbours_3.add(2);
                neighbours_3.add(5);
                node.setNeighbours(neighbours_3);
                neighboursProbe_3.add(false);
                neighboursProbe_3.add(false);
                neighboursProbe_3.add(false);
                node.setNeighboursProbe(neighboursProbe_3);
                address = "230.0.0.3";
                break;
            case 4:
                neighbours_4.add(2);
                neighbours_4.add(5);
                node.setNeighbours(neighbours_4);
                neighboursProbe_4.add(false);
                neighboursProbe_4.add(false);
                node.setNeighboursProbe(neighboursProbe_4);
                address = "230.0.0.4";
                break;
            case 5:
                neighbours_5.add(3);
                neighbours_5.add(4);
                node.setNeighbours(neighbours_5);
                neighboursProbe_5.add(false);
                neighboursProbe_5.add(false);
                node.setNeighboursProbe(neighboursProbe_5);
                address = "230.0.0.5";
                break;
        }

        //Group that node belongs to
        node.setRootNode( 1 );
        InetAddress group = InetAddress.getByName( address );

        //Receiver object
        NodeReceiver receiver = new NodeReceiver( node, Integer.parseInt(args[0]), group );
        Thread threadReceiver = new Thread(receiver);
        threadReceiver.start();

        Thread.sleep(1000);

        //TransmitterOrigin object
        NodeTransmitter transmitter = new NodeTransmitter( node, Integer.parseInt(args[2]), Integer.parseInt(args[0]), group);
        Thread threadTransmitter = new Thread(transmitter);
        threadTransmitter.start();

    }

}
