
package projects.trab2.nodes.timers;

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
        this.start();
    }
}
