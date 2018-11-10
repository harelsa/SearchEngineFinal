package Engine.Model;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class ReadFile {
    synchronized public ArrayList<Pair<String, String>> readFile(String filePathName) {
        return generateDocuments(filePathName);
    }

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

