
package projects.trab1.nodes.messages;

import sinalgo.nodes.messages.Message;
import projects.trab1.nodes.nodeImplementations.Node;

public class Require extends Message {

    public final Node node;
    public final int ts;

    public Require(Node node, int ts)
    {
        this.node = node;
        this.ts = ts;
    }

    public Message clone(){
        return this;
    }

}
