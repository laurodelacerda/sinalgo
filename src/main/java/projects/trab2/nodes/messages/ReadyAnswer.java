package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class ReadyAnswer extends Message {

    public final Node node;

    public ReadyAnswer(Node node)
    {
        this.node = node;
    }

    public Message clone(){
        return this;
    }

}