package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

public class SegmentFilePartition implements Serializable {
    private BufferedWriter file_buffer_writer;
    private BufferedReader file_buffer_reader;
    private int counter;
    private char partitionChar;

    public SegmentFilePartition(String path, char partitionChar) {
        String segmantPartitionFilePath = path + "_" + partitionChar + ".txt";
        this.partitionChar = partitionChar;
        try {
            file_buffer_writer = new BufferedWriter(new FileWriter(segmantPartitionFilePath));
            file_buffer_reader = new BufferedReader(new FileReader(segmantPartitionFilePath));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public char getPartitionChar() {
        return partitionChar;
    }

    synchronized public void signNewTerm(Term term) {
        try {
            file_buffer_writer.append(term.lightToString() + "\n");
            counter++;
            if (counter > 13000){
                file_buffer_writer.flush();
                counter = 0;
            }
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

    public void flushFile() {
        try {
            file_buffer_writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeBuffers() {
        try {
            file_buffer_writer.close();
            file_buffer_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//
//}

}
