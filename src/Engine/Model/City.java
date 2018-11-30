package Engine.Model;

import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 */
public class City {
    private  String state_name = "" ;
    private  String currency = "" ;
    private  String population = "" ;



    private HashMap < String , LinkedList> docs_appearences;


    public City(String state_name, String currency, String population) {
        this.state_name = state_name;
        this.currency = currency;
        this.population = population;
        docs_appearences = new HashMap<>( ) ;
    }

    public City() {
        docs_appearences = new HashMap<>( ) ;

    }


    public void setState_name(String state_name) {
        this.state_name = state_name;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPopulation(String population) {
        this.population = population;
    }
    public void setDocs_appearences(HashMap<String, LinkedList> docs_appearences) {
        this.docs_appearences = docs_appearences;
    }
}
