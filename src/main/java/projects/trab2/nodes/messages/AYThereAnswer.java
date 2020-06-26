package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class AYThereAnswer extends Message {

    public final Node node;
    public final boolean answer;

    public AYThereAnswer(Node node, boolean answer)
    {
        this.node = node;
        this.answer = answer;
    }

    public Message clone(){
        return this;
    }

}