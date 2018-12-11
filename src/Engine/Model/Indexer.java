package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class Indexer {
    public static TreeMap<String, String> terms_dictionary;
    public static TreeMap<String, String> cities_dictionary;
    public static TreeMap<String, String> docs_dictionary;
    private static FileWriter termDictionary_fw;
    public static String staticPostingsPath;


    private static BufferedWriter termDictionary_bf;


    public static void initIndexer(String postingPath) {
        terms_dictionary = new TreeMap<>(new TermComparator());
        try {
            termDictionary_fw = new FileWriter(postingPath + "\\termDictionary.txt");
            termDictionary_bf = new BufferedWriter(termDictionary_fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cities_dictionary = new TreeMap<>();

//        cities_dictionary = new TreeMap<>((Comparator) (o1, o2) -> {
//            String s1 = ((City) (o1)).getCityName();
//            String s2 = ((City) (o2)).getCityName();
//            return s1.compareTo(s2);
//        });

        docs_dictionary = new TreeMap<>(new DocComparator());
        staticPostingsPath = postingPath;
    }

    private SegmentFilePartition[] segmentFilePartitions;
    private Posting termsPosting;
    private Posting docsPosting;

    public Indexer(SegmentFilePartition[] segmentFilePartitions) {
        this.segmentFilePartitions = segmentFilePartitions;
    }

    public Indexer(SegmentFilePartition[] segmentFilesInverter, Posting termsPostingFile) {
        termsPosting = termsPostingFile;
        //docsPosting = docsPostingFile;
        this.segmentFilePartitions = segmentFilesInverter;
    }


//    public void appendSegmentPartitionRangeToPostingAndIndexes() {
//        //TreeMap<String, String> TermToDocs = new TreeMap<>(new TermComparator()); // <TermContent, list of docs in format: <docNum>,<tf>,<termLocationInDoc>,"#">
//        TreeMap<String, String> charTerms = new TreeMap<>();//new TermComparator());
//        HashMap<String, Boolean> ifTermStartsWithCapital = new HashMap<>();
//        for (int i = 0; i < segmentFilePartitions.length; i++) {
//            StringBuilder sb;
//            String line = segmentFilePartitions[i].readLine();
//            while (line != null) {
//                if (line.contains("<D>")) {
//                    sb = new StringBuilder();
//                    String docNo = "";
//                    line = line.replace("<D>", "");
//                    String[] docLineSplited = StringUtils.split(line, ",");
//                    docNo = docLineSplited[0];
//                    line = segmentFilePartitions[i].readLine();
//                    while (line != null && !line.contains("<D>")) {
//                        String tf = "";
//                        String[] termLineSplitedByLocsPar = StringUtils.split(line, "[");
//                        int lastIndexOfComma = StringUtils.lastIndexOf(termLineSplitedByLocsPar[0], ",");
//                        if (lastIndexOfComma == -1) { // Not a really term (there is no location)
//                            line = segmentFilePartitions[i].readLine();
//                            continue;
//                        }
//                        termLineSplitedByLocsPar[0] = StringUtils.substring(termLineSplitedByLocsPar[0], 0, lastIndexOfComma); // To get <Term>,<tf>
//                        lastIndexOfComma = StringUtils.lastIndexOf(termLineSplitedByLocsPar[0], ",");
//                        if (lastIndexOfComma == -1) {
//                            line = segmentFilePartitions[i].readLine();
//                            continue;
//                        }
//                        String locs = "";
//                        if (termLineSplitedByLocsPar.length > 1)
//                            locs = "[" + termLineSplitedByLocsPar[1];
//                        tf = StringUtils.substring(termLineSplitedByLocsPar[0], lastIndexOfComma + 1); // to get <tf>
//                        String term = StringUtils.substring(termLineSplitedByLocsPar[0], 0, lastIndexOfComma); // to get <term>
//                        sb.append(docNo).append(",").append(tf).append(",").append(locs).append("#");
//
//                        if (term.charAt(0) == '*' && term.length() > 1) {
//                            term = StringUtils.substring(term, 1);
//                            if (!ifTermStartsWithCapital.containsKey(term))
//                                ifTermStartsWithCapital.put(term, true);
//                        } else if (term.charAt(0) != '*') {
//                            ifTermStartsWithCapital.put(term, false);
//                        }
//                        if (charTerms.containsKey(term)) {
//                            String value = charTerms.get(term);
//                            value = sb.append(value).toString();
//                            charTerms.put(term, value);
//                        } else
//                            charTerms.put(term, sb.toString());
//                        line = segmentFilePartitions[i].readLine();
//                        sb.delete(0, sb.length());
//                        sb.setLength(0);
//                    }
//                }
//            }
//        }
//        System.out.println("Starting to writeTermToPosting");
//        termsPosting.writeToTermsPosting(charTerms, ifTermStartsWithCapital);
//    }
public void appendSegmentPartitionRangeToPostingAndIndexes() {
    //TreeMap<String, String> TermToDocs = new TreeMap<>(new TermComparator()); // <TermContent, list of docs in format: <docNum>,<tf>,<termLocationInDoc>,"#">
    TreeMap<String, String>[] seg_i_arr = new TreeMap[8] ;
    for (int i =0 ; i< seg_i_arr.length ; i++
         ) {
        seg_i_arr[i] = new TreeMap<String , String>();//new TermComparator());
    }


    HashMap<String, Boolean> ifTermStartsWithCapital = new HashMap<>();
    for (int i = 0; i < segmentFilePartitions.length; i++) {
        StringBuilder sb;
        List<String> lines = segmentFilePartitions[i].readAllLines();
        Iterator<String> line_it = lines.iterator() ;
        String line ;
        if ( line_it.hasNext())
            line = line_it.next();
        else line = null ;
        while (line != null ) {
            if (line.contains("<D>")) {
                sb = new StringBuilder();
                String docNo = "";
                line = line.replace("<D>", "");
                String[] docLineSplited = StringUtils.split(line, ",");
                docNo = docLineSplited[0];
                if ( line_it.hasNext())
                line = line_it.next();
                else line = null ;
                while (line != null && !line.contains("<D>")) {
                    String tf = "";
                    String[] termLineSplitedByLocsPar = StringUtils.split(line, "[");
                    int lastIndexOfComma = StringUtils.lastIndexOf(termLineSplitedByLocsPar[0], ",");
                    if (lastIndexOfComma == -1) { // Not a really term (there is no location)
                        if ( line_it.hasNext())
                            line = line_it.next();
                        else line = null ;
                        continue;
                    }
                    termLineSplitedByLocsPar[0] = StringUtils.substring(termLineSplitedByLocsPar[0], 0, lastIndexOfComma); // To get <Term>,<tf>
                    lastIndexOfComma = StringUtils.lastIndexOf(termLineSplitedByLocsPar[0], ",");
                    if (lastIndexOfComma == -1) {
                        if ( line_it.hasNext())
                            line = line_it.next();
                        else line = null ;
                        continue;
                    }
                    String locs = "";
                    if (termLineSplitedByLocsPar.length > 1)
                        locs = "[" + termLineSplitedByLocsPar[1];
                    tf = StringUtils.substring(termLineSplitedByLocsPar[0], lastIndexOfComma + 1); // to get <tf>
                    String term = StringUtils.substring(termLineSplitedByLocsPar[0], 0, lastIndexOfComma); // to get <term>
                    sb.append(docNo).append(",").append(tf).append(",").append(locs).append("#");

                    if (term.charAt(0) == '*' && term.length() > 1) {
                        term = StringUtils.substring(term, 1);
                        if (!ifTermStartsWithCapital.containsKey(term))
                            ifTermStartsWithCapital.put(term, true);
                    } else if (term.charAt(0) != '*') {
                        ifTermStartsWithCapital.put(term, false);
                    }
                    if (seg_i_arr[i].containsKey(term)) {
                        String value = seg_i_arr[i].get(term);
                        value = sb.append(value).toString();
                        seg_i_arr[i].put(term, value);
                    } else
                        seg_i_arr[i].put(term, sb.toString());
                    if ( line_it.hasNext())
                        line = line_it.next();
                    else line = null ;
                    sb.delete(0, sb.length());
                    sb.setLength(0);
                }
            }
        }
    }
    System.out.println("Starting to writeTermToPosting");
    termsPosting.writeToTermsPosting(seg_i_arr, ifTermStartsWithCapital);
}

//    private boolean isRealDoc(String line) {
//        if (line.contains("null"))
//            return false;
//        return true;
//    }

    public static void writeDictionariesToDisc() {
        try {
            Iterator termIt = terms_dictionary.entrySet().iterator();
            int counter = 0;
            while (termIt.hasNext()) {
                Map.Entry pair = (Map.Entry) termIt.next();
                try {
                    termDictionary_bf.append(pair.getKey().toString() + "," + pair.getValue().toString() + "\n");
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
            termDictionary_bf.close();
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
                    citiesDictionary_bf.append(key).append(",").append(cityDetailsFromApi).append(",").append(pair.getValue().toString() + "\n");
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
        s = s.toLowerCase();
        boolean test = CorpusProcessingManager.cities.containsKey(s);
        if (test) {
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

    public static void closeIO() {
        try {
            termDictionary_bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

