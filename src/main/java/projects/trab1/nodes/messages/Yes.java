
package projects.trab1.nodes.messages;

import projects.trab1.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Yes extends Message {

    public Node node;
    public int ts;

    public Yes(Node node, int ts)
    {
        this.node = node;
        this.ts = ts;
    }

    public Yes(){}

    public Message clone(){
        return this;
    }

}
