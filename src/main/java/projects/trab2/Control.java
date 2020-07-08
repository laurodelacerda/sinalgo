package projects.trab2;

import projects.trab2.nodes.nodeImplementations.Node;
import projects.trab2.nodes.timers.Timer;
import sinalgo.runtime.Global;
import sinalgo.tools.logging.Logging;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Class stores all statistics from simulation
public final class Control {

    private Logging logging = Global.getLog();
    private boolean logActive = true;

    public int counter_merge = 0;
    public int counter_convergence = 0;
    public List<Integer> timer_convergence = null;

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
        this.timer_convergence = new ArrayList<Integer>();

    }


    public void init() {

        this.timer.fire();

    }


    public void printStats(){
        System.out.println("\n### GENERAL STATISTICS ###\n");
//        System.out.println("Coordinators:\t" );
        System.out.println(String.format("Merge Count: %d", this.counter_merge));
        System.out.println(String.format("Convergence Count: %d", this.timer_convergence.size()));

        int sum = 0;
        double avg = 0;
        if (!this.timer_convergence.isEmpty()) {
            for (Integer n : this.timer_convergence)
                sum += n;
            avg = (double) sum / this.timer_convergence.size();
        }


        DecimalFormat df = new DecimalFormat("#.##");
        String formatted = df.format(avg);
        System.out.println(String.format("Convergence Average Time: %s (%d / %d)",
                                                                                formatted,
                                                                                sum,
                                                                                this.timer_convergence.size()));


        System.out.println("\n### SPECIFIC STATISTICS ###\n");
        for (Node n : this.nodes)
            n.printStats();
    }

    public void printStatus() {
        for (Node node : this.nodes)
            node.printStatus();
    }

    public void toggleLog(){
        this.logActive = !this.logActive;
    }

    public void log(String format, Object[] args){
        if (this.logActive) {
            String s = String.format(" [STEP %d]", (int) Global.getCurrentTime());
            this.logging.logln(String.format(format + s, args));
        }
    };

    public void showID() {
        for (Node n : this.nodes)
            n.value_to_inspect = 0;
    }

    public void showCoordinator() {
        for (Node n : this.nodes)
            n.value_to_inspect = 1;
    }
}
