package Engine.View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

public class View extends Observable  {
    @FXML
    public javafx.scene.control.TextField corpus_txt_field ;
    public javafx.scene.control.TextField posting_txt_field;
    public javafx.scene.control.CheckBox check_stemming;
    public javafx.scene.control.ChoiceBox lang_list;


    private Scene scene;
    private Stage parent;

    public void browseCorpus() {

        JFileChooser fc = new JFileChooser() ;
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false );
        int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            corpus_txt_field.appendText(selectedFile.getAbsolutePath());
        }
    }

    public void browsePosting() {
        JFileChooser fc = new JFileChooser() ;
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false );
        int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            posting_txt_field.appendText(selectedFile.getAbsolutePath());
        }
    }

    public void  run_btn_pressed () {
        System.out.println("pressed");
        if ( corpus_txt_field.getText().isEmpty() || posting_txt_field.getText().isEmpty())
            JOptionPane.showMessageDialog(null, "One or  more Paths is missing", "Error", JOptionPane.ERROR_MESSAGE);
        else {
            setChanged();
            notifyObservers("run");
        }

    }

    public void setScene(Scene scene) {
        this.scene = scene ;
    }

    public void setParent(Stage primaryStage) {
        this.parent = primaryStage ; 
    }

    public void updateLangLIst(String[] list_lang) {
        ArrayList<String> lang = new ArrayList<String>(Arrays.asList(list_lang));
        ObservableList<String> list = FXCollections.observableArrayList(lang);
        lang_list.setItems(list);
    }
}
