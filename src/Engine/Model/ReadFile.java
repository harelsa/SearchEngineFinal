package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class ReadFile {
    public ArrayList<Pair<String, String>> readFile(String filePathName) {
        return generateDocuments(filePathName);
    }
    /*  */
    public ArrayList<Pair<String, String>> read2files(String path1, String path2){
        Charset charset = Charset.forName("UTF-8");
        ArrayList<String> list = new ArrayList<String>();
        try(
                FileInputStream is1=new FileInputStream(path1);
                FileInputStream is2=new FileInputStream(path2);
                SequenceInputStream is=new SequenceInputStream(is1, is2);
                BufferedReader reader=new BufferedReader(new InputStreamReader(is, charset));)
        {
            try {
                String line;
                while((line = reader.readLine()) != null){
                    list.add(line);
                }
            } catch (IOException e) {
                System.err.format("IOException: %s%n", e);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Pair<String, String>> ans = splitDocumentsFromFile(list.toString());
        return splitDocumentsFromFile(list.toString());



//        ArrayList<ArrayList<Pair<String, String>>> docsFrom2Files = new ArrayList<>();
//        docsFrom2Files.add(generateDocuments(path1));
//        docsFrom2Files.add(generateDocuments(path2));
//        return docsFrom2Files;
    }

    /**
     *
     * @param filePathName
     * @return ArrayList of Pairs in with the following format: <DocumentNumber, fullContentOfDocument>
     */
    private ArrayList<Pair<String, String>> generateDocuments(String filePathName) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(filePathName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                return splitDocumentsFromFile(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null; //Need to fix this
    }




    private ArrayList<Pair<String, String>> splitDocumentsFromFile(String fileContent) {
        ArrayList<Pair<String, String>> doNODocument = new ArrayList<>();
        String[] fileDocuments = fileContent.split("</DOC>");
        for (int i = 0; i < fileDocuments.length; i++) {
            String currentFullDocument = fileDocuments[i];
            String docNumber = getDocNumber(currentFullDocument);
            if (docNumber != null){
                doNODocument.add(new Pair<>(docNumber, currentFullDocument));
            }
        }
        return doNODocument;
    }

    private String getDocNumber(String fileDocument) {
        String[] str = fileDocument.split("DOCNO>");
        if (str.length > 1) {
            if (str[1].contains(" "))
                str[1].replaceAll("\\s+","");
            int indexOfArrow = str[1].indexOf("<");
            String docNumber = str[1].substring(0, indexOfArrow);
            return docNumber;
        }
        return null;
    }
}

