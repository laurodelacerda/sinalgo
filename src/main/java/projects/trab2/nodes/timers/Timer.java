
package projects.trab2.nodes.timers;

import projects.trab1.Control;
import projects.trab1.nodes.nodeImplementations.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class Timer extends sinalgo.nodes.timers.Timer {

    private Random rng = new Random();
    private int n = 0;
    private int psc = 20;
    private int limit = 1000000;


    public void restart() {
        this.n = 0;
        this.start();
    }

    public void start() {
        this.startGlobalTimer(1);
    }

    @Override
    public void fire() {
        List<Node> nodes = new ArrayList<>(Control.instance.nodes);
        Collections.shuffle(nodes);

        for (Node node : nodes) {

            if (node.wait_time == 0) {
                if (this.rng.nextInt(100) < this.psc)
                    node.requestCS();
                else
                    node.wait_time = 10;
            }
            else
                node.wait_time -= 1;
        }

        if (this.n++ < this.limit)
            this.start();

    }
}
