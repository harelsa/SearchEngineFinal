package Engine.Model;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TextOperationsManager {
    ReadFile reader;
    Parse parser;
    String curposPath;
    HashMap<Document, HashSet<String>> DocumentsTerms;
    public ArrayList<String> filesPathsList;

    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        this.parser = new Parse();
        this.curposPath = curposPath;
        filesPathsList = new ArrayList<>();
    }

    public void StartTextOperations(){
        initFilesPathList(curposPath);


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

