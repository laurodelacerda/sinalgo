
package projects.trab2;

import sinalgo.runtime.AbstractCustomGlobal;

public class CustomGlobal extends AbstractCustomGlobal {

    public void onExit(){
        Control.instance.printStats();
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

    @CustomButton(buttonText = "Show Coordinator", toolTipText = "")
    public void showCoordinator(){
        Control.instance.showCoordinator();
    }

    @Override
    public void preRun(){
//        Global.setAsynchronousMode(false);
        Control.start();
        Control.instance.init();
    }

    @Override
    public void postRound(){}

    public boolean hasTerminated(){
        return false;
    }
}
