package Engine.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parse {
    // enums
    private double THOUSAND = Math.pow(10, 3);
    private double MILLION = Math.pow(10, 6);
    private double BILLION = Math.pow(10, 9);
    private double TRILLION = Math.pow(10, 12);


    private static ConcurrentHashMap<String, Term> AllTerms = new ConcurrentHashMap<>();  // < str_term , obj_term >  // will store all the terms in curpos
    HashSet<String> stopwords = new HashSet<>();
    HashSet<String> specialwords = new HashSet<>();
    HashSet<String> specialchars = new HashSet<>();


    //[0-9]{1,2}(/|-)[0-9]{1,2}(/|-)[0-9]{4}
    //Pattern NumberThousand = Pattern.compile("\\d* \\w Thousand");
    //Pattern PRICE_DOU_DOLLAR = Pattern.compile( "$" + "\\d*" +" " + "(billion|million|Million|Billion)");
    Pattern NUMBER_ADDS = Pattern.compile("\"^[0-9]*$\"" + " " + "(Thousand|Million|Billion|Trillion|percent|percentage|Dollars)");
    Pattern PRICE_MBT_US_DOLLARS = Pattern.compile("\"^[0-9]*$\"" + " " + "(million|billion|trillion)" + " " + "U.S." + " " + "dollars");
    Pattern PRICE_DOU = Pattern.compile("\"^[0-9]*$\"" + "(m|bn) " + "(Dollars)");
    Pattern PRICE_FRACTION_DOLLARS = Pattern.compile("\"^[0-9]*$\"" + " " + "\"^[0-9]*$\"" + "/" + "\"^[0-9]*$\"" + " " + "(Dollars)");
    Pattern DATE_DD_MONTH = Pattern.compile(/*"(3[01]|[0-2][0-9])"*/"(3[0-1]|[0-2][0-9])" + " " + "(january|february|march|april|may|june|july|august|september|october|november|december|JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)");
    Pattern DATE_MONTH_DD = Pattern.compile("(january|february|march|april|may|june|july|august|september|october|november|december|JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)" + " " + "(3[0-1]|[0-2][0-9])$" /*"[0-9]{1,2}" /*"(3[0-1]|[0-2][0-9])" */);
    Pattern PRICE_SIMPLE = Pattern.compile("$" + "\"^[0-9]*$\"");
    Pattern FRACTURE_SIMPLE = Pattern.compile("\"^[0-9]*$\"" + " " + "\"^[0-9]*$\"" + "/" + "\"^[0-9]*$\"");
    Pattern DATE_MONTH_YYYY = Pattern.compile("(january|february|march|april|may|june|july|august|september|october|november|december|JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)" + " " + "([1-2]|[0-9][0-9][0-9])$"); /*"[0-9]{4}");*/
    Pattern REGULAR_NUM = Pattern.compile("^[0-9]*$");

    public Parse() {
        try {

            FileReader stopwords_fr = new FileReader("src\\Engine\\resources\\stop_words.txt"); // read stop words from the file
            FileReader specialwords_fr = new FileReader("src\\Engine\\resources\\special_words.txt"); // read stop words from the file
            FileReader specialchars_fr= new FileReader("src\\Engine\\resources\\special_chars.txt");
            BufferedReader stopwords_br = new BufferedReader(stopwords_fr);
            BufferedReader specialwords_br = new BufferedReader(specialwords_fr);
            BufferedReader specialchars_br = new BufferedReader(specialchars_fr);
            String curr_line;

            while ((curr_line = stopwords_br.readLine()) != null) {
                stopwords.add(curr_line);
            }
            while ((curr_line = specialwords_br.readLine()) != null) {
                specialwords.add(curr_line);
            }
            while ((curr_line = specialchars_br.readLine()) != null) {
                specialchars.add(curr_line);
            }
        } catch (Exception e) {

        }


    }

    public HashSet<String> parse(String text, Document currDoc) {
        //text = remove_stop_words(text);
        HashMap<String, Term> AllTerms = new HashMap<>();  // < str_term , obj_term >  // will store all the terms in curpos
        String[] tokens = text.split(" ");
        AllTerms = getTerms(tokens, currDoc);
        SegmentFile parserSegmentFile = new SegmentFile(AllTerms, currDoc);
        parserSegmentFile.writeToFile();
        return null;
    }

    /**
     * check witch pattars the tokens match
     *
     * @param tokensArray
     * @param currDoc
     */
    private HashMap<String, Term> getTerms(String[] tokensArray, Document currDoc) {
        HashMap<String, Term> docTerms = new HashMap<>();  // < str_term , obj_term >  // will store all the terms in curpos
        for (int i = 0; i < tokensArray.length; ) {


           //tokensArray[i] = remove_stop_words(tokensArray[i]);
            if ( tokensArray[i].equals("")) {
                i += 1;
                continue;
            }

            tokensArray[i] = cleanToken(tokensArray[i] ) ;

            if (stopwords.contains(tokensArray[i]) ) {
                i += 1;
                continue;
            }

           // Matcher regularNUMmatcher = REGULAR_NUM.matcher(tokensArray[i]);
            if ( i < tokensArray.length - 1 &&isNumeric(tokensArray[i]) && !(specialwords.contains(tokensArray[i+1].toLowerCase()))){
//                System.out.println("token1: " + tokensArray[i] + " token2: " + tokensArray[i+1]);
                addToDocTerms(tokensArray[i],currDoc);
                i += 1;
                continue;
            }


            //  check if its date first ..

            if (i < tokensArray.length - 1) {
                tokensArray[i+1] = cleanToken(tokensArray[i+1] ) ;
                //date - < Month + decimal >
                Matcher dateFormatMatcher2 = DATE_MONTH_DD.matcher(tokensArray[i].toLowerCase() + " " + tokensArray[i + 1].toLowerCase());
                if (dateFormatMatcher2.find()) {
                    String term = PairTokensIsDate2Format(tokensArray[i].toLowerCase(), tokensArray[i + 1].toLowerCase());
                    //System.out.println("Term added: " + term);
                    addToDocTerms(term, currDoc);
                    i += 2;
                    continue;
                }
                //date - < Month + YYYY >
                Matcher dateFormatMatcherYear = DATE_MONTH_YYYY.matcher(tokensArray[i].toLowerCase() + " " + tokensArray[i + 1].toLowerCase());
                if (dateFormatMatcherYear.find()) {
                    String term = PairTokensIsDate3Format(tokensArray[i].toLowerCase(), tokensArray[i + 1].toLowerCase());
                    //System.out.println("Term added: " + term);
                    addToDocTerms(term, currDoc);
                    i += 2;
                    continue;
                }
            }

            //  check if its $ or % ..

            if ((tokensArray[i].startsWith("$") || tokensArray[i].startsWith("%")) && i < tokensArray.length) {
                if (i < tokensArray.length - 1) {
                    tokensArray [i+1] = cleanToken(tokensArray[i+1] ) ;
                    if (check2WordsPattern(tokensArray[i], tokensArray[i + 1], currDoc)) {
                        i += 2;
                        continue;
                    }
                }
                if (check1WordPattern(tokensArray[i], currDoc)) {
                    i += 1;
                    continue;
                }
            }

            // check a term with  num
            Matcher regularNUMmatcher = REGULAR_NUM.matcher(tokensArray[i]);
            Matcher regularNUMmatcher2 = REGULAR_NUM.matcher(cleanToken(tokensArray[i].replaceAll("[mbn]", "")));
            if (regularNUMmatcher.find() || regularNUMmatcher2.find()) {  // change the term only if the first token is a number !!!!


                if (i < tokensArray.length - 3) {
                    tokensArray [i+3] = cleanToken(tokensArray[i+3] ) ;
                    tokensArray [i+2] = cleanToken(tokensArray[i+2] ) ;
                    tokensArray [i+1] = cleanToken(tokensArray[i+1] ) ;
                    if (check4WordsPattern(tokensArray[i], tokensArray[i + 1], tokensArray[i + 2], tokensArray[i + 3], currDoc)) {
                        i += 4;
                        continue;
                    }
                }
                if (i < tokensArray.length - 2) {
                    tokensArray [i+1] = cleanToken(tokensArray[i+1] ) ;
                    tokensArray [i+2] = cleanToken(tokensArray[i+2] ) ;
                    if (check3WordsPattern(tokensArray[i], tokensArray[i + 1], tokensArray[i + 2], currDoc)) {
                        i += 3;
                        continue;
                    }
                }
                if (i < tokensArray.length - 1) {
                    tokensArray [i+1] = cleanToken(tokensArray[i+1] ) ;
                    if (check2WordsPattern(tokensArray[i], tokensArray[i + 1], currDoc)) {
                        i += 2;
                        continue;
                    }
                }
                if (i < tokensArray.length) {
                    if (check1WordPattern(tokensArray[i], currDoc)) {
                        i += 1;
                        continue;
                    }
                }

            }
            //System.out.println("Term added: " + cleanToken(tokensArray[i])  );
            addToDocTerms(tokensArray[i], currDoc);
            i++;
        }
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private String cleanToken(String token) {

        StringBuilder s = new StringBuilder(token);
        if ( specialchars.contains(token.charAt(0)))
            s.deleteCharAt(0);
        if ( specialchars.contains(token.charAt(s.length()-1)))
            s.deleteCharAt(s.length() -1 );
        return s.toString();
    }

    private boolean isExpression(String token) {
        if (token.equals("U.S."))
            return true;
        return false;
    }

    private boolean check1WordPattern(String token, Document currDoc) {
        if (token.equals("")) return false;

        String term;
        String originalToken = token;
        token = cleanToken(token);
        // < $number >

        if (token.startsWith("$")) {

            String temp = token.replace("$", "");
            temp = temp.replaceAll("," , "");
            Matcher regularNUMmatcher = REGULAR_NUM.matcher(temp);
            if (regularNUMmatcher.find()) {
                term = get_term_from_simple_price(temp, originalToken);
                //System.out.println("Term added: " + term);
                addToDocTerms(term, currDoc);
                ;
                return true;
            }
        }
        //< number + % >
        if (token.endsWith("%")) {
            term = token;
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            ;
            return true;
        }

        // < number >
        Matcher regularNUMmatcher = REGULAR_NUM.matcher(token);
        if (regularNUMmatcher.find()) {
            //if (Character.isDigit(token.charAt(0))) {
            term = get_term_from_simple_number(token);
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            ;
            return true;
        }

        // < simple token - just add as is >
        term = token;
        //System.out.println("Term added: " + term);
        addToDocTerms(term, currDoc);
        ;

        return false;
    }

    private String get_term_from_simple_price(String token, String originalToken) {
        originalToken = originalToken.replace("$", "");
        Matcher regularNUMmatcher = REGULAR_NUM.matcher(token);
        double value = 0;
        if (regularNUMmatcher.find()) {
            try {
                value = Double.parseDouble(token);
            } catch (Exception e) {

            }
            if (isBetween(value, 0, MILLION - 1))
                return originalToken + " Dollars";

            if (isBetween(value, MILLION, Double.MAX_VALUE))
                return checkVal(value / MILLION) + " M Dollars";
        }
        return "ERROR!!!";

    }

    private boolean check2WordsPattern(String token1, String token2, Document currDoc) {
        String term = "";
        String saved_original = token1;
        token1 = cleanToken(token1);
        token2 = cleanToken(token2);

        //datre < decimal + decimal\decimal  >
        Matcher fractureMatcher = FRACTURE_SIMPLE.matcher(token1 + " " + token2);
        if (fractureMatcher.find()) {
            term = token1 + " " + token2;
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            return true;
        }

        // check < $ + Decimal + million|billion >
        Matcher priceDouMatcher$ = PRICE_SIMPLE.matcher(token1);
        if (priceDouMatcher$.find() && token2.equals("million") || token2.equals("billion")) {
            String temp = "";
            double value = 0;
            temp = token1.replace("$", "");
            Matcher regularNUMmatcher = REGULAR_NUM.matcher(temp);
            if (token2.equals("billion")) {
//                // for test
//                System.out.println(currDoc.getDocNo());
//                System.out.println(temp);
                if (regularNUMmatcher.find()) value = Double.parseDouble(temp) * BILLION;
            }
            if (token1.endsWith("million")) {
                if (regularNUMmatcher.find()) value = Double.parseDouble(temp) * MILLION;
            }

            term = get_term_from_simple_price(value + "", "");
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            return true;
        }

        // check < Decimal+m|bn + Dollars >
        Matcher priceDouMatcher = PRICE_DOU.matcher(token1 + " " + token2);
        if (priceDouMatcher.find()) {
            String temp = "";
            double value = 0;
            if (token1.endsWith("bn")) {
                temp = saved_original.replaceAll("bn", "");
                Matcher regularNUMmatcher = REGULAR_NUM.matcher(temp);
                if (regularNUMmatcher.find()) value = Double.parseDouble(temp) * BILLION;

            }
            if (token1.endsWith("m")) {
                temp = saved_original.replaceAll("m", "");
                Matcher regularNUMmatcher = REGULAR_NUM.matcher(temp);
                if (regularNUMmatcher.find()) value = Double.parseDouble(temp) * MILLION;
            }

            term = get_term_from_simple_price(value + "", "");
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            ;
            return true;
        }

        // check <decimal + NumberSize >
        Matcher numberSizeMatcher = NUMBER_ADDS.matcher(token1 + " " + token2);
        if (numberSizeMatcher.find()) {
            term = PairTokensIsNumberFormat(token1, token2);
            //System.out.println("Term added: " +term);
            addToDocTerms(term, currDoc);
            ;
            return true;
        }
        //date < decimal + Month >
        Matcher dateFormatMatcher = DATE_DD_MONTH.matcher(token1 + " " + token2.toLowerCase());
        if (dateFormatMatcher.find()) {
            term = PairTokensIsDateFormat(token1, token2.toLowerCase());
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            ;
            return true;
        }
        return false;
    }

    /**
     * Check if term exits , and updates fields accordingly
     *
     * @param term
     */
    private void addToDocTerms(String term, Document currDoc) {
        System.out.println(term);
        if (term.equals(""))
            return;

        if (AllTerms.containsKey(term)) {
            // AllTerms.get(term).addDoc(currDoc);
        } else { // new term

            // mutex
            Term obj_term = new Term(0, 0);
            obj_term.addDoc(currDoc);
            AllTerms.put(term, obj_term);
        }
    }

    private boolean check3WordsPattern(String token1, String token2, String token3, Document currDoc) {
        String term = "";
        token1 = cleanToken(token1);
        token2 = cleanToken(token2);
        token3 = cleanToken(token3);

        // check <decimal + fraction + dollars>
        Matcher decFractionDollarsMatcher = PRICE_FRACTION_DOLLARS.matcher(token1 + " " + token2 + " " + token3);
        if (decFractionDollarsMatcher.find()) {
            term = token1 + " " + token2 + " " + token3;
            //System.out.println("Term added: " + term);
            addToDocTerms(term, currDoc);
            return true;
        }
//
//        // check <decimal + NumberSize >
//        Matcher numberSizeMatcher = NUMBER_SIZE.matcher(token1 + " " + token2);
//        if (numberSizeMatcher.find()) {
//            token1 =
//                    term = PairTokensIsNumberFormat(token1, token2);
//            System.out.println("Term added: " + term);
//            addToDocTerms(term , currDoc)  ; ;
//            return true;
//        }
//        //datre < decimal + Month >
//        Matcher dateFormatMatcher = DATE_DD_MONTH.matcher(token1 + " " + token2);
//        if (dateFormatMatcher.find()) {
//            term = PairTokensIsDateFormat(token1, token2);
//            System.out.println("Term added: " + term);
//            addToDocTerms(term , currDoc)  ; ;
//            return true;
//        }
////        //date - < Month + decimal >
////        Matcher dateFormatMatcher2 = DATE_MONTH_DD.matcher(token1 + " " + token2);
////        if (dateFormatMatcher2.find()) {
////            term = PairTokensIsDate2Format(token1, token2);
////            System.out.println("Term added: " + term);
////            addToDocTerms(term)  ; ;
////            return true;
////        }
        return false;


    }

    private boolean check4WordsPattern(String token1, String token2, String token3, String token4, Document currDoc) {
        String term = "";
        token1 = cleanToken(token1);
        token2 = cleanToken(token2);
        token3 = cleanToken(token3);
        token4 = cleanToken(token4);

        Matcher priceSizeUSdollarsMatcher = PRICE_MBT_US_DOLLARS.matcher(token1 + " " + token2 + " " + token3 + " " + token4);
        if (priceSizeUSdollarsMatcher.find()) {
            String temp = token2;
            switch (temp.toLowerCase()) {
                case "million":
                    term = token1 + "M Dollars";
                    break;
                case "billion":
                    term = ((int) Double.parseDouble(token1) * 1000) + "M Dollars";
                    break;
                case "trillion":
                    term = ((int) Double.parseDouble(token1) * 10000000) + "M Dollars";
                    break;

            }
            //System.out.println(term);
            addToDocTerms(term, currDoc);
            return true;
        }
        return false;
    }

    private String PairTokensIsNumberFormat(String token, String anotherToken) {
        String term = "";
        String temp = anotherToken;
        switch (temp.toLowerCase()) {
            case "thousand":
                term = token + "K";
                break;
            case "million":
                term = token + "M";
                break;
            case "billion":
                term = token + "B";
                break;
            case "percent":
                term = token + "%";
                break;
            case "percentage":
                term = token + "%";
                break;
            case "dollars":
                term = get_term_from_simple_price(token, token);
                break;
            case "trillion":
                double value = Double.parseDouble(token) * TRILLION;
                term = get_term_from_simple_number(value + "");
                break;
        }
        return term;
    }

    /* Month DD */
    private String PairTokensIsDate2Format(String token, String anotherToken) {
        String term = "";
        String temp = token;
        switch (temp.toLowerCase().substring(0,3)) {
            case "jan":
                term = "01-" + anotherToken;
                break;
            case "feb":
                term = "02-" + anotherToken;
                break;
            case "mar":
                term = "03-" + anotherToken;
                break;
            case "apr":
                term = "04-" + anotherToken;
                break;
            case "may":
                term = "05-" + anotherToken;
                break;
            case "jun":
                term = "06-" + anotherToken;
                break;
            case "jul":
                term = "07-" + anotherToken;
                break;
            case "aug":
                term = "08-" + anotherToken;
                break;
            case "sep":
                term = "09-" + anotherToken;
                break;
            case "oct":
                term = "10-" + anotherToken;
                break;
            case "nov":
                term = "11-" + anotherToken;
                break;
            case "dec":
                term = "12-" + anotherToken;
                break;
        }
        return term;

    }


    /* Month YYYY */
    private String PairTokensIsDate3Format(String token, String anotherToken) {
        String term = "";
        String temp = token;
        switch (temp.toLowerCase().substring(0,3)) {
            case "jan":
                term = anotherToken + "-01";
                break;
            case "feb":
                term = anotherToken + "-02";
                break;
            case "mar":
                term = anotherToken + "-03";
                break;
            case "apr":
                term = anotherToken + "-04";
                break;
            case "may":
                term = anotherToken + "-05";
                break;
            case "jun":
                term = anotherToken + "-06";
                break;
            case "jul":
                term = anotherToken + "-07";
                break;
            case "aug":
                term = anotherToken + "-08";
                break;
            case "sep":
                term = anotherToken + "-09";
                break;
            case "oct":
                term = anotherToken + "-10";
                break;
            case "nov":
                term = anotherToken + "-11";
                break;
            case "dec":
                term = anotherToken + "-12";
                break;
        }
        return term;

    }

    /* DD Month */
    private String PairTokensIsDateFormat(String token, String anotherToken) {
        String term = "";
        String temp = anotherToken;
        switch (temp.toLowerCase().substring(0,3)) {
            case "jan":
                term = "01-" + token;
                break;
            case "feb":
                term = "02-" + token;
                break;
            case "mar":
                term = "03-" + token;
                break;
            case "apr":
                term = "04-" + token;
                break;
            case "may":
                term = "05-" + token;
                break;
            case "jun":
                term = "06-" + token;
                break;
            case "jul":
                term = "07-" + token;
                break;
            case "aug":
                term = "08-" + token;
                break;
            case "sep":
                term = "09-" + token;
                break;
            case "oct":
                term = "10-" + token;
                break;
            case "nov":
                term = "11-" + token;
                break;
            case "dec":
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
    private String get_term_from_simple_number(String token) {
        double value = 0;
        try {
            value = Double.parseDouble(token);
        } catch (Exception e) {

        }

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

        StringBuilder res = new StringBuilder("");

        String[] words = str.split(" ");
        for (String s : words) {
            if (stopwords.contains(s) || s.equals(" ") || s.equals("")) {
                // Do something with the stop words found in the sample input or discard them.
            } else {
                res.append(s); // Append non-stop word to textOutput.
                res.append(" ");
            }

        }

        return res.toString();
    }

}
