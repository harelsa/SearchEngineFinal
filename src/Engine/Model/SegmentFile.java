package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SegmentFile implements Serializable {
    private SegmentFilePartition[] filePartitions;
    private final int NUM_OF_PARTITATIONS = 36;
    private boolean STEMMING = false ; // tells if to do stemmig - will be changed from gui

    public SegmentFile(String path , boolean stemming ) {
        STEMMING = stemming ;
        filePartitions = new SegmentFilePartition[NUM_OF_PARTITATIONS]; // startsWith Digit ,a-f, g-p, q-z, startsWith "
        char j = '0';
        for (int i = 0 ; i < 10; i++, j++) {
            filePartitions[i] = new SegmentFilePartition(path, j);
        }
        j = 'a';
        for (int i = 10; i < 36; i++, j++) {
            filePartitions[i] = new SegmentFilePartition(path, j);
        }
    }

    public void signToSpecificPartition(SortedMap<String, Term> allTerms, Document currDoc) {
        if (allTerms.size() == 0)
            return;
        Thread writeToDocumentPosting = new Thread(() -> Posting.writeToDocumentsPosting(currDoc, allTerms));
        writeToDocumentPosting.start();
        CorpusProcessingManager.docsPostingWriterExecutor.execute(writeToDocumentPosting);
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

            int partitionNum = getPartitionDest(term); // 0-36
            if (partitionNum > 35 || partitionNum < 0)
                return;
            filePartitions[partitionNum].signNewTerm((Term)pair.getValue());
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

    private int getPartitionDest(String term) {
        char termFirstChar = term.toLowerCase().charAt(0);
        if (Character.isDigit(termFirstChar)){
            String number = "";
            number += termFirstChar;
            return Integer.parseInt(number);
        }
        int ascii = (int) termFirstChar;
        int ans = ascii - 87;
        return ans;
    }


    public SegmentFilePartition getSegmentFilePartitions(char charFile){
        if (Character.isDigit(charFile)) {
            String number = "";
            number += charFile;
            int index = Integer.parseInt(number);
            return filePartitions[index];
        }
        int ascii = (int) charFile;
        int ans = ascii - 87;
        return filePartitions[ans];
    }

    public void closeBuffers() {
        for (int i = 0; i < filePartitions.length; i++) {
            filePartitions[i].closeBuffers();
        }
    }
}
