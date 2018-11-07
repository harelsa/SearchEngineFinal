package Engine.Model;

import java.util.HashMap;

public class Term {
    private int df = 0 ; // the number of docs the term is appearing
    private int tf = 0 ; // the number of
    private HashMap< Document , Integer > docs  ;// < doc obj , tr - number tomes the term appear in the doc >

}
