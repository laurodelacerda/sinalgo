package projects.trab2.nodes.nodeImplementations;

import projects.trab2.Control;
import projects.trab2.nodes.messages.*;
import projects.trab2.nodes.timers.DelayTimer;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Node extends sinalgo.nodes.Node {

    // consts
    public final double INTENSITY_SIGNAL = 15.0;
    public final int TIMER_AYCOORD = 5;
    public final int TIMER_AYTHERE = 5;

    public enum States {NORMAL, ELECTION, REORGANIZING} ;

    // Definition
    private States state;
    private List<Node> my_group;            // conjunto dos membros do próprio grupo
    private List<Node> union_group;         // conjunto dos membros da união dos grupos
    private int group_id;                   // identificação do grupo, através do par (CoordID, count)
    private List<Node> others_coord;        // outros coordenadores

    private DelayTimer dt_coord = new DelayTimer(this, TIMER_AYCOORD);

    private Node coord = null;

    private int count = 0;

    public void init() {
        this.setRadioIntensity(INTENSITY_SIGNAL);
        Control.instance.nodes.add(this);
        this.group_id = (int) this.getID();
        this.state = States.NORMAL;
        this.my_group  = new ArrayList<Node>();
        this.union_group  = new ArrayList<Node>();
        this.others_coord = new ArrayList<Node>();
        this.dt_coord.startRelative(1, this);
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


    private boolean iAmCoord() {
        return this.group_id == (int) this.getID();
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
        log("received Accept from %d", m.node.getID());
        if ((this.state == States.ELECTION) && (this.coord == this) && (this.group_id == m.group_id))
        {
            this.union_group.add(m.node);
            send(new AcceptAnswer(this, true), m.node);
            
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

        this.state = States.NORMAL;

    }

    private void handleAYCoordAnswer(AYCoordAnswer m) {
        log("received AYCoordAnswer from %d", m.sender.getID());
    }


    private void handleReadyAnswer(ReadyAnswer m) {
    }


    private void handleAYThereAnswer(AYThereAnswer m) {

        this.state = States.NORMAL;

    }


    private void sendReady() {
//        for (Node n: this.union_group)
//            send(new Ready(this), n);


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


    public void recovery() {
        log("Started recovery!");
        this.state = States.ELECTION;
        this.count += 1;
        this.group_id = (int) this.getID();
        this.coord = this;
        this.union_group.clear();
        this.state = States.REORGANIZING;
        // definition = my_appl_state
        this.state = States.NORMAL;
    }

    public void stop_processing() {

    }

    public void check_members() {

        if ((this.iAmCoord()) && (this.state == States.NORMAL)) {
            this.others_coord.clear();
            AYCoord msg = new AYCoord(this);
            broadcast(msg);
//            DelayTimer dt = new DelayTimer(this, msg, TIMER_AYCOORD);
        }
    }

    public void check_coord() {

        if (this.iAmCoord())
            return;

        AYThere msg = new AYThere(this, this.group_id);
        send(msg, coord);
//        DelayTimer dt = new DelayTimer(this, msg, TIMER_AYTHERE);

    }


    public void preStep() {}

    public void postStep() {

        // if coord, check other coordinators
        this.check_members();

        // if member, check if coord is alive
//        this.check_coord();


    }

    public void checkRequirements() {}
    public void neighborhoodChange() {}
}
