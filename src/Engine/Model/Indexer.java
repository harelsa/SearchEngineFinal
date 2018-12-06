package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
    public static TreeMap<String, String> terms_dictionary;
    public static TreeMap<String, String> cites_dictionary;
    public static TreeMap<String, String> docs_dictionary;
    private static FileWriter termDictionary_fw;

    static {
        try {
            termDictionary_fw = new FileWriter("src\\Engine\\resources\\Dictionaries\\termDictionary.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static BufferedWriter termDictionary_bf = new BufferedWriter(termDictionary_fw);

    public static ConcurrentHashMap<String, Boolean> termStartsWithCapitalLetter;

    public static void initIndexer() {
        terms_dictionary = new TreeMap<>(new TermComparator());

        cites_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((City) (o1)).getCityName();
            String s2 = ((City) (o2)).getCityName();
            return s1.compareTo(s2);
        });

        docs_dictionary = new TreeMap<>(new DocComparator());

    }

    private SegmentFilePartition[] segmentFilePartitions;
    private Posting termsPosting;
    private Posting docsPosting;

    public Indexer(SegmentFilePartition[] segmentFilePartitions) {
        this.segmentFilePartitions = segmentFilePartitions;
    }

    public Indexer(SegmentFilePartition[] segmentFilesInverter, Posting termsPostingFile, Posting docsPostingFile) {
        termsPosting = termsPostingFile;
        docsPosting = docsPostingFile;
        this.segmentFilePartitions = segmentFilesInverter;
    }

    public void buildInvertedIndexes() {
        readDocsFromEachSegmentFile();
    }

    private void readDocsFromEachSegmentFile() {
        //TreeMap<String, String> TermToDocs = new TreeMap<>(new TermComparator()); // <TermContent, list of docs in format: <docNum>,<tf>,<termLocationInDoc>,"#">
        TreeMap[] termToDocsArr = new TreeMap[segmentFilePartitions.length];
        for (int i = 0; i < segmentFilePartitions.length; i++) {
            termToDocsArr[i] = new TreeMap<>(new TermComparator());
        }
        for (int i = 0; i < segmentFilePartitions.length; i++) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            System.out.println("Starting to handle: " + "Segment File " + i + " " + timeStamp);
            StringBuilder sb;
            String line;
            while ((line = segmentFilePartitions[i].readLine()) != null) {
                if (line.contains("<D>")) {
                    sb = new StringBuilder();
                    String docNo = "";
                    if (isRealDoc(line)) {
                        // <D>FBIS3-1830,FB396008,BEIJING,8,administrative,164</D>
                        line.replace("<D>", "");
                        String[] splitedLine = StringUtils.split(line, ",");
                        docNo = splitedLine[0];
                        line = segmentFilePartitions[i].readLine();
                        while (line != null && !line.contains("<D>")) {
                            String tf = "";
                            String[] locsSplited = StringUtils.split(line, "[");
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
                        // finished to read one doc from segment partition. sb = <Term>,<tf>"#"<Term>,<tf>"#"...
                        //DocToTerms.put(docNo.toLowerCase(), sb.toString());
                    }
                }
            }
        }

        termsPosting.writeToTermsPosting(termToDocsArr);

    }


    private boolean isRealDoc(String line) {
        if (line.contains("null"))
            return false;
        return true;
    }

//    synchronized private void addNewTermToDictionary(String term, String docNo, String tf) {
//        terms_dictionary.put(term, docNo + "," + tf + ",");
//    }
//
//    synchronized private void checkTermTfFromAnotherDoc(String term, String docNo, String tf) {
//        String value = terms_dictionary.get(term);
//        if (value == null)
//            return;
//        String[] splitedValue = StringUtils.split(value, ",");
//        try {
//            if (Integer.parseInt(tf) > Integer.parseInt(splitedValue[1])) {
//                terms_dictionary.put(term, docNo + "," + tf + ",");
//            }
//        } catch (java.lang.NumberFormatException nfe) {
//            nfe.printStackTrace();
//        }
//    }

    public static void writeDictionariesToDisc() {
        try {
            Iterator termIt = terms_dictionary.entrySet().iterator();
            int counter = 0;
            while (termIt.hasNext()) {
                Map.Entry pair = (Map.Entry) termIt.next();
                try {
                    termDictionary_bf.append(pair.getKey().toString() + pair.getValue().toString() + "\n");
                    counter++;
                    if (counter > 10000) {
                        termDictionary_bf.flush();
                        counter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                termIt.remove(); // avoids a ConcurrentModificationException
            }
            termDictionary_bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileWriter docDictionary_fw = null;
        try {
            docDictionary_fw = new FileWriter("src\\Engine\\resources\\Dictionaries\\docDictionary.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter docDictionary_bf = new BufferedWriter(docDictionary_fw);
        Iterator docIt = docs_dictionary.entrySet().iterator();
        int counter = 0;
        while (docIt.hasNext()) {
            Map.Entry pair = (Map.Entry) docIt.next();
            try {
                docDictionary_bf.append(pair.getKey().toString() + "," + pair.getValue().toString() + "\n");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        terms_dictionary.clear();
        System.out.println("writeDictionariesToDisc Done");
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

