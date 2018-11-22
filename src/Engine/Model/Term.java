package Engine.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Term implements Comparable, Serializable {
    private int df = 0; // the number of docs the term is appearing
    private int tf = 0; // the number of
    private String content;
    private HashMap<Document, LinkedList<Integer>> docs;// < doc obj , a list of locations(row num)
    // in doc (tr - number tomes the term appear in the doc , the length of the list ) >

    public Term(int df, int tf, String content) {
        this.df = df;
        this.tf = tf;
        this.content = content;
        docs = new HashMap<>();
    }

    public String getContent() {
        return content;
    }

    public void addDoc(Document doc ) { // check if we need to save row
        //check if the doc exists and add accordinaly
        // if not df +1
        if (docs.containsKey(doc)) {
            docs.get(doc).add(0) ; // need to add location in doc

        } else {
            df++;
            tf++ ;
            docs.put(doc, new LinkedList<Integer>());

        }
    }

    @Override
    public String toString() {
        return "Term{" +
                "df=" + df +
                ", tf=" + tf +
                ", content='" + content + '\'' +
                ", docs=" + docs +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return content.compareTo(((Term) (o)).content);
    }
}

