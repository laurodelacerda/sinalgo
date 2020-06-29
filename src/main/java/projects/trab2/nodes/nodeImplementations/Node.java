package projects.trab2.nodes.nodeImplementations;

import projects.trab2.Control;
import projects.trab2.nodes.messages.*;
import projects.trab2.nodes.timers.*;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Node extends sinalgo.nodes.Node {

    // consts
    public final double INTENSITY_SIGNAL = 15.0;
    public final int TIMER_AYCOORD = 5;
    public final int TIMER_AYTHERE = 5;
    public final int TIMER_MERGE = 10;
    public final int TIMER_READY = 10;
    public final int TIMER_ACCEPT = 10;

    public enum States {NORMAL, ELECTION, REORGANIZING} ;

    // timers
    private DelayTimer timer_aycoord = new DelayTimer(this, DelayTimer.Timers.AYCOORD, TIMER_AYCOORD);
    private DelayTimer timer_aythere = new DelayTimer(this, DelayTimer.Timers.AYCOORD, TIMER_AYTHERE);
    private DelayTimer timer_merge   = new DelayTimer(this, DelayTimer.Timers.MERGE, TIMER_MERGE);
    private DelayTimer timer_ready   = new DelayTimer(this, DelayTimer.Timers.READY, TIMER_READY);
    private DelayTimer timer_accept  = new DelayTimer(this, DelayTimer.Timers.ACCEPT, TIMER_ACCEPT);

    // Definition
    private States state;
    private List<Node> my_group;            // conjunto dos membros do próprio grupo
    private List<Node> union_group;         // conjunto dos membros da união dos grupos
    private int group_id;                   // identificação do grupo, através do par (CoordID, count)
    private List<Node> others_coord;        // outros coordenadores

    private Node coord = null;
    private int counter = 0;
    private int number_answers_ready = 0;


    public void init() {
        this.setRadioIntensity(INTENSITY_SIGNAL);
        Control.instance.nodes.add(this);
        this.group_id = (int) this.getID();
        this.state = States.NORMAL;
        this.my_group  = new ArrayList<Node>();
        this.union_group  = new ArrayList<Node>();
        this.others_coord = new ArrayList<Node>();

    }

    public void handleMessages(Inbox inbox) {

        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof AYCoord)
                handleAYCoord((AYCoord) msg);
            else if (msg instanceof AYCoordAnswer)
                handleAYCoordAnswer((AYCoordAnswer) msg);
            else if (msg instanceof AYThere)
                handleAYThere((AYThere) msg);
            else if (msg instanceof AYThereAnswer)
                handleAYThereAnswer((AYThereAnswer) msg);
            else if (msg instanceof Accept)
                handleAccept((Accept) msg);
            else if (msg instanceof AcceptAnswer)
                handleAcceptAnswer((AcceptAnswer) msg);
            else if (msg instanceof Ready)
                handleReady((Ready) msg);
            else if (msg instanceof ReadyAnswer)
                handleReadyAnswer((ReadyAnswer) msg);
            else if (msg instanceof Invitation)
                handleInvitation((Invitation) msg);
        }
    }

    public void handleTimers(DelayTimer.Timers timer) {

        if (timer == DelayTimer.Timers.AYCOORD)
        {
            this.log("finished timer of AYCOORD");
//            if (this.timer_aycoord.isMsg_arrived()) return;
//            else this.node.recovery();
        }
        else if (timer == DelayTimer.Timers.MERGE) {
            this.log("finished timer of MERGE");
            this.state = States.REORGANIZING;
            this.number_answers_ready = 0;
            this.timer_ready.activate();
            for (Node n: this.my_group) send(new Ready(this, this.group_id), n);
        }
        else if (timer == DelayTimer.Timers.READY) {
            this.log("finished timer of READY");
            if (this.number_answers_ready < this.my_group.size())
                this.recovery();
            else
                this.state = States.NORMAL;

        }
        else if (timer == DelayTimer.Timers.ACCEPT) {
            this.recovery();

        }

    }

    private void handleAYCoord(AYCoord m) { ;
        log("received AYCoord from %d", m.node.getID());
        if ((this.state == States.NORMAL) && (this.coord == this))
            send(new AYCoordAnswer(this, true), m.node);
        else
            send(new AYCoordAnswer(this, false), m.node);
    }

    private void handleAYThere(AYThere m) {
        log("received AYThere from %d", m.node.getID());
        if ((this.group_id == m.group_id) && (this.coord == this) && (this.my_group.contains(m.node)))
            send(new AYThereAnswer(this, true), m.node);
        else
            send(new AYThereAnswer(this, false), m.node);
    }

    private void handleAccept(Accept m) {
        log("received Accept from %d", m.sender.getID());
        if ((this.state == States.ELECTION) && (this.coord == this) && (this.group_id == m.group_id))
        {
            this.union_group.add(m.sender);
            send(new AcceptAnswer(this, true), m.sender);

        }


    }

    private void handleReady(Ready m) {
        log("received Ready from %d", m.node.getID());

        if ((this.group_id == m.group_id) && (this.state == States.REORGANIZING))
        {
            // this.definition = m.definition;
            this.state = States.NORMAL;
            send(new ReadyAnswer(this, true, this.group_id), this.coord);
        }
        else
            send(new ReadyAnswer(this, false), m.node);

    }

    private void handleInvitation(Invitation m) {

        if (this.state == States.NORMAL) {
            this.suspend_processing_application();
            Node old_coord = this.coord;
            this.my_group = this.union_group;
            this.state = States.ELECTION;
            this.coord = m.sender;
            this.group_id = m.group_id;

            if (old_coord == this) for (Node n : this.my_group) send (m.clone(), n);

            send(new Accept(this, this.group_id), this.coord);
            this.timer_accept.activate();






        }
//        if (this.iAmCoord()) {
//            for (Node n: my_group)
//                send(new Invitation(this, m.sender), n);
//            send(new Accept(this), m.sender);
//        }
//        else
//            send(new Accept(this), m.new_coord);

        this.state = States.REORGANIZING;
    }

    private void handleAcceptAnswer(AcceptAnswer m) {

        if (this.coord == m.sender)
            this.state = States.NORMAL;

    }

    private void handleAYCoordAnswer(AYCoordAnswer m) {
        log("received AYCoordAnswer from %d", m.sender.getID());
        if (this.iAmCoord())
        {
            this.others_coord.add(m.sender);
        }
    }

    private void handleReadyAnswer(ReadyAnswer m) {

        if ((m.accept == true) && (m.group_id == this.group_id))
        {
            log("received AYReadyAnswer from %d", m.sender.getID());
            this.number_answers_ready += 1;
        }
    }

    private void handleAYThereAnswer(AYThereAnswer m) {

        this.state = States.NORMAL;

    }

    private void sendReady() {
//        for (Node n: this.union_group)
//            send(new Ready(this), n);


    }

    private boolean iAmCoord() {
        return this.group_id == (int) this.getID();
    }

    public void recovery() {
        log("started recovery");
        this.state = States.ELECTION;
        this.counter += 1;
        this.group_id = (int) this.getID();
        this.coord = this;
        this.union_group.clear();
        this.state = States.REORGANIZING;
        // definition = my_appl_state
        this.state = States.NORMAL;
    }

    public void check_members() {

        if ((this.state == States.NORMAL) && (!this.timer_aycoord.isEnabled())) {
            AYCoord msg = new AYCoord(this);
            this.log("broadcasting AYCOORD");
            this.others_coord.clear();
            broadcast(msg);
            this.timer_aycoord.activate();
        }
    }

    public void check_coord() {

        if (this.iAmCoord())
            return;

        AYThere msg = new AYThere(this, this.group_id);
        send(msg, coord);
//        DelayTimer dt = new DelayTimer(this, msg, TIMER_AYTHERE);

    }

    public void suspend_processing_application() {

    }

    public void merge() {

        if ((this.iAmCoord()) && (this.state == States.NORMAL)) {
            this.state = States.ELECTION;
            this.suspend_processing_application();
            this.counter += 1;
            this.group_id = (int) this.getID();
            this.my_group.clear();
            this.my_group.addAll(this.union_group);
            this.union_group.clear();
            this.timer_merge.activate();

            Set<Node> set_nodes = new HashSet<Node>();
            set_nodes.addAll(this.others_coord);
            set_nodes.addAll(this.my_group);

            for (Node n: set_nodes) send(new Invitation(this, this.group_id), n);

        }
    }

    public void postStep() {

        if (this.iAmCoord()) // if coord, check other coordinators
            this.check_members();
        else // if member, check if coord is alive
            this.check_coord();


    }

    @NodePopupMethod(menuText = "Print status") // Maybe show the coordinator
    public void printStatus() {
//        log("trying: " + String.valueOf(cs) + " | tsCS:" + this.tsCS +  " | votes: " + this.yes + " | waiting: " + s);
    }

    public void log(String format, Object... args) {
        String s = String.format("NODE %d : ", this.getID());
        Control.instance.log(s + format, args);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        String text = String.valueOf(this.getID());
        super.drawNodeAsDiskWithText(g, pt, highlight, text,12, Color.WHITE);
    }

    public void preStep() {}
    public void checkRequirements() {}
    public void neighborhoodChange() {}
}
