package Engine.Model;

import org.apache.commons.lang3.StringUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
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
    private static double MILLION = Math.pow(10, 6);



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
    private HashMap<String,String > inveted_city;


    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        initParsers();
        initInverters();
        this.curposPath = curposPath;
        filesPathsList = new ArrayList<>();
        parseExecutor = Executors.newFixedThreadPool(NUM_OF_PARSERS);
        cities = new ConcurrentHashMap<>();
        inveted_city = new HashMap<String , String >() ;
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
        //end of parse
    }

    private void readAndParse() throws InterruptedException {
        List<Callable<Object>> calls = new ArrayList<Callable<Object>>();
        for (int i = 0; i < filesPathsList.size(); i++) {
            int finalI = i;
            Thread readNParseThread = new Thread(() -> reader.readAndParseLineByLine(filesPathsList.get(finalI), parsers[finalI%8]));
            parseExecutor.execute(readNParseThread) ;
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

    //URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;currencies;");
    //URL url = new URL("http://restcountries.eu/rest/v2/all?fields=name;population");

}

