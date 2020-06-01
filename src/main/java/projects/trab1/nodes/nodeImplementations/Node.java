package projects.trab1.nodes.nodeImplementations;

import projects.trab1.Control;
import projects.trab1.nodes.messages.*;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class Node extends sinalgo.nodes.Node {

    private List<Node> my_district;

    private int ts = 0;
    private int tsCS = -1;
    private int yes = 0;
    public int wait_time = 0;
    public int value_to_inspect = 0;
    public int inquiries = 0;
    public int relinquishes = 0;

    private Require candidate;
    private boolean hasInquired = false;
    private boolean inCS = false;

    private Comparator c = new Comparator(); // comparator for requires
    private PriorityQueue<Require> deferred = new PriorityQueue<Require>(c);

    public void init() {
        Control.instance.nodes.add(this);
        this.printDistrict();
    }

    public void handleMessages(Inbox inbox) {

        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof Require)
                handleRequire((Require) msg);
            else if (msg instanceof Release)
                handleRelease((Node) inbox.getSender());
            else if (msg instanceof Yes)
                handleYes((Node) inbox.getSender());
            else if (msg instanceof Inquire)
                handleInquire((Inquire) msg);
            else if (msg instanceof Relinquish)
                handleRelinquish();
        }
    }

    public void tryToEnterTheCS() {
        if (this.isTryingToEnterTheCS()) {
            return;
        }

        this.ts +=1;
        this.tsCS = this.ts;
        Require m = new Require(this, this.ts);
        List<Node> district = this.getDistrict();

        log("sending REQUEST to district");
        for (Node n: district)
            this.sendDirect(m, n);

    }

    private void handleRequire(Require m) { ;
        this.ts = Math.max(this.ts, m.ts) + 1;
        log("got REQUIRE from %d", m.node.getID());
        this.tryToVote(m);
    }

    private void tryToVote(Require m) {
        if (this.hasVoted()) {
            this.deferred.add(m);

            int result = this.c.compare(m, this.candidate);
            log("Comparing timestamp: " + m.ts + " - " + this.candidate.ts + " = " + (result));

            if ((result <= 0) && !this.hasInquired) {

                Inquire inquire = new Inquire(this, this.candidate.ts);
                this.sendDirect(inquire, this.candidate.node);
                this.hasInquired = true;
                log("sending INQUIRE to %d", this.candidate.node.getID());

            }
        } else {
            this.vote(m);
        }
    }

    private void handleYes(Node sender) {
        log("received YES from %d", sender.getID());
        this.yes +=1;
        if(this.yes == Control.instance.k)
        {
            log("ACCESSING CS");
            this.inCS = true;

            Control.instance.enterCS[(int) this.getID()-1] += 1;
            this.tsCS = -1;
            this.yes  = 0;

            List<String> district = new ArrayList<String>();
            for (Node node : this.getDistrict()) {
                this.sendDirect(new Release(), node);
                district.add(Long.toString(node.getID()));
            }

            log(String.format("sending RELEASE to district [%s]", String.join(", ", district)));
            this.inCS = false;
        }
    }

    private void handleRelease(Node sender) {
        log("received RELEASE from %d", sender.getID());

        Require deferred = this.deferred.poll();

        if (deferred != null)
            this.vote(deferred);
        else
            this.resetVote();

        this.hasInquired = false;
    }

    private void handleInquire(Inquire m) {
        Control.instance.inquire += 1;
        this.inquiries += 1;

        int senderID = (int) m.node.getID();
        log("received INQUIRE from %d", senderID, this.tsCS, m.ts);

        if (this.tsCS == m.ts)
        {
            log("sending RELINQUISH to %d", senderID);
            this.sendDirect(new Relinquish(), m.node);
            this.yes -= 1;
        }
    }

    private void handleRelinquish() {
        Control.instance.relinquish +=1;
        this.relinquishes +=1;

        log("received RELINQUISH");

        if (this.candidate != null) {
            this.deferred.add(this.candidate);
            this.vote(this.deferred.poll());
        }
        this.hasInquired = false;
    }

    private void vote(Require m) {
        this.candidate = (Require) m.clone();
        boolean same = m.node.getID() == this.getID();
        log("sending YES to %d",  this.candidate.node.getID());

        if (same)
            this.handleYes(this);
        else
            this.sendDirect(new Yes(), this.candidate.node);
    }

    private void resetVote() {
        this.candidate = null;
    }

    private boolean hasVoted() {
        return this.candidate != null;
    }

    private boolean isTryingToEnterTheCS(){
        return this.tsCS >= 0;
    }

    private List<Node> getDistrict() {
        if (this.my_district == null){
            this.my_district = Control.instance.getDistrict(this);
        }
        return this.my_district;
    }

    @NodePopupMethod(menuText = "Print district")
    public void printDistrict() {
        Control.instance.printDistrict(this);
    }

    @NodePopupMethod(menuText = "Print status")
    public void printStatus() {

        String s = "";
        for (Require r : this.deferred)
            s += " " + r.node.getID() + ":" + r.ts;

        boolean cs = this.isTryingToEnterTheCS();

        log("trying: " + String.valueOf(cs) + " | tsCS:" + this.tsCS +  " | votes: " + this.yes + " | waiting: " + s);
    }

    @NodePopupMethod(menuText="Last vote")
    public void printLastVote() {
        if (this.hasVoted())
        {
            String s = String.format("%d [ts %d] (ts_cs: %d)", this.candidate.node.getID(), this.candidate.ts, this.tsCS) ;
            log(s);
        }
        else
            log("no votes");
    }

    public void log(String format, Object... args) {
        String s = String.format("NODE %d [ts %d]: ", this.getID(), this.ts);
        Control.instance.log(s + format, args);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight)
    {
        if (this.inCS)
            this.setColor(new Color(0, 128, 0));
        else
            this.setColor(new Color(0, 0, 0));

        int value = -1;

        if (this.value_to_inspect == 0) // id
            value = (int) this.getID();

        if (this.value_to_inspect == 1) // cs
            value = (int) Control.instance.enterCS[(int) this.getID()-1];

        if (this.value_to_inspect == 2) // inq
            value = this.inquiries;

        if (this.value_to_inspect == 3) // rel
            value = this.relinquishes;


        String text = String.valueOf(value);
        super.drawNodeAsDiskWithText(g, pt, highlight, text,12, Color.WHITE);
    }

    public void checkRequirements(){}
    public void preStep(){}
    public void postStep(){}
    public void neighborhoodChange(){}
}
