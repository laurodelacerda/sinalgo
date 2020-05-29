
package projects.trab1.nodes.timers;

import projects.trab1.App;
import projects.trab1.nodes.nodeImplementations.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class Timer extends sinalgo.nodes.timers.Timer {

    private int chance = 20;
    private double interval = 1;
    private int limit = 100000;

    private int n = 0;

    public void restart() {
        this.n = 0;
        this.start();
    }

    public void start() {
        this.startGlobalTimer(this.interval);
    }

    @Override
    public void fire() {
        List<Node> nodes = new ArrayList<>(App.instance.nodes);
        Collections.shuffle(nodes);
        Random rng = new Random();
        for (Node node : nodes) {
            if (rng.nextInt(100) < this.chance) {
                node.tryToEnterTheCS();
            }
        }
        if (this.n++ < this.limit) {
            this.start();
        }
    }
}
