package projects.trab2.nodes.timers;

import lombok.Getter;
import lombok.Setter;
import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

@Getter
@Setter
public class DelayTimer extends Timer {

    private int timeout;
    private Node node;
    private Message msg;
    private boolean enabled = true;

    public void disable() {
        this.setEnabled(false);
    }

    public DelayTimer(Node node, Message msg, int timeout) {
        this.setNode(node);
        this.setMsg(msg);
        this.setTimeout(timeout);
    }

    @Override
    public void fire() {
        if (this.isEnabled()) {
            this.startRelative(this.timeout, this.node);
            this.node.time_fired();
        }
    }

}
