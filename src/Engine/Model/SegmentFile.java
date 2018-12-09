package Engine.Model;
/**
 * This class represents Segment File according to the Map Reduce model.
 * The class is responsible for managing the writing the resulting output from parse into the appropriate partitions in the Segment file.
 * It should be noted that in practice, this class does not represent a physical file but rather links to the instances of the SegmentFilePartition class that are contained in it.
 * Each instance of SegmentFilePartition is a physical file.
 */

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;

public class SegmentFile implements Serializable {
    private SegmentFilePartition[] filePartitions;
    private final int NUM_OF_PARTITATIONS = 7;
    private boolean STEMMING; // tells if to do stemming - will be changed from gui


    /**
     * Constructor: Initializes Partitions as the number of categories we have chosen to divide each Segment File.
     * @param path: The path in which the SegmentFilePartition files are created
     * @param stemming:  If the current processing should be used in the stemming
     */
    public SegmentFile(String path , boolean stemming ) {
        STEMMING = stemming ;
        filePartitions = new SegmentFilePartition[7];
        filePartitions[0] = new SegmentFilePartition(path, '0', '9');
        filePartitions[1] = new SegmentFilePartition(path, 'a', 'c');
        filePartitions[2] = new SegmentFilePartition(path, 'd', 'f');
        filePartitions[3] = new SegmentFilePartition(path, 'g', 'k');
        filePartitions[4] = new SegmentFilePartition(path, 'l', 'p');
        filePartitions[5] = new SegmentFilePartition(path, 'q', 'z');
        filePartitions[6] = new SegmentFilePartition(path, 'z', 'z'); // This Partition will actually catch "strange creatures" that will not enter inverted index / posting files
    }

    /**
     * The method sorts the terms received from the parse for a specific document and inserts them into the appropriate SegmentFilePartition.
     * In addition, the raw information contained in the parameters obtained is also useful for a more advanced stage, the documents posting create phase.
     * Therefore, during this method, we will send the parameters to another thread in order for it to start writing the posting.
     * @param allTerms The terms received from parse
     * @param currDoc The document from which the terms were received
     */
    public void sortDocTermsToSpecificPartition(SortedMap<String, Term> allTerms, Document currDoc) {
        if (allTerms.size() == 0)
            return;
        Thread writeToDocumentPosting = new Thread(() -> Posting.writeToDocumentsPosting(currDoc, allTerms));
        writeToDocumentPosting.start();
        Iterator it = allTerms.entrySet().iterator();
        signNewDocSection(currDoc);
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String term = (String) pair.getKey();
//            if (term.length() > 2 && (term.charAt(0) == '|' || term.charAt(0) == '\'' || term.charAt(0) == '`'))
//                term = StringUtils.substring(term, 1);
            if ( STEMMING ) {
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

    /**
     * In order for us to know which term came from each document
     * we will first write the document details in each FilePartition
     * and only then will we begin to insert the terms associated with this document.
     * @param currDoc The document from which the terms were received
     */
    private void signNewDocSection(Document currDoc) {
        for (int i = 0; i < NUM_OF_PARTITATIONS; i++) {
            filePartitions[i].signDocSection(currDoc);
        }
    }

    /**
     * This function returns the number of the appropriate Partition for the received term.
     * The number is determined by the first character of the term.
     * @param term The term should be inserted into the partition
     * @return number of the appropriate Partition
     */
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

    public void closeBuffers() {
        for (int i = 0; i < filePartitions.length; i++) {
            filePartitions[i].closeBuffers();
        }
    }
}
