package Engine;
import Engine.Model.Parse ;
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
        String str = "$450,000 BLA BLA 0.6 percent BLA BLA 10.6 percentage BLA BLA  10,123 55 Million 55 Billion 1010.56 6% 1,000,000 Dollars 1.7320 Dollars $450,000,000 7 Trillion 10,123,000,000";
        //String str = "7 Trillion";
        Parse parser = new Parse();
        parser.parse(str);
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
