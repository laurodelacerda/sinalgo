package projects.trab2.nodes.nodeImplementations;

import projects.trab2.Control;
import projects.trab2.nodes.messages.*;
import projects.trab2.nodes.timers.DelayTimer;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;


public class Node extends sinalgo.nodes.Node {

    // consts
    public final double INTENSITY_SIGNAL = 15.0;
    public final int TIMER_AYCOORD = 25;
    public final int TIMER_AYTHERE = 10;
    public final int TIMER_MERGE = 10;
    public final int TIMER_READY = 10;
    public final int TIMER_ACCEPT = 10;
    public final int TIMER_PRIORITY = (int) this.getID() * 2;

    public enum States {NORMAL, ELECTION, REORGANIZING} ;

    // timers
    private DelayTimer timer_aycoord  = new DelayTimer(this, DelayTimer.Timers.AYCOORD,  TIMER_AYCOORD);
    private DelayTimer timer_aythere  = new DelayTimer(this, DelayTimer.Timers.AYTHERE,  TIMER_AYTHERE);
    private DelayTimer timer_merge    = new DelayTimer(this, DelayTimer.Timers.MERGE,    TIMER_MERGE);
    private DelayTimer timer_ready    = new DelayTimer(this, DelayTimer.Timers.READY,    TIMER_READY);
    private DelayTimer timer_accept   = new DelayTimer(this, DelayTimer.Timers.ACCEPT,   TIMER_ACCEPT);
    private DelayTimer timer_priority = new DelayTimer(this, DelayTimer.Timers.PRIORITY, TIMER_PRIORITY);

    // Definition
    private States state;
    private Set<Node> my_group = new HashSet<Node>();     // conjunto dos membros do próprio grupo
    private Set<Node> union_group = new HashSet<Node>();  // conjunto dos membros da união dos grupos
    private Set<Node> others_coord = new HashSet<Node>(); // outros coordenadores

    private int group_id;                                    // identificação do grupo, através do par (CoordID, count)
    private Node coord;
    private int counter = 0;
    private int number_answers_ready = 0;

    public int value_to_inspect = 0;


    public void init() {
        this.setRadioIntensity(INTENSITY_SIGNAL);
        Control.instance.nodes.add(this);

        this.coord = this;
        this.group_id = (int) this.getID();
        this.state = States.NORMAL;

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

        if (timer == DelayTimer.Timers.AYCOORD) {
            log("finished timer of AYCOORD");
            if (this.others_coord.isEmpty()) {
                this.timer_aycoord.deactivate();
                return;
            }
            else
                this.timer_priority.activate();

        }
        else if (timer == DelayTimer.Timers.AYTHERE) {
            if (!this.timer_aythere.isMsg_arrived()) {
                log(String.format("Coordinator %d has not answered in time. Starting Recovery Mode.", this.coord.getID()));
                this.recovery();
            }
            else {
                this.timer_aythere.deactivate();
            }
        }
        else if (timer == DelayTimer.Timers.MERGE) {
            log("finished timer of MERGE");
            this.state = States.REORGANIZING;
            this.number_answers_ready = 0;
            this.timer_ready.activate();
            for (Node n: this.union_group) send(new Ready(this, this.group_id), n);
            this.timer_merge.deactivate();
        }
        else if (timer == DelayTimer.Timers.READY) {
            log("finished timer of READY");
            if (this.number_answers_ready < this.my_group.size())
                this.recovery();
            else
                this.state = States.NORMAL;
            this.timer_ready.deactivate();
        }
        else if (timer == DelayTimer.Timers.ACCEPT) {
            this.recovery();
        }
        else if (timer == DelayTimer.Timers.PRIORITY) {
            this.merge();
            this.timer_priority.deactivate();
            this.timer_aycoord.deactivate();

        }

    }

    private void handleAYCoord(AYCoord m) { ;
        log("received AYCoord from %d", m.sender.getID());

        if ((this.state == States.NORMAL) && (this.coord == this)) {
            send(new AYCoordAnswer(this, true), m.sender);
            log("sending AYCoordAnswer as TRUE to %d", m.sender.getID());
        }
        else {
            send(new AYCoordAnswer(this, false), m.sender);
            log("sending AYCoordAnswer as FALSE to %d", m.sender.getID());
        }
    }

    private void handleAYCoordAnswer(AYCoordAnswer m) {
        log("received AYCoordAnswer from %d", m.sender.getID());
        if (m.answer)
            this.others_coord.add(m.sender);
    }

    private void handleAYThere(AYThere m) {
        log("received AYThere from %d", m.sender.getID());
        if ((this.iAmCoord()) && (this.group_id == m.group_id) && (this.my_group.contains(m.sender))) {
            log("sending AYThereAnswer as TRUE to %d", m.sender.getID());
            send(new AYThereAnswer(this, true), m.sender);
        }
        else{
            log("sending AYThereAnswer as TRUE to %d", m.sender.getID());
            send(new AYThereAnswer(this, false), m.sender);
        }
    }

    private void handleAYThereAnswer(AYThereAnswer m) {
        log("received AYThereAnswer from %d", m.sender.getID());
        if (this.coord == m.sender)
            this.timer_aythere.setMsg_arrived(true); // Coordinator is alive, wait for some time
    }

