package Engine.Model;
/**
 * This class represents a Segment File Partition.
 * In fact, this department manages the writing and reading from an exclusive text file intended to hold information in the following format: DocDetails, Term1, Term2, Term3, ...
 * The terms that are held are terms within the alphabetic range defined for the specific partition instance.
 */

import java.io.*;


public class SegmentFilePartition implements Serializable {
    private BufferedWriter file_buffer_writer;
    private BufferedReader file_buffer_reader;
    private int counter;

    public SegmentFilePartition(String path, char from, char to) {
        String segmantPartitionFilePath = path + "_" + from + "_" + "to" + "_" + to + ".txt";
        try {

            file_buffer_writer = new BufferedWriter(new FileWriter(segmantPartitionFilePath));
            file_buffer_reader = new BufferedReader(new FileReader(segmantPartitionFilePath));

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    synchronized public void signNewTerm(Term term) {
        try {
            file_buffer_writer.append(term.lightToString()).append("\n");
            counter++;
            if (counter > 13000) {
                file_buffer_writer.flush();
                counter = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine() {
        String line;
        try {
            if ((line = file_buffer_reader.readLine()) != null) {
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void signDocSection(Document currDoc) {
        try {
            file_buffer_writer.append("<D>").append(currDoc.lightToString()).append("</D>").append("\n");
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
}
