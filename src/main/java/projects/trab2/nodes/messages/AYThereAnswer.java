package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class AYThereAnswer extends Message {

    public final Node sender;
    public final boolean answer;

    public AYThereAnswer(Node node, boolean answer)
    {
        this.sender = node;
        this.answer = answer;
    }

    public Message clone(){
        return this;
    }

}