package projects.trab2.nodes.nodeImplementations;

import projects.trab2.Control;
import projects.trab2.nodes.messages.*;
import projects.trab2.nodes.timers.DelayTimer;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Node extends sinalgo.nodes.Node {

    public enum States {NORMAL, ELECTION, REORGANIZING} ;

    // timeouts
    public final int TIMER_AYCOORD = 25;
    public final int TIMER_AYTHERE = 20;
    public final int TIMER_MERGE = 10; // timeout T1
    public final int TIMER_READY = 10; // timeout T2
    public final int TIMER_ACCEPT = 10;
    public final int TIMER_PRIORITY = (int) this.getID() * 3;

    // timers
    private DelayTimer timer_aycoord  = new DelayTimer(this, DelayTimer.Timers.AYCOORD,  TIMER_AYCOORD);
    private DelayTimer timer_aythere  = new DelayTimer(this, DelayTimer.Timers.AYTHERE,  TIMER_AYTHERE);
    private DelayTimer timer_merge    = new DelayTimer(this, DelayTimer.Timers.MERGE,    TIMER_MERGE);
    private DelayTimer timer_ready    = new DelayTimer(this, DelayTimer.Timers.READY,    TIMER_READY);
    private DelayTimer timer_accept   = new DelayTimer(this, DelayTimer.Timers.ACCEPT,   TIMER_ACCEPT);
    private DelayTimer timer_priority = new DelayTimer(this, DelayTimer.Timers.PRIORITY, TIMER_PRIORITY);

    private List<DelayTimer> timers_answer = new ArrayList<DelayTimer>();

    // definition
    private States state;
    private Set<Node> my_group = new HashSet<Node>();     // conjunto dos membros do próprio grupo
    private Set<Node> others_coord = new HashSet<Node>(); // outros coordenadores
    private int group_id;                              // identificação do grupo, através do par (CoordID, count)
    private Node coord;                                // nó coordenador

    // stats
    private int number_answers_ready = 0;
    private int counter_merge = 0;
    private int counter_recovery = 0;
    private List<Integer> timer_convergence = new ArrayList<Integer>();
    private int start_convergence = 0;
    private int end_convergence = 0;

    // visualization
    public int value_to_inspect = 0;


    public void init() {
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
            log(String.format("finished AYCOORD timer after %d steps", this.TIMER_AYCOORD));
            if (this.others_coord.isEmpty()) {
                this.timer_aycoord.deactivate();
                return;
            }
            else
                this.timer_priority.activate();

        }
        else if (timer == DelayTimer.Timers.AYTHERE) {
            log(String.format("finished AYTHERE timer after %d steps", this.TIMER_AYTHERE));
            if (!this.timer_aythere.isMsg_arrived()) {
                log(String.format("Coordinator %d has not answered in time. Starting Recovery Mode.", this.coord.getID()));
                this.recovery();
            }
            else {
                this.timer_aythere.deactivate();
            }
        }
        else if (timer == DelayTimer.Timers.MERGE) {
            log(String.format("finished MERGE timer after %d steps", this.TIMER_MERGE));
            this.state = States.REORGANIZING;
            this.number_answers_ready = 0;
            this.timer_ready.activate();
            String set_str = "";
            for (Node n: this.my_group) set_str += String.valueOf(n.getID()) + " ";
            log(String.format("sending READY to %s", set_str));
            for (Node n: this.my_group) send(new Ready(this, this.group_id), n);
            this.timer_merge.deactivate();
        }
        else if (timer == DelayTimer.Timers.READY) {
            log(String.format("finished READY timer after %d steps", this.TIMER_READY));
            if (this.number_answers_ready < this.my_group.size()) {
                log("Coordinator has NOT get ready_answers enough");
                this.recovery();
            }
            else {
                if (this.number_answers_ready > 0) {
                    this.end_convergence = (int) Global.getCurrentTime();
                    int result = this.end_convergence - this.start_convergence;
                    log(String.format("Coordinator has got ready_answers. Network converged in %d steps", result));
                    this.timer_convergence.add(result);
                    Control.instance.timer_convergence.add(this.end_convergence - this.start_convergence);
                }
                else {
                    log("Coordinator has 0 ready_answers");
                }
                this.state = States.NORMAL;
            }
            this.timer_ready.deactivate();
        }
        else if (timer == DelayTimer.Timers.ACCEPT) {
            log(String.format("AcceptAnswer has not arrived! Finished ACCEPT timer after %d steps", this.TIMER_ACCEPT));
            this.recovery();
        }
        else if (timer == DelayTimer.Timers.PRIORITY) {
            log(String.format("finished PRIORITY timer after %d steps", this.TIMER_PRIORITY));
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
    } // all nodes

    private void handleAYCoordAnswer(AYCoordAnswer m) {
        log("received AYCoordAnswer from %d", m.sender.getID());
        if (m.answer)
            this.others_coord.add(m.sender);
    } // coord

    private void handleAYThere(AYThere m) {
        log("received AYThere from %d", m.sender.getID());
        if ((this.iAmCoord()) && (this.group_id == m.group_id) && (this.my_group.contains(m.sender))) {
            log("sending AYThereAnswer as TRUE to %d", m.sender.getID());
            send(new AYThereAnswer(this, true), m.sender);
        }
        else{
            log("sending AYThereAnswer as FALSE to %d", m.sender.getID());
            send(new AYThereAnswer(this, false), m.sender);
        }
    } // all nodes

    private void handleAYThereAnswer(AYThereAnswer m) {
        log("received AYThereAnswer from %d", m.sender.getID());
        if ((this.coord == m.sender) && (m.answer))
            this.timer_aythere.setMsg_arrived(true); // Coordinator is alive, wait for some time
    } // member

    private void handleInvitation(Invitation m) {
        log("received Invitation from %d", m.sender.getID());

        if (this.state == States.NORMAL) {
            this.suspend_processing_application();
            Node old_coord = this.coord;
            Set<Node> temp_group = new HashSet<Node>();
            temp_group.addAll(this.my_group);
            this.state = States.ELECTION;
            this.coord = m.sender;
            this.group_id = m.group_id;

            log("relaying Invitation to my_group");

            if (old_coord == this)
                for (Node n : temp_group) send (new Invitation(this, this.coord, this.group_id), n);

            log("sending Accept to %d", this.coord.getID());
            send(new Accept(this, this.group_id), this.coord);
            this.timer_accept.activate();

        }

    } // coord to be member

    private void handleAccept(Accept m) { // coord
        log("received Accept from %d", m.sender.getID());
        if ((this.state == States.ELECTION) && (this.coord == this) && (this.group_id == m.group_id))
        {
            this.my_group.add(m.sender);
            send(new AcceptAnswer(this, true), m.sender);
            log("sending AcceptAnswer as TRUE to %d", m.sender.getID());
        }
        else {
            send(new AcceptAnswer(this, false), m.sender);
            log("sending AcceptAnswer as FALSE to %d", m.sender.getID());
        }
    } // coord

    private void handleAcceptAnswer(AcceptAnswer m) { // old_coord and its members
        this.timer_accept.deactivate();
        if (this.coord == m.sender){
            if (m.answer) {
                log("received AcceptAnswer as ACCEPTED from %d", m.sender.getID());
                this.state = States.REORGANIZING;
            }
            else {
                log("received AcceptAnswer as NOT ACCEPTED from %d", m.sender.getID());
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
            this.timer_aythere.deactivate();
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
        this.counter_recovery += 1;
        this.coord = this;
        this.group_id = (int) this.getID();
        this.my_group.clear();
        this.state = States.REORGANIZING;
        // definition = my_appl_state
        this.state = States.NORMAL;
    }

    public void check_members() {

        if ((this.state == States.NORMAL) && (!this.timer_aycoord.isEnabled()) && (!this.timer_priority.isEnabled())) {
            log("broadcasting AYCOORD");
            this.others_coord.clear();
            broadcast(new AYCoord(this));
            this.timer_aycoord.activate();
            this.start_convergence = (int) Global.getCurrentTime();
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
            this.counter_merge += 1;
            Control.instance.counter_merge += 1;
            this.state = States.ELECTION;
            this.suspend_processing_application();
            this.group_id = (int) this.getID();
            Set<Node> temp_group = new HashSet<Node>();  // conjunto dos membros da união dos grupos
            temp_group.addAll(this.my_group);
            this.my_group.clear();
            this.timer_merge.activate();

            Set<Node> set_nodes = new HashSet<Node>();
            set_nodes.addAll(this.others_coord);
            set_nodes.addAll(temp_group);

            String nodes_str = "";
            for (Node n: set_nodes) nodes_str += String.valueOf(n.getID()) + " ";

            log(String.format("sending Invitation to %s", nodes_str));
            for (Node n: set_nodes) send(new Invitation(this, this.coord, this.group_id), n);

        }
    }

    public void preStep() {
        if (this.iAmCoord()) // if coord, check other coordinators
            this.check_members();
        else                 // if member, check if coord is alive
            this.check_coord();
    }

    public void postStep() {
    }

    @NodePopupMethod(menuText = "Print status")
    public void printStatus() {
        String my_group_str = "";
        for (Node n : this.my_group) my_group_str += " " + n.getID();

        String other_coord_str = "";
        for (Node n : this.others_coord) other_coord_str += " " + n.getID();


        log("Coordinator: " + String.valueOf(this.coord.getID())
          + " | my_group: " + String.valueOf(my_group_str)
          + " | other_coord: " + String.valueOf(other_coord_str));
    }

    public void log(String format, Object... args) {
        String s = String.format("NODE %d : [%s] ", this.getID(), String.valueOf(this.state));
        Control.instance.log(s + format, args);
    }

    @NodePopupMethod(menuText = "Print status")
    public void printStats() {
        int sum = 0;
        for (Integer i : this.timer_convergence) sum += i;
        double avg = 0;
        if (this.timer_convergence.size() > 0)
            avg = (double) sum / this.timer_convergence.size();

        DecimalFormat df = new DecimalFormat("#.##");
        String formatted = df.format(avg);
        int num_fail = this.counter_merge - this.timer_convergence.size();

        log(String.format("merge: %d | recovery: %d | fail to converge: %d | avg convergence time: %s",
                this.counter_merge, this.counter_recovery, num_fail, formatted));

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

    public void checkRequirements() {}
    public void neighborhoodChange() {}
}
