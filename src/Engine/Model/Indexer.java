package Engine.Model;

import sun.reflect.generics.tree.Tree;

import javax.print.Doc;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
    public static TreeMap<Term, String> terms_dictionary;
    public static TreeMap<City, String> cites_dictionary;
    public static TreeMap<Document, String> docs_dictionary;

    public static TreeMap<Term, Boolean> termStartsWithCapitalLetter;

    public static void initIndexer() {

        terms_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((Term) (o1)).getContent();
            String s2 = ((Term) (o2)).getContent();
            return s1.compareTo(s2);
        });

        cites_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((City) (o1)).getCityName();
            String s2 = ((City) (o2)).getCityName();
            return s1.compareTo(s2);
        });

        docs_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((Document) (o1)).getDocNo();
            String s2 = ((Document) (o2)).getDocNo();
            return s1.compareTo(s2);
        });

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

    public Indexer(SegmentFilePartition[] segmentFilesInverter1, Posting termsPostingFile_0_9, Posting docsPostingFile_0_9) {
        termsPosting = termsPostingFile_0_9;
        docsPosting = docsPostingFile_0_9;
        this.segmentFilePartitions = segmentFilesInverter1;
    }

    public void buildInvertedIndexes() {
        TreeMap<String, String> docsAndTermsFromMultipleSegmentFiles = readDocsFromEachSegmentFile();
    }

    private TreeMap<String, String> readDocsFromEachSegmentFile() {
        TreeMap<String, String> docTerms = new TreeMap<>(); // <DocID, list of strings in format: <Term>,<tf>"#"<Term>,<tf>"#"....
        TreeMap<String, String> termsDoc = new TreeMap<>(); // <TermContent, list of docs in format: <docNum>,<tf>,<termLocationInDoc>,"#">

        for (int i = 0; i < 7; i++) {
            StringBuilder sb;
            String line;
            String maxContentTermFreq;
            while ((line = segmentFilePartitions[i].readLine()) != null) {
                if (line.contains("<D>")) {
                    sb = new StringBuilder();
                    if (isRealDoc(line)) {
                        // <D>FBIS3-1830,FB396008,BEIJING,8,administrative,164</D>
                        line.replace("<D>", "");
                        String[] splitedLine = line.split(",");
                        String docNo = splitedLine[0];
                        if (splitedLine.length > 1){
                            String parentFileName = splitedLine[1];
                            if (splitedLine.length > 2){
                                String docCity = splitedLine[2];
                                if (splitedLine.length > 3){
                                    int maxTermFreq = Integer.parseInt(splitedLine[3]);
                                    if (splitedLine.length > 4)
                                        maxContentTermFreq = splitedLine[4];
                                }
                            }

                        }

                        line = segmentFilePartitions[i].readLine();

                        while (line != null && !line.contains("<D>")) {
                            String[] locsSplited = line.split("\\[");
                            String[] splited = locsSplited[0].split(",");
                            String term = splited[0];
                            String tf = splited[1];
                            String locs = "[" + locsSplited[1];
                            sb.append(term).append(",").append(tf).append('#');
                            if (termsDoc.containsKey(term)) {
                                String tmp = termsDoc.get(term);
                                termsDoc.put(term, tmp + docNo + "," + tf + "," + locs + "#");
                            } else
                                termsDoc.put(term, docNo + "," + tf + "," + locs + "#");
                            line = segmentFilePartitions[i].readLine();
                        }
                        // finished to read one doc from segment partition. sb = <Term>,<tf>"#"<Term>,<tf>"#"...
                        docTerms.put(docNo, sb.toString());
                    }
                }
            }
        }
        termsPosting.writeToPosting(termsDoc);


        //System.out.println(docAndTerms0);
        //System.out.println(docAndTerms1);
        //System.out.println(docAndTerms2);
        //System.out.println(docAndTerms3);
        //System.out.println(docAndTerms4);
        //System.out.println(docAndTerms5);
        //System.out.println(docAndTerms6);
        //return null;
        return null;
    }

    private boolean isRealDoc(String line) {
        if (line.contains("null"))
            return false;
        return true;
    }
}

