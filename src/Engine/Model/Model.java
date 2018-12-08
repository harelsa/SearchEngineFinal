package Engine.Model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.IOException;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

public class Model extends Observable {
    private String corpusPath;
    private String postingPath ;
    private boolean is_stemming ;
    public String [] list_lang ;
    HashMap < String , String[] > termDictionary = new HashMap<>();

    public void run(String corpusPath, String postingPath, boolean stemming) {
        long startTime = System.currentTimeMillis();
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
        this.is_stemming = stemming ;
        TextOperationsManager textOperationsManager = new TextOperationsManager(corpusPath, postingPath, stemming);
        textOperationsManager.StartTextOperations();
        textOperationsManager.BuildCitiesPosting();
        int uniqueTerms = Indexer.terms_dictionary.size();
        int docsGenerate = Indexer.docs_dictionary.size();
        Indexer.writeDictionariesToDisc();
        long estimatedTime = System.currentTimeMillis() - startTime;
        String summery = getSummary(estimatedTime, uniqueTerms, docsGenerate);
        list_lang = textOperationsManager.getDocLang();
        setChanged();
        notifyObservers("finished");
        JOptionPane.showMessageDialog(null, summery, "Build Info", JOptionPane.INFORMATION_MESSAGE);

    }

    private String getSummary(long estimatedTime, int uniqueTerms, int docsGenerate) {
        long runTime = TimeUnit.MILLISECONDS.toSeconds(estimatedTime);
        String ans = uniqueTerms + " Unique Terms" + "\n";
        ans += docsGenerate + " Docs Indexed" + "\n";
        ans += "Total Runtime: " + runTime + "\n";
        return ans;
    }


    public void showDic() {
    }

    public void loadDicToMemory(String stemming) {
        File dir = new File(postingPath + "\\Postings"+ ifStemming() );
        if ( dir!= null && dir.exists()){
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br_dic = new BufferedReader(new FileReader(postingPath + "\\Postings" + ifStemming()+ "\\termDictionary.txt"));
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
            JOptionPane.showMessageDialog(null, "Posting Directory does not Exists", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean resetAll() {
        File dir = new File(postingPath + "\\Postings" + ifStemming());
        System.out.println("Deletes :" + postingPath + "\\Postings"+ifStemming());
        if (dir.exists()) {

            try {
                return deleteDirectory(dir);
            } catch (Exception e) {
            }
        }else {

            JOptionPane.showMessageDialog(null, "Posting Directory does not Exists", "Error", JOptionPane.ERROR_MESSAGE);
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
        this.is_stemming = stemming ;
    }

    private String ifStemming() {
        if (is_stemming)
            return "withStemming";
        return "";
    }
}
