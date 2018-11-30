package Engine.Model;

import java.io.Serializable;
import java.util.HashMap;

public class Document {
    private String docNo;
    private String parentFileName;
    private int unique_t; // quantity of unique terms in doc
    private String city = "" ; // city of doc - appear in <F P=104> ...</F>
    private HashMap<Term, Integer> termFrequency;
    private Term maxFreqTerm;
    private int maxFreqTermNumber;  // frequancy of the most common term in doc



    public Document(String docNo, String parentFileName, String docCity) {
        this.docNo = docNo;
        city = docCity;
        this.parentFileName = parentFileName;
        termFrequency = new HashMap<>();
    }

    public String getDocNo() {
        return docNo;
    }

    public String getParentFileName() {
        return parentFileName;
    }

    public void addTerm(Term term){
        if (maxFreqTermNumber == 0){
            maxFreqTerm = term;
            maxFreqTermNumber++;
        }

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
                ", parentFileName='" + parentFileName + '\'' +
                ", max_tf=" + maxFreqTermNumber +
                ", unique_t=" + unique_t +
                ", city='" + city + '\'' +
                //", termFrequency=" + termFrequency +
                ", maxFreqTerm=" + maxFreqTerm +
                ", maxFreqTermNumber=" + maxFreqTermNumber +
                '}';
    }
    // Format: <docNo>,<max_tf>,<unique_t>,<city>,<maxFreqTerm>,<maxFreqTermNumber>
    public String shortToString(){
        return docNo+","+maxFreqTermNumber+","+unique_t+","+city+","+maxFreqTermNumber; //+maxFreqTerm.shortToString()+","
    }

    public void updateAfterParsing() {
        unique_t = termFrequency.size();
    }


    //private byte[]
}
