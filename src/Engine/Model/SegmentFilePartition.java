package Engine.Model;

import javafx.util.Pair;

import javax.print.Doc;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.SortedMap;

public class SegmentFilePartition implements Serializable {
    Hashtable<Character, Integer> prefixCharLastWordPositionInFile;
    FileOutputStream f_os;
    FileInputStream f_is;
    ObjectOutputStream o_os;
    ObjectInputStream o_is;
    BufferedWriter writer;
    BufferedReader reader;
    private String segmantPartitionFilePath;

    public SegmentFilePartition(String path, char from, char to) {
        segmantPartitionFilePath = path + "_" + from + "_" + "to" + "_" + to + ".txt";
        try {
            f_os = new FileOutputStream(new File(segmantPartitionFilePath));
            o_os = new ObjectOutputStream(f_os);

            f_is = new FileInputStream(new File(segmantPartitionFilePath));
            o_is = new ObjectInputStream(f_is);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    synchronized public void signNewTerm(Term term, Document doc) {
        Pair<Term, Document> object = new Pair<>(term, doc);
        try {
            o_os.writeObject(object);
//            o_os.flush();
//            System.out.println("The following term added to: " + segmantPartitionFilePath); //for test
//            System.out.println(term.toString()); //for test
        } catch (IOException e) {
            e.printStackTrace();
        }
//        segmentTerms.put(term, doc);
    }

    synchronized public Pair<Term, Document> extractTermFromSegmentFilePartition() {
        Pair<Term,Document> pr1 = null;
        try {
            pr1 = (Pair<Term,Document>) o_is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

//        System.out.println(pr1.toString()); //for test
        return pr1;
    }

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
