/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.t1.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.t1.nodes.messages.SimpleMessage;
import projects.t1.nodes.messages.SemaphoreMessage;
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

import sinalgo.models.MobilityModel;

import java.awt.*;

import java.math.*;

/**
 * The Node of the sample project.
 */
@Getter
@Setter
public class SimpleNode extends Node {

    private double RANGE_AREA  = 7000.0;
    private double CRITIC_AREA = 6000.0;

    private double distance = -1;

    private int msgs = 0;
    private boolean inRange  = false;
    private boolean inCS = false;
    private boolean waiting  = false;

    public boolean isInRange() {
        return this.inRange;
    }
    public boolean isInCriticZone(Position semaphore_pos) { return this.inCS; }
    public boolean hasToken() {
        return this.hasToken;
    }

    public double distanceToSemaphore(Position pos)
    {
        return this.getPosition().squareDistanceTo(pos);
    }

    private boolean hasToken = false;
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

    private int msgReceived;

    /**
     * The amount to increment the data of the message each time it goes throug a
     * node.
     */
    private int increment;

    Logging my_log = Logging.getLogger("/home/lauro/Development/github/sinalgo/logs/simple_log.txt");

    public boolean isWaiting(){ return this.waiting; }

    public void setWaiting(boolean w){ this.waiting = w; }

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

//                this.msgReceived += 1;

//                if (this.getNext() != null) {

//                this.setColor(new Color((float) 0.5 / (1), (float) 0.5, (float) 1.0 / (1)));

//                    Tools.appendToOutput("Got a message from node in position" + "(" + String.valueOf(m.getPosition()) + ")\n");
//                    m.incrementData();
//                    DelayTimer dt = new DelayTimer(m, this, m.getData());
//                    dt.startRelative(m.getData(), this);


//                }
            }
            else if (msg instanceof SemaphoreMessage)
            {
                SemaphoreMessage m = (SemaphoreMessage) msg;

                Position my_pos = this.getPosition();
                Position semaphore_pos = m.getPosition();

                this.msgs += 1;
                this.distance = my_pos.squareDistanceTo(semaphore_pos);
                this.inRange = this.distance <= this.RANGE_AREA;
                this.inCS = this.distance <= this.CRITIC_AREA;

                boolean my_turn = m.getAuthorized() == this.getID() ? true : false ;

                // Alcance do sinal
                if (my_turn)
                {
                    this.hasToken = true;
                    this.waiting = false;
                    this.msgs = 0;
                }
                else
                {
                    this.hasToken = false;
                }


            }
        }
    }

    @Override
    public void preStep() {

    }

    @Override
    public void init() {

//        Position pos = this.getPosition();
//        SimpleMessage mes = new SimpleMessage(pos);
//        MessageTimer msgTimer = new MessageTimer(mes); // broadcast
//        msgTimer.startRelative(1, this);
//        broadcast(mes);
        this.msgSent += 0;
//                this.msgSentInThisRound;
//        this.msgSentInThisRound = 0;
        this.msgReceived = 0;
    }

    @Override
    public void neighborhoodChange() {
//        this.setNext(null);
//        for (Edge e : this.getOutgoingConnections()) {
//            if (this.getNext() == null) {
//                this.setNext((SimpleNode) e.getEndNode());
//            } else {
//                if (e.getEndNode().compareTo(this.getNext()) < 0) {
//                    this.setNext((SimpleNode) e.getEndNode());
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

//        MessageTimer msgTimer = new MessageTimer(new SimpleMessage(1)); // broadcast
//        msgTimer.startRelative(1, this);
//        Tools.appendToOutput("Start Routing from node " + this.getID() + "\n");

//        Position pos = this.getPosition();
//        double lat =  pos.xCoord;
//        double lon =  pos.xCoord;
//        MessageTimer msgTimer = new MessageTimer(new SimpleMessage(pos)); // broadcast
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

        if ((this.inRange) && (!this.hasToken)) // gray - parado esperando na fila
        {
            this.setColor(new Color(128, 128, 128));
        }
        else if((this.inRange) && (this.hasToken)) // green - no range e com o token
        {
            this.setColor(new Color(0, 255, 0));
        }
        else if ((!this.inRange) && (!this.hasToken)) // blue - fora do range sem o token (qualquer nó fora do range)
        {
            this.setColor(new Color(0, 0, 255));
        }
//        else // !this.waiting && this.hastoken  // orange - em movimento e com o token
//        {
//            this.setColor(new Color(255, 165, 0));
//        }
//        String text = Integer.toString(this.msgSent) + "|" + Integer.toString(this.msgReceived);
        // draw the node as a circle with the text inside
        String text = String.valueOf(this.getID());
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.YELLOW);
        // super.drawNodeAsSquareWithText(g, pt, highlight, text, 10, Color.YELLOW);
    }

    @Override
    public void postStep() {

        if (this.inRange)
        {
            if (this.msgs > 3)
                this.waiting = true;
            // else node it note heading to CRITICAL_AREA
        }
        else {
            this.waiting = false;
            this.hasToken = false;
        }

        Position pos = this.getPosition();
        SimpleMessage mes = new SimpleMessage(this.getID(), pos, this.waiting);
        MessageTimer msgTimer = new MessageTimer(mes); // broadcast
        msgTimer.startRelative(1, this);
        broadcast(mes, this.RANGE_AREA);
//        this.msgSent += 1;
    }

    @Override
    public String toString() {
//        return "Messages sent so far: " + this.msgSent + "\nMessages sent in this round: " + this.msgSentInThisRound;
//        return "Messages sent: " + this.msgSent + "\nMessages received: " + this.msgReceived;

        return  "In Range: " + String.valueOf(this.inRange) + "(" + String.valueOf(round(this.distance,2)) + ")"
                + "| Waiting: " + String.valueOf(this.waiting)
                + "| Token: " + String.valueOf(this.hasToken);
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
        if (this.increment < 0) {
            throw new WrongConfigurationException(
                    "SimpleNode: The increment value (specified in the config file) must be greater or equal to 1.");
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
