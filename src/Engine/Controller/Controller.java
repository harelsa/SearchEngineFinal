package Engine.Controller;

import Engine.Model.Model;
import Engine.Model.TextOperationsManager;
import Engine.View.View;

import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable implements Observer {
    private Model  model;
    private View view ;
    @Override
    public void update(Observable o, Object arg) {

        if ( o == view) {
            switch ((String) arg) {
                case "run":
                    System.out.println("good");
                    /* The next two lines in comment only for test 8/12/18 10:45*/
//                    model.run(view.corpus_txt_field.getText(), view.posting_txt_field.getText(), view.check_stemming.isSelected());
////                    model.run(view.corpus_txt_field.getText(), view.posting_txt_field.getText(), view.check_stemming.isSelected());
                    model.run("C:\\Users\\Nadav\\Desktop\\Engine Project\\corpus\\corpus-10files", "C:\\Users\\Nadav\\Desktop\\Engine Project\\resources\\Postings", false);
                    break;
                case "show_dic":
                    model.showDic();
                    break;
                case "load_to_memory":
                    model.loadDicToMemory();
                    break;
                case "reset":
                    model.resetAll();
                    break;

                default:
                    System.out.println("no match");
            }
        }
        if ( o == model) {
            switch ((String) arg) {
                case "finished":
                    view.updateLangLIst(model.list_lang);
                    break;
            }
        }
    }




    public void setVM(View view, Model model) {
        this.model = model;
        this.view = view ;
    }
}
