package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class Indexer {
    public static TreeMap<String, String> terms_dictionary;
    public static TreeMap<String, String> cites_dictionary;
    public static TreeMap<String, String> docs_dictionary;

    public static TreeMap<Term, Boolean> termStartsWithCapitalLetter;

    public static void initIndexer() {
        terms_dictionary = new TreeMap<>(new TermComparator());

        cites_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((City) (o1)).getCityName();
            String s2 = ((City) (o2)).getCityName();
            return s1.compareTo(s2);
        });

        docs_dictionary = new TreeMap<String, String>(new DocComparator());

        termStartsWithCapitalLetter = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((Term) (o1)).getContent();
            String s2 = ((Term) (o2)).getContent();
            return s1.compareTo(s2);
        });

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
        TreeMap<String, String> DocToTerms = new TreeMap<>(); // <DocID, list of strings in format: <Term>,<tf>"#"<Term>,<tf>"#"....
        TreeMap<String, String> TermToDocs = new TreeMap<>(new TermComparator()); // <TermContent, list of docs in format: <docNum>,<tf>,<termLocationInDoc>,"#">
        for (int i = 0; i < segmentFilePartitions.length; i++) {
            StringBuilder sb;
            String line;
            while ((line = segmentFilePartitions[i].readLine()) != null) {
                if (line.contains("<D>")) {
                    sb = new StringBuilder();
                    String parentFileName = "";
                    String docCity = "";
                    String maxTermFreq = "";
                    String maxContentTermFreq = "";
                    String docNo = "";
                    if (isRealDoc(line)) {
                        // <D>FBIS3-1830,FB396008,BEIJING,8,administrative,164</D>
                        line.replace("<D>", "");
                        String[] splitedLine = StringUtils.split(line, ",");
                        docNo = splitedLine[0];
                        if (splitedLine.length > 1) {
                            parentFileName = splitedLine[1];
                            if (splitedLine.length > 2) {
                                docCity = splitedLine[2];
                                if (splitedLine.length > 3) {
                                    maxTermFreq = splitedLine[3];
                                    if (splitedLine.length > 4)
                                        maxContentTermFreq = splitedLine[4];
                                }
                            }
                        }
                        //Document d = new Document(docNo, parentFileName, docCity, maxTermFreq, maxContentTermFreq);

                        line = segmentFilePartitions[i].readLine();

                        while (line != null && !line.contains("<D>")) {
                            String tf = "";
                            String[] locsSplited = StringUtils.split(line, "[");
                            int lastIndex = StringUtils.lastIndexOf(locsSplited[0], ",");
                            if (lastIndex == -1){
                                line = segmentFilePartitions[i].readLine();
                                continue;
                            }
                            locsSplited[0] = StringUtils.substring(locsSplited[0],0,lastIndex);
                            lastIndex = StringUtils.lastIndexOf(locsSplited[0], ",");
                            if (lastIndex == -1){
                                line = segmentFilePartitions[i].readLine();
                                continue;
                            }
                            //tf = StringUtils.substring(locsSplited[0], 0, lastIndex+1);
                            tf = locsSplited[0].substring(lastIndex+1);
                            //String term = StringUtils.substring(locsSplited[0], 0, lastIndex);
                            String term = locsSplited[0].substring(0,lastIndex);
                            String locs = "";
                            if (locsSplited.length > 1)
                                locs = "[" + locsSplited[1];
                            sb.append(term).append(",").append(tf).append('#');
                            if (TermToDocs.containsKey(term)) {
                                String tmp = TermToDocs.get(term);
                                TermToDocs.put(term, tmp + docNo + "," + tf + "," + locs + "#");
                                checkTermTfFromAnotherDoc(term, docNo, tf);
                            }
                            else {
                                TermToDocs.put(term, docNo + "," + tf + "," + locs + "#");
                                addNewTermToDictionary(term, docNo, tf);
                            }
                            line = segmentFilePartitions[i].readLine();
                        }
                        // finished to read one doc from segment partition. sb = <Term>,<tf>"#"<Term>,<tf>"#"...
                        DocToTerms.put(docNo.toLowerCase(), sb.toString());
                    }
                }
            }
        }

        termsPosting.writeToTermsPosting(TermToDocs);
        //docsPosting.writeToPosting(DocToTerms);

    }

    private boolean isRealDoc(String line) {
        if (line.contains("null"))
            return false;
        return true;
    }

    synchronized private void addNewTermToDictionary(String term, String docNo, String tf) {
        terms_dictionary.put(term, docNo + "," + tf + ",");
    }

    synchronized private void checkTermTfFromAnotherDoc(String term, String docNo, String tf) {
        String value = terms_dictionary.get(term);
        if (value == null)
            return;
        String[] splitedValue = StringUtils.split(value, ",");
        try{
            if (Integer.parseInt(tf) > Integer.parseInt(splitedValue[1])) {
                terms_dictionary.put(term, docNo + "," + tf + ",");
        }
        }catch (java.lang.NumberFormatException nfe){
            nfe.printStackTrace();
        }
    }

    public static void writeDictionariesToDisc() {
        try {
            FileWriter termDictionary_fw = new FileWriter("src\\Engine\\resources\\Dictionaries\\termDictionary.txt");
            BufferedWriter termDictionary_bf = new BufferedWriter(termDictionary_fw);
            //PrintWriter pw = new PrintWriter("src\\Engine\\resources\\Segment Files\\mergesDocsPostingFile.txt");

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
                if (counter > 10000){
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

    public static class DocComparator implements Comparator<Object>{

        @Override
        public int compare(Object o1, Object o2) {
            String s1 = ((String) (o1));
            String s2 = ((String) (o2));
            return s1.compareTo(s2);
        }
    }
}

