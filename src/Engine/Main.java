package Engine;
import Engine.Model.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.print.Doc;
import java.util.HashMap;
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

//        // move to TExt operationg !!!
//        Parse parser = new Parse();
////        Document doc = new Document( "FBIS3-50" , "C:\\Users\\harel_000\\Desktop\\Retrival\\corpus\\corpus\\FB396002\\FB3960020") ;
//        parser.parse(str , doc );
//        Term term = new Term(0,0, "Nadav");
//        SegmentFilePartition sfp = new SegmentFilePartition("d:\\documents\\users\\bardanad\\Downloads\\Segment Files", 'a', 'd');
//        sfp.signNewTerm(term, doc);
//        sfp.extractTermFromSegmentFilePartition();


//        Document doc = new Document("NF-70", "d:\\documents\\users\\bardanad\\Downloads\\");
//        HashMap<String, Term> allTerms = new HashMap<>();
//        Term term = new Term(0,0, "Aba");
//        Term term1 = new Term(0,0, "baba");
//        Term term2 = new Term(0,0, "Home");
//        Term term3 = new Term(0,0, "Zzz");
//        Term term4 = new Term(0,0, "Yuli");
//        allTerms.put("Aba", term);
//        allTerms.put("baba", term1);
//        allTerms.put("Home", term2);
//        allTerms.put("Zzz", term3);
//        allTerms.put("Yuli", term4);
//        SegmentFile segmentFile = new SegmentFile();
//        segmentFile.signToSpecificPartition(allTerms, doc);



//        HashMap<String, Term> allTerms, Document currDoc

//        TextOperationsManager textOperationsManager = new TextOperationsManager("d:\\documents\\users\\bardanad\\Downloads\\corpus-split");
        TextOperationsManager textOperationsManager = new TextOperationsManager("D:\\documents\\users\\bardanad\\Downloads\\corpus");
        //TextOperationsManager textOperationsManager = new TextOperationsManager("D:\\documents\\users\\bardanad\\corpus\\test");
//        TextOperationsManager textOperationsManager = new TextOperationsManager("d:\\documents\\users\\harelsa\\Downloads\\corpus");
        textOperationsManager.StartTextOperations();
        //System.out.println("lalal");
        //textOperationsManager.p();
        textOperationsManager.BuildCitiesPosting();




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
