package projects.trab1.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import org.javatuples.Pair;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.trab1.nodes.messages.*;
import sinalgo.configuration.Configuration;
import sinalgo.exception.CorruptConfigurationEntryException;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

import java.awt.*;
import java.util.*;


/**
 * The Node of the sample project.
 */
@Getter
@Setter
public class SimpleNode extends Node {

    /**
     * the neighbor with the smallest ID
     */
    private SimpleNode next;

    /**
     * number of messages sent by this node in the current round
     */
    private int msgSentInThisRound;

    /**
     * total number of messages sent by this node
     */
    private int msgSent;

//    /**
//     * The amount to increment the data of the message each time it goes throug a
//     * node.
//     */
//    private int increment;

    /**
     * The number of districts
     */
    private int number_districts;


    /**
     * The disctrict of node
     */
    private int si;


    private LinkedList<Long> si_members = new LinkedList<>();

    /**
     * If node is in CS
     */
    private boolean inCS;

    /**
     * Current logical timestamp
     */
    private int curr_TS;

    /**
     * Timestamp of CS entry
     */
    private int my_TS;

    /**
     * Number of processes who answered YES
     */
    private int yes_votes;

    /**
     * If node has voted
     */
    private boolean has_voted;

    /**
     * ID of candidate which a vote was given
     */
    private long cand;

    /**
     * Candidade timestamp
     */
    private long cand_TS;

    /**
     * TRUE if process tried to cancel its vote
     */
    private boolean inquired;

    /**
     * Set of neighbours
     */
    private Set<Long> nbors = new HashSet<Long>();

    /**
     * Queue of pending requests
     */
    private LinkedList<Pair<Long, Long>> deferredQ = new LinkedList<>();
    /* private Map<Long, Int> deferredQ = new HashMap<Long, Int>() */

    Logging log = Logging.getLogger("s1_log");

    /**
     * Remove the Pair<Process, Timestamp> with lowest timestamp
     */
    public Pair<Long, Long> rem_min()
    {
        if (!this.deferredQ.isEmpty()) {

            Pair<Long, Long> min = null;
            int min_index = -1;

            for (int i = 0; i < this.deferredQ.size(); i++) {
                if (min == null || min.getValue1() > this.deferredQ.get(i).getValue1()) {
                    min = this.deferredQ.get(i);
                    min_index = i;
                }
            }

//            this.deferredQ.remove(min_index);
            return this.deferredQ.get(min_index);
        }
        else
        {
            return null;
        }
    }


    public void enter_CS()
    {
        this.my_TS = this.curr_TS;

        for (int i = 0; i < this.si_members.size(); i++) { // multicast to coterie

            ReqMessage req = new ReqMessage(this.getID(), this.si, this.my_TS);

            SimpleNode destination = new SimpleNode();
            destination.setID(this.si_members.get(i));
            MessageTimer msgTimer = new MessageTimer(req, destination);

            msgTimer.startRelative(1, this);
//            broadcast(mes);
            send(req, destination);

        }

    }


    public void exit_CS()
    {
        this.inCS = false;

        for (int i = 0; i < this.si_members.size(); i++) {

            RelinquishMessage rel = new RelinquishMessage(this.getID());

            SimpleNode destination = new SimpleNode();
            destination.setID(this.si_members.get(i));

            MessageTimer msgTimer = new MessageTimer(rel, destination);

            msgTimer.startRelative(1, this);
//            broadcast(mes);
            send(rel, destination);

        }
    }


    // a flag to prevent all nodes from sending messages
    @Getter
    @Setter
    private static boolean isSending = true;

