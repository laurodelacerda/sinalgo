package projects.trab2.nodes.nodeImplementations;

import projects.trab2.Control;
import projects.trab2.nodes.messages.*;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;


public class Node extends sinalgo.nodes.Node {

    public void init() {
        Control.instance.nodes.add(this);
    }

    public void handleMessages(Inbox inbox) {

        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof AYCoordinator)
                handleAYCoordinator((AYCoordinator) msg);
            else if (msg instanceof AYThere)
                handleAYThere((AYThere) msg);
            else if (msg instanceof Accept)
                handleAccept((Accept) msg);
            else if (msg instanceof Ready)
                handleReady((Ready) msg);
        }
    }

    private void handleAYCoordinator(AYCoordinator m) { ;
        log("received AYCoordinator from %d", m.node.getID());
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

    @NodePopupMethod(menuText = "Print status") // Maybe show the coordinator
    public void printStatus() {
//        log("trying: " + String.valueOf(cs) + " | tsCS:" + this.tsCS +  " | votes: " + this.yes + " | waiting: " + s);
    }

    public void log(String format, Object... args) {
        String s = String.format("NODE %d : ", this.getID());
        Control.instance.log(s + format, args);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight)
    {
        String text = String.valueOf(this.getID());
        super.drawNodeAsDiskWithText(g, pt, highlight, text,12, Color.WHITE);
    }

    public void checkRequirements(){}
    public void preStep(){}
    public void postStep(){}
    public void neighborhoodChange(){}
}
