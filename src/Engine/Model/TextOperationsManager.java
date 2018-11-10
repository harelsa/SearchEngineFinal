package Engine.Model;

import javafx.util.Pair;

import java.io.File;
import java.sql.Array;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextOperationsManager {
    ReadFile reader;
    Parse parser;
    String curposPath;
    HashMap<Document, HashSet<String>> DocumentsTerms;
    public ArrayList<String> filesPathsList;
    ExecutorService executor;

    /* FOR TEST ONLY */
    public static int filesCounter;
    public static int docsCounter;


    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        this.parser = new Parse();
        this.curposPath = curposPath;
        filesPathsList = new ArrayList<>();
        executor = Executors.newFixedThreadPool(8);
    }

    public void StartTextOperations() {
        initFilesPathList(curposPath);
        readAndParse();
    }

    private void readAndParse() {
        for (int i = 0; i < filesPathsList.size(); i++) {
//            int finalI = i;
            int finalII = i+1;
            int finalI = i;
            Thread parseThread = new Thread() {
                public void run() {
                    readAndParseTwoFiles(finalI, finalII);
//                      readAndParseOneFile(filesPathsList.get(finalI));
                }
            };
            executor.execute(parseThread);
        }
    }

    private void readAndParseTwoFiles(int finalI, int finalII) {
        String path1 = filesPathsList.get(finalI);
        String path2 = filesPathsList.get(finalII);
        ArrayList<Pair<String, String>> twoDocumentsFromFile;
        twoDocumentsFromFile = reader.read2files(path1, path2);
        System.out.println("Starting to parse documents of file: " + path1);
        for (int i = 0; i < twoDocumentsFromFile.size(); i++) {
            // Need to add more methods here
            String docText = getTextFromFullDoc(twoDocumentsFromFile.get(i).getValue());
            Document document = new Document(twoDocumentsFromFile.get(i).getKey(), path1);
            parser.parse(docText, document);
        }
    }

    private void readAndParseOneFile(String filePath) {
        ArrayList<Pair<String, String>> documentsFromFile = reader.readFile(filePath); // The Pair is: <docNo, FullDocContent>
        System.out.println("Starting to parse documents of file: " + filePath);
        for (int i = 0; i < documentsFromFile.size(); i++) {
            // Need to add more methods here
            String docText = getTextFromFullDoc(documentsFromFile.get(i).getValue());
            Document document = new Document(documentsFromFile.get(i).getKey(), filePath);
//            Thread parseThread = new Thread(){
//                    public void run(){
//                        parser.parse(docText,document);
//                    }
//            };
//            executor.execute(parseThread);
            parser.parse(docText, document);
        }
    }

    private String getTextFromFullDoc(String fullDocContent) {
        String text = "";
        text = fullDocContent.split("TEXT>")[1].replaceAll("/<", "");
        return text;
    }

    private void initFilesPathList(String curposPath) {
        final File folder = new File(curposPath);
        listFilesOfFolder(folder);
    }

    public void listFilesOfFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesOfFolder(fileEntry);
            } else {
                filesPathsList.add(fileEntry.getPath());
            }
        }
    }

}

