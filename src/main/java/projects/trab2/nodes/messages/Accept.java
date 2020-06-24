package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Accept extends Message {

    public final Node node;
    public final int ts;

    public Accept(Node node, int ts)
    {
        this.node = node;
        this.ts = ts;
    }

    public Message clone(){
        return this;
    }

}