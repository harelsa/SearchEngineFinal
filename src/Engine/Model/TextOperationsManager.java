package Engine.Model;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Array;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextOperationsManager {
    final int NUM_OF_PARSERS = 8;
    final int NUM_OF_SEGMENT_FILES= 8;
    final int NUM_OF_SEGMENT_FILE_PARTITIONS= 5;
    final int NUM_OF_INVERTERS = 5;



    ReadFile reader;
    Parse parser;
    String curposPath;
    private Parse[] parsers = new Parse[NUM_OF_PARSERS];
    private SegmentFile[] segmentFiles = new SegmentFile[NUM_OF_SEGMENT_FILES];
    private Indexer[] inverters = new Indexer[NUM_OF_INVERTERS];
    public ArrayList<String> filesPathsList;
    public static ExecutorService parseExecutor;
    static ConcurrentHashMap<String, City> cities ; // cities after parsing

    /* FOR TEST ONLY */
    public static int filesCounter;
    public static int docsCounter;


    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        initParsers();
        initInverters();
        this.curposPath = curposPath;
        filesPathsList = new ArrayList<>();
        parseExecutor = Executors.newFixedThreadPool(NUM_OF_PARSERS);
        cities = new ConcurrentHashMap<>();
    }

    private void initInverters() {
        SegmentFilePartition[] segmentFilesInverter1 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
        SegmentFilePartition[] segmentFilesInverter2 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
        SegmentFilePartition[] segmentFilesInverter3 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
        SegmentFilePartition[] segmentFilesInverter4 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
        SegmentFilePartition[] segmentFilesInverter5 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
//        SegmentFilePartition[] segmentFilesInverter6 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
//        SegmentFilePartition[] segmentFilesInverter7 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
//        SegmentFilePartition[] segmentFilesInverter8 = new SegmentFilePartition[NUM_OF_SEGMENT_FILE_PARTITIONS];
        for (int i = 0; i < NUM_OF_INVERTERS; i++) {
            for (int j = 0; j < NUM_OF_SEGMENT_FILES; j++) {
                segmentFilesInverter1[i] = segmentFiles[j].getSegmentFilePartitions('0', '9');
                segmentFilesInverter2[i] = segmentFiles[j].getSegmentFilePartitions('a', 'f');
                segmentFilesInverter3[i] = segmentFiles[j].getSegmentFilePartitions('g', 'p');
                segmentFilesInverter4[i] = segmentFiles[j].getSegmentFilePartitions('q', 'z');
                segmentFilesInverter5[i] = segmentFiles[j].getSegmentFilePartitions('z', 'z');
            }
        }
        inverters[0] = new Indexer(segmentFilesInverter1);
        inverters[1] = new Indexer(segmentFilesInverter2);
        inverters[2] = new Indexer(segmentFilesInverter3);
        inverters[3] = new Indexer(segmentFilesInverter4);
        inverters[4] = new Indexer(segmentFilesInverter5);
//        inverters[5] = new Indexer(segmentFilesInverter6);
//        inverters[6] = new Indexer(segmentFilesInverter7);
//        inverters[7] = new Indexer(segmentFilesInverter8);
    }


    private void initParsers() {
        for (int i = 0; i < NUM_OF_PARSERS; i++) {
            segmentFiles[i] = new SegmentFile(getSegmentFilePath(i));
            parsers[i] = new Parse(segmentFiles[i]);
        }
    }

    public void StartTextOperations() {
        initFilesPathList(curposPath);
        try {
            readAndParse();
        }
        catch (Exception e ){
        }
        cities = reader.getCities() ;
        getCitiesInfo () ;
        //end of parse
    }

    synchronized private void readAndParse() throws InterruptedException {
        List<Callable<Object>> calls = new ArrayList<Callable<Object>>();
        Thread readNParseThread = null;
        for (int i = 0; i < filesPathsList.size(); i++) {
            int finalI = i;
            readNParseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%8]));
            parseExecutor.execute(readNParseThread) ;
            calls.add(Executors.callable(readNParseThread));
//            Thread parseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
//            executor.execute(parseThread);
        }
        parseExecutor.isTerminated();
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
            case 4:
                segmantFilePath = segmantBaseFilePath + "\\Parser5";
                break;
            case 5:
                segmantFilePath = segmantBaseFilePath + "\\Parser6";
                break;
            case 6:
                segmantFilePath = segmantBaseFilePath + "\\Parser7";
                break;
            case 7:
                segmantFilePath = segmantBaseFilePath + "\\Parser8";
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

    /**
     * save city in a global hash map
     * @param
     */
    public void getCitiesInfo (){
        for (Map.Entry<String, City> entry : cities.entrySet())
        {
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }



    }
    /**
     * return the info about a city
     * @param city_name
     * @return
     * @throws Exception
     */
    public  String getText(String city_name) throws Exception {
        URL website = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + city_name);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }

}

