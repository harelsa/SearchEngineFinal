package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;


public class SegmentFile implements Serializable {

    String path ;
    private boolean STEMMING = false ; // tells if to do stemmig - will be changed from gui

    public SegmentFile(String path , boolean stemming) {
        STEMMING = stemming ;
        this.path = path + "\\Postings" + CorpusProcessingManager.ifStemming(stemming) ;

//        }
    }

    public void signToSpecificPartition(HashMap<String, String> filesTerms, TreeMap<String, String> OnlyTerms, int CHUNK) {
        if (OnlyTerms.size() == 0)
            return;

        SegmentFilePartition sfp = new SegmentFilePartition(path , CHUNK) ;

        Iterator it = OnlyTerms.keySet().iterator();
        //signNewDocSection(currDoc);
        while (it.hasNext()) {
            //String key = (String)it.next();
            String term = (String) it.next() ;
            String  value = filesTerms.get(term );
            if ( STEMMING ) {
                Stemmer stemmer = new Stemmer() ;
                stemmer.add(term.toCharArray(), term.length());
                String stemmed = "";
                stemmer.stem();
                term = stemmer.toString();

            }
            sfp.signNewTerm(term  , value);
        }
        sfp.flushFile();
        sfp.closeBuffers();


    }








}
