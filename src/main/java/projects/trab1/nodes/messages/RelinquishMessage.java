
package projects.trab1.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Relinquish Messages that are sent by the SimpleNodes in the t2 project.
 */
@Getter
@Setter
//@AllArgsConstructor
public class RelinquishMessage extends Message {

    /**
     * The id of the Sender.
     */
    private long id;

    public RelinquishMessage(long id)
    {
        this.id = id;
    }


    @Override
    public Message clone() {
        return new RelinquishMessage(this.id);
    }

}
