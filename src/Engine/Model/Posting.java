package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.util.*;


public class Posting {
    private final int NUM_OF_PARTITATIONS = 7;
    private String postingPath;
    private BufferedWriter file_buffer_writer;
    private BufferedReader file_buffer_reader;
    public Posting() {
    }

    public Posting(String s) {
        postingPath = s + ".txt";
        try {
            file_buffer_writer = new BufferedWriter(new FileWriter(postingPath));
            file_buffer_reader = new BufferedReader(new FileReader(postingPath));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }

    public void addTermPostingEntry(){

    }

    public void addChunkOfTermsPostingEntry(){

    }


    public void addPostingEntry(Term term, Document doc) {
    }

    public void writeToPosting(TreeMap<String, String> termsDoc) {
        Iterator it = termsDoc.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            try {
                file_buffer_writer.append(pair.getKey().toString() + '\n');
                file_buffer_writer.append(pair.getValue().toString() + '\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}


