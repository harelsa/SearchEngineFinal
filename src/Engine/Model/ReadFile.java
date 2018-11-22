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

    public ArrayList<Pair<String, String>> readFile(String filePathName) {
        return generateDocuments(filePathName);
    }

    /*  */
    public ArrayList<Pair<String, String>> read2files(String path1, String path2) {
        Charset charset = Charset.forName("UTF-8");
        ArrayList<String> list = new ArrayList<String>();
        try (
                FileInputStream is1 = new FileInputStream(path1);
                FileInputStream is2 = new FileInputStream(path2);
                SequenceInputStream is = new SequenceInputStream(is1, is2);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));) {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
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
    }

    /**
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

            while ((line = br.readLine()) != null) {
                StringBuilder sb = new StringBuilder();
                while (line != null && !line.equals("</DOC>")) {
                    sb.append(line);
                    line = br.readLine();
                }
                sb.append(line);
                String text = sb.toString();
                String docNo = getDocNumber(sb.toString());
                Document doc = new Document(docNo, filePathName);
//                parser.parse(text, doc);
                Thread parseThread = new Thread(() -> parser.parse(text,doc));
//                readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
                executor.execute(parseThread);
                sb.delete(0, sb.length());

            }
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

