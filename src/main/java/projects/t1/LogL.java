
package projects.t1;

import lombok.Getter;
import lombok.Setter;

/**
 * Enumerates the log-levels. Levels above THRESHOLD will be included in the
 * log-file. The levels below (with a higher enumeration value) not.
 */
public class LogL extends sinalgo.tools.logging.LogL {

    /**
     * An additional loglevel to inform about loggings for the sending process of
     * sending.
     */
    @Getter
    @Setter
    private static boolean SEND = true;

}
