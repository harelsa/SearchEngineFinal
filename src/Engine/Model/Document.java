package Engine.Model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Document implements Serializable {
    private String docNo;
    private String parentFilePath;
    private int max_tf  = 0 ; // frequancy of the most common term in doc
    private int unique_t  = 0 ; // quantity of unique terms in doc
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

    @Override
    public String toString() {
        return "Document{" +
                "docNo='" + docNo + '\'' +
                ", parentFileName='" + parentFilePath + '\'' +
                ", max_tf=" + max_tf +
                ", unique_t=" + unique_t +
                ", city='" + city + '\'' +
                ", termFrequency=" + termFrequency +
                ", maxFreqTerm=" + maxFreqTerm +
                ", maxFreqTermNumber=" + maxFreqTermNumber +
                '}';
    }
    // Format: <docNo>,<max_tf>,<unique_t>,<city>,<maxFreqTerm>,<maxFreqTermNumber>
    public String shortToString(){
        return docNo+","+max_tf+","+unique_t+","+city+","+maxFreqTermNumber; //+maxFreqTerm.shortToString()+","
    }

    //private byte[]
}
