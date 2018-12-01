package Engine.Model;

import sun.reflect.generics.tree.Tree;

import javax.print.Doc;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
    public static TreeMap<Term, PostingListEntry> terms_dictionary;
    public static TreeMap<City, PostingListEntry> cites_dictionary;
    public static TreeMap<Document, PostingListEntry> docs_dictionary;

    public static TreeMap<Term, Boolean> termStartsWithCapitalLetter;

    public static void initIndexer(){

        terms_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((Term)(o1)).getContent();
            String s2 = ((Term)(o2)).getContent();
            return s1.compareTo(s2);
        });

        cites_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((City)(o1)).getCityName();
            String s2 = ((City)(o2)).getCityName();
            return s1.compareTo(s2);
        });

        docs_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((Document)(o1)).getDocNo();
            String s2 = ((Document)(o2)).getDocNo();
            return s1.compareTo(s2);
        });

        termStartsWithCapitalLetter = new TreeMap<>((Comparator) (o1, o2) -> {
            String s1 = ((Term)(o1)).getContent();
            String s2 = ((Term)(o2)).getContent();
            return s1.compareTo(s2);
        });

    }

    private SegmentFilePartition[] segmentFilePartitions;

    public Indexer(SegmentFilePartition[] segmentFilePartitions) {
            this.segmentFilePartitions = segmentFilePartitions;
        }



    public void buildInvertedIndex() {
        TreeMap<String, String> docsAndTermsFromMultipleSegmentFiles = readDocFromEachSegmentFile();


    }

    private TreeMap<String,String> readDocFromEachSegmentFile() {
        TreeMap<String, String> ans = new TreeMap<>();
        String docAndTerms0 = segmentFilePartitions[0].extractDocTermsFromSegmentFile();
        String docAndTerms1 = segmentFilePartitions[1].extractDocTermsFromSegmentFile();
        String docAndTerms2 = segmentFilePartitions[2].extractDocTermsFromSegmentFile();
        String docAndTerms3 = segmentFilePartitions[3].extractDocTermsFromSegmentFile();
        String docAndTerms4 = segmentFilePartitions[4].extractDocTermsFromSegmentFile();
        String docAndTerms5 = segmentFilePartitions[5].extractDocTermsFromSegmentFile();
        String docAndTerms6 = segmentFilePartitions[6].extractDocTermsFromSegmentFile();
        System.out.println(docAndTerms0);
        System.out.println(docAndTerms1);
        System.out.println(docAndTerms2);
        System.out.println(docAndTerms3);
        System.out.println(docAndTerms4);
        System.out.println(docAndTerms5);
        System.out.println(docAndTerms6);
        return null;

    }
}
