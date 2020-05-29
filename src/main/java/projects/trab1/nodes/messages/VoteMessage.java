
package projects.trab1.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Vote Messages that are sent by the SimpleNodes in the t2 project.
 */
@Getter
@Setter
//@AllArgsConstructor
public class VoteMessage extends Message {

    /**
     * The id of the Sender.
     */
    private long id;

//    private int si;

    private long ts;

    /**
     * Vote for yes.
     */
    private boolean yes;


    public VoteMessage(long id, long timestamp, boolean yes)
    {
        this.id = id;
//        this.si = si;
        this.ts = timestamp;
        this.yes = yes;
    }

    @Override
    public Message clone() {
        return new VoteMessage(this.id, this.ts, this.yes);
    }

}