    @Override
    public void handleMessages(Inbox inbox) {
        if (!isSending()) { // don't even look at incoming messages
            return;
        }
        if (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof HelloMessage)
            {
                HelloMessage hello = (HelloMessage) msg;
                this.nbors.add(hello.getId());

                // Nó conhece seus vizinhos de coterie
                for (int i = 0; i < this.number_districts; i++)
                {
                    if (hello.getId() % this.number_districts == this.si)
                        this.si_members.add(hello.getId());
//                    else if
//                    {}
//                    if (i < this.number_districts)
                }
            }
            else if (msg instanceof ReqMessage)
                handleReqMsg(msg);
            else if (msg instanceof ReleaseMessage)
                handleReleaseMsg(msg);
            else if (msg instanceof VoteMessage)
                handleVoteMsg(msg);
            else if (msg instanceof InqMessage)
                handleInqMsg(msg);
            else if (msg instanceof RelinquishMessage)
                handleRelinquishMsg(msg);
        }
    }


    public void handleReqMsg(Message msg)
    {
        ReqMessage req = (ReqMessage) msg;

        SimpleNode dest = new SimpleNode();
        dest.setID(req.getId());

        if (!this.has_voted){

            VoteMessage vote = new VoteMessage(this.getID(), this.my_TS, true);

            MessageTimer msgTimer = new MessageTimer(vote, dest);
            msgTimer.startRelative(1, dest);
            send(vote, dest);

            this.cand = req.getId();            // registrando para quem mandou
            this.cand_TS = req.getTs();
            this.has_voted = true;
        }
        else
        {
            this.deferredQ.add(Pair.with(req.getId(), req.getTs()));

            if ((req.getTs() < cand_TS) && (!this.inquired))
            {

                InqMessage inq = new InqMessage(this.cand, this.si, this.cand_TS);


                MessageTimer msgTimer = new MessageTimer(inq, dest);
                msgTimer.startRelative(1, dest);
                send(inq, dest); // pede anulação do voto
            }

        }
    }

    public void handleReleaseMsg(Message msg)
    {
        ReleaseMessage rel = (ReleaseMessage) msg;

        if (!this.deferredQ.isEmpty())
        {
            Pair<Long, Long> cand = this.rem_min();
            long r_id = cand.getValue0();
            long r_ts = cand.getValue1();

            SimpleNode dest = new SimpleNode();
            dest.setID(rel.getId());

            VoteMessage vote = new VoteMessage(this.getID(), r_ts, true);
            this.cand = r_id;
            this.cand_TS = r_ts;

            send(vote, dest);
        }
        else
        {
            this.has_voted = false;
            this.inquired = false;
        }
    }

    public void handleVoteMsg(Message msg)
    {
        VoteMessage vote = (VoteMessage) msg;
        if (vote.isYes())
            this.yes_votes += 1;
    }

    public void handleInqMsg(Message msg)
    {
        InqMessage inq = (InqMessage) msg;

        if(inq.getTs() == this.my_TS)
        {
            SimpleNode dest = new SimpleNode();
            dest.setID(inq.getId());

            RelinquishMessage rel = new RelinquishMessage(this.getID());

            MessageTimer msgTimer = new MessageTimer(rel, dest);
            msgTimer.startRelative(1, this);

            send(rel, dest);
            this.yes_votes -= 1;
        }
    }

    public void handleRelinquishMsg(Message msg)
    {
        RelinquishMessage rel = (RelinquishMessage) msg;

        SimpleNode dest = new SimpleNode();
        dest.setID(rel.getId());

        this.deferredQ.add(Pair.with(cand, cand_TS));
        Pair<Long, Long> pair = this.rem_min(); // resgata requisição de menor TS
        long r_id = pair.getValue0();
        long r_ts = pair.getValue0();

        VoteMessage vote = new VoteMessage(r_id, r_ts, true);
        this.cand = r_id;
        this.cand_TS = r_ts;
        this.inquired = false;

        send(vote, dest);
    }


    @Override
    public void preStep() {
        this.msgSent += this.msgSentInThisRound;
        this.msgSentInThisRound = 0;
    }

    @Override
    public void init() {
        // initialize the node
        try {
            /*
             Read a value from the configuration file config.xml.
             The following command reads an integer, which is expected to
             be stored in either of the two following styles in the XML file:
             <SimpleNode>
             <increment value="2"/>
             </SimpleNode>
             OR
             <SimpleNode increment="2"/>getIntegerParameter
                        this.increment = Configuration.getIntegerParameter("SimpleNode/increment");
            */

            // Nó conhece seu si
            this.number_districts = Configuration.getIntegerParameter("SimpleNode/numberofdistricts");
            this.si = (int) (this.getID() % this.number_districts);


        } catch (CorruptConfigurationEntryException e) {
            // Missing entry in the configuration file: Abort the simulation and
            // display a message to the user
            throw new SinalgoFatalException(e.getMessage());
        }
    }

    @Override
    public void neighborhoodChange() {
        this.setNext(null);
        for (Edge e : this.getOutgoingConnections()) {
            if (this.getNext() == null) {
                this.setNext((SimpleNode) e.getEndNode());
            } else {
                if (e.getEndNode().compareTo(this.getNext()) < 0) {
                    this.setNext((SimpleNode) e.getEndNode());
                }
            }
        }
    }

    /*
     * Methods with the annotation NodePopupMethod can be executed by the user from
     * the GUI by clicking on the node and selecting the menu point in the popup
     * menu.
     */

    /**
     * Initiate a message to be sent by this node in the next round. This starts the
     * process of resending the message infinitely.
     * <p>
     * This method is part of the user-implemenation of this sample project.
     */
    @NodePopupMethod(menuText = "Start")
    public void start() {
        // This sample project is designed for the round-based simulator.
        // I.e. a node is only allowed to send a message when it is its turn.
        // To comply with this rule, we're not allowed to call the
        // method 'SendMessage()' here, but need either to remember that the
        // user has clicked to send a message and then send it in the intervalStep()
        // manually. Here, we show a simpler and more elegant approach:
        // Set a timer (with time 1), which will fire the next time this node is
        // handled. The defaultProject already contains a MessageTimer which can
        // be used for exactly this purpose.
//        MessageTimer msgTimer = new MessageTimer(new S1Message(1)); // broadcast
//        msgTimer.startRelative(1, this);
//        Tools.appendToOutput("Start Routing from node " + this.getID() + "\n");
        HelloMessage hello = new HelloMessage(this.getID());
        MessageTimer msgTimer = new MessageTimer(hello);
        msgTimer.startRelative(1, this);
        broadcast(new HelloMessage(this.getID()));
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        // set the color of this node
        this.setColor(
                new Color((float) 0.5 / (1 + this.msgSentInThisRound), (float) 0.5, (float) 1.0 / (1 + this.msgSentInThisRound)));
        String text = Integer.toString(this.msgSent) + "|" + this.msgSentInThisRound;
        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.YELLOW);
        // super.drawNodeAsSquareWithText(g, pt, highlight, text, 10, Color.YELLOW);
    }

    @Override
    public void postStep() {
//        SemaphoreMessage mes = new SemaphoreMessage(pos, this.busy, this.id_authorized);

//        MessageTimer msgTimer = new MessageTimer(mes);
//        msgTimer.startRelative(1, this);
//        broadcast(mes, this.RANGE_AREA);
//        ReqMessage mes = new ReqMessage(this.getID(), this.si);
//
//        MessageTimer msgTimer = new MessageTimer(mes);
//        msgTimer.startRelative(1, this);
//        broadcast(mes);

    }

    @Override
    public String toString() {
//        return "Messages sent so far: " + this.msgSent + "\nMessages sent in this round: " + this.msgSentInThisRound;
        String members = "";
        for (Long m : si_members)
            members += " " + String.valueOf(m);

        return "si: " + this.si + "\nnbors: " + this.nbors.size() + "\nmembers: " + members;
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
//        if (this.increment < 0) {
//            throw new WrongConfigurationException(
//                    "SimpleNode: The increment value (specified in the config file) must be greater or equal to 1.");
//        }
    }
}
