package Engine.Model;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.util.Observable;

public class Model extends Observable {
    String corpusPath;
    String postingPath ;
    public String [] list_lang ;

    public void run (String corpusPath , String postingPath , boolean stemming ){
        this.corpusPath = corpusPath ;
        this.postingPath = postingPath ;
        System.out.println("posting: " + postingPath);
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

    public void resetAll() {
        File dir = new File( postingPath+"\\Postings") ;
        System.out.println("Deletes :"+ postingPath+"\\Postings");
        if (dir.exists()){

            try {
                System.out.println( deleteDirectory(dir)) ;
                //FileUtils.deleteDirectory(dir);
//                FileUtils.cleanDirectory(dir);
//                FileUtils.forceDelete(dir);

            } catch ( Exception e ){} ;
            //dir.delete();
        }

    }
    /**
     * Force deletion of directory
     * @param path
     * @return
     */
    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
