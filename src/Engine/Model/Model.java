package Engine.Model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.TimeUnit;
/**
 * Part of MVC Design pattern  , get called from controller after View events
 */
public class Model extends Observable {
    private String corpusPath; //saved corpus path
    private String postingPath ;// saved outpot posting path
    private boolean is_stemming ; // using a stemmer on terms ot not
    public String [] list_lang ; //list of lang returns from parsing the docs
    // will allow to load the term dic to prog memory -
    // will be used in project part 2
    HashMap < String , String[] > termDictionary = new HashMap<>();

    /**
     * run corpus processing manager , get back info from posting process and
     * display it at the end ,
     * get lang lind and set it in gui
     * @param corpusPath
     * @param postingPath
     * @param stemming
     */
    public void run(String corpusPath, String postingPath, boolean stemming) {
        long startTime = System.currentTimeMillis();
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
        this.is_stemming = stemming ;
        TextOperationsManager corpusProcessingManager = new TextOperationsManager(corpusPath, postingPath, stemming);
        corpusProcessingManager.StartTextOperations();
        int uniqueTerms = Indexer.terms_dictionary.size();
        int docsGenerate = Indexer.docs_dictionary.size();
        Indexer.writeDictionariesToDisc();
        long estimatedTime = System.currentTimeMillis() - startTime;
        String summery = getSummary(estimatedTime, uniqueTerms, docsGenerate);
        list_lang = corpusProcessingManager.getDocLang();
        setChanged();
        notifyObservers("finished");
        JOptionPane.showMessageDialog(null, summery, "Build Info", JOptionPane.INFORMATION_MESSAGE);

    }

    /**
     * help to present a summary of run info in the end of posting
     * @param estimatedTime - runtime
     * @param uniqueTerms - num of unique terms in the corpus
     * @param docsGenerate - how many docs in the corpus
     * @return
     */
    private String getSummary(long estimatedTime, int uniqueTerms, int docsGenerate) {
        long runTime = TimeUnit.MILLISECONDS.toSeconds(estimatedTime);
        String ans = uniqueTerms + " Unique Terms" + "\n";
        ans += docsGenerate + " Docs Indexed" + "\n";
        ans += "Total Runtime: " + runTime + "\n";
        return ans;
    }


    public void showDic() {
    }

    /**
     * load the dic file from disk to memory - insert to termDictionary
     * @param stemming
     */
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

    /**
     * delete all files & folder created after posting process
     * @return
     */
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
