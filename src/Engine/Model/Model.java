package Engine.Model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.IOException;
import java.util.Observable;

public class Model extends Observable {
    String corpusPath;
    String postingPath ;
    public String [] list_lang ;
    //String postingPath = "C:\\Users\\Nadav\\Desktop\\Engine Project\\resources";
    HashMap < String , String[] > termDictionary = new HashMap<>();

    public void run(String corpusPath, String postingPath, boolean stemming) {
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
        System.out.println("posting: " + postingPath);
        TextOperationsManager textOperationsManager = new TextOperationsManager(corpusPath, postingPath, stemming);

        String[] buildInfo = textOperationsManager.StartTextOperations(); // will start build & return info : num of doc , num of terms , runtime
        textOperationsManager.BuildCitiesPosting();

        list_lang = textOperationsManager.getDocLang();
        setChanged();
        notifyObservers("finished");
        if (buildInfo != null)
            JOptionPane.showMessageDialog(null, buildInfo.toString(), "Build Info", JOptionPane.INFORMATION_MESSAGE);

    }

    public void showDic() {
    }

    public void loadDicToMemory(String stemming) {
        File dir = new File(postingPath + "\\Postings"+ stemming) ;
        if ( dir!= null && dir.exists()){
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br_dic = new BufferedReader(new FileReader(postingPath + "\\Postings" + stemming + "\\termDictionary.txt"));
                String line = "" ;
                while ((line = br_dic.readLine()) != null){

                    String term = "";
                    String tf = "";
                    String[] splited = StringUtils.split(line,"<D>");
                    String[] termSplited = StringUtils.split(splited[1], ",");
                    term = splited[0];
                    termDictionary.put ( term , termSplited) ;
                }
            }
            catch (Exception e ){}

            String line = null;

            JOptionPane.showMessageDialog(null, "Directoy Loaded to Memory", "Load", JOptionPane.INFORMATION_MESSAGE);

        }
        else {
            JOptionPane.showMessageDialog(null, "Directoy not Exists", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean resetAll() {
        File dir = new File(postingPath + "\\Postings");
        System.out.println("Deletes :" + postingPath + "\\Postings");
        if (dir.exists()) {

            try {
                return deleteDirectory(dir);
            } catch (Exception e) {
            }
        }
        return false;
    }

    /**
     * Force deletion of directory
     *
     * @param path
     * @return
     */
    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            try {
                Indexer.closeIO();
                Posting.closeIO();
                FileUtils.deleteDirectory(path);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public void pathUpdate ( String corpusPath , String postingPath , boolean stemming ) {
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
    }
}
