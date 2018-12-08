package Engine.Model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Array;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextOperationsManager {
    final int NUM_OF_PARSERS = 8;
    final int NUM_OF_SEGMENT_FILES= 8;
    final int NUM_OF_SEGMENT_FILE_PARTITIONS= 6;
    final int NUM_OF_INVERTERS = 6;
    private static double MILLION = Math.pow(10, 6);



    private ReadFile reader;
    private String curposPath;
    private String postingPath ;
    private final boolean stemming;
    private Parse[] parsers = new Parse[NUM_OF_PARSERS];
    private SegmentFile[] segmentFiles = new SegmentFile[NUM_OF_SEGMENT_FILES];
    private Indexer[] inverters = new Indexer[NUM_OF_INVERTERS];
    public ArrayList<String> filesPathsList;
    public static ExecutorService parseExecutor;
    public static ExecutorService invertedExecutor;
    static ConcurrentHashMap<String, City> cities ; // cities after parsing



    private HashMap<String,String > inveted_city;


    public TextOperationsManager(String curposPath , String postingPath ,boolean stemming) {
        this.curposPath = curposPath;
        this.postingPath = postingPath ;
        this.stemming = stemming ;
        this.reader = new ReadFile();
        createDirs(postingPath);
        Posting.initPosting(postingPath + "\\Docs");
        initParsers();
        initInverters();

        filesPathsList = new ArrayList<>();
        parseExecutor = Executors.newFixedThreadPool(NUM_OF_PARSERS);
        invertedExecutor = Executors.newFixedThreadPool(NUM_OF_INVERTERS);
        cities = new ConcurrentHashMap<>();
        inveted_city = new HashMap<String , String >() ;
    }

    private void createDirs(String postingPath) {
        new File(postingPath + "\\Terms").mkdirs();
        new File(postingPath + "\\Docs").mkdirs();
        new File(postingPath + "\\Segment Files").mkdirs();
    }

    private void initInverters() {
        // postingBaseFilePath = postingPath + "\\Posting Files";
        String postingBaseFilePath = postingPath;

        Posting termsPostingFile_0_9 = new Posting(postingBaseFilePath + "\\Terms\\" + "0_9");
        Posting termsPostingFile_a_c = new Posting(postingBaseFilePath + "\\Terms\\" + "a_c");
        Posting termsPostingFile_d_f = new Posting(postingBaseFilePath + "\\Terms\\" + "d_f");
        Posting termsPostingFile_g_k = new Posting(postingBaseFilePath + "\\Terms\\" + "g_k");
        Posting termsPostingFile_l_p = new Posting(postingBaseFilePath + "\\Terms\\" + "l_p");
        Posting termsPostingFile_q_z = new Posting(postingBaseFilePath + "\\Terms\\" + "q_z");
        //Posting termsPostingFile_z_z = new Posting(postingBaseFilePath + "\\Terms\\" + "z_z");

//        Posting docsPostingFile_0_9 = new Posting(postingBaseFilePath + "\\Docs\\" + "0_9");
//        Posting docsPostingFile_a_c = new Posting(postingBaseFilePath + "\\Docs\\" + "a_c");
//        Posting docsPostingFile_d_f = new Posting(postingBaseFilePath + "\\Docs\\" + "d_f");
//        Posting docsPostingFile_g_k = new Posting(postingBaseFilePath + "\\Docs\\" + "g_k");
//        Posting docsPostingFile_l_p = new Posting(postingBaseFilePath + "\\Docs\\" + "l_p");
//        Posting docsPostingFile_q_z = new Posting(postingBaseFilePath + "\\Docs\\" + "q_z");
        //Posting docsPostingFile_z_z = new Posting(postingBaseFilePath + "\\Docs\\" + "z_z");

        SegmentFilePartition[] segmentFilesInverter1 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter2 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter3 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter4 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter5 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter6 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];
        SegmentFilePartition[] segmentFilesInverter7 = new SegmentFilePartition[NUM_OF_SEGMENT_FILES];

        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter1[i] = segmentFiles[i].getSegmentFilePartitions('0', '9');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter2[i] = segmentFiles[i].getSegmentFilePartitions('a', 'c');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter3[i] = segmentFiles[i].getSegmentFilePartitions('d', 'f');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter4[i] = segmentFiles[i].getSegmentFilePartitions('g', 'k');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter5[i] = segmentFiles[i].getSegmentFilePartitions('l', 'p');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter6[i] = segmentFiles[i].getSegmentFilePartitions('q', 'z');
        }
        for (int i = 0; i < NUM_OF_SEGMENT_FILES; i++) {
            segmentFilesInverter7[i] = segmentFiles[i].getSegmentFilePartitions('z', 'z');
        }


