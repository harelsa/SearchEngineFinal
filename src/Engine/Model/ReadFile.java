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
            boolean text_adding = false ;
            String line = "";
            StringBuilder sb_docInfo = new StringBuilder();
            StringBuilder sb_text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                while (line != null && !line.equals("</DOC>")) {
                    if ( line.equals("<TEXT>")) text_adding  = true ;
                    if (! text_adding )
                    sb_docInfo.append(line);
                    else sb_text.append(line) ;
                    line = br.readLine();
                }
                sb_docInfo.append(line);
                String text = sb_text.toString();
                String doc_text = sb_docInfo.toString();
                String docNo = getDocNumber(doc_text);
                Document doc = new Document(docNo, getFileName(filePathName));
//                parser.parse(text, doc);
                //Thread parseThread = new Thread(() -> parser.parse(text,doc));
//                readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));

//                Thread parseThread = new Thread(){
//                    public void run(){
//                        parser.parse(text, doc);
//                        text = null;
//                        doc = null;
//                    }
//                };

                sb_docInfo.delete(0, sb_docInfo.length());
                sb_text.delete(0, sb_text.length());
                sb_docInfo.setLength(0);
                sb_text.setLength(0);
                sb_text = new StringBuilder();
                sb_docInfo = new StringBuilder();
                parser.parse(text,doc);
                //executor.execute(parseThread);
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

    private String getFileName(String filePathName) {
        String[] splited = filePathName.split("\\\\");
        return splited[splited.length-1];
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

