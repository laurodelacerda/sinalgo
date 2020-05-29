
package projects.trab1.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Inquirement Messages that are sent by the SimpleNodes in the t2 project.
 */
@Getter
@Setter
//@AllArgsConstructor
public class InqMessage extends Message {

    /**
     * The id of the Sender.
     */
    private long id;

    // TODO Inserir timestamp
    private int si;

    private long ts;

    public InqMessage(long id, int si, long timestamp)
    {
        this.id = id;
        this.si = si;
        this.ts = timestamp;
    }

    @Override
    public Message clone() {
        return new InqMessage(this.id, this.si, this.ts);
    }

}
