package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class AYCoordAnswer extends Message {

    public final Node sender;
    public boolean answer;

    public AYCoordAnswer(Node sender, boolean answer)
    {
        this.sender = sender;
        this.answer = answer;
    }

    public Message clone(){
        return this;
    }

}