package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Accept extends Message {

    public final Node node;
    public int group_id;

    public Accept(Node node, int group_id)
    {
        this.node = node;
        this.group_id = group_id;
    }

    public Message clone(){
        return this;
    }

}