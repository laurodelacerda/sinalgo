package projects.trab2;

import projects.trab2.nodes.nodeImplementations.Node;
import projects.trab2.nodes.timers.Timer;
import sinalgo.runtime.Global;
import sinalgo.tools.logging.Logging;

import java.util.*;

// Class stores all statistics from simulation
public final class Control {

    private Logging logging = Global.getLog();
    private boolean logActive = true;

    public Timer timer;

    public static Control instance = null;

    public List<Node> nodes = null ;


    public static Control start() {
        if (instance == null)
            instance = new Control();
        return instance;
    }

    public Control() {

        this.timer = new Timer();
        this.nodes = new ArrayList<Node>();

    }


    public void init() {

        this.timer.fire();

    }


    public void printStats(){
        System.out.println("\n### STATISTICS ###\n");
        System.out.println("Coordinators:\t" );

    }

    public void printStatus() {
        for (Node node : this.nodes)
            node.printStatus();
    }

    public void toggleLog(){
        this.logActive = !this.logActive;
    }

    public void log(String format, Object[] args){
        if (this.logActive)
            this.logging.logln(String.format(format, args));
    };

    public void showID() {
//        for (Node n : this.nodes)
    }

}
