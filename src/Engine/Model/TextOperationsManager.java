package Engine.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TextOperationsManager {
    ReadFile reader;
    Parse parser;
    String curposPath;
    HashMap<Document, HashSet<String>> DocumentsTerms;

    public TextOperationsManager(String curposPath) {
        this.reader = new ReadFile();
        this.parser = new Parse();
        this.curposPath = curposPath;
    }

    public void StartTextOperations(){
        public ArrayList<String> filesPathsList = new ArrayList<>();


    }
}
