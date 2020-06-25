package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class AYThereAnswer extends Message {

    public final Node node;
    public final int ts;

    public AYThereAnswer(Node node, int ts)
    {
        this.node = node;
        this.ts = ts;
    }

    public Message clone(){
        return this;
    }

}