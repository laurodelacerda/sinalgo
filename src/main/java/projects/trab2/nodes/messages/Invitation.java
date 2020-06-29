package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Invitation extends Message {

    public final Node sender;
    public final Node coord;
    public int group_id;

    public Invitation(Node sender, Node coord, int group_id)
    {
        this.sender = sender;
        this.coord = coord;
        this.group_id = group_id;
    }

    public Message clone(){
        return this;
    }

}