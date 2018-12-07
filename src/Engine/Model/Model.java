package Engine.Model;

import javax.swing.*;
import java.util.Observable;

public class Model extends Observable {
    public String [] list_lang ;

    public void run (String corpusPath , String postingPath , boolean stemming ){
        TextOperationsManager textOperationsManager = new TextOperationsManager(corpusPath , postingPath , stemming);

        String[] buildInfo  =textOperationsManager.StartTextOperations(); // will start build & return info : num of doc , num of terms , runtime
        textOperationsManager.BuildCitiesPosting();
         list_lang = textOperationsManager.getDocLang() ;
        setChanged();
        notifyObservers("finished");
        if ( buildInfo != null)
            JOptionPane.showMessageDialog(null, buildInfo.toString(), "Build Info", JOptionPane.INFORMATION_MESSAGE);

    }

    public void showDic() {
    }

    public void loadDicToMemory() {
    }
}
