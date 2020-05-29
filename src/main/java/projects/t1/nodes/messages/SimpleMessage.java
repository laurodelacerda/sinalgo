
package projects.t1.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.Position;

/**
 * The Messages that are sent by the SimpleNodes in the t1 projects. They
 * contain one int as payload.
 */
@Getter
@Setter
//@AllArgsConstructor
public class SimpleMessage extends Message {

    private long     id;
    private Position pos;
    private boolean  waiting;

    public SimpleMessage(long id, Position pos, boolean waiting)
    {
        this.id = id;
        this.pos = pos;
        this.waiting = waiting;
    }

    public Position getPosition() {
        return this.pos;
    }

    public long getId(){ return this.id; }

    public boolean isWaiting(){ return this.waiting; }

    @Override
    public Message clone() {
        return new SimpleMessage(this.id, this.pos, this.waiting);
    }

}