    private void handleInvitation(Invitation m) {
        log("received Invitation from %d", m.sender.getID());

        if (this.state == States.NORMAL) {
            this.suspend_processing_application();
            Node old_coord = this.coord;
            this.my_group = this.union_group;
            this.state = States.ELECTION;
            this.coord = m.sender;
            this.group_id = m.group_id;

            log("relaying Invitation to my_group");

            if (old_coord == this)
                for (Node n : this.my_group) send (new Invitation(this, this.coord, this.group_id), n);

            log("sending Accept to %d", this.coord.getID());
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

    private void handleAccept(Accept m) {
        log("received Accept from %d", m.sender.getID());
        if ((this.state == States.ELECTION) && (this.coord == this) && (this.group_id == m.group_id))
        {
            this.union_group.add(m.sender);
            send(new AcceptAnswer(this, true), m.sender);
            log("sending AcceptAnswer as TRUE to %d", m.sender.getID());
        }
        else {
            send(new AcceptAnswer(this, false), m.sender);
            log("sending AcceptAnswer as FALSE to %d", m.sender.getID());
        }
    }

    private void handleAcceptAnswer(AcceptAnswer m) {
        log("received AcceptAnswer from %d", m.sender.getID());
        this.timer_accept.deactivate();
        if (this.coord == m.sender){
            if (m.answer) {
                this.state = States.REORGANIZING;
            }
            else {
                this.recovery();
            }

        }

    }

    private void handleReady(Ready m) {
        log("received Ready from %d", m.node.getID());

        if ((this.group_id == m.group_id) && (this.state == States.REORGANIZING))
        {
            // this.definition = m.definition;
            this.state = States.NORMAL;
            send(new ReadyAnswer(this, true, this.group_id), this.coord);
            log("sending ReadyAnswer as TRUE to %d", this.coord.getID());
        }
        else {
            send(new ReadyAnswer(this, false), m.node);
            log("sending ReadyAnswer as FALSE to %d", m.node.getID());
        }
    }

    private void handleReadyAnswer(ReadyAnswer m) {

        if ((m.accept == true) && (m.group_id == this.group_id))
        {
            log("received ReadyAnswer from %d", m.sender.getID());
            this.number_answers_ready += 1;
        }
    }


    private boolean iAmCoord() {
        return this.coord == this;
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

        if ((this.state == States.NORMAL) && (this.iAmCoord()) && (!this.timer_aycoord.isEnabled())) {
            log("broadcasting AYCOORD");
            this.others_coord.clear();
            broadcast(new AYCoord(this));
            this.timer_aycoord.activate();
        }
    }

    public void check_coord() {

        if ((this.state == States.NORMAL) && (!this.timer_aythere.isEnabled()))
        {
            AYThere msg = new AYThere(this, this.group_id);
            log("sending AYTHERE");
            send(msg, coord);
            this.timer_aythere.setMsg_arrived(false);
            this.timer_aythere.activate();
        }
    }

    public void suspend_processing_application() {

    }

    public void merge() {

        if ((this.iAmCoord()) && (this.state == States.NORMAL)) {
            log("starting MERGE");
            this.state = States.ELECTION;
            this.suspend_processing_application();
            this.counter += 1;
            this.group_id = (int) this.getID();
            this.my_group.addAll(this.union_group);
            this.union_group.clear();
            this.timer_merge.activate();

            Set<Node> set_nodes = new HashSet<Node>();
            set_nodes.addAll(this.others_coord);
            set_nodes.addAll(this.my_group);

            for (Node n: set_nodes) send(new Invitation(this, this.coord, this.group_id), n);

        }
    }

    public void postStep() {

        if (this.iAmCoord()) // if coord, check other coordinators
            this.check_members();
        else                 // if member, check if coord is alive
            this.check_coord();

    }

    @NodePopupMethod(menuText = "Print status") // Maybe show the coordinator
    public void printStatus() {
        String my_group_str = "";
        for (Node n : this.my_group) my_group_str += " " + n.getID();

        String union_group_str = "";
        for (Node n : this.union_group) union_group_str += " " + n.getID();

        String other_coord_str = "";
        for (Node n : this.others_coord) other_coord_str += " " + n.getID();


        log("Coordinator: " + String.valueOf(this.coord.getID())
                + " | status: " + String.valueOf(this.state)
                + " | my_group: " + String.valueOf(my_group_str)
                + " | union_group: " + String.valueOf(union_group_str)
                + " | other_coord: " + String.valueOf(other_coord_str)
        );
    }

    public void log(String format, Object... args) {
        String s = String.format("NODE %d : ", this.getID());
        Control.instance.log(s + format, args);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {

        this.setLeaderColor();

        int value = -1;

        if (this.value_to_inspect == 0) // id
            value = (int) this.getID();

        if (this.value_to_inspect == 1) // coord
            value = (int) this.coord.getID();

        String text = String.valueOf(value);

        super.drawNodeAsDiskWithText(g, pt, highlight, text,2, Color.WHITE);
    }

    public void setLeaderColor() {
        int colors[][] = {{1,184,120}, {125,58,192}, {0,176,55}, {255,85,197}, {65,149,0},
                            {140,125,255}, {88,118,0}, {70,142,255}, {233,92,25}, {103,184,255},
                            {168,7,15}, {58,220,199}, {186,0,129}, {41,94,28}, {255,75,143},
                            {249,187,95}, {88,69,149}, {163,100,0}, {235,165,255}, {236,190,140},
                            {101,108,159}, {255,79,110}, {201,144,184}, {131,59,78}, {255,175,184}};

        if (this.coord.getID() < 25) {
            int c[] = colors[(int) this.coord.getID()];
            this.setColor(new Color(c[0], c[1], c[2]));
        }
    }

    public void preStep() {}
    public void checkRequirements() {}
    public void neighborhoodChange() {}
}
