package projects.trab1;

import projects.trab1.nodes.nodeImplementations.Node;
import projects.trab1.nodes.timers.Timer;
import sinalgo.runtime.Global;
import sinalgo.tools.logging.Logging;

import java.util.*;

//import sinalgo.configuration.Configuration;

public final class Control {

    private Logging logging = Global.getLog();
    private boolean logActive = true;

    public Timer timer;

    public static Control instance = null;
    private Vector<Vector<Integer>> districts;
    public List<Node> nodes = null ;
    public int[] enterCS;
    
    public int relinquish = 0;
    public int inquire = 0;
    public int tie_breaks = 0;
    public int n;
    public int k;

    public static Control start() {
        if (instance == null)
            instance = new Control();
        return instance;
    }

    public Control() {

        // Ler quantidade de coteries, k e numero de nós

        for (int i = 0; i < this.n; i++)
            this.enterCS[i] = 0;

        // Tamanho de cada coterie
        this.n = 4;
        this.k = (int) (2* (Math.sqrt(this.n))) - 1;
        this.enterCS = new int[this.n];

        this.timer = new Timer();
        this.nodes = new ArrayList<>();
        this.districts = new Vector<>();

        System.out.println("Nodes: " + this.n);
        System.out.println("k: " + this.k);

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

            this.districts.add(district);

        }

    }

    public void init() {

        this.timer.start();
        this.timer.fire();

    }

    public void printDistricts() {
        System.out.println("\n### DISTRICTS ###");

        for (Node n : this.nodes)
            this.printDistrict(n);
    }

    public void printDistrict(Node node) {
        int i = (int) node.getID() - 1;
        System.out.print("\nD" + (i + 1) + ": ");
        for (int j = 0; j < this.districts.get(i).size(); j++)
            System.out.print(" " + (this.districts.get(i).get(j) + 1));
    }

    public List<Node> getDistrict(Node node) {
        int node_id = (int) node.getID() - 1 ;

        List<Node> nodes = new ArrayList<>();
        if (this.districts.get(node_id) != null)
        {
            for (Integer i : this.districts.get(node_id))
            {
                for (Node n : this.nodes)
                {
                    if ((int) n.getID() == Integer.valueOf(i) + 1)
                        nodes.add(n);
                }
            }
        }

        return nodes;
    }

    public void printLastVotes(){
        System.out.println("\n### LAST VOTES ###\n");

        boolean temp = this.logActive;
        for (Node node: this.nodes){
            node.printLastVote();
        }
        this.logActive = temp;
    }

    public void printStats(){
        System.out.println("\n### STATISTICS ###\n");
        System.out.println("Tie breaks:\t" + this.tie_breaks);
        System.out.println("Relinquish:\t" + this.relinquish);
        System.out.println("Inquire:\t" + this.inquire);

        for (int i = 0; i < this.n; i++)
            System.out.println("NODE " + (i + 1) + " accessed CS " + this.enterCS[i] + " times");
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
        for (Node n : this.nodes)
            n.value_to_inspect = 0;
    }

    public void showCS() {
        for (Node n : this.nodes)
            n.value_to_inspect = 1;
    }

    public void showInquires() {
        for (Node n : this.nodes)
            n.value_to_inspect = 2;
    }

    public void showRelinquishes() {
        for (Node n : this.nodes)
            n.value_to_inspect = 3;
    }

}
