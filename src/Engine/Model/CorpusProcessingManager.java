/**
 This class actually manages the corpus processing and uses the various departments to perform the following actions (in chronological order):
 1. Reading and parsing the document repository (The parse output will be written into temporary files called segment files).
 2. Creating the inverted indexes
 */

package Engine.Model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CorpusProcessingManager {
    public static final boolean testMode = false;

    private final int NUM_OF_PARSERS = 8; // Number of parsers threads
    private final int NUM_OF_SEGMENT_FILES= 8; // Unique segment file for each parse thread
    private final int NUM_OF_SEGMENT_PARTITIONS= 36; // Unique segment file for each parse thread
    private final int NUM_OF_INVERTERS = 36; // Num of inverters. Determined according to the subgroups that will be defined for each segment file (a-c, d-g, etc)
    private static double MILLION = Math.pow(10, 6);
    private ReadFile reader;
    private String corpusPath;
    private String postingPath ;
    private String originalPath;
    private final boolean stemming; // If the current processing should be used in the stemming
    private Parse[] parsers = new Parse[NUM_OF_PARSERS];
    private SegmentFile[] segmentFiles = new SegmentFile[NUM_OF_SEGMENT_FILES];
    private Indexer[] inverters = new Indexer[NUM_OF_INVERTERS];
    private ArrayList<String> filesPathsList; // A data structure that will hold all file paths in the corpus
    private static ExecutorService parseExecutor; // Thread pool for the parsers run
    private static ExecutorService invertedExecutor; // Thread pool for the inverters run
    private HashMap<String,String > inverted_city; // < State , City >
    public static ExecutorService docsPostingWriterExecutor;
    public static ConcurrentHashMap<String, City> cities ; // < City , City_obj >  cities after parsing


    public CorpusProcessingManager(String corpusPath, String postingPath , boolean stemming) {
        this.corpusPath = corpusPath;
        this.originalPath = postingPath;
        this.postingPath =  postingPath + "\\Postings" + ifStemming(stemming);
        this.stemming = stemming ;
        this.reader = new ReadFile();
        createDirs(this.postingPath);
        Posting.initPosting(this.postingPath + "\\Docs");
        initParsers();
        initInverters();

        filesPathsList = new ArrayList<>();
        parseExecutor = Executors.newFixedThreadPool(NUM_OF_PARSERS);
        invertedExecutor = Executors.newFixedThreadPool(NUM_OF_INVERTERS);
        docsPostingWriterExecutor = Executors.newFixedThreadPool(4);
        cities = new ConcurrentHashMap<>();
        inverted_city = new HashMap<>() ;
    }

    private String ifStemming(boolean stemming) {
        if (stemming)
            return "withStemming";
        return "";
    }

    /**
     * Creates the required folder tree to hold the final output files that will be generated from the processing process
     * @param postingPath The desired posting path given by the user
     */
    private void createDirs(String postingPath) {
        new File(postingPath + "\\Terms").mkdirs();
        new File(postingPath + "\\Docs").mkdirs();
        new File(postingPath + "\\Segment Files").mkdirs();
    }

    /**
     * Initialization of the inverters.
     * The method links each inverter to bunch of specific segment file partition (a certain alphabet range of terms) from the segment files.
     * In addition, each inverter is linked to its own unique posting file.
     * This posting will eventually contain the terms that will be in the alphabetical range defined for each inverter.
     */
    private void initInverters() {
        String postingBaseFilePath = postingPath;
        Posting termsPostingFile_0 = new Posting(postingBaseFilePath + "\\Terms\\" + "0");
        Posting termsPostingFile_1 = new Posting(postingBaseFilePath + "\\Terms\\" + "1");
        Posting termsPostingFile_2 = new Posting(postingBaseFilePath + "\\Terms\\" + "2");
        Posting termsPostingFile_3 = new Posting(postingBaseFilePath + "\\Terms\\" + "3");
        Posting termsPostingFile_4 = new Posting(postingBaseFilePath + "\\Terms\\" + "4");
        Posting termsPostingFile_5 = new Posting(postingBaseFilePath + "\\Terms\\" + "5");
        Posting termsPostingFile_6 = new Posting(postingBaseFilePath + "\\Terms\\" + "6");
        Posting termsPostingFile_7 = new Posting(postingBaseFilePath + "\\Terms\\" + "7");
        Posting termsPostingFile_8 = new Posting(postingBaseFilePath + "\\Terms\\" + "8");
        Posting termsPostingFile_9 = new Posting(postingBaseFilePath + "\\Terms\\" + "9");
        Posting termsPostingFile_a = new Posting(postingBaseFilePath + "\\Terms\\" + "a");
        Posting termsPostingFile_b = new Posting(postingBaseFilePath + "\\Terms\\" + "b");
        Posting termsPostingFile_c = new Posting(postingBaseFilePath + "\\Terms\\" + "c");
        Posting termsPostingFile_d = new Posting(postingBaseFilePath + "\\Terms\\" + "d");
        Posting termsPostingFile_e = new Posting(postingBaseFilePath + "\\Terms\\" + "e");
        Posting termsPostingFile_f = new Posting(postingBaseFilePath + "\\Terms\\" + "f");
        Posting termsPostingFile_g = new Posting(postingBaseFilePath + "\\Terms\\" + "g");
        Posting termsPostingFile_h = new Posting(postingBaseFilePath + "\\Terms\\" + "h");
        Posting termsPostingFile_i = new Posting(postingBaseFilePath + "\\Terms\\" + "i");
        Posting termsPostingFile_j = new Posting(postingBaseFilePath + "\\Terms\\" + "j");
        Posting termsPostingFile_k = new Posting(postingBaseFilePath + "\\Terms\\" + "k");
        Posting termsPostingFile_l = new Posting(postingBaseFilePath + "\\Terms\\" + "l");
        Posting termsPostingFile_m = new Posting(postingBaseFilePath + "\\Terms\\" + "m");
        Posting termsPostingFile_n = new Posting(postingBaseFilePath + "\\Terms\\" + "n");
        Posting termsPostingFile_o = new Posting(postingBaseFilePath + "\\Terms\\" + "o");
        Posting termsPostingFile_p = new Posting(postingBaseFilePath + "\\Terms\\" + "p");
        Posting termsPostingFile_q = new Posting(postingBaseFilePath + "\\Terms\\" + "q");
        Posting termsPostingFile_r = new Posting(postingBaseFilePath + "\\Terms\\" + "r");
        Posting termsPostingFile_s = new Posting(postingBaseFilePath + "\\Terms\\" + "s");
        Posting termsPostingFile_t = new Posting(postingBaseFilePath + "\\Terms\\" + "t");
        Posting termsPostingFile_u = new Posting(postingBaseFilePath + "\\Terms\\" + "u");
        Posting termsPostingFile_v = new Posting(postingBaseFilePath + "\\Terms\\" + "v");
        Posting termsPostingFile_w = new Posting(postingBaseFilePath + "\\Terms\\" + "w");
        Posting termsPostingFile_x = new Posting(postingBaseFilePath + "\\Terms\\" + "x");
        Posting termsPostingFile_y = new Posting(postingBaseFilePath + "\\Terms\\" + "y");
        Posting termsPostingFile_z = new Posting(postingBaseFilePath + "\\Terms\\" + "z");


        SegmentFilePartition[] segmentFilesInverter_0 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_1 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_2 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_3 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_4 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_5 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_6 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_7 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_8 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_9 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_a = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_b = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_c = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_d = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_e = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_f = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_g = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_h = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_i = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_j = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_k = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_l = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_m = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_n = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_o = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_p = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_q = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_r = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_s = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_t = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_u = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_v = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_w = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_x = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_y = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter_z = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];


        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_0[i] = segmentFiles[i].getSegmentFilePartitions('0');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_1[i] = segmentFiles[i].getSegmentFilePartitions('1');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_2[i] = segmentFiles[i].getSegmentFilePartitions('2');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_3[i] = segmentFiles[i].getSegmentFilePartitions('3');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_4[i] = segmentFiles[i].getSegmentFilePartitions('4');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_5[i] = segmentFiles[i].getSegmentFilePartitions('5');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_6[i] = segmentFiles[i].getSegmentFilePartitions('6');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_7[i] = segmentFiles[i].getSegmentFilePartitions('7');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_8[i] = segmentFiles[i].getSegmentFilePartitions('8');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_9[i] = segmentFiles[i].getSegmentFilePartitions('9');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_a[i] = segmentFiles[i].getSegmentFilePartitions('a');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_b[i] = segmentFiles[i].getSegmentFilePartitions('b');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_c[i] = segmentFiles[i].getSegmentFilePartitions('c');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_d[i] = segmentFiles[i].getSegmentFilePartitions('d');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_e[i] = segmentFiles[i].getSegmentFilePartitions('e');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_f[i] = segmentFiles[i].getSegmentFilePartitions('f');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_g[i] = segmentFiles[i].getSegmentFilePartitions('g');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_h[i] = segmentFiles[i].getSegmentFilePartitions('h');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_i[i] = segmentFiles[i].getSegmentFilePartitions('i');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_j[i] = segmentFiles[i].getSegmentFilePartitions('j');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_k[i] = segmentFiles[i].getSegmentFilePartitions('k');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_l[i] = segmentFiles[i].getSegmentFilePartitions('l');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_m[i] = segmentFiles[i].getSegmentFilePartitions('m');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_n[i] = segmentFiles[i].getSegmentFilePartitions('n');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_o[i] = segmentFiles[i].getSegmentFilePartitions('o');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_p[i] = segmentFiles[i].getSegmentFilePartitions('p');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_q[i] = segmentFiles[i].getSegmentFilePartitions('q');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_r[i] = segmentFiles[i].getSegmentFilePartitions('r');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_s[i] = segmentFiles[i].getSegmentFilePartitions('s');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_t[i] = segmentFiles[i].getSegmentFilePartitions('t');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_u[i] = segmentFiles[i].getSegmentFilePartitions('u');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_v[i] = segmentFiles[i].getSegmentFilePartitions('v');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_w[i] = segmentFiles[i].getSegmentFilePartitions('w');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_x[i] = segmentFiles[i].getSegmentFilePartitions('x');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_y[i] = segmentFiles[i].getSegmentFilePartitions('y');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter_z[i] = segmentFiles[i].getSegmentFilePartitions('z');
        }

        inverters[0] = new Indexer(segmentFilesInverter_0, termsPostingFile_0);
        inverters[1] = new Indexer(segmentFilesInverter_1, termsPostingFile_1);
        inverters[2] = new Indexer(segmentFilesInverter_2, termsPostingFile_2);
        inverters[3] = new Indexer(segmentFilesInverter_3, termsPostingFile_3);
        inverters[4] = new Indexer(segmentFilesInverter_4, termsPostingFile_4);
        inverters[5] = new Indexer(segmentFilesInverter_5, termsPostingFile_5);
        inverters[6] = new Indexer(segmentFilesInverter_6, termsPostingFile_6);
        inverters[7] = new Indexer(segmentFilesInverter_7, termsPostingFile_7);
        inverters[8] = new Indexer(segmentFilesInverter_8, termsPostingFile_8);
        inverters[9] = new Indexer(segmentFilesInverter_9, termsPostingFile_9);
        inverters[10] = new Indexer(segmentFilesInverter_a, termsPostingFile_a);
        inverters[11] = new Indexer(segmentFilesInverter_b, termsPostingFile_b);
        inverters[12] = new Indexer(segmentFilesInverter_c, termsPostingFile_c);
        inverters[13] = new Indexer(segmentFilesInverter_d, termsPostingFile_d);
        inverters[14] = new Indexer(segmentFilesInverter_e, termsPostingFile_e);
        inverters[15] = new Indexer(segmentFilesInverter_f, termsPostingFile_f);
        inverters[16] = new Indexer(segmentFilesInverter_g, termsPostingFile_g);
        inverters[17] = new Indexer(segmentFilesInverter_h, termsPostingFile_h);
        inverters[18] = new Indexer(segmentFilesInverter_i, termsPostingFile_i);
        inverters[19] = new Indexer(segmentFilesInverter_j, termsPostingFile_j);
        inverters[20] = new Indexer(segmentFilesInverter_k, termsPostingFile_k);
        inverters[21] = new Indexer(segmentFilesInverter_l, termsPostingFile_l);
        inverters[22] = new Indexer(segmentFilesInverter_m, termsPostingFile_m);
        inverters[23] = new Indexer(segmentFilesInverter_n, termsPostingFile_n);
        inverters[24] = new Indexer(segmentFilesInverter_o, termsPostingFile_o);
        inverters[25] = new Indexer(segmentFilesInverter_p, termsPostingFile_p);
        inverters[26] = new Indexer(segmentFilesInverter_q, termsPostingFile_q);
        inverters[27] = new Indexer(segmentFilesInverter_r, termsPostingFile_r);
        inverters[28] = new Indexer(segmentFilesInverter_s, termsPostingFile_s);
        inverters[29] = new Indexer(segmentFilesInverter_t, termsPostingFile_t);
        inverters[30] = new Indexer(segmentFilesInverter_u, termsPostingFile_u);
        inverters[31] = new Indexer(segmentFilesInverter_v, termsPostingFile_v);
        inverters[32] = new Indexer(segmentFilesInverter_w, termsPostingFile_w);
        inverters[33] = new Indexer(segmentFilesInverter_x, termsPostingFile_x);
        inverters[34] = new Indexer(segmentFilesInverter_y, termsPostingFile_y);
        inverters[35] = new Indexer(segmentFilesInverter_z, termsPostingFile_z);
    }

    /**
     * Initialization of the parsers.
     * And links each parser to specific segment file which will contain the parsing results of all documents parse processed.
     */
    private void initParsers() {
        for (int i = 0; i < NUM_OF_PARSERS; i++) {
            segmentFiles[i] = new SegmentFile(getSegmentFilePath(i) , stemming );
            parsers[i] = new Parse(segmentFiles[i], originalPath);
        }
    }


    /**
     * The method that manages the entire corpus processing process.
     * The method calls for auxiliary methods in order to obtain the final outputs required
     */
    public void StartCorpusProcessing() {
        initFilesPathList(corpusPath);
        Indexer.initIndexer(postingPath);

        try {
            if (testMode) {
                String startParseTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                System.out.println("Starting parsing: " + startParseTimeStamp);
            }
            readAndParse();

            if (testMode) {
                String finishParseTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                System.out.println("Finish parsing: " + finishParseTimeStamp);
            }


        }
        catch (Exception e ){
        }
        //end of parse
        cities = reader.cities;
        if (testMode){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            System.out.println("Starting building Inverted Index: " + timeStamp);
        }

        buildInvertedIndex();
        if (testMode){
            System.out.println("Finished building Inverted Index");
        }
        closeAllSegmentFiles();
//        try {
//            FileUtils.deleteDirectory(new File(postingPath + "//Segment Files"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        buildCitiesPosting();
        docsPostingWriterExecutor.shutdown();
        while (!docsPostingWriterExecutor.isTerminated()) {
        }

    }


    private void closeAllSegmentFiles() {
        for (int i = 0; i < segmentFiles.length; i++) {
            segmentFiles[i].closeBuffers();
        }
    }


    private void buildInvertedIndex() {
        for (int i = 0; i < NUM_OF_INVERTERS; i++) {
            System.out.println("inverter : " + i%NUM_OF_INVERTERS);
            inverters[i%NUM_OF_INVERTERS].appendSegmentPartitionRangeToPostingAndIndexes();

        }
        System.out.println("done");
    }

    /**
     * This method manages the parallel run of the parsers.
     * The method is part of a mechanism that takes care of parallelism in performing the reading and parsing operation of multiple files simultaneously.
     * @throws InterruptedException
     */
    private void readAndParse() throws InterruptedException {
        for (int i = 0; i < filesPathsList.size(); i++) {
            int finalI = i;
            Thread readNParseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%8]));
            parseExecutor.execute(readNParseThread);
