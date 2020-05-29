package projects.trab1.nodes.nodeImplementations;

import projects.trab1.nodes.messages.Require;


public class Comparator implements java.util.Comparator<Require> {


    public int compare(Require m, Require candidate) {
        return (m.ts - candidate.ts);
    }
}
