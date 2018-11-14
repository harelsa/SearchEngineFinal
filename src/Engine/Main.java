package Engine;
import Engine.Model.Document;
import Engine.Model.Parse ;
import Engine.Model.TextOperationsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("View/view.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
        //primaryStage.show();
        //String str = "isn't it great to parse in Java-Parser KOKO - STR U.S.A  , od don't it ? ";
        String str = "$450,000 BLA BLA June 16 16 June 0.6 percent BLA BLA 10.6 percentage BLA BLA 10,123 55 Million 55 Billion 1010.56 6% 1,000,000 Dollars 1.7320 Dollars $450,000,000 7 Trillion 10,123,000,000";
        //String str = "16 June";

        // move to TExt operationg !!!
        Parse parser = new Parse();
        Document doc = new Document( "FBIS3-50" , "C:\\Users\\harel_000\\Desktop\\Retrival\\corpus\\corpus\\FB396002\\FB3960020") ;
        parser.parse(str , doc );

        //TextOperationsManager textOperationsManager = new TextOperationsManager("d:\\documents\\users\\bardanad\\Documents\\Engine\\corpus\\corpus");
//        TextOperationsManager textOperationsManager = new TextOperationsManager("d:\\documents\\users\\harelsa\\Downloads\\corpus");
//        textOperationsManager.StartTextOperations();


        // Print words and Pos Tags
//        for (Tree leaf : leaves) {
//            Tree parent = leaf.parent(tree);
//            System.out.print( "'" + leaf.label().value() + "'");
//        }
//        System.out.println();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
