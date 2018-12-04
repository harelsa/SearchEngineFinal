package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

public class SegmentFilePartition implements Serializable {
    private BufferedWriter file_buffer_writer;
    private BufferedReader file_buffer_reader;

    public SegmentFilePartition(String path, char from, char to) {
        String segmantPartitionFilePath = path + "_" + from + "_" + "to" + "_" + to + ".txt";
        try {

            file_buffer_writer = new BufferedWriter(new FileWriter(segmantPartitionFilePath));
            file_buffer_reader = new BufferedReader(new FileReader(segmantPartitionFilePath));

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    synchronized public void signNewTerm(Term term) {
        try {
            file_buffer_writer.append(term.lightToString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine(){
        String line;
        try {
            if ((line = file_buffer_reader.readLine()) != null){
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void signDocSection(Document currDoc) {
        try {
            file_buffer_writer.append("<D>" + currDoc.lightToString() +"</D>" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//
//}

}
