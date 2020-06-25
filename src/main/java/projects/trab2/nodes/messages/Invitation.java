package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Invitation extends Message {

    public final Node sender;
    public Node new_coord = null;

    public Invitation(Node sender)
    {
        this.sender = sender;
    }

    public Invitation(Node sender, Node coord)
    {
        this.sender = sender;
        this.new_coord = coord;
    }

    public Message clone(){
        return this;
    }

}