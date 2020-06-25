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

    // Definition
    private int state;                 // {Normal = 0, Election = 1, Reorganizing = 2}
    private List<Node> my_group;       // conjunto dos membros do próprio grupo
    private List<Node> union_group;    // conjunto dos membros da união dos grupos
    private int group_id;              // identificação do grupo, através do par (CoordID, count)
    private List<Node> others_coord;   // outros coordenadores


    public void init() {
        Control.instance.nodes.add(this);
        this.group_id = (int) this.getID();
        this.state = 0;
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


    private boolean iAmCoord() {
        return this.group_id == (int) this.getID();
    }


    private void handleAYCoord(AYCoord m) { ;
        log("received AYCoord from %d", m.node.getID());
        send(new Accept(this), m.node, INTENSITY_SIGNAL);
    }


    private void handleAYThere(AYThere m) {
        log("received AYThere from %d", m.node.getID());
    }


    private void handleAccept(Accept m) {
        log("received Accept from %d", m.node.getID());

    }


    private void handleReady(Ready m) {
        log("received Ready from %d", m.node.getID());

    }


    private void handleInvitation(Invitation m) {

        if (this.iAmCoord()) {
            for (Node n: my_group)
                send(new Invitation(this, m.sender), n, INTENSITY_SIGNAL);
            send(new Accept(this), m.sender, INTENSITY_SIGNAL);
        }
        else
            send(new Accept(this), m.new_coord, INTENSITY_SIGNAL);

        this.state = 2;
    }


    private void handleAcceptAnswer(AcceptAnswer m) {

        this.state = 0;

    }

    private void handleAYCoordAnswer(AYCoordAnswer m) {

    }


    private void handleReadyAnswer(ReadyAnswer m) {
    }


    private void handleAYThereAnswer(AYThereAnswer m) {

        this.state = 0;

    }


    private void recovery() {
    }


    private void sendReady() {
        for (Node n: this.union_group)
            send(new Ready(this), n, INTENSITY_SIGNAL);


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


    public void time_fired() {
        log("Time has fired!");
    }


    public void check_members() {

        if ((this.iAmCoord()) && (this.state == 0)) {
            this.others_coord.clear();
            AYCoord msg = new AYCoord(this);
            broadcast(msg, INTENSITY_SIGNAL);
            DelayTimer dt = new DelayTimer(this, msg, TIMER_AYCOORD);
        }
    }

    public void preStep(){}

    public void postStep(){

        // if Coord, broadcast AYCoord messages
        this.check_members();


    }

    public void checkRequirements(){}
    public void neighborhoodChange(){}
}
