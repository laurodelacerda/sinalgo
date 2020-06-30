package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class AYThere extends Message {

    public final Node sender;
    public final int group_id;

    public AYThere(Node node, int group_id)
    {
        this.sender = node;
        this.group_id = group_id;
    }

    public Message clone(){
        return this;
    }

}