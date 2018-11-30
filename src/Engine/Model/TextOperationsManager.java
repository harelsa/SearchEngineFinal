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
    private Parse[] parsers = new Parse[4];
    public ArrayList<String> filesPathsList;
    public static ExecutorService parseExecutor;

    /* FOR TEST ONLY */
    public static int filesCounter;
    public static int docsCounter;


    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        initParsers();
        this.curposPath = curposPath;
        filesPathsList = new ArrayList<>();
        parseExecutor = Executors.newFixedThreadPool(8);
    }

    private void initParsers() {
        for (int i = 0; i < 4; i++) {
            SegmentFile sf = new SegmentFile(getSegmentFilePath(i));
            parsers[i] = new Parse(sf);
        }
    }

    public void StartTextOperations() {
        initFilesPathList(curposPath);
        readAndParse();
    }

    private void readAndParse() {
        for (int i = 0; i < filesPathsList.size(); i++) {
            int finalI = i;
            Thread readNParseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
            parseExecutor.execute(readNParseThread);
//            Thread parseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
//            executor.execute(parseThread);
        }
    }

    private String getSegmentFilePath(int i) {
        String segmantBaseFilePath = "src\\Engine\\resources\\Segment Files";
        String segmantFilePath = "";
        switch (i) {
            case 0:
                segmantFilePath = segmantBaseFilePath + "\\Parser1";
                break;
            case 1:
                segmantFilePath = segmantBaseFilePath + "\\Parser2";
                break;
            case 2:
                segmantFilePath = segmantBaseFilePath + "\\Parser3";
                break;
            case 3:
                segmantFilePath = segmantBaseFilePath + "\\Parser4";
                break;
        }
        return segmantFilePath;
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

