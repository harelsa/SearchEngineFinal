package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

public class SegmentFilePartition implements Serializable {
    private BufferedWriter file_buffer_writer;
    private BufferedReader file_buffer_reader;
    private int counter;
    private int chunk_num;
    private String path_u ;

    public SegmentFilePartition(String path, int chunk_num) {
        this.chunk_num = chunk_num;
        String segmantPartitionFilePath = path+"\\Segment Files\\seg" + "_" + this.chunk_num + ".txt";
        File newFile = new File(segmantPartitionFilePath );
        try {
            //newFile.getParentFile().mkdirs();
            newFile.createNewFile();
        }
        catch (Exception e ){}
        this.path_u = segmantPartitionFilePath ;
        //this.partitionChar = partitionChar;
        try {
            file_buffer_writer = new BufferedWriter(new FileWriter(segmantPartitionFilePath));
            file_buffer_reader = new BufferedReader(new FileReader(segmantPartitionFilePath));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

   // public int getPartitionChar() {
//        return chunk_num;
//    }

    synchronized public void signNewTerm(String term , StringBuilder sb) {
        try {
            file_buffer_writer.append(term + "\n");
            file_buffer_writer.append(sb.toString() + "\n");
            counter++;
            if (counter > 1000){
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

    public List<String> readAllLines(){
        try {
            System.out.println(path_u);
        List<String> lines = Files.readAllLines(Paths.get(path_u), StandardCharsets.UTF_8);
        return lines ;
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
            if (file_buffer_writer != null)
                file_buffer_writer.close();
            if (file_buffer_reader != null)
                file_buffer_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        return  path_u ;
    }

//
//}

}
