package projects.trab1.nodes.nodeImplementations;

import projects.trab1.nodes.messages.Require;


public class Comparator implements java.util.Comparator<Require> {


    public int compare(Require m, Require candidate) {
        int result = Integer.compare(m.ts, candidate.ts);
        return result == 0? Long.compare(m.node.getID(), candidate.node.getID()) : result;
    }
}
