package Engine.Model;

import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class ReadFile {
    ExecutorService executor;

    ConcurrentHashMap< String ,City> cities = new ConcurrentHashMap<>() ; // save all the doc info cities

    public ReadFile() {
        executor = Executors.newFixedThreadPool(8);
    }


    public void readAndParseLineByLine(String filePathName, Parse parser) {
        BufferedReader br = null;
        System.out.println(filePathName);
        String parentFileName = getParentFileName(filePathName);
        try {
            br = new BufferedReader(new FileReader(filePathName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            boolean text_adding = false ;
            String line = "" ;
            StringBuilder sb_docInfo = new StringBuilder();
            StringBuilder sb_text = new StringBuilder();
            String docNo = "" ;
            String docCity = "" ;
            String doc_date = "" ;
            while ((line = br.readLine()) != null) {
                while (line != null && !line.equals("</DOC>") && !line.equals("</TEXT>")) {
                    if (line.equals("<DOC>")) {
                        line=br.readLine() ;
                        text_adding = false ;
                        continue;
                    }
                    if (  line.equals("<P>") || line.equals("</ P>") ) {
                        line = br.readLine() ;  // start doc
                        continue;
                    }
                    if ( line.equals("<TEXT>")) {
                        text_adding  = true ;
                        line = br.readLine();
                    }
                    //clean
                    if ( line.startsWith("<F P=106>" ) ){
                       String[] temp =  StringUtils.split(line , "><");
                       line = temp[1] ;
                    }

                    if (! text_adding ) // add to doc info
                    sb_docInfo.append(line);
                    else sb_text.append(line) ; // add to text

                    if (  line.startsWith("Language: <F P=105>")) // not working
                        System.out.println(" ???? ");
                        //add to doc lang !!!
                    //doc date
//                    if ( line.equals("<DATE1>")){     /// date has diff format in diff docs
//                        doc_date = get_doc_date( line ) ;
//                    }
                    // CITY
                    if (line.startsWith("<F P=104>")){
                       String[] arr = StringUtils.split(line , " ");
                       if (arr.length < 4){
                           line = br.readLine();
                           continue;
                       }
                       int i = 4 ;
                        docCity = arr [3] ; // only the first word between tags
                       this.cities.put( docCity , new City(docCity) ) ;
                       docCity = docCity.toUpperCase();
                    }
                    // Doc num
                    if (line.startsWith("<DOCNO>")){
                        String[] arr = StringUtils.split( line , " ");
                        if (arr.length >= 2)
                            docNo = arr [1] ;
                    }
                    line = br.readLine();
                }
                //sb_docInfo.append(line);
                String text = sb_text.toString();
                Document doc = new Document(docNo, parentFileName , docCity );


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

    private String get_doc_date(String line) {
        String[] arr = StringUtils.split(line , " ");

        return "" ;
    }

    private String getParentFileName(String filePathName) {
        String[] split = StringUtils.split(filePathName , "\\\\");
        return split[split.length-1];
    }

    /**
     * check if all the chars are upper case
     * @param str
     * @return
     */
    public static boolean testAllUpperCase(String str){
        for(int i=0; i<str.length(); i++){
            char c = str.charAt(i);
            if(c >= 97 && c <= 122) {
                return false;
            }
        }
        //str.charAt(index)
        return true;
    }

    public  ConcurrentHashMap<String, City> getCities() {
        return cities;
    }


//    private ArrayList<Pair<String, String>> splitDocumentsFromFile(String fileContent) {
//        ArrayList<Pair<String, String>> doNODocument = new ArrayList<>();
//        String[] fileDocuments = fileContent.split("</DOC>");
//        for (int i = 0; i < fileDocuments.length; i++) {
//            String currentFullDocument = fileDocuments[i];
//            String docNumber = getDocNumber(currentFullDocument);
//            if (docNumber != null) {
//                doNODocument.add(new Pair<>(docNumber, currentFullDocument));
//            }
//        }
//        return doNODocument;
//    }

//    private String getDocNumber(String fileDocument) {
//        String[] str = fileDocument.split("DOCNO>");
//        if (str.length > 1) {
//            if (str[1].contains(" "))
//                str[1].replaceAll("\\s+", "");
//            int indexOfArrow = str[1].indexOf("<");
//            String docNumber = str[1].substring(0, indexOfArrow);
//            return docNumber;
//        }
//        return null;
//    }
}

