
package projects.trab1.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Release Messages that are sent by the SimpleNodes in the t2 project.
 */
@Getter
@Setter
//@AllArgsConstructor
public class ReleaseMessage extends Message {

    /**
     * The id of the Sender.
     */
    private long id;

    private int si;

    public ReleaseMessage(long id, int si)
    {
        this.id = id;
        this.si = si;
    }


    @Override
    public Message clone() {
        return new ReleaseMessage(this.id, this.si);
    }

}
