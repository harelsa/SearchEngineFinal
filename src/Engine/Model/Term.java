package Engine.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Term {
    private int df = 0 ; // the number of docs the term is appearing
    private int tf = 0 ; // the number of
    private HashMap< Document , LinkedList<Integer>> docs  ;// < doc obj , a list of locations(row num)
    // in doc (tr - number tomes the term appear in the doc , the length of the list ) >

         public  Term( int df , int tf )    {
             this.df = df ;
             this.tf = tf ;
             docs = new HashMap<>() ;
         }

         public void addDoc ( Document doc){ // check if we need to save row
             //check if the doc exists and add accordinaly
             // if not df +1
             if ( docs.containsKey( doc )){
                 //updates the curr doc
             }
             else {
                 df++ ;
                 //tf++ ;
                docs.put(doc, new LinkedList<Integer>() ) ;

             }
         }


}
