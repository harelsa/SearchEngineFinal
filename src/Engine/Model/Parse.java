package Engine.Model ;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parse {
    //final ArrayList<String> NUMBER_SIZES = new ArrayList<String>(Arrays.asList("Thousand", "Million", "Billion", "Trillion")){};
    private HashSet<String> documentTerms;
    private StringTokenizer stringTokenizer;

    //Pattern NumberThousand = Pattern.compile("\\d* \\w Thousand");
    Pattern NumberSize = Pattern.compile("\\d*" + " " + "(Thousand|Million|Billion|percent|percentage|Dollars)");
    Pattern DATE_DD_MONTH = Pattern.compile("\\d*" + " " + "(January|February|March|April|May|June|July|August|September|October|November|December)");
    //"|JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGOST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)")
    Pattern DATE_MONTH_DD = Pattern.compile("\\d*" + " " + "(Thousand|Million|Billion|percent|percentage|Dollars)");


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

    private void checkValidTerm(String token) {
        String term = null; // the finel term !
        token = token.replaceAll("[]\\[()?\",]", ""); // clean token
        if ( ! Character.isDigit(token.charAt(0)) )  token = token.replaceAll("[.]", ""); // clean token

        String anotherToken = stringTokenizer.nextToken(); // save next token to check
        /* Cleaning anotherToken */
        anotherToken = anotherToken.replaceAll("[]\\[()?\",]", "");
        if ( ! Character.isDigit(anotherToken.charAt(0)) )
            anotherToken = anotherToken.replaceAll("[.]", "");

        term = getSpecialTermFromTwoTokens(token, anotherToken);
        if (term != null){
            documentTerms.add(term);
            System.out.println("Term added: " + term);
            return;
        }
        else{

            if (stringTokenizer.countTokens() > 0) // If "anotherToken" is not the last token.
                checkValidTerm(anotherToken);
            else{
                documentTerms.add(anotherToken);
                System.out.println("Term added: " + term);
            }
        }
    }

    private String getSpecialTermFromTwoTokens(String token, String anotherToken) {
        // check <decimal + NumberSize >
        Matcher numberSizeMatcher = NumberSize.matcher(token + " " + anotherToken);
        if(numberSizeMatcher.find())
            return PairTokensIsNumberFormat(token, anotherToken);
        Matcher dateFormatMatcher = DATE_DD_MONTH.matcher(token + " " + anotherToken);
        if (dateFormatMatcher.find())
            return PairTokensIsDateFormat(token, anotherToken);
        Matcher dateFormatMatcher2 = DATE_MONTH_DD.matcher(token + " " + anotherToken);
        if (dateFormatMatcher2.find())
            return PairTokensIsDate2Format(token, anotherToken);
        return null;
    }

    private String PairTokensIsNumberFormat(String token, String anotherToken) {
        String term = "";
        String temp =anotherToken ;
        switch (temp ){
            case "Thousand" :
                term = token + "K" ;
                break;
            case "Million" :
                term = token + "M" ;
                break;
            case "Billion" :
                term = token + "B" ;
                break;
            case "percent" :
                term = token + "%" ;
                break;
            case "percentage" :
                term = token + "%" ;
                break;
            case "Dollars" :
                term = token + "$" ;
                break;
        }
        return term;
    }
    private String PairTokensIsDate2Format(String token, String anotherToken) {
        return null;
    }

    private String PairTokensIsDateFormat(String token, String anotherToken) {
        String term = "";
        String temp =anotherToken ;
        switch (temp ){
            case "January" :
                term = "01-" + token ;
                break;
            case "February" :
                term = "02-" + token ;
                break;
            case "March" :
                term = "03-" + token ;
                break;
            case "April" :
                term = "04-" + token ;
                break;
            case "May" :
                term = "05-" + token ;
                break;
            case "June" :
                term = "06-" + token ;
                break;
            case "July" :
                term = "07-" + token ;
                break;
            case "Augost" :
                term = "08-" + token ;
                break;
            case "September" :
                term = "09-" + token ;
                break;
            case "October" :
                term = "10-" + token ;
                break;
            case "November" :
                term = "11-" + token ;
                break;
            case "December" :
                term = "12-" + token ;
                break;
        }
        return term;
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




