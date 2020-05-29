
package projects.trab1.nodes.messages;

import projects.trab1.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Inquire extends Message {

    public final Node node;
    public final int ts;

    public Inquire(Node node, int ts)
    {
        this.node = node;
        this.ts = ts;
    }

    public Message clone(){
        return this;
    }

}
