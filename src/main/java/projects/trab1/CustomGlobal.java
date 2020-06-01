
package projects.trab1;

import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;

public class CustomGlobal extends AbstractCustomGlobal {

    public void onExit(){
        Control.instance.printStats();
    }

    @CustomButton(buttonText = "Districts", toolTipText = "Print all coteries.")
    public void printCoteries(){
        Control.instance.printDistricts();
    }

    @CustomButton(buttonText = "Votes", toolTipText = "")
    public void printVotes(){
        Control.instance.printLastVotes();
    }

    @CustomButton(buttonText = "Stats", toolTipText = "")
    public void printStats(){
        Control.instance.printStats();
    }

    @CustomButton(buttonText = "Status", toolTipText = "")
    public void printStatus() { Control.instance.printStatus(); }

    @CustomButton(buttonText = "Toggle Logging", toolTipText = "")
    public void toggleLog(){
        Control.instance.toggleLog();
    }

    @CustomButton(buttonText = "Show ID", toolTipText = "")
    public void showID(){
        Control.instance.showID();
    }

    @CustomButton(buttonText = "Show CS", toolTipText = "")
    public void showCS(){
        Control.instance.showCS();
    }

    @CustomButton(buttonText = "Show INQ", toolTipText = "")
    public void showINQ(){
        Control.instance.showInquires();
    }

    @CustomButton(buttonText = "Show REL", toolTipText = "")
    public void showREL(){
        Control.instance.showRelinquishes();
    }

    public void preRun(){
        Global.setAsynchronousMode(false);
        Control.start();
        Control.instance.init();
    }

    public boolean hasTerminated(){
        return false;
    }
}