//        inverters[0] = new Indexer(segmentFilesInverter1, termsPostingFile_0_9, docsPostingFile_0_9);
//        inverters[1] = new Indexer(segmentFilesInverter2, termsPostingFile_a_c, docsPostingFile_a_c);
//        inverters[2] = new Indexer(segmentFilesInverter3, termsPostingFile_d_f, docsPostingFile_d_f);
//        inverters[3] = new Indexer(segmentFilesInverter4, termsPostingFile_g_k, docsPostingFile_g_k);
//        inverters[4] = new Indexer(segmentFilesInverter5, termsPostingFile_l_p, docsPostingFile_l_p);
//        inverters[5] = new Indexer(segmentFilesInverter6, termsPostingFile_q_z, docsPostingFile_q_z);
        inverters[0] = new Indexer(segmentFilesInverter1, termsPostingFile_0_9);
        inverters[1] = new Indexer(segmentFilesInverter2, termsPostingFile_a_c);
        inverters[2] = new Indexer(segmentFilesInverter3, termsPostingFile_d_f);
        inverters[3] = new Indexer(segmentFilesInverter4, termsPostingFile_g_k);
        inverters[4] = new Indexer(segmentFilesInverter5, termsPostingFile_l_p);
        inverters[5] = new Indexer(segmentFilesInverter6, termsPostingFile_q_z);
    }


    private void initParsers() {
        for (int i = 0; i < NUM_OF_PARSERS; i++) {
            segmentFiles[i] = new SegmentFile(getSegmentFilePath(i) , stemming );
            parsers[i] = new Parse(segmentFiles[i]);
        }
    }

    public  String[] StartTextOperations() {
        initFilesPathList(curposPath);
        Indexer.initIndexer(postingPath);

        try {
            String startParseTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            System.out.println("Starting parsing: " + startParseTimeStamp);
            readAndParse();
            String finishParseTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            System.out.println("Finish parsing: " + finishParseTimeStamp);
        }
        catch (Exception e ){
        }
        //end of parse
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println("Starting building Inverted Index: " + timeStamp);
        buildInvertedIndex();
        System.out.println("Finished building Inverted Index");
        closeAllSegmentFiles();
        try {
            FileUtils.deleteDirectory(new File(postingPath + "//Segment Files"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Indexer.writeDictionariesToDisc();
        String timeStamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

        return null ; //return info
    }

    private void closeAllSegmentFiles() {
        for (int i = 0; i < segmentFiles.length; i++) {
            segmentFiles[i].closeBuffers();
        }
    }


    private void buildInvertedIndex() {
        for (int i = 0; i < NUM_OF_INVERTERS; i++) {

            System.out.println("inverter : " + i%NUM_OF_INVERTERS);

            inverters[i%NUM_OF_INVERTERS].buildInvertedIndexes();
//            Thread parseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
//            executor.execute(parseThread);
//            int finalI = i;

//            int finalI = i;
//            Thread buildInvertedIndex = new Thread(() -> inverters[finalI % NUM_OF_INVERTERS].buildInvertedIndexes());
//            invertedExecutor.execute(buildInvertedIndex);
       }
//        invertedExecutor.shutdown();
//        while (!invertedExecutor.isTerminated());

        System.out.println("done");

    }

    private void readAndParse() throws InterruptedException {
        for (int i = 0; i < filesPathsList.size(); i++) {
            int finalI = i;
            Thread readNParseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%8]));
            parseExecutor.execute(readNParseThread);
//            Thread parseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%4]));
//            executor.execute(parseThread);
        }
        parseExecutor.shutdown();
        while (!parseExecutor.isTerminated()) {

        }


    }

    public void BuildCitiesPosting(){
        cities = reader.getCities() ;
        getCitiesInfo () ;
        //end of parse

    }

    private String getSegmentFilePath(int i) {
        String segmantBaseFilePath = postingPath+ "\\Segment Files";
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

            String state = splited[i];
            String city = splited[i+2];
            String first_part = "" ;
            if ( city.contains(" ") ) // 2 word city
                first_part = city.split(" ")[0];
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
                    inveted_city.put(state, city);
                    cities.put(s, city_obj);
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
            String state = splited[i];
            String population = splited[i+3];
            String first_part = "" ;

            if (inveted_city.containsKey(state) ) {
                String city = inveted_city.get(state);

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
                    cities.put(s, city_obj);
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
            while( i < splited.length-3 &&  !inveted_city.containsKey(state)  ){
                state = splited[i] ;
                if (state.equals("name")){
                    count_name++;
                }
                if ( count_name == 2) {// counted 2 names , should stop
                    state = splited[i + 1];
                    break;
                }
                i++ ;
            }

            String first_part = "" ;
            if (inveted_city.containsKey(state) ) {
                String city = inveted_city.get(state);

                City city_obj = cities.get(city); // try 1 word city first
                if (city_obj == null) {
                    first_part = city.split(" ")[0];// try 2 words city
                    city_obj = cities.get(first_part);
                }
                try {
                    city_obj.setCurrency(currency);
                    cities.put(s, city_obj);
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

