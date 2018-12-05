package Engine.Model;

import org.apache.commons.lang3.tuple.Pair;

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
    private static BufferedReader documents_buffer_reader = new BufferedReader(docPosting_fr);


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
    synchronized public static void writeToDocumentsPosting(Document doc, SortedMap<String, Term> sm){
        StringBuilder listOfTerms = new StringBuilder();
        listOfTerms.append("#");
        for (String term : sm.keySet()) {
            listOfTerms.append(sm.get(term).postingToString()).append("#");
        }
        StringBuilder docValueInDictionary = new StringBuilder();
        docValueInDictionary.append(doc.getParentFileName()).append(",").append(doc.getFreqTermContent()).
                append(",").append(doc.getMaxTF()).append(",").append(doc.getNumOfUniqueTerms()).append(",").append(docsPointer);
        docsPointer += 2;
        Indexer.addNewDocToDocDictionary(doc.getDocNo(), docValueInDictionary.toString());
        try {
            documents_buffer_writer.append(doc.getDocNo() + "\n");
            documents_buffer_writer.append(listOfTerms + "\n");
            if (docsCounter > 7000){
                documents_buffer_writer.flush();
                docsCounter = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // public void writeToTermsPosting(TreeMap[] termDocs) {
    //     Iterator[] itArr = new Iterator[termDocs.length];
    //     for (int i = 0; i < termDocs.length; i++)
    //         itArr[i] = termDocs[i].entrySet().iterator();
    //     Iterator it = termDocs.entrySet().iterator();
    //     int pointer = 1;
    //     while (hasNext(itArr)) {
    //         Map.Entry pair = (Map.Entry) it.;
    //         try {
    //             String listOfTermDocs = pair.getValue().toString();
    //             int df = getDf(listOfTermDocs);
    //             String key = pair.getKey().toString();
    //             String currDicValue = Indexer.terms_dictionary.get(key);
    //             Indexer.terms_dictionary.put(key, currDicValue + df + "," + pointer);
    //             pointer += 2;
    //             terms_buffer_writer.append(key + '\n');
    //             terms_buffer_writer.append(listOfTermDocs + '\n');
    //             if (pointer > 3000)
    //                 terms_buffer_writer.flush();
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //         it.remove(); // avoids a ConcurrentModificationException
    //     }
    //     try {
    //         terms_buffer_writer.flush();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }


    public void writeToTermsPosting(TreeMap[] termDocs) {
       //char[] range = getRange(termDocs[0]);
       //TreeMap<String, String> tmp;
       //Iterator[] itArr = new Iterator[termDocs.length];
       //boolean[] itArrUsedAlready = new boolean[termDocs.length];
       //for (int i = 0; i < termDocs.length; i++)
       //    itArrUsedAlready[i] = false;
       //for (int i = 0; i < termDocs.length; i++)
       //    itArr[i] = termDocs[i].entrySet().iterator();
       //int pointer = 1;
       //while (hasNext(itArr)) {
       //    for (int i = 0; i < 6000; i++) {
       //        //while(get)
       //       // Map.Entry pair = getMin(itArr, itArrUsedAlready);
       //    }
       //    //Map.Entry pair = (Map.Entry) it.;
       //    try {
       //        String listOfTermDocs = pair.getValue().toString();
       //        int df = getDf(listOfTermDocs);
       //        String key = pair.getKey().toString();
       //        String currDicValue = Indexer.terms_dictionary.get(key);
       //        Indexer.terms_dictionary.put(key, currDicValue + df + "," + pointer);
       //        pointer += 2;
       //        terms_buffer_writer.append(key + '\n');
       //        terms_buffer_writer.append(listOfTermDocs + '\n');
       //        if (pointer > 3000)
       //            terms_buffer_writer.flush();
       //    } catch (IOException e) {
       //        e.printStackTrace();
       //    }
       //    it.remove(); // avoids a ConcurrentModificationException
       //}
       //try {
       //    terms_buffer_writer.flush();
       //} catch (IOException e) {
       //    e.printStackTrace();
       //}
    }

    private char[] getRange(TreeMap termDoc) {
        char[] ans = new char[2];
        Iterator it = termDoc.entrySet().iterator();
        if (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            String key = pair.getKey().toString();
            if (key.charAt(0) == '*'){
                key = key.substring(1);
            }
            if (key.charAt(0) == '0'){
                ans[0] = '0';
                ans[1] = '9';
            }
            else if (key.charAt(0) == 'a'){
                ans[0] = 'a';
                ans[1] = 'a';
            }

        }
        return null;
    }

//    private Map.Entry getMin(Iterator[] itArr, boolean[] itArrUsedAlready) {
//        Map.Entry minPair;
//
//    }


    private boolean hasNext(Iterator[] itArr) {
        for (Iterator anItArr : itArr) {
            if (anItArr.hasNext())
                return true;
        }
        return false;
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



