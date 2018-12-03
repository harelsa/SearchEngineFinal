package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;

public class SegmentFilePartition implements Serializable {
    Hashtable<Character, Integer> prefixCharLastWordPositionInFile;
    FileOutputStream f_os;
    FileInputStream f_is;
    ObjectOutputStream o_os;
    ObjectInputStream o_is;
    BufferedWriter file_buffer_writer;
    BufferedReader file_buffer_reader;
    BufferedOutputStream file_buffer_output;
    BufferedWriter o_buf_os;
    BufferedReader f_buf_is;
    BufferedReader o_buf_is;
    private String segmantPartitionFilePath;

    //inputStream = new BufferedReader(new FileReader("xanadu.txt"));
    //outputStream = new BufferedWriter(new FileWriter("characteroutput.txt"));

    public SegmentFilePartition(String path, char from, char to) {
        segmantPartitionFilePath = path + "_" + from + "_" + "to" + "_" + to + ".txt";
        try {
//            Writer file_os_writer = new OutputStreamWriter(new ObjectOutputStream(
//                    new FileOutputStream(
//                            new File(segmantPartitionFilePath))));
            //ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                                                                    //new File(segmantPartitionFilePath)));
            //file_buffer_output = new BufferedOutputStream(oos);
            file_buffer_writer = new BufferedWriter(new FileWriter(segmantPartitionFilePath));
            file_buffer_reader = new BufferedReader(new FileReader(segmantPartitionFilePath));

            //file_buffer_output = new BufferedOutputStream(new FileOutputStream(new File(segmantPartitionFilePath)));

            f_os = new FileOutputStream(new File(segmantPartitionFilePath));
            o_os = new ObjectOutputStream(f_os);
           // Writer w2 = new

            f_is = new FileInputStream(new File(segmantPartitionFilePath));
            o_is = new ObjectInputStream(f_is);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    synchronized public void signNewTerm(Term term) {
        //System.out.println("aa");
        try {
            file_buffer_writer.write(term.lightToString() + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
//        segmentTerms.put(term, doc);
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

//    synchronized public HashMap<String,String> extractDocsTermsFromSegmentFile() {
//        StringBuilder sb = new StringBuilder();
//        try {
//            String line = "";
//            if ((line = file_buffer_reader.readLine()) != null)
//                sb.append(file_buffer_reader.readLine());
//            else
//                return null;
//            while ((line = file_buffer_reader.readLine()) != null && !(line = file_buffer_reader.readLine()).contains("<D>")) {
//                sb.append(file_buffer_reader.readLine());
//            }
//            file_buffer_reader.readLine();
//            return sb.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sb.toString();
//    }


    public void signDocSection(Document currDoc) {
        try {
            file_buffer_writer.write("<D>" + currDoc.lightToString() +"</D>" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   //public String readWholePartition() {
   //    StringBuilder sb = new StringBuilder();
   //    try {
   //        String line = "";
   //        while ((line = file_buffer_reader.readLine()) != null){
   //            sb.append(line);
   //            file_buffer_reader.readLine();
   //        }
   //    } catch (IOException e) {
   //        e.printStackTrace();
   //    }
   //}

//    Charset charset = Charset.forName("UTF-8");
//    ArrayList<String> list = new ArrayList<String>();
//
//        // Write objects to file
//        o.writeObject(p1);
//        o.writeObject(p2);
//
//        o.close();
//        f.close();
//
//        FileInputStream fi = new FileInputStream(new File("myObjects.txt"));
//        ObjectInputStream oi = new ObjectInputStream(fi);
//
//        // Read objects
//        Person pr1 = (Person) oi.readObject();
//        Person pr2 = (Person) oi.readObject();
//
//        System.out.println(pr1.toString());
//        System.out.println(pr2.toString());
//
//        oi.close();
//        fi.close();
//
//    } catch (FileNotFoundException e) {
//        System.out.println("File not found");
//    } catch (IOException e) {
//        System.out.println("Error initializing stream");
//    } catch (ClassNotFoundException e) {
//        e.printStackTrace();
//    }
//
//}

}
