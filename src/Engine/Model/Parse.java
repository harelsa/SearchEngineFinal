package Engine.Model ;
import com.sun.deploy.util.StringUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parse {
    //final ArrayList<String> NUMBER_SIZES = new ArrayList<String>(Arrays.asList("Thousand", "Million", "Billion", "Trillion")){};
    private HashSet<String> documentTerms;
    private StringTokenizer stringTokenizer;

    //Pattern NumberThousand = Pattern.compile("\\d* \\w Thousand");
    Pattern NumberSize = Pattern.compile("\\d*" + " " + "(Thousand|Million|Billion|percent|percentage|Dollars)");


    public HashSet<String> parse(String text) {
        text = remove_stop_words(text);
        stringTokenizer = new StringTokenizer(text);
        documentTerms = new HashSet<>();
        getTermsFromTokens(stringTokenizer);

        return null ;
    }

    private void getTermsFromTokens(StringTokenizer stringTokenizer) {
        while(stringTokenizer.hasMoreTokens()){
            checkValidTerm(stringTokenizer.nextToken());
        }
    }

    private void checkValidTerm(String word) {
        String term = ""; // the finel term !

        word = word.replaceAll("[]\\[()?\",]", ""); // clean token
        if ( ! Character.isDigit(word.charAt(0)) )  word = word.replaceAll("[.]", ""); // clean token

        String anotherToken = stringTokenizer.nextToken(); // save next token to check

        anotherToken = anotherToken.replaceAll("[]\\[()?\",]", ""); // clean token
        if ( ! Character.isDigit(anotherToken.charAt(0)) )  anotherToken = anotherToken.replaceAll("[.]", "");

        String newWord = word + " " + anotherToken;

        // check <decimal + NumberSize >
        System.out.println("Pattern print: " + NumberSize.pattern());
        Matcher m = NumberSize.matcher(word + " " + anotherToken);
        if(m.find()) {
            String temp =anotherToken ;
            switch (temp ){
                case "Thousand" :
                    term = word + "K" ;
                    break;
                case "Million" :
                    term = word + "M" ;
                    break;
                case "Billion" :
                    term = word + "B" ;
                    break;
                case "percent" :
                    term = word + "%" ;
                    break;
                case "percentage" :
                    term = word + "%" ;
                    break;
                case "Dollars" :
                    term = word + "%" ;
                    break;
            }

        }
        System.out.println( term );

        //documentTerms.add(term);
    }




    public String remove_stop_words(String str) {
        String res = "" ;
        int k = 0, i, j;
        ArrayList<String> wordsList = new ArrayList<String>();
        ArrayList<String> result = new ArrayList<String>();
        String sCurrentLine;
        String[] stopwords = new String[2000];
        try {
            FileReader fr = new FileReader("src\\Engine\\resources\\stop_words.txt"); // read stop words from the file
            BufferedReader br = new BufferedReader(fr);
            while ((sCurrentLine = br.readLine()) != null) {
                stopwords[k] = sCurrentLine;
                k++;
            }
            StringBuilder builder = new StringBuilder(str);
            String[] words = builder.toString().split("\\s");
            for (String word : words) {
                wordsList.add(word);
                result.add(word) ;
            }
            for (int ii = 0; ii < wordsList.size(); ii++) {
                for (int jj = 0; jj < k; jj++) {
                    if (stopwords[jj].contains(wordsList.get(ii).toLowerCase())) {
                        result.remove(wordsList.get(ii));
                        break;
                    }
                }
            }
            for (String s : result) {
                res +=  s + " ";
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return res ;
    }

}




