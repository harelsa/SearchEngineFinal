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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class ReadFile {
    ExecutorService executor;



    private  static ConcurrentHashMap< String ,City> Cities = new ConcurrentHashMap<>() ; // save all the doc info cities

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
            while ((line = br.readLine()) != null) {
                while (line != null && !line.equals("</DOC>") && !line.equals("</TEXT>")) {
                    if ( line.equals("<TEXT>")) {
                        text_adding  = true ;
                        line = br.readLine();
                    }
                    if (! text_adding ) // add to doc info
                    sb_docInfo.append(line);
                    else sb_text.append(line) ; // add to text
                    if (line.startsWith("<F P=104>")){ // CITY
                       String[] arr =  line.split(" ");
                       if (arr.length < 4){
                           line = br.readLine();
                           continue;
                       }
                       int i = 4 ;
                        docCity = arr [3] ;
                       while(i < arr.length-1 && !testAllUpperCase(arr[i])){
                          docCity = docCity +" " + arr[i] ;
                          i++;
                       }
                       docCity = docCity.toUpperCase();
                    }
                    if (line.startsWith("<DOCNO>")){ // Doc num
                        String[] arr =  line.split(" ");
                        if (arr.length >= 2)
                            docNo = arr [1] ;
                    }
                    line = br.readLine();
                }
                sb_docInfo.append(line);
                String text = sb_text.toString();
                String doc_text = sb_docInfo.toString();
                Document doc = new Document(docNo, parentFileName , docCity);
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

    private String getParentFileName(String filePathName) {
        String[] split = filePathName.split("\\\\");
        return split[split.length-1];
    }

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
        return Cities;
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

