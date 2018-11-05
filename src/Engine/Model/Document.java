package Engine.Model;

import java.util.TreeSet;

public class Document {
    private String docNo;
    private String parentFilePath;
    private int max_tf  = 0 ; // frequancy of the most common term in doc
    private int unique_t  = 0 ; // quantity of unuque terms in doc
    private String city = "" ; // city of doc - appear in <F P=104> ...</F>



    public Document(String docNo, String parentFilePath) {
        this.docNo = docNo;
        this.parentFilePath = parentFilePath;
    }


}
