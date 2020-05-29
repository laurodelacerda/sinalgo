
package projects.trab1.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Hello Message that are sent by the SimpleNodes in the t2 project.
 */
@Getter
@Setter
//@AllArgsConstructor
public class HelloMessage extends Message {

    /**
     * The id of the Sender.
     */
    private long id;

    private long ts;


    public HelloMessage(long id)
    {
        this.id = id;
    }

    @Override
    public Message clone() {
        return new HelloMessage(this.id);
    }

}
