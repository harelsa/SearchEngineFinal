package Engine.Model;

/**
 * This class is responsible for creating the various indexes.
 * The methods in this class manage the creation of the Posting files for the terms index,
 * And the creation of dictionaries:
 *  1) Doc to Terms
 *  2) Term to Docs
 *  3) City to Docs
 */

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Indexer {
    private SegmentFilePartition[] segmentFilePartitions; // The indexer extracts information from the segment file partition
    private Posting termsPosting;
    private static String staticPostingsPath;

    public static TreeMap<String, String> terms_dictionary;
    public static TreeMap<String, String> cities_dictionary;
    public static TreeMap<String, String> docs_dictionary;


    public static void initIndexer(String postingPath) {
        terms_dictionary = new TreeMap<>(new TermComparator());
        cities_dictionary = new TreeMap<>();
        docs_dictionary = new TreeMap<>(new DocComparator());
        staticPostingsPath = postingPath;
    }

    Indexer(SegmentFilePartition[] segmentFilesInverter, Posting termsPostingFile) {
        termsPosting = termsPostingFile;
        this.segmentFilePartitions = segmentFilesInverter;
    }

    /**
     * This method is triggered by a number of threads as the number of Partitions we have divided into each Segment File.
     * Each thread is actually contains an instance of the indexer and is responsible for processing a specific alphabetic range of terms.
     * The following method makes preliminary processing of information in the Segment Files Partitions;
     * it creates a TreeMap array when the keys of each TreeMap is the Term value and the value is details about the Term.
     * Each TreeMap contains the information from one Segment File Partition.
     * After the information of all segment file partitions has been entered into the array, we send the array to write in posting.
     */
    public void appendSegmentPartitionRangeToPostingAndIndexes() {
        TreeMap[] termToDocsArr = new TreeMap[segmentFilePartitions.length];
        for (int i = 0; i < segmentFilePartitions.length; i++) {
            termToDocsArr[i] = new TreeMap<>(new TermComparator());
        }
        for (int i = 0; i < segmentFilePartitions.length; i++) {
            if (CorpusProcessingManager.testMode) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                System.out.println("Starting to handle: " + "Segment File " + i + " " + timeStamp);
            }
            StringBuilder sb;
            String line;
            while ((line = segmentFilePartitions[i].readLine()) != null) {
                if (line.contains("<D>")) {
                    sb = new StringBuilder();
                    String docNo = "";
                    if (isRealDoc(line)) {
                        line.replace("<D>", "");
                        String[] splitedLine = StringUtils.split(line, ",");
                        docNo = splitedLine[0];
                        line = segmentFilePartitions[i].readLine();
                        while (line != null && !line.contains("<D>")) {
                            String tf = "";
                            if (!line.contains("[")){
                                System.out.println(line);
                            }
                            String[] locsSplited = StringUtils.split(line, "["); // Cuts the locations details
                            if (locsSplited == null){
                                line = segmentFilePartitions[i].readLine();
                                continue;
                            }
                            int lastIndex = StringUtils.lastIndexOf(locsSplited[0], ",");
                            if (lastIndex == -1) {
                                line = segmentFilePartitions[i].readLine();
                                continue;
                            }
                            locsSplited[0] = StringUtils.substring(locsSplited[0], 0, lastIndex);
                            lastIndex = StringUtils.lastIndexOf(locsSplited[0], ",");
                            if (lastIndex == -1) {
                                line = segmentFilePartitions[i].readLine();
                                continue;
                            }
                            tf = locsSplited[0].substring(lastIndex + 1);
                            String term = locsSplited[0].substring(0, lastIndex);
                            String locs = "";
                            if (locsSplited.length > 1)
                                locs = "[" + locsSplited[1];
                            sb.append(term).append(",").append(tf).append('#');
                            //if (TermToDocs.containsKey(term)) {
                            //    String tmp = TermToDocs.get(term);
                            //    termToDocsArr[i].put(term, tmp + docNo + "," + tf + "," + locs + "#");
                            //    if (term.charAt(0) == '*')
                            //        term = StringUtils.substring(term, 1);
                            //    checkTermTfFromAnotherDoc(term, docNo, tf);
                            //}
                            //else {
                            termToDocsArr[i].put(term, docNo + "," + tf + "," + locs + "#");
                            line = segmentFilePartitions[i].readLine();
                        }
                    }
                }
            }
        }
        termsPosting.writeToTermsPosting(termToDocsArr);
    }

    // To handle inside bug.
    private boolean isRealDoc(String line) {
        if (line.contains("null"))
            return false;
        return true;
    }


    public static void writeDictionariesToDisc() {
        try {
            FileWriter termDictionary_fw = new FileWriter(staticPostingsPath + "\\termDictionary.txt");
            BufferedWriter termDictionary_bw = new BufferedWriter(termDictionary_fw);
            Iterator termIt = terms_dictionary.entrySet().iterator();
            int counter = 0;
            while (termIt.hasNext()) {
                Map.Entry pair = (Map.Entry) termIt.next();
                try {
                    termDictionary_bw.append(pair.getKey().toString()).append(pair.getValue().toString()).append("\n"); // Format example: accommodation<D>FBIS3-58,2,8,6,179
                    counter++;
                    if (counter > 10000) {
                        termDictionary_bw.flush();
                        counter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                termIt.remove(); // avoids a ConcurrentModificationException
            }
            termDictionary_bw.flush();
            termDictionary_bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileWriter docDictionary_fw = null;

        try {
            docDictionary_fw = new FileWriter(staticPostingsPath + "\\docDictionary.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter docDictionary_bf = new BufferedWriter(docDictionary_fw);
        Iterator docIt = docs_dictionary.entrySet().iterator();
        int counter = 0;
        while (docIt.hasNext()) {
            Map.Entry pair = (Map.Entry) docIt.next();
            try {
                docDictionary_bf.append(pair.getKey().toString() + "," + pair.getValue().toString() + "\n"); // Format example: FBIS3-1007,FB396005,AFRIKAANS,JOHANNESBURG,money,9,203,103
                counter++;
                if (counter > 10000) {
                    docDictionary_bf.flush();
                    counter = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            docIt.remove(); // avoids a ConcurrentModificationException
        }
        try {
            docDictionary_bf.flush();
            docDictionary_bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        terms_dictionary.clear();

        try {
            Iterator termIt = cities_dictionary.entrySet().iterator();
            BufferedWriter citiesDictionary_bf = new BufferedWriter(new FileWriter(staticPostingsPath + "\\citiesDictionary.txt"));
            counter = 0;
            while (termIt.hasNext()) {
                Map.Entry pair = (Map.Entry) termIt.next();
                try {
                    String key = pair.getKey().toString();
                    String cityDetailsFromApi = getCityDetailsFromApi(key);
                    citiesDictionary_bf.append(key).append(",").append(cityDetailsFromApi).append(",").append(pair.getValue().toString() + "\n"); // Format example: AMSTERDAM,EUR,M17.02,<D>FBIS3-29,1,[15184]#<D>FBIS3-2378,1,[46]#
                    counter++;
                    if (counter > 10000) {
                        citiesDictionary_bf.flush();
                        counter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                termIt.remove(); // avoids a ConcurrentModificationException
            }
            citiesDictionary_bf.flush();
            citiesDictionary_bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("writeDictionariesToDisc Done");
    }

    private static String getCityDetailsFromApi(String s) {
        StringBuilder sb = new StringBuilder();
        s= s.toLowerCase() ;
        boolean test = CorpusProcessingManager.cities.containsKey(s) ;
        if (test){
            City city = CorpusProcessingManager.cities.get(s.toLowerCase());
            String currency = city.getCurrency();
            String pop = city.getPopulation();
            sb.append(currency).append(",").append(pop);
            return sb.toString();
        }
        return sb.toString();
    }


    synchronized public static void addNewDocToDocDictionary(String docNo, String docValue) {
        docs_dictionary.put(docNo, docValue);
    }


    public static class TermComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            String s1 = ((String) (o1));
            String s2 = ((String) (o2));
            if (s1.charAt(0) == '*')
                s1 = s1.substring(1);
            if (s2.charAt(0) == '*')
                s2 = s2.substring(1);
            return s1.compareTo(s2);
        }
    }

    public static class DocComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            String s1 = ((String) (o1));
            String s2 = ((String) (o2));
            return s1.compareTo(s2);
        }
    }
}

