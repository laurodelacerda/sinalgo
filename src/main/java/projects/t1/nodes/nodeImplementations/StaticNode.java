
package projects.t1.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.t1.nodes.messages.SemaphoreMessage;
import projects.t1.nodes.messages.SimpleMessage;
import projects.t1.nodes.timers.DelayTimer;
import sinalgo.configuration.Configuration;
import sinalgo.exception.CorruptConfigurationEntryException;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

import sinalgo.nodes.Position;

import java.awt.*;

import java.util.*;


/**
 * The Node of the sample project.
 */
@Getter
@Setter
public class StaticNode extends Node {

    private double RANGE_AREA  = 7000.0;
    private double CRITIC_AREA = 6000.0;
    private int MAX_STEPS = 3;

    private boolean busy = false;
    private long id_authorized = -99;
    private long critical_area = -100;
    private long current_step;

//    Queue<Long> waiting_list = new LinkedList<Long>();
    LinkedList<Long> waiting_list = new LinkedList<Long>();
    LinkedList<Long> to_remove    = new LinkedList<Long>();
    Map<Long, Long> last_step     = new HashMap<Long, Long>();


//    Statistics
    private int msgSent;
    private int msgReceived;

//    Logging my_log = Logging.getLogger("/home/lauro/Development/github/sinalgo/logs/simple_log.txt");

    // a flag to prevent all nodes from sending messages
    @Getter
    @Setter
    private static boolean isSending = true;

