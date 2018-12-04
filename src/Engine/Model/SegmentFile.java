package Engine.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeSet;

public class SegmentFile implements Serializable {
    private SegmentFilePartition[] filePartitions;
    private final int NUM_OF_PARTITATIONS = 7;
    private boolean STEMMING = false ; // tells if to do stemmig - will be changed from gui
    //public static Stemmer stemmer = new Stemmer() ;

    // ('0', '9');
    // ('a', 'c');
    // ('d', 'f');
    // ('g', 'k');
    // ('l', 'p');
    // ('p', 'z');
    // ('z', 'z');
    public SegmentFile(String path) {
        filePartitions = new SegmentFilePartition[7]; // startsWith Digit ,a-f, g-p, q-z, startsWith "
        filePartitions[0] = new SegmentFilePartition(path, '0', '9');
        filePartitions[1] = new SegmentFilePartition(path, 'a', 'c');
        filePartitions[2] = new SegmentFilePartition(path, 'd', 'f');
        filePartitions[3] = new SegmentFilePartition(path, 'g', 'k');
        filePartitions[4] = new SegmentFilePartition(path, 'l', 'p');
        filePartitions[5] = new SegmentFilePartition(path, 'q', 'z');
        filePartitions[6] = new SegmentFilePartition(path, 'z', 'z');
    }

/* Need to do it concurrent (new thread in parser which calls this method) */
    public void signToSpecificPartition(SortedMap<String, Term> allTerms, Document currDoc) {
        if (allTerms.size() == 0)
            return;
        Thread writeToDocumentPosting = new Thread(() -> Posting.writeToDocumentsPosting(currDoc, allTerms));
        writeToDocumentPosting.start();
        Iterator it = allTerms.entrySet().iterator();
        signNewDocSection(currDoc);
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String term = (String) pair.getKey();
            if ( STEMMING ) {
               // String[] s = {"d:\\documents\\users\\harelsa\\Downloads\\corpus\\stem.txt"}  ;
               // stemmer.main(s);
                Stemmer stemmer = new Stemmer() ;
                stemmer.add(term.toCharArray(), term.length());
                String stemmed = "";
                stemmer.stem();
                term = stemmer.toString();

            }

            int partitionNum = getPartitionDest(term); // 0-6
            switch (partitionNum){
                case 0:
                    filePartitions[0].signNewTerm((Term)pair.getValue());
                    break;
                case 1:
                    filePartitions[1].signNewTerm((Term)pair.getValue());
                    break;
                case 2:
                    filePartitions[2].signNewTerm((Term)pair.getValue());
                    break;
                case 3:
                    filePartitions[3].signNewTerm((Term)pair.getValue());
                    break;
                case 4:
                    filePartitions[4].signNewTerm((Term)pair.getValue());
                    break;
                case 5:
                    filePartitions[5].signNewTerm((Term)pair.getValue());
                    break;
                case 6:
                    filePartitions[6].signNewTerm((Term)pair.getValue());
                    break;
            }
        }
        flushAllPartitions();

    }

    private void flushAllPartitions() {
        for (int i = 0; i < NUM_OF_PARTITATIONS; i++) {
            filePartitions[i].flushFile();
        }
    }

    private void signNewDocSection(Document currDoc) {
        for (int i = 0; i < NUM_OF_PARTITATIONS; i++) {
            filePartitions[i].signDocSection(currDoc);
        }
    }
    // ('0', '9');
    // ('a', 'c');
    // ('d', 'f');
    // ('g', 'k');
    // ('l', 'p');
    // ('q', 'z');
    // ('z', 'z');

    private int getPartitionDest(String term) {
        char termFirstChar = term.toLowerCase().charAt(0);
        if (Character.isDigit(termFirstChar))
            return 0;
        else if (termFirstChar >= 'a' && termFirstChar <= 'c')
            return 1;
        else if (termFirstChar >= 'd' && termFirstChar <= 'f')
            return 2;
        else if (termFirstChar >= 'g' && termFirstChar <= 'k')
            return 3;
        else if (termFirstChar >= 'l' && termFirstChar <= 'p')
            return 4;
        else if (termFirstChar >= 'q' && termFirstChar <= 'z')
            return 5;
        else
            return 6;
    }

    // ('0', '9');
    // ('a', 'c');
    // ('d', 'f');
    // ('g', 'k');
    // ('l', 'p');
    // ('q', 'z');
    // ('z', 'z');

    public SegmentFilePartition getSegmentFilePartitions(char from, char to){
        if (from == '0' && to == '9')
            return filePartitions[0];
        if (from == 'a' && to == 'c')
            return filePartitions[1];
        if (from == 'd' && to == 'f')
            return filePartitions[2];
        if (from == 'g' && to == 'k')
            return filePartitions[3];
        if (from == 'l' && to == 'p')
            return filePartitions[4];
        if (from == 'q' && to == 'z')
            return filePartitions[5];
        if (from == 'z' && to == 'z')
            return filePartitions[6];
        return null;
    }
}
