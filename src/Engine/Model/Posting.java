package Engine.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;


public class Posting {
    private String termsPostingPath;
    private String documentsPostingPath;
    private BufferedWriter terms_buffer_writer;
    private BufferedReader terms_buffer_reader;
    private static int docsPointer = 1;
    private static int docsCounter = 0;
    private static FileWriter docPosting_fw;
    private static FileReader docPosting_fr;

    static {
        try {
            docPosting_fw = new FileWriter("src\\Engine\\resources\\Posting Files\\Docs\\Merged.txt");
            docPosting_fr = new FileReader("src\\Engine\\resources\\Posting Files\\Docs\\Merged.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedWriter documents_buffer_writer = new BufferedWriter(docPosting_fw);
    //private static BufferedReader documents_buffer_reader = new BufferedReader(docPosting_fr);


    public Posting(String termsPostingPath) {
        this.termsPostingPath = termsPostingPath + ".txt";
        this.documentsPostingPath = documentsPostingPath + ".txt";
        try {
            terms_buffer_writer = new BufferedWriter(new FileWriter(this.termsPostingPath));
            terms_buffer_reader = new BufferedReader(new FileReader(this.termsPostingPath));
//            documents_buffer_writer = new BufferedWriter(docDictionary_fw);
//            documents_buffer_reader = new BufferedReader(docDictionary_fr);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }

    synchronized public static void writeToDocumentsPosting(Document doc, SortedMap<String, Term> sm) {
        StringBuilder listOfTerms = new StringBuilder();
        listOfTerms.append("#");
        for (String term : sm.keySet()) {
            listOfTerms.append(sm.get(term).postingToString()).append("#");
        }
        StringBuilder docValueInDictionary = new StringBuilder();
        docValueInDictionary.append(doc.getParentFileName()).append(",").append(doc.getCity()).append(",").append(doc.getFreqTermContent()).
                append(",").append(doc.getMaxTF()).append(",").append(doc.getNumOfUniqueTerms()).append(",").append(docsPointer);
        docsPointer += 2;
        Indexer.addNewDocToDocDictionary(doc.getDocNo(), docValueInDictionary.toString());
        try {
            documents_buffer_writer.append(doc.getDocNo() + "\n");
            documents_buffer_writer.append(listOfTerms + "\n");
            if (docsCounter > 7000) {
                documents_buffer_writer.flush();
                docsCounter = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToTermsPosting(TreeMap[] termDocs) {
        Iterator[] iterators = new Iterator[termDocs.length];
        for (int i = 0; i < termDocs.length; i++) {
            iterators[i] = termDocs[i].entrySet().iterator();
        }

        String[] range = getRange(termDocs[0]);
        int pointer = 1;
        int counter = 0;
        TreeMap<String, String> tmp;
        for (int i = 0; i < range.length; i++) {
            HashMap<String, Boolean> ifTermStartsWithCapital = new HashMap<>();
            tmp = new TreeMap<>(new Indexer.TermComparator());

            for (int j = 0; j < termDocs.length; j++) {
                Iterator it = iterators[j];
                Map.Entry pair = (Map.Entry) it.next();
                String key = pair.getKey().toString();
                String value = pair.getValue().toString();

                while (firstChar(key).equals(range[i])) {
                    if (key.charAt(0) == '*' && key.length() > 1) {
                        key = StringUtils.substring(key, 1);
                        if (!ifTermStartsWithCapital.containsKey(key))
                            ifTermStartsWithCapital.put(StringUtils.substring(key, 1), true);
                    }

                    else if (key.charAt(0) != '*'){
                        ifTermStartsWithCapital.put(key, false);
                    }

                    if (tmp.containsKey(key)) {
                        String curValue = tmp.get(key);
                        tmp.put(key, curValue + value);
                    } else {
                        tmp.put(key, value);
                    }
                    if (it.hasNext()){
                        pair = (Map.Entry)it.next();
                        key = pair.getKey().toString();
                        value = pair.getValue().toString();
                    }
                    else
                        break;
                }
                //termDocs[j] = termDocs[j].subMap(key, true, termDocs[j].lastKey(), true);
            }

            Iterator it = tmp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String listOfTermDocs = pair.getValue().toString();
                String docMostTermFreq = getMostFreqDoc(listOfTermDocs);
                String[] splitedValue = StringUtils.split(docMostTermFreq, ",");
                int tf = Integer.parseInt(splitedValue[1]);
                int df = getDf(listOfTermDocs);
                String key = pair.getKey().toString();

                //String currDicValue = Indexer.terms_dictionary.get(key);

                // Filtering low tf & df terms
                if ((df == 1 && tf < 3) || (key.length() == 1 && !Character.isDigit(key.charAt(i)))){
                    continue;
                }

                if (key.charAt(0) == '*' && key.length() > 1){
                    key = StringUtils.substring(key, 1);
                    if (ifTermStartsWithCapital.containsKey(key) && ifTermStartsWithCapital.get(key)){
                        key = key.toUpperCase();
                        Indexer.terms_dictionary.put(key, docMostTermFreq + "," + df + "," + pointer);
                    }
                }
                else
                    Indexer.terms_dictionary.put(key, docMostTermFreq + "," + df + "," + pointer);

                //Indexer.terms_dictionary.put(key, currDicValue + df + "," + pointer);
                pointer += 2;
                try {
                    terms_buffer_writer.append(key).append(String.valueOf('\n'));
                    counter++;
                    terms_buffer_writer.append(listOfTermDocs).append(String.valueOf('\n'));
                    counter++;
                    if (counter > 3000) {
                        terms_buffer_writer.flush();
                        counter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                terms_buffer_writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMostFreqDoc(String listOfTermDocs) {
        String[] docs = StringUtils.split(listOfTermDocs, "#");
        int maxTf = 0;
        String docNoOfMax = "";
        for (int i = 0; i < docs.length; i++) {
            String[] splited = StringUtils.split(docs[i], ",");
            int tmp =  Integer.parseInt(splited[1]);
            if (tmp > maxTf){
                maxTf = tmp;
                docNoOfMax = splited[0];
            }
        }
        return docNoOfMax + "," + maxTf;
    }


    private String firstChar(String key) {
        if (key.charAt(0) == '*') {
            key = key.substring(1);
        }
        return key.substring(0, 1);
    }

    private String[] getRange(TreeMap termDoc) {
        String[] ans = null;
        Iterator it = termDoc.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = pair.getKey().toString();
            if (key.charAt(0) == '*') {
                key = key.substring(1);
            }
            char firstChar = key.charAt(0);
            if (firstChar == '0') {
                ans = new String[10];
                ans[0] = "0";
                ans[1] = "1";
                ans[2] = "2";
                ans[3] = "3";
                ans[4] = "4";
                ans[5] = "5";
                ans[6] = "6";
                ans[7] = "7";
                ans[8] = "8";
                ans[9] = "9";
            } else if (firstChar == 'a') {
                ans = new String[3];
                ans[0] = "a";
                ans[1] = "b";
                ans[2] = "c";
            } else if (firstChar == 'd') {
                ans = new String[3];
                ans[0] = "d";
                ans[1] = "e";
                ans[2] = "f";
            } else if (firstChar == 'g') {
                ans = new String[5];
                ans[0] = "g";
                ans[1] = "h";
                ans[2] = "i";
                ans[3] = "j";
                ans[4] = "k";
            } else if (firstChar == 'l') {
                ans = new String[5];
                ans[0] = "l";
                ans[1] = "m";
                ans[2] = "n";
                ans[3] = "o";
                ans[4] = "p";
            } else if (firstChar == 'q') {
                ans = new String[10];
                ans[0] = "q";
                ans[1] = "r";
                ans[2] = "s";
                ans[3] = "t";
                ans[4] = "u";
                ans[5] = "v";
                ans[6] = "w";
                ans[7] = "x";
                ans[8] = "y";
                ans[9] = "z";
            }

        }
        return ans;
    }


    private int getDf(String listOfTermDocs) {

        int count = 0;

        for (int i = 0; i < listOfTermDocs.length(); i++) {
            if (listOfTermDocs.charAt(i) == '#')
                count++;
        }

        return count;
    }

}



