package Engine.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parse {
    // enums
    private double THOUSAND = Math.pow(10, 3);
    private double MILLION = Math.pow(10, 6);
    private double BILLION = Math.pow(10, 9);

    //final ArrayList<String> NUMBER_SIZES = new ArrayList<String>(Arrays.asList("Thousand", "Million", "Billion", "Trillion")){};
    private HashSet<String> documentTerms;
    private StringTokenizer stringTokenizer;

    //Pattern NumberThousand = Pattern.compile("\\d* \\w Thousand");
    Pattern NumberSize = Pattern.compile("\\d*" + " " + "(Thousand|Million|Billion|percent|percentage|Dollars)");
    Pattern DATE_DD_MONTH = Pattern.compile("(3[01]|[0-2][0-9])" + " " + "(January|February|March|April|May|June|July|August|September|October|November|December|JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)");
    Pattern DATE_MONTH_DD = Pattern.compile("(January|February|March|April|May|June|July|August|September|October|November|December|JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)" + " " + "(3[01]|[0-2][0-9])");


    public HashSet<String> parse(String text) {
        text = remove_stop_words(text);
        String[] tokens = text.split(" ");
        documentTerms = new HashSet<>();
        getTerms(tokens);
        return null;
    }

    private void getTerms(String[] tokensArray) {
        for (int i = 0; i < tokensArray.length; ) {
            if (i < tokensArray.length - 3) {
                if (check4WordsPattern(tokensArray[i], tokensArray[i + 1], tokensArray[i + 2], tokensArray[i + 3])) {
                    i += 4;
                    continue;
                }
            }
            if (i < tokensArray.length - 2) {
                if (check3WordsPattern(tokensArray[i], tokensArray[i + 1], tokensArray[i + 2])) {
                    i += 3;
                    continue;
                }
            }
            if (i < tokensArray.length - 1) {
                if (check2WordsPattern(tokensArray[i], tokensArray[i + 1])) {
                    i += 2;
                    continue;
                }
            }
            if (i < tokensArray.length) {
                if (check1WordPattern(tokensArray[i])) {
                    i += 1;
                    continue;
                }
            }
        }
    }

    private String cleanToken(String token) {
        token = token.replaceAll("[]\\[()?\",]", ""); // clean token
        if (!Character.isDigit(token.charAt(0))) token = token.replaceAll("[.]", ""); // clean token
        return token;
    }

    private boolean check1WordPattern(String token) {
        // check decimal num
        token = cleanToken(token);
        String term;
        if (Character.isDigit(token.charAt(0))) {
            term = get_term_to_save(token);
            System.out.println("Term added: " + term);
            documentTerms.add(term);
            return true;
        }
        return false;
    }

    private boolean check2WordsPattern(String token1, String token2) {
        String term = "";
        token1 = cleanToken(token1);
        token2 = cleanToken(token2);

        // check <decimal + NumberSize >
        Matcher numberSizeMatcher = NumberSize.matcher(token1 + " " + token2);
        if (numberSizeMatcher.find()) {
            term = PairTokensIsNumberFormat(token1, token2);
            System.out.println("Term added: " + term);
            documentTerms.add(term);
            return true;
        }

        Matcher dateFormatMatcher = DATE_DD_MONTH.matcher(token1 + " " + token2);
        if (dateFormatMatcher.find()) {
            term = PairTokensIsDateFormat(token1, token2);
            System.out.println("Term added: " + term);
            documentTerms.add(term);
            return true;
        }

        Matcher dateFormatMatcher2 = DATE_MONTH_DD.matcher(token1 + " " + token2);
        if (dateFormatMatcher2.find()) {
            term = PairTokensIsDate2Format(token1, token2);
            System.out.println("Term added: " + term);
            documentTerms.add(term);
            return true;
        }
        return false;
    }

    private boolean check3WordsPattern(String s, String s1, String s2) {
        return false;
    }

    private boolean check4WordsPattern(String s, String s1, String s2, String s3) {
        return false;
    }

    private String getSpecialTermFromTwoTokens(String token, String anotherToken) {
        return null;
    }

    private String PairTokensIsNumberFormat(String token, String anotherToken) {
        String term = "";
        String temp = anotherToken;
        switch (temp) {
            case "Thousand":
                term = token + "K";
                break;
            case "Million":
                term = token + "M";
                break;
            case "Billion":
                term = token + "B";
                break;
            case "percent":
                term = token + "%";
                break;
            case "percentage":
                term = token + "%";
                break;
            case "Dollars":
                term = token + "$";
                break;
        }
        return term;
    }

    /* Month DD */
    private String PairTokensIsDate2Format(String token, String anotherToken) {
        String term = "";
        String temp = token;
        switch (temp.toLowerCase()) {
            case "january":
                term = "01-" + anotherToken;
                break;
            case "february":
                term = "02-" + anotherToken;
                break;
            case "march":
                term = "03-" + anotherToken;
                break;
            case "april":
                term = "04-" + anotherToken;
                break;
            case "may":
                term = "05-" + anotherToken;
                break;
            case "june":
                term = "06-" + anotherToken;
                break;
            case "july":
                term = "07-" + anotherToken;
                break;
            case "august":
                term = "08-" + anotherToken;
                break;
            case "september":
                term = "09-" + anotherToken;
                break;
            case "october":
                term = "10-" + anotherToken;
                break;
            case "november":
                term = "11-" + anotherToken;
                break;
            case "december":
                term = "12-" + anotherToken;
                break;
        }
        return term;

    }

    /* DD Month */
    private String PairTokensIsDateFormat(String token, String anotherToken) {
        String term = "";
        String temp = anotherToken;
        switch (temp.toLowerCase()) {
            case "january":
                term = "01-" + token;
                break;
            case "february":
                term = "02-" + token;
                break;
            case "march":
                term = "03-" + token;
                break;
            case "april":
                term = "04-" + token;
                break;
            case "may":
                term = "05-" + token;
                break;
            case "june":
                term = "06-" + token;
                break;
            case "july":
                term = "07-" + token;
                break;
            case "august":
                term = "08-" + token;
                break;
            case "september":
                term = "09-" + token;
                break;
            case "october":
                term = "10-" + token;
                break;
            case "november":
                term = "11-" + token;
                break;
            case "december":
                term = "12-" + token;
                break;
        }
        return term;
    }

    /**
     * check and handle a token of decimal num
     *
     * @param token a number
     */
    private String get_term_to_save(String token) {
        double value = Double.parseDouble(token);

        if (isBetween(value, 0, THOUSAND - 1))
            return checkVal(value) + "";
        if (isBetween(value, THOUSAND, MILLION - 1))
            return checkVal(value / THOUSAND) + "K";

        if (isBetween(value, MILLION, BILLION - 1))
            return checkVal(value / MILLION) + "M";

        if (isBetween(value, BILLION, Double.MAX_VALUE))
            return checkVal(value / BILLION) + "B";


        return "ERROR!!!";
    }

    private String checkVal(double v) {
        Double d = v;
        if (v == d.intValue())
            return d.intValue() + "";
        else return v + "";
    }

    public static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x <= upper;
    }

    public String remove_stop_words(String str) {
        String res = "";
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
                result.add(word);
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
                res += s + " ";
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return res;
    }

}




