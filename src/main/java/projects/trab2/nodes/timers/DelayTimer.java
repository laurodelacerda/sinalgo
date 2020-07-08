package projects.trab2.nodes.timers;

import lombok.Getter;
import lombok.Setter;
import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.timers.Timer;

@Getter
@Setter
public class DelayTimer extends Timer {

    public enum Timers {AYCOORD, AYTHERE, MERGE, READY, ACCEPT, PRIORITY};

    private int timeout;
    private Node node;
    private boolean enabled = false;
    private boolean msg_arrived = false;
    private Timers timer;
    public void deactivate() {
        this.setEnabled(false);
    }

    public void activate() {
        if (!this.enabled)
            this.setEnabled(true);
            this.startRelative(this.timeout, this.node);
    }


    public DelayTimer(Node node, Timers t, int timeout) {
        this.setNode(node);
        this.setTimer(t);
        this.setTimeout(timeout);
    }

    @Override
    public void fire() {

        if (this.isEnabled())
            this.node.handleTimers(this.timer);

    }

}
