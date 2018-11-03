package Engine;
import Engine.Model.Parse ;
import edu.stanford.nlp.trees.Tree;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View/view.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        String str = "isn't it great to parse in Java-Parser KOKO - STR U.S.A  , od don't it ? ";
        Parse parser = new Parse();
        Tree tree = parser.parse(str);

        List<Tree> leaves = tree.getLeaves();
        // Print words and Pos Tags
        for (Tree leaf : leaves) {
            Tree parent = leaf.parent(tree);
            System.out.print( "'" + leaf.label().value() + "'");
        }
        System.out.println();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
