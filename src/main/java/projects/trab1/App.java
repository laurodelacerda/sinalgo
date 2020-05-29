package projects.trab1;

import projects.trab1.nodes.nodeImplementations.Node;
import projects.trab1.nodes.timers.Timer;
import sinalgo.runtime.Global;
import sinalgo.tools.logging.Logging;

import java.util.*;

//import sinalgo.configuration.Configuration;

public final class App {

    Timer timer;

    private Logging logging = Global.getLog();

    private boolean shouldLog = true;

    public static App instance = null;

    public List<Node> nodes = null ;

    public int relinquish = 0;

    public int inquire = 0;

    public int n;

    public int k;

    public int[] enterCS;

    private Vector<Vector<Integer>> coteries;

    public static App start() {
        if (instance == null)
            instance = new App();
        return instance;
    }

    public App() {

        // Ler quantidade de coteries, k e numero de nós

        for (int i = 0; i < this.n; i++)
            this.enterCS[i] = 0;

        // Tamanho de cada coterie
        this.n = 4;
        this.k = (int) (2* (Math.sqrt(this.n))) - 1;
        this.enterCS = new int[this.n];

        timer = new Timer();
        nodes = new ArrayList<>();
        coteries = new Vector<>();

        this.startMatrix();
    }

    private void startMatrix() {
        Vector<Vector<Integer>> matrix = new Vector<Vector<Integer>>();

        int side = (int) Math.sqrt(this.n);

        // initializing matrix
        for (int i = 0; i < side; i++)
        {
            Vector<Integer> col = new Vector<Integer>();
            for (int j = 0; j < side; j++){
                col.add(side*i + j);
            }
            matrix.add(col);
        }

        for (int i = 0; i < this.n; i++)
        {
            int l = i / side;
            int c = i % side;

            // add line
            Vector<Integer> lin = matrix.get(l);

            // add column
            Vector<Integer> col = new Vector<Integer>();
            for (Vector<Integer> line : matrix)
                col.add(line.get(c));

            // remove repeated items
            Set<Integer> set_lin = new LinkedHashSet<Integer>(lin);
            Set<Integer> set_col = new LinkedHashSet<Integer>(col);
            set_col.removeAll(set_lin);

            // save coterie
            Vector<Integer> district = new Vector<Integer>();
            district.addAll(lin);
            district.addAll(set_col);

            this.coteries.add(district);

        }

//        this.printCoteries();

    }

    public void init() {

//        timer.fire();

        //        App app = App.instance;
//        app.startMatrix();
        timer.start();
        timer.fire();

//        try {
//            this.n = Configuration.getIntegerParameter("Node/nodes");
//            this.k = (int) Math.sqrt(this.n);
//            App.start();
//            timer.fire();
//        }
//        catch (CorruptConfigurationEntryException e) {
//            throw new SinalgoFatalException(e.getMessage());
//        }

    }

    public void printCoteries() {
        System.out.println("--------");
        System.out.println("COTERIES");
        System.out.println("--------");

        for (int i = 0; i < this.n; i++) {
            System.out.print("C" + (i + 1) + " => ");
            for (int j = 0; j < this.coteries.get(i).size(); j++)
                System.out.print(" " + (this.coteries.get(i).get(j) + 1));
            System.out.println();
        }

    }

    public void printCoterie(Node node) {
        int i = (int) node.getID() - 1;
        System.out.print("C" + (i + 1) + " =>");
        for (int j = 0; j < this.coteries.get(i).size(); j++)
            System.out.print(" " + (this.coteries.get(i).get(j) + 1));
        System.out.println();
    }

    public List<Node> getCoterie(Node node) {
        int node_id = (int) node.getID() - 1 ;

        List<Node> nodes = new ArrayList<>();
        if (this.coteries.get(node_id) != null)
        {
            for (Integer i : this.coteries.get(node_id))
            {
                for (Node n : this.nodes)
                {
                    if ((int) n.getID() == Integer.valueOf(i) + 1)
                        nodes.add(n);
                }
            }
        }

        return nodes;
//        return this.coteries.get((int) node.getID() - 1);
    }

    public void printVotes(){
        System.out.println("-----");
        System.out.println("VOTES");
        System.out.println("-----");
        boolean temp = this.shouldLog;
        for (Node node: this.nodes){
            node.printVote();
        }
        this.shouldLog = temp;
    }

    public void printStats(){
        System.out.println("-----");
        System.out.println("STATS");
        System.out.println("-----");
        System.out.println("Relinquish:\t" + this.relinquish);
        System.out.println("Inquire:\t" + this.inquire);

        for (int i = 0; i < this.n; i++) {
            System.out.print("NODE " + (i + 1));
            System.out.println(" entered the CS " + this.enterCS[i] + " times");
        }
    }

    public void toggleLog(){
        this.shouldLog = !this.shouldLog;
    }

    public void log(String format, Object[] args){
        if (this.shouldLog)
            this.logging.logln(String.format(format, args));
    };

    public void restartTimer() {
        timer.restart();
    }

}
