package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class Indexer {
    public static TreeMap<String, String> terms_dictionary;
    public static TreeMap<String, String> cities_dictionary;
    public static TreeMap<String, Integer> docs_dictionary;
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
        docs_dictionary = new TreeMap<>(new DocComparator());
        staticPostingsPath = postingPath;
    }

    private ArrayList<String> chunksPath;
    private String[] chunksCurrLines;
    private BufferedReader[] segsReaders;
    private Posting termsPosting;
    private Posting docsPosting;
    private HashMap<String, Boolean> ifTermStartsWithCapital;


    public Indexer(Posting termsPostingFile) {
        termsPosting = termsPostingFile;
        //docsPosting = docsPostingFile;
    }

    public void appendSegmentPartitionRangeToPostingAndIndexes() throws FileNotFoundException {
        chunksPath = getChunkPath();
        chunksCurrLines = new String[chunksPath.size()];
        segsReaders = new BufferedReader[chunksPath.size()];
        TreeMap<String, String> termToDocs = new TreeMap<>(new TermComparator());
        ifTermStartsWithCapital = new HashMap<>();
        for (int i = 0; i < segsReaders.length; i++) {
            segsReaders[i] = new BufferedReader(new FileReader(chunksPath.get(i)));
            try {
                chunksCurrLines[i] = segsReaders[i].readLine(); // each readers reads it's first term.
                if (chunksCurrLines[i].charAt(0) == '*' && chunksCurrLines[i].length() > 1) {
                    chunksCurrLines[i] = StringUtils.substring(chunksCurrLines[i], 1);
                    if (!ifTermStartsWithCapital.containsKey(chunksCurrLines[i]))
                        ifTermStartsWithCapital.put(chunksCurrLines[i], true);
                } else if (chunksCurrLines[i].charAt(0) != '*') {
                    ifTermStartsWithCapital.put(chunksCurrLines[i], false);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (!finishToRead()) {
            termToDocs.clear();
            while (termToDocs.size() < 2500 || isContainsTheNextMin(termToDocs)) {
                String minTermDetails = getMinimum(chunksCurrLines);
                if (minTermDetails.contains("null"))
                    break;
                int termIsUntilIndex = StringUtils.indexOf(minTermDetails, "@");
                String term = StringUtils.substring(minTermDetails, 0, termIsUntilIndex);
//                if (term.charAt(0) == 'z')
//                    System.out.println("z");

                String listOfDocs = StringUtils.substring(minTermDetails, termIsUntilIndex + 1);
                if (termToDocs.containsKey(term)) {
                    String curValue = termToDocs.get(term);
                    String newValue = curValue + listOfDocs;
                    termToDocs.put(term, newValue);
                } else
                    termToDocs.put(term, listOfDocs);
            }
            termsPosting.writeToTermsPosting(termToDocs, ifTermStartsWithCapital);
            ifTermStartsWithCapital.clear();
            ifTermStartsWithCapital = new HashMap<>();
        }
        for (int i = 0; i < segsReaders.length; i++) {
            try {
                segsReaders[i].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isContainsTheNextMin(TreeMap<String, String> termToDocs) {
        for (int i = 0; i < chunksCurrLines.length; i++) {
            if (termToDocs.containsKey(chunksCurrLines[i]))
                return true;
        }
        return false;
    }

    private String getMinimum(String[] chunksCurrLines) {
        String docsOfTerm = "";
        String minSoFar = ""; // The term it self
        for (int i = 0; i < chunksCurrLines.length; i++) {
            if (chunksCurrLines[i] != null && !chunksCurrLines[i].equals("")) {
                minSoFar = chunksCurrLines[i];
                break;
            }
        }
        int indexOfMin = 0;
        for (int i = 0; i < chunksCurrLines.length; i++) {
            if (chunksCurrLines[i] != null && minSoFar != null && chunksCurrLines[i].compareTo(minSoFar) < 1) {
                minSoFar = chunksCurrLines[i];
                indexOfMin = i;
            }
        }
        try {
            docsOfTerm = segsReaders[indexOfMin].readLine();  // The docs which contains the minimum term
            String nextTerm = segsReaders[indexOfMin].readLine(); // Hold the next term
            chunksCurrLines[indexOfMin] = nextTerm;
            if (chunksCurrLines[indexOfMin] != null) {
                if (nextTerm.charAt(0) == '*' && nextTerm.length() > 1) {
                    nextTerm = StringUtils.substring(nextTerm, 1);
                    chunksCurrLines[indexOfMin] = nextTerm;
                    if (!ifTermStartsWithCapital.containsKey(nextTerm))
                        ifTermStartsWithCapital.put(nextTerm, true);
                } else if (nextTerm.charAt(0) != '*') {
                    ifTermStartsWithCapital.put(nextTerm, false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return minSoFar + "@" + docsOfTerm; // <Term>"@"<ListOfDocs>
    }

    private boolean finishToRead() {
        for (int i = 0; i < chunksCurrLines.length; i++) {
            if (chunksCurrLines[i] != null)
                return false;
        }
        return true;
    }


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
                termDictionary_bf.flush();
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


    synchronized public static void addNewDocToDocDictionary(String docNo, int docValue) {
        docs_dictionary.put(docNo, docValue);
    }

    public ArrayList<String> getChunkPath() {
        ArrayList<String> filesPathsList = new ArrayList<>();
        final File folder = new File(staticPostingsPath + "\\Segment Files");
        for (final File fileEntry : folder.listFiles())
            filesPathsList.add(fileEntry.getPath());
        return filesPathsList;
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



