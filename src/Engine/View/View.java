package Engine.View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Optional;

public class View extends Observable  {
    @FXML
    public javafx.scene.control.TextField corpus_txt_field ;
    public javafx.scene.control.TextField posting_txt_field;
    public javafx.scene.control.CheckBox check_stemming;
    public javafx.scene.control.ChoiceBox lang_list;
    public javafx.scene.control.Button show_dic_btn;
    public javafx.scene.control.Button load_dic_btn;
    public javafx.scene.control.Button reset_btn;
    public javafx.scene.layout.AnchorPane anchore_pane;


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
        /* The next two lines in comment only for test 8/12/18 10:45*/
//        if ( corpus_txt_field.getText().isEmpty() || posting_txt_field.getText().isEmpty())
//            JOptionPane.showMessageDialog(null, "One or  more Paths is missing", "Error", JOptionPane.ERROR_MESSAGE);
//        else {
            setChanged();
            notifyObservers("run");
            load_dic_btn.setDisable(false );
            show_dic_btn.setDisable(false );
            reset_btn.setDisable(false);
            }
 //       }



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


    public  void show_dic_pressed() throws Exception{
        Stage secondaryStage = new Stage();
        ScrollPane  sp =  new ScrollPane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dic_view.fxml"));
        Parent root = fxmlLoader.load();
        secondaryStage.setTitle("Dic view");
        Scene scene = new Scene(root, 600, 600) ;
        secondaryStage.setScene(scene);
        //need to load dic
        secondaryStage.show();


    }

    public void reset_btn_pressed() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset");
        alert.setHeaderText("Are you sure ? ");
        alert.setContentText (" This will reset all the posting files and Dictionaries that saved . ");
        Optional<ButtonType> option = alert.showAndWait();
        if ( ButtonType.OK.equals(option.get())){
            //System.out.println("dsad");
            notifyObservers("reset");
        }else {

        }


    }
}
