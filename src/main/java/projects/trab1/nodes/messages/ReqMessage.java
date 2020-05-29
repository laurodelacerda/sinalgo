
package projects.trab1.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Requirement Messages that are sent by the SimpleNodes in the t2 project.
 */
@Getter
@Setter
//@AllArgsConstructor
public class ReqMessage extends Message {

    /**
     * The id of the Sender.
     */
    private long id;

    private int si;

    private long ts;

    // TODO Inserir timestamp

    public ReqMessage(long id, int si, long timestamp)
    {
        this.id = id;
        this.si = si;
        this.ts = timestamp;
    }


    @Override
    public Message clone() {
        return new ReqMessage(this.id, this.si, this.ts);
    }

}
