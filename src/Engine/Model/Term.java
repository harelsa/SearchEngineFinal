package Engine.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Term implements Comparable, Serializable {
    private int df = 0; // the number of docs the term is appearing
    private int tf = 0; // length of location docs
    private String content;
    private HashMap<Document, LinkedList<Integer>> location_docs;// < doc obj , a list of locations(row num)
    private HashMap<Document, Integer > tf_docs ;
    // in doc (tr - number tomes the term appear in the doc , the length of the list ) >

    public Term(int df, int tf, String content) {
        this.df = df;
        this.tf = tf;
        this.content = content;
        location_docs = new HashMap<>();
        tf_docs = new HashMap<>();
    }

    public String getContent() {
        return content;
    }

    public void addDoc(Document doc ) { // check if we need to save row
        //check if the doc exists and add accordinaly
        // if not df +1
        if (location_docs.containsKey(doc)) {
           location_docs.get(doc).add(0) ; // need to add location in doc
            tf_docs.put(doc, tf_docs.get(doc) + 1);
        } else { // create new doc
            df++;
            location_docs.put(doc, new LinkedList<Integer>());
            tf_docs.put ( doc , 1) ;
        }
    }

    @Override
    public String toString() {
        return "Term{" +
                "df=" + df +
                ", tf=" + tf +
                ", content='" + content + '\'' +
                ", docs=" + location_docs +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return content.compareTo(((Term) (o)).content);
    }
}

