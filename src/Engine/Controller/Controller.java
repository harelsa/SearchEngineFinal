package Engine.Controller;

import Engine.Model.TextOperationsManager;
import Engine.View.View;

import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable implements Observer {
    private TextOperationsManager  model;
    private View view ;
    @Override
    public void update(Observable o, Object arg) {
        switch ((String) arg) {
            case "run":
                System.out.println("good");
                model.StartTextOperations(view.corpus_txt_field.getText() , view.posting_txt_field.getText() ,view.check_stemming.isSelected() );
                break;
            case "show_dic":
                model.showDic();
                break;
            case "load_to_memory":
                model.laodDicToMemory();
                break;

            default:
                System.out.println("no match");
        }
    }




    public void setVM(View view, TextOperationsManager model) {
        this.model = model;
        this.view = view ;
    }
}
