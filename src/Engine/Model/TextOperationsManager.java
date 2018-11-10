package Engine.Model;
import javafx.util.Pair;
import java.io.File;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;

public class TextOperationsManager {
    ReadFile reader;
    Parse parser;
    String curposPath;
    HashMap<Document, HashSet<String>> DocumentsTerms;
    public ArrayList<String> filesPathsList;

    /* FOR TEST ONLY */
    public static int filesCounter;
    public static int docsCounter;



    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        this.parser = new Parse();
        this.curposPath = curposPath;
        filesPathsList = new ArrayList<>();
    }

    public void StartTextOperations(){
        initFilesPathList(curposPath);
        readAndParse();

    }

    private void readAndParse() {
        for (int i = 0; i < filesPathsList.size(); i++) {
            readAndParseOneFile(filesPathsList.get(i));
        }
    }

    private void readAndParseOneFile(String filePath) {
        System.out.println("Parsing documents from filepath: " + filePath);
        ArrayList<Pair<String, String>> documentsFromFile = reader.readFile(filePath); // The Pair is: <docNo, FullDocContent>
        for (int i = 0; i < documentsFromFile.size(); i++) {
            // Need to add more methods here
            String docText = getTextFromFullDoc(documentsFromFile.get(i).getValue());
            Document document = new Document(documentsFromFile.get(i).getKey(), filePath);
            parser.parse(docText, document);
            docsCounter++;
        }
        filesCounter++;
        System.out.println("Number of files parsing: " + filesCounter);
        System.out.println("Number of documents parsing: " + docsCounter);

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

