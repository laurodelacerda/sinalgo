package projects.trab2.nodes.messages;

import projects.trab2.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class ReadyAnswer extends Message {

    public final Node sender;
    public boolean accept;
    public int group_id = -1;


    public ReadyAnswer(Node sender, boolean accept, int group_id)
    {
        this.sender = sender;
        this.accept = accept;
        this.group_id = group_id;
    }

    public ReadyAnswer(Node sender, boolean accept)
    {
        this(sender, accept, -1);
    }


    public Message clone(){
        return this;
    }

}