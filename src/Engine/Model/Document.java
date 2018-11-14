package Engine.Model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Document {
    private String docNo;
    private String parentFilePath;
    private int max_tf  = 0 ; // frequancy of the most common term in doc
    private int unique_t  = 0 ; // quantity of unuque terms in doc
    private String city = "" ; // city of doc - appear in <F P=104> ...</F>
    private HashMap<Term, Integer> termFrequency;
    private Term maxFreqTerm;
    private int maxFreqTermNumber;



    public Document(String docNo, String parentFilePath) {
        this.docNo = docNo;
        this.parentFilePath = parentFilePath;
        termFrequency = new HashMap<>();
    }

    public String getDocNo() {
        return docNo;
    }

    public String getParentFilePath() {
        return parentFilePath;
    }

    public void advanceTermFrequency(Term term){
        if (termFrequency.containsKey(term)){
            int termFreq = termFrequency.get(term) + 1;
            if (termFreq > maxFreqTermNumber) {
                maxFreqTermNumber = termFreq;
                maxFreqTerm = term;
            }
            termFrequency.put(term, termFreq);
        }
        else{
            termFrequency.put(term, 1);
            if (maxFreqTermNumber == 0){
                maxFreqTermNumber = 1;
                maxFreqTerm = term;
            }
        }
    }

}
