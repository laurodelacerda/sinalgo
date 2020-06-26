package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class AcceptAnswer extends Message {

    public final Node sender;
    public boolean answer;

    public AcceptAnswer(Node sender, boolean answer)
    {
        this.sender = sender;
        this.answer = answer;
    }

    public Message clone(){
        return this;
    }

}