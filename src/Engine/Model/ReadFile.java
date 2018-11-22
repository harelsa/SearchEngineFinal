package Engine.Model;

import javafx.util.Pair;

import javax.print.Doc;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class ReadFile {
    ExecutorService executor;

    public ReadFile() {
        executor = Executors.newFixedThreadPool(8);
    }


    public void readAndParseLineByLine(String filePathName, Parse parser) {
        BufferedReader br = null;
        System.out.println(filePathName);
        try {
            br = new BufferedReader(new FileReader(filePathName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                while (line != null && !line.equals("</DOC>")) {
                    sb.append(line);
                    line = br.readLine();
                }
                sb.append(line);
                String text = sb.toString();
                String docNo = getDocNumber(text);
                Document doc = new Document(docNo, filePathName);
//                parser.parse(text, doc);
                Thread parseThread = new Thread(() -> parser.parse(text,doc));
//                readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
                sb.delete(0, sb.length());
                sb.setLength(0);
                sb = new StringBuilder();
                executor.execute(parseThread);
            }
            br.close();
            //return splitDocumentsFromFile(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private ArrayList<Pair<String, String>> splitDocumentsFromFile(String fileContent) {
        ArrayList<Pair<String, String>> doNODocument = new ArrayList<>();
        String[] fileDocuments = fileContent.split("</DOC>");
        for (int i = 0; i < fileDocuments.length; i++) {
            String currentFullDocument = fileDocuments[i];
            String docNumber = getDocNumber(currentFullDocument);
            if (docNumber != null) {
                doNODocument.add(new Pair<>(docNumber, currentFullDocument));
            }
        }
        return doNODocument;
    }

    private String getDocNumber(String fileDocument) {
        String[] str = fileDocument.split("DOCNO>");
        if (str.length > 1) {
            if (str[1].contains(" "))
                str[1].replaceAll("\\s+", "");
            int indexOfArrow = str[1].indexOf("<");
            String docNumber = str[1].substring(0, indexOfArrow);
            return docNumber;
        }
        return null;
    }
}

