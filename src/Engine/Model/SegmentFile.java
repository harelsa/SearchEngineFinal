package Engine.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class SegmentFile implements Serializable {
    private SegmentFilePartition[] filePartitions;

    public SegmentFile() {
    }

    public SegmentFile(String path) {
        filePartitions = new SegmentFilePartition[5]; // startsWith Digit ,a-f, g-p, q-z, startsWith "
        filePartitions[0] = new SegmentFilePartition(path, '0', '9');
        filePartitions[1] = new SegmentFilePartition(path, 'a', 'f');
        filePartitions[2] = new SegmentFilePartition(path, 'g', 'p');
        filePartitions[3] = new SegmentFilePartition(path, 'q', 'z');
        filePartitions[4] = new SegmentFilePartition(path, 'z', 'z');
    }

/* Need to do it concurrent (new thread in parser which calls this method) */
    public void signToSpecificPartition(HashMap<String, Term> allTerms, Document currDoc) {
        Iterator it = allTerms.entrySet().iterator();
        signNewDocSection(currDoc);
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String term = (String) pair.getKey();
            int partitionNum = getPartitionDest(term); // 0-4
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
            }
        }
    }

    private void signNewDocSection(Document currDoc) {
        for (int i = 0; i < filePartitions.length; i++) {
            filePartitions[i].signDocSection(currDoc);
        }
    }

    private int getPartitionDest(String term) {
        char termFirstChar = term.toLowerCase().charAt(0);
        if (Character.isDigit(termFirstChar))
            return 0;
        else if (termFirstChar >= 'a' && termFirstChar <= 'f')
            return 1;
        else if (termFirstChar >= 'g' && termFirstChar <= 'p')
            return 2;
        else if (termFirstChar >= 'q' && termFirstChar <= 'z')
            return 3;
        else
            return 4;
    }
}
