
package projects.trab1.nodes.messages;

import projects.trab1.nodes.nodeImplementations.Node;
import sinalgo.nodes.messages.Message;

public class Release extends Message {

    public Node node ;
    public int ts ;

    public Release(Node node, int ts)
    {
        this.node = node;
        this.ts = ts;
    }

    public Release(){

    }

    public Message clone(){
        return this;
    }

}
