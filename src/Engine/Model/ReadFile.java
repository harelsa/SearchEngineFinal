package Engine.Model;

import sun.misc.IOUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;


public class ReadFile {
    /* This map will contains the <DOCNO> as a key and the full document string as value*/
    public HashMap<String, String> documents;

    public void readAllFiles() {
        final File folder = new File("d:\\documents\\users\\bardanad\\Documents\\Engine\\corpus");
        ArrayList<String> filesPathsList = listFilesForFolder(folder);
        getDocuments(filesPathsList);
    }

    private void getDocuments(ArrayList<String> filesPathsList) {
        for (int i = 0; i < filesPathsList.size(); i++) {
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

    public ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> filesPaths = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filesPaths.add(fileEntry.getPath());
            }
        }
        return filesPaths;
    }


    private ArrayList<String> getFilesPathList(Stream<Path> paths) {
        paths.forEach(System.out::println);
        return null;
    }
}
//    public void readAllFiles(){
//        try (Stream<Path> paths = Files.walk(Paths.get("/home/you/Desktop"))) {
//            paths.filter(Files::isRegularFile).forEach(System.out::println);

