package projects.trab2.nodes.timers;

import lombok.Getter;
import lombok.Setter;
import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.timers.Timer;

@Getter
@Setter
public class DelayTimer extends Timer {

    private int timeout;
    private Node node;
//    private Message msg;
    private boolean enabled = true;
    private boolean msg_arrived = false;

    public void disable() {
        this.setEnabled(false);
    }

    public DelayTimer(Node node, int timeout) {
        this.setNode(node);
//        this.setMsg(msg);
        this.setTimeout(timeout);
    }

    @Override
    public void fire() {

        if (this.isEnabled()) {
            this.node.log(String.format("waited %d steps", this.timeout));
            this.startRelative(this.timeout, this.node);
        }
        else
        {
            if (msg_arrived)
                return;
            else
                this.node.recovery();
        }
    }

}
