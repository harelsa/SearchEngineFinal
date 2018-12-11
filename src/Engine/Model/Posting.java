package Engine.Model;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;


public class Posting {
    private String termsPostingPath;
    private BufferedWriter terms_buffer_writer;
    private static int docsPointer = 1;
    private static int docsCounter = 0;
    private static BufferedWriter documents_buffer_writer;

    public Posting(String postingsPath) {
        this.termsPostingPath = postingsPath + ".txt";
        //this.documentsPostingPath = documentsPostingPath + ".txt";
        try {
            terms_buffer_writer = new BufferedWriter(new FileWriter(this.termsPostingPath));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void initPosting(String postingPath) {
        try {
            documents_buffer_writer = new BufferedWriter(new FileWriter(postingPath + "\\docsPosting.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static void writeToDocumentsPosting(Document doc, SortedMap<String, Term> sm) {
        StringBuilder listOfTerms = new StringBuilder();
        listOfTerms.append("#");
        for (String term : sm.keySet()) {
            listOfTerms.append(sm.get(term).postingToString()).append("#");
        }
        StringBuilder docValueInDictionary = new StringBuilder();
        docValueInDictionary.append(doc.getParentFileName()).append(",").append(doc.getLang()).append(",").append(doc.getCity()).append(",").append(doc.getFreqTermContent()).
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

//    public void writeToTermsPosting(TreeMap<String, String>[] seg_i_arr, HashMap<String, Boolean> ifTermStartsWithCapital) {
//        Iterator iterator = termDocs.entrySet().iterator();
//        int pointer = 1;
//        int counter = 0;
//        while (iterator.hasNext()) {
//            Map.Entry pair = (Map.Entry) iterator.next();
//            String key = pair.getKey().toString(); // term
//            String listOfDocs = pair.getValue().toString(); // list of docs
//            String termDetails = getMostFreqDocAndTotalTf(listOfDocs); // "<D>"<DOC-NO>","<MaxTf>","<TotalTf>,<df>
//            String[] termDetailsSplited = StringUtils.split(termDetails, ",");
//            int df = Integer.parseInt(termDetailsSplited[termDetailsSplited.length - 1]);
//            int totalTf = Integer.parseInt(termDetailsSplited[termDetailsSplited.length - 2]);
//            // Filtering low tf & df terms
//            if ((df == 1 && totalTf < 3)) {
//                continue;
//            }
//
//            if (ifTermStartsWithCapital.containsKey(key) && ifTermStartsWithCapital.get(key))
//                key = key.toUpperCase();
//            Indexer.terms_dictionary.put(key, termDetails + "," + pointer);
//            pointer += 2;
//            if (CorpusProcessingManager.cities.containsKey(key.toLowerCase())){
//                Indexer.cities_dictionary.put(key, listOfDocs);
//            }
//            //Indexer.terms_dictionary.put(key, currDicValue + df + "," + pointer);
//            try {
//                terms_buffer_writer.append(key).append('\n');
//                counter++;
//                terms_buffer_writer.append(listOfDocs).append(String.valueOf('\n'));
//                counter++;
//                if (counter > 3000) {
//                    terms_buffer_writer.flush();
//                    counter = 0;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void writeToTermsPosting(TreeMap<String, String>[] seg_i_arr, HashMap<String, Boolean> ifTermStartsWithCapital) {

        TreeMap< String , String  > small_tree = new TreeMap<String, String>() ;
        HashMap< String , Integer > which_seg = new HashMap<> ();
        //int j = 0 ;
        //insert first 8 min values
        for (int j = 0 ; j < seg_i_arr.length ;
             ) {
            Map.Entry<String , String> curr_ent = seg_i_arr[j].firstEntry();
            if ( curr_ent == null ) {
                j++;
                continue;
            }
            seg_i_arr[j].remove(curr_ent.getKey()) ;
            if (small_tree.containsKey(curr_ent.getKey())) {
                StringBuilder sb  = new StringBuilder(curr_ent.getValue());
                String value = sb.append(small_tree.get(curr_ent.getKey())).toString();
                small_tree.put( curr_ent.getKey(), value);
            } else {
                small_tree.put(curr_ent.getKey() , curr_ent.getValue()) ;
                which_seg.put ( curr_ent.getKey() , j) ;
                j++;
            }
        }
        // now we will write the min each time and insert a new min
        int pointer = 1;
        int counter = 0;
        while (!small_tree.isEmpty()) {
            Map.Entry<String, String> curr_ent = small_tree.firstEntry();
            //check all the other segs
            for (int j = 0; j < seg_i_arr.length; j++
            ) {
                //add to small tree before writing and remover from all trees
                if ( seg_i_arr[j] == null )continue;
                if (seg_i_arr[j].containsKey(curr_ent.getKey())) {
                    String val = seg_i_arr[j].get(curr_ent.getKey());
                    seg_i_arr[j].remove(curr_ent.getKey());
                    StringBuilder sb = new StringBuilder(val);
                    val = sb.append(small_tree.get(curr_ent.getKey())).toString();
                    small_tree.put(curr_ent.getKey(), val);
                }
            }
            //write to disk
            Map.Entry pair = small_tree.firstEntry();
            small_tree.remove(pair.getKey());

            String key = pair.getKey().toString(); // term
            String listOfDocs = pair.getValue().toString(); // list of docs
            String termDetails = "" ;
            if ( listOfDocs.length() > 0 )
            termDetails = getMostFreqDocAndTotalTf(listOfDocs); // "<D>"<DOC-NO>","<MaxTf>","<TotalTf>,<df>
            String[] termDetailsSplited = StringUtils.split(termDetails, ",");
            int df = 0 ;  int totalTf = 0 ;
            if ( termDetailsSplited.length > 1) {
                 df = Integer.parseInt(termDetailsSplited[termDetailsSplited.length - 1]);
                totalTf = Integer.parseInt(termDetailsSplited[termDetailsSplited.length - 2]);
            }
            // Filtering low tf & df terms
            if (!(df == 1 && totalTf < 3)) {
                if (ifTermStartsWithCapital.containsKey(key) && ifTermStartsWithCapital.get(key))
                    key = key.toUpperCase();
                Indexer.terms_dictionary.put(key, termDetails + "," + pointer);
                pointer += 2;
                if (CorpusProcessingManager.cities.containsKey(key.toLowerCase())){
                    Indexer.cities_dictionary.put(key, listOfDocs);
                }
                //Indexer.terms_dictionary.put(key, currDicValue + df + "," + pointer);
                try {
                    terms_buffer_writer.append(key).append('\n');
                    counter++;
                    terms_buffer_writer.append(listOfDocs).append(String.valueOf('\n'));
                    counter++;
                    if (counter > 500) {
                        terms_buffer_writer.flush();
                        System.out.println("3000 writen");
                        counter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //insert back next term from seg
            int which = which_seg.get(pair.getKey());
            // add next from i
           while( !seg_i_arr[which].isEmpty()){
                Map.Entry<String , String> entry = seg_i_arr[which].firstEntry();
                seg_i_arr[which].remove(entry.getKey()) ;
                if (small_tree.containsKey(entry.getKey())) {
                    StringBuilder sb  = new StringBuilder(entry.getValue());
                    String value = sb.append(small_tree.get(entry.getKey())).toString();
                    small_tree.put( entry.getKey(), value);
                } else {
                    small_tree.put( entry.getKey() , curr_ent.getValue()) ;
                    which_seg.put ( entry.getKey() , which) ;
                    break ;
                }
            }
            which_seg.remove(pair.getKey());
        }
        try {
            terms_buffer_writer.flush();
        }
        catch (Exception e ){}
        System.out.println( "done writing to disk letter!" ) ;
    }

    private String getMostFreqDocAndTotalTf(String listOfTermDocs) { // return "<D>"<DOC-NO>","<MaxTf>","<TotalTf>,<df>
        String[] docs = StringUtils.split(listOfTermDocs, "#");
        int df = getDf(listOfTermDocs);
        int maxTf = 0;
        int totalTf = 0;
        String docNoOfMax = "";
        for (int i = 0; i < docs.length; i++) {
            String[] splited = StringUtils.split(docs[i], ",");
            int tmp = Integer.parseInt(splited[1]);
            totalTf += tmp;
            if (tmp > maxTf) {
                maxTf = tmp;
                docNoOfMax = splited[0];
            }
        }
        return docNoOfMax + "," + maxTf + "," + totalTf + "," + df;
    }



    private int getDf(String listOfTermDocs) {
        int count = 0;

        for (int i = 0; i < listOfTermDocs.length(); i++) {
            if (listOfTermDocs.charAt(i) == '#')
                count++;
        }

        return count;
    }

    public static void closeIO() {
        try {
            documents_buffer_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



