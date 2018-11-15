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
    public static ExecutorService executor;

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
            String segmentFilePath = getSegmentFilePath(i);
            int finalI = i;
            Thread parseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parser));
            executor.execute(parseThread);
        }
    }

    private String getSegmentFilePath(int i) {
        String segmantBaseFilePath = "src\\Engine\\resources\\Segment Files";
        String segmantFilePath = "";
        int ans = i % 4;
        switch (ans) {
            case 0:
                segmantFilePath = segmantBaseFilePath + "\\Thread1";
                break;
            case 1:
                segmantFilePath = segmantBaseFilePath + "\\Thread2";
                break;
            case 2:
                segmantFilePath = segmantBaseFilePath + "\\Thread3";
                break;
            case 3:
                segmantFilePath = segmantBaseFilePath + "\\Thread4";
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

