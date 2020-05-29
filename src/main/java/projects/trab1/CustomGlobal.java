
package projects.trab1;

import sinalgo.runtime.AbstractCustomGlobal;

public class CustomGlobal extends AbstractCustomGlobal {

    public void onExit(){
        App.instance.printStats();
    }

    @CustomButton(buttonText = "Print Coteries", toolTipText = "Print all coteries.")
    public void printCoteries(){
        App.instance.printCoteries();
    }

    @CustomButton(buttonText = "Print Votes", toolTipText = "")
    public void printVotes(){
        App.instance.printVotes();
    }

    @CustomButton(buttonText = "Print Stats", toolTipText = "")
    public void printStats(){
        App.instance.printStats();
    }

    @CustomButton(buttonText = "Toggle Log", toolTipText = "")
    public void toggleLog(){
        App.instance.toggleLog();
    }

    @CustomButton(buttonText = "Restart Timer", toolTipText = "")
    public void restartTimer(){
        App.instance.restartTimer();
    }

    public void preRun(){
        App.start();
        App.instance.init();
    }

    public boolean hasTerminated(){
        return false;
    }
}
