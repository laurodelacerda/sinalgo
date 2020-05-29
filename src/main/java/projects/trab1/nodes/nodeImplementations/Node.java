package projects.trab1.nodes.nodeImplementations;

import projects.trab1.App;
import projects.trab1.nodes.messages.*;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class Node extends sinalgo.nodes.Node {

    private List<Node> coterie;

    private int ts = 0;

    private int tsCS = -1;

    private Require candidate;

    private int yes = 0;

    private boolean hasInquired = false;

    private Comparator c = new Comparator(); // comparator for requires

    private PriorityQueue<Require> deferred = new PriorityQueue<Require>(c);

    public void init(){
        App.instance.nodes.add(this);
    }

    public void handleMessages(Inbox inbox)
    {
        if (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof ReqMessage)
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


    @NodePopupMethod(menuText="Enter CS")
    public void tryToEnterTheCS()
    {
        if (this.isTryingToEnterTheCS()) {
            return;
        }
        log("trying to enter the CS", this.getID());
        this.ts +=1;
        this.tsCS = this.ts;
        Require m = new Require(this, this.ts);
        for (Node n: this.getCoterie()){
            this.sendDirect(m, n);
        }
    }

    private void handleRequire(Require m)
    {
        this.ts = Math.max(this.ts, m.ts) + 1;
        log("received REQ from %d", m.node.getID());
        this.tryToVote(m);
    }

    private void tryToVote(Require m)
    {
        if (this.hasVoted()) {
            this.deferred.add(m);
            log("BUT already voted");
            if (this.c.compare(m, this.candidate) == -1 && !this.hasInquired) {
                Inquire inquire = new Inquire(this, this.candidate.ts);
                this.sendDirect(inquire, this.candidate.node);
                this.hasInquired = true;
                log("sent INQ to %d", this.candidate.node.getID());
            }
        } else {
            this.vote(m);
        }
    }

    private void handleYes(Node sender)
    {
        log("received YES from %d", sender.getID());
        this.yes +=1;
        if(this.yes == App.instance.k)
        {
            App.instance.enterCS[(int) this.getID()-1] += 1;
            List<String> ids = new ArrayList<String>();
            this.tsCS = -1;
            this.yes = 0;
            for (Node node : this.getCoterie()){
                this.sendDirect(new Release(), node);
                ids.add(Long.toString(node.getID()));
            }
            this.handleRelease(this);
            log("ENTERED (AND LEFT) THE CRITICAL REGION");
            log("sent RELEASE to (%s)", String.join(",", ids));
        }
    }

    private void handleRelease(Node sender){
        log("receive RELEASE from %d", sender.getID());
        Require deferred = this.deferred.poll();
        if (deferred != null){
            this.vote(deferred);
        } else {
            this.resetVote();
        }
        this.hasInquired = false;
    }

    private void handleInquire(Inquire m)
    {
        int senderID = (int) m.node.getID();
        log("received INQ from %d (%d ?= %d)", senderID, this.tsCS, m.ts);
        App.instance.inquire += 1;
        if (this.tsCS == m.ts)
        {
            log("sent RELINQUISH to %d", senderID);
            this.sendDirect(new Relinquish(), m.node);
            this.yes -= 1;
        }
    }

    private void handleRelinquish()
    {
        log("-----------------------");
        log("received RELINQUISH");
        App.instance.relinquish +=1;
        this.deferred.add(this.candidate);
        this.vote(this.deferred.poll());
        this.hasInquired = false;
    }

    private void vote(Require m) {
        this.candidate = (Require) m.clone();
        boolean same = m.node.getID() == this.getID();
        if (same) {
            log("vote for itself");
            this.handleYes(this);
        }
        else {
            this.sendDirect(new Yes(), this.candidate.node);
            log("sent YES to %d", this.candidate.node.getID());
        }
    }

    private void resetVote(){
        this.candidate = null;
    }

    private boolean hasVoted(){
        return this.candidate != null;
    }

    private boolean isTryingToEnterTheCS(){
        return this.tsCS >= 0;
    }

    private List<Node> getCoterie(){
        if (this.coterie == null){
            this.coterie = App.instance.getCoterie(this);
        }
        return this.coterie;
    }


    @NodePopupMethod(menuText="Print vote")
    public void printVote(){
        if (this.hasVoted())
        {
            String s = "voted for %d at timestamp %d (timestamp CS is %d)";
            log(s, this.candidate.node.getID(), this.candidate.ts, this.tsCS);
        } else {
            log("has not voted");
        }
    }


    public void log(String format, Object... args){
        String s = String.format("NODE %d => TS %d => ", this.getID(), this.ts);
        App.instance.log(s + format, args);
    }


    public void checkRequirements(){}
    public void preStep(){}
    public void postStep(){}
    public void neighborhoodChange(){}
}