//            reader.readAndParseLineByLine(filesPathsList.get(i), parsers[i%8]);
        }
        parseExecutor.shutdown();
        while (!parseExecutor.isTerminated()) {
        }
    }


    public void buildCitiesPosting(){
        cities = reader.getCities() ;
        getCitiesInfo () ;
        //end of parse
    }

    /**
     * An auxiliary method that makes it easy to retrieve a segment file path that belongs to a specific parser
     * @param i The parse number
     * @return Segment file path of a specific parse.
     */
    private String getSegmentFilePath(int i) {
        String segmantBaseFilePath = this.postingPath + "\\Segment Files";
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

    /**
     * This method, along with the following method, is responsible for adding the paths of all corpus files into the filePathsList data structure.
     * @param curposPath The path of the corpus given by the user.
     */
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
//        for (Map.Entry<String, City> entry : cities.entrySet())
//        {
        //System.out.println(entry.getKey() + "/" + entry.getValue());
        try {
            // System.out.println(getText(entry.getKey()));
            //get hash maps
            getCitiesState();
            getCitiesPopulation() ;
            getCitiesCurrencies() ;
        }
        catch (Exception e ){
            System.out.println(e.getCause());
        }
//        }



    }

    /**
     * get cities info about states and insert to citias hashmap
     * @return
     * @throws Exception
     */
    public  String getCitiesState() throws Exception {

        //URL website = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + city_name);
        URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;capital;");


        //URLConnection connection = website.openConnection();
        HttpURLConnection con  = ( HttpURLConnection)  url.openConnection();
        con.setRequestMethod("GET");


        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        con.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;
        inputLine = in.readLine() ;
        //while (() != null) {
        //response.append(inputLine);
        String[] splited = StringUtils.split(inputLine,"[]}{,:\"") ;
        for ( int i= 0 ; i < splited.length-3; ) {
            String s =  splited[i] ;
            if (s.equals("[") || s.equals(",") || s.equals("]") || s.equals("name") || s.equals("capital")) {
                i++;
                continue;
            }
            //String[] splited_split = StringUtils.split(inputLine,"") ;

            String state = splited[i].toLowerCase();
            String city = splited[i+2].toLowerCase();
            String first_part = "" ;
            if ( city.contains(" ") ) // 2 word city
                first_part = city.split(" ")[0].toLowerCase();
            if (cities.containsKey(city) || cities.containsKey(first_part)) {
                City city_obj = cities.get(city);
                if (city_obj == null )
                    city_obj = cities.get(first_part) ;
                if ( city_obj == null ) {
                    i=i+3 ;
                    continue;
                }
                try {
                    city_obj.setState_name(state);
                    inverted_city.put(state, city);
                    cities.put(city, city_obj);
                }
                catch (Exception e ){
                    System.out.println(city + " : " + state);
                }

            }
            i=i+3;
        }

        // }

        in.close();

        return null ;
    }

    /**
     * get info of cities pop from api
     * @return
     * @throws Exception
     */
    public  String getCitiesPopulation() throws Exception {
        //URL website = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + city_name);
        URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;population");
        //URLConnection connection = website.openConnection();
        HttpURLConnection con  = ( HttpURLConnection)  url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        con.getInputStream()));
        //StringBuilder response = new StringBuilder();
        String inputLine;
        inputLine = in.readLine() ;
        String[] splited = StringUtils.split(inputLine,"[]}{:\"") ;
        for ( int i= 0 ; i < splited.length-3; ) {
            String s =  splited[i] ;
            if (s.equals("[") || s.equals(",") || s.equals("]") || s.equals("name") || s.equals("population")) {
                i++;
                continue;
            }
            //String[] splited_split = StringUtils.split(inputLine,"") ;
            String state = splited[i].toLowerCase();
            String population = splited[i+3];
            String first_part = "" ;

            if (inverted_city.containsKey(state) ) {
                String city = inverted_city.get(state);

                City city_obj = cities.get(city); // try 1 word city first
                if (city_obj == null) {
                    first_part = city.split(" ")[0];// try 2 words city
                    city_obj = cities.get(first_part);
                }
                //round num
                int num = Integer.parseInt(population);
                try {
                    if ( Parse.isNumber( population ) && num > MILLION ){

                        double num_d = round_num ( num ) ;
                        population =  "M" + Double.toString(num_d) ;
                    }
                    city_obj.setPopulation(population);
                    cities.put(city, city_obj);
                }
                catch (Exception e ){
                    System.out.println(state + " " + city_obj.toString());
                }
            }
            i=i+4;
        }
        in.close();

        return null ;
    }

    /**
     * round a num over 1 m
     * @param num
     * @return
     */
    private double round_num(int num) {
        double rounded = num / MILLION;
        rounded = round( rounded , 2 ) ;
        return rounded ;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * get cities info about currencies and insert to citias hashmap
     * @return
     * @throws Exception
     */
    public  String getCitiesCurrencies() throws Exception {


        //URL website = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + city_name);
        URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;currencies;");


        //URLConnection connection = website.openConnection();
        HttpURLConnection con  = ( HttpURLConnection)  url.openConnection();
        con.setRequestMethod("GET");


        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        con.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;
        inputLine = in.readLine() ;
        //while (() != null) {
        //response.append(inputLine);
        String[] splited = StringUtils.split(inputLine,"[]}{,:\"") ;
        int jump = 7 ;
        for ( int i= 0 ; i < splited.length-3; ) {
            String s =  splited[i] ;
            if (!s.equals("code")) {
                i++;
                continue;
            }
            //String[] splited_split = StringUtils.split(inputLine,"") ;
            String currency = "" ;
            if ( s.equals("code"))
                currency= splited[i+1] ; // got cuurency
            i++ ;
            //now find state
            String state = "" ;
            int count_name = 0 ;
            while( i < splited.length-3 &&  !inverted_city.containsKey(state)  ){
                state = splited[i].toLowerCase() ;
                if (state.equals("name")){
                    count_name++;
                }
                if ( count_name == 2) {// counted 2 names , should stop
                    state = splited[i + 1].toLowerCase();
                    break;
                }
                i++ ;
            }

            String first_part = "" ;
            if (inverted_city.containsKey(state) ) {
                String city = inverted_city.get(state);

                City city_obj = cities.get(city); // try 1 word city first
                if (city_obj == null) {
                    first_part = city.split(" ")[0];// try 2 words city
                    city_obj = cities.get(first_part);
                }
                try {
                    city_obj.setCurrency(currency);
                    cities.put(city, city_obj);
                }
                catch (Exception e ){
                    System.out.println(state + " : " + state);
                }

            }
            i++;
        }

        // }

        in.close();

        return null ;
    }

    public String[] getDocLang ()
    {
        return reader.getLanguagesList();
    }
    //URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;currencies;");
    //URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;population");

}