    @Override
    public void handleMessages(Inbox inbox) {
        if (!isSending()) { // don't even look at incoming messages
            return;
        }
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof SimpleMessage)
            {
                SimpleMessage m = (SimpleMessage) msg;

                Position my_pos   = this.getPosition();
                Position nbor_pos = m.getPosition();

                this.last_step.put(m.getId(), this.current_step);

                if (m.isWaiting())
                {
                    // add node to the waiting list and save the last step
                    if(!this.waiting_list.contains(m.getId()))
                        this.waiting_list.add(m.getId());


                }
                else
                {
                    if(this.waiting_list.contains(m.getId()) && (m.getId() == this.waiting_list.get(0)))
                    {
                        // only remove authorized node when it goes out critic area
                        if (my_pos.squareDistanceTo(nbor_pos) > this.CRITIC_AREA)
                            this.to_remove.add(m.getId());
//                            this.waiting_list.remove(m.getId());
                    }
                }



//                    else
//                        this.id_authorized = this.waiting_list.peek();
//
//                }
//                else
//                {
//                    if ((this.waiting_list.contains(m.getId())) && (m.getId() == this.id_authorized))
//                    {
//                        if (my_pos.squareDistanceTo(nbor_pos) > 2000) {
//                            this.id_authorized = this.waiting_list.peek();
//                            this.waiting_list.remove();
//                        }
//                        else
//                            this.id_authorized = this.waiting_list.peek();
//                    }
//                }


                // área de cobertura
//                if (my_pos.squareDistanceTo(nbor_pos) <= 300)
//                {
//                    // Adiciona nó que não tiver na lista de espera
//                    if(!this.waiting_list.contains(m.getId()))
//                    {
//                        if ((m.getId() != this.critical_area) && (m.isWaiting()))
//                            this.waiting_list.add(m.getId());
//                    }
//                    else
//                    {
//                        // se mensagem veio de nó com o token
//                        if (m.getId() == this.waiting_list.peek())//  && (m.isWaiting())) {
//                        {
//                            this.id_authorized = m.getId();
//
//                            if (my_pos.squareDistanceTo(nbor_pos) <= 100) // nó na área crítica
//                            {
////                                this.waiting_list.remove();
//                                this.critical_area = m.getId();
//                            }
//                            else // nó fora da área crítica, autoriza passagem
//                            {
//                                this.waiting_list.remove();
////                                this.id_authorized = -99;
////                                this.critical_area = -100;
//                            }
//                        }
//                    }
//                        //                        this.id_authorized = !this.waiting_list.isEmpty() ? this.waiting_list.peek() : -99;
//                }


//                    else
//                    {
//                        if (m.getId() == this.waiting_list.peek())
//                        {
//
//                        }
//                    }
//
//                    if (m.getId() != this.critical_area)
//                    {
//
//                    }








//                this.msgReceived += 1;

//                if (this.getNext() != null) {

//                this.setColor(new Color((float) 0.5 / (1), (float) 0.5, (float) 1.0 / (1)));

//                    Tools.appendToOutput("Got a message from node in position" + "(" + String.valueOf(m.getPosition()) + ")\n");
//                    m.incrementData();
//                    DelayTimer dt = new DelayTimer(m, this, m.getData());
//                    dt.startRelative(m.getData(), this);


//                }
            }
        }
    }

    @Override
    public void preStep() {

    }

    @Override
    public void init() {

//        Position pos = this.getPosition();
//        SemaphoreMessage mes = new SemaphoreMessage(pos);
//        MessageTimer msgTimer = new MessageTimer(mes); // broadcast
//        msgTimer.startRelative(1, this);
//        broadcast(mes);
        this.msgSent += 0;
//                this.msgSentInThisRound;
//        this.msgSentInThisRound = 0;
        this.msgReceived = 0;

        this.current_step = 0;
    }

    @Override
    public void neighborhoodChange() {
//        this.setNext(null);
//        for (Edge e : this.getOutgoingConnections()) {
//            if (this.getNext() == null) {
//                this.setNext((StaticNode) e.getEndNode());
//            } else {
//                if (e.getEndNode().compareTo(this.getNext()) < 0) {
//                    this.setNext((StaticNode) e.getEndNode());
//                }
//            }
//        }
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

//        MessageTimer msgTimer = new MessageTimer(new SemaphoreMessage(1)); // broadcast
//        msgTimer.startRelative(1, this);
//        Tools.appendToOutput("Start Routing from node " + this.getID() + "\n");

//        Position pos = this.getPosition();
//        double lat =  pos.xCoord;
//        double lon =  pos.xCoord;
//        MessageTimer msgTimer = new MessageTimer(new SemaphoreMessage(pos, this.busy)); // broadcast
//        msgTimer.startRelative(1, this);
//        Tools.appendToOutput("Start Broadcasting from node " + this.getID() + "\n");
//        my_log.log("Start Broadcasting from node " + this.getID() + "(" + String.valueOf(pos) + ")\n");
//        Tools.appendToOutput("Start Broadcasting from node " + this.getID() + "(" + String.valueOf(pos) + ")\n");
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        // set the color of this node
//        this.setColor(
//                new Color((float) 0.5 / (1 + this.msgSent), (float) 0.5, (float) 1.0 / (1 + this.msgReceived)));
//        this.setColor(new Color(this.msgSent % 255, this.msgSent % 255, this.msgSent % 255));

        if (this.busy)
            this.setColor(new Color(255, 0, 0));
        else
            this.setColor(new Color(0, 255, 0));

//        String text = Integer.toString(this.msgSent) + "|" + Integer.toString(this.msgReceived);
        String text = String.valueOf(this.getID());
        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.YELLOW);
        // super.drawNodeAsSquareWithText(g, pt, highlight, text, 10, Color.YELLOW);
    }

    @Override
    public void postStep() {

        this.current_step += 1;

        if (!this.waiting_list.isEmpty())
        {
            //  Remove if a node from waiting_list if it does not communicate in the last 2 steps
            ListIterator<Long> iterator_waiting = this.waiting_list.listIterator();
            while (iterator_waiting.hasNext())
            {
                Long item = iterator_waiting.next();

                if (this.last_step.get(item) < this.current_step - this.MAX_STEPS)
                    iterator_waiting.remove();
            }

            //  Remove if a node has had the permission to go
//            ListIterator<Long> iterator_remove = this.to_remove.listIterator();
//            while (iterator_remove.hasNext())
//            {
//                Long item = iterator_remove.next();
//
//                if (this.waiting_list.contains(item))
//                {
//                    this.waiting_list.remove(item);
//                    iterator_remove.remove();
//                }
//            }
        }

        if (!this.waiting_list.isEmpty())
        {
            this.busy = true;
            this.id_authorized = this.waiting_list.get(0);
        }
        else
        {
            this.busy = false;
            this.id_authorized = -99;
        }

        Position pos = this.getPosition();
        SemaphoreMessage mes = new SemaphoreMessage(pos, this.busy, this.id_authorized);
        MessageTimer msgTimer = new MessageTimer(mes); // broadcast
        msgTimer.startRelative(1, this);
        broadcast(mes, this.RANGE_AREA);
//        this.msgSent += 1;

    }

    @Override
    public String toString() {
//        return "Messages sent so far: " + this.msgSent + "\nMessages sent in this round: " + this.msgSentInThisRound;
//        return "Messages sent: " + this.msgSent + "\nMessages received: " + this.msgReceived;

        String queue = "";
        for(Long s : this.waiting_list)
            queue += " " + String.valueOf(s) + ":" + String.valueOf(this.last_step.get(s));

        return "Authorized: " + String.valueOf(this.id_authorized) + " | Queue: " + queue;
    }


    @Override
    public void checkRequirements() throws WrongConfigurationException {
//        if (this.increment < 0) {
//            throw new WrongConfigurationException(
//                    "StaticNode: The increment value (specified in the config file) must be greater or equal to 1.");
//        }
    }
}
