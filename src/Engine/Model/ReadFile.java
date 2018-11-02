package Engine.Model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


public class ReadFile {
    /* This map will contains the <DOCNO> as a key and the file path as value*/
    public ArrayList<String> filesPathsList;

    public ReadFile(){
        filesPathsList = new ArrayList<>();
    }

    public void readAllFiles() {
        final File folder = new File("C:\\Users\\nadavbar\\Documents\\SearchEngineCorpus\\corpusTest");
        listFilesOfFolder(folder);
        Long startTime = System.currentTimeMillis();
        System.out.println("Starting to split documents at: " + startTime);
        generateDocuments();
        Long finishTime = System.currentTimeMillis();
        System.out.println("Finish to split documents at: " + finishTime);
        Long totalTime = (startTime - finishTime);
        System.out.println("Total time: " + totalTime);

    }

    private void generateDocuments() {
        for (int i = 0; i < filesPathsList.size(); i++) {
            String documentFilePath = filesPathsList.get(i);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(filesPathsList.get(i)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                String parentFilePath = documentFilePath;
                String parentDirPath = documentFilePath.substring(0, documentFilePath.lastIndexOf('\\'));
                splitDocumentsFromFile(sb.toString(), parentDirPath);
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
    }

    private void splitDocumentsFromFile(String fileContent, String path) {
        String[] fileDocuments = fileContent.split("</DOC>");
        for (int i = 0; i < fileDocuments.length; i++) {
            String currentFullDocument = fileDocuments[i];
            String docNumber = getDocNumber(currentFullDocument);
            if (docNumber != null){
                File document = new File(path + File.separator + docNumber);
                try {
                    document.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(document));
                    writer.write(currentFullDocument);
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getDocNumber(String fileDocument) {
        String[] str = fileDocument.split("DOCNO>");
        if (str.length > 1) {// && str[1].split(" ").length > 1) {
            if (str[1].contains(" "))
                str[1].replaceAll("\\s+","");
            int indexOfArrow = str[1].indexOf("<");
            String docNumber = str[1].substring(0, indexOfArrow);
            return docNumber;
        }
        return null;
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

