package Engine.Model;



import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parse {


    // enums
    private static double THOUSAND = Math.pow(10, 3);
    private static double MILLION = Math.pow(10, 6);
    private static double BILLION = Math.pow(10, 9);
    private static double TRILLION = Math.pow(10, 12);

    //private static ConcurrentHashMap<String, Term> AllTerms = new ConcurrentHashMap<>();  // < str_term , obj_term >  // will store all the terms in curpos
    private static HashSet<String> stopwords = new HashSet<>();
    private static HashSet<String> specialwords = new HashSet<>();
    private static HashSet<String> specialchars = new HashSet<>();
    private static HashSet<String> months = new HashSet<>();

    private static FileReader stopwords_fr; // read stop words from the file
    private static FileReader specialwords_fr;
    private static FileReader specialchars_fr;
    private static FileReader months_fr;

    static {
        try {
            stopwords_fr = new FileReader("src\\Engine\\resources\\stop_words.txt");
            specialwords_fr = new FileReader("src\\Engine\\resources\\special_words.txt"); // read stop words from the file
            specialchars_fr = new FileReader("src\\Engine\\resources\\special_chars.txt");
            months_fr = new FileReader("src\\Engine\\resources\\months.txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //[0-9]{1,2}(/|-)[0-9]{1,2}(/|-)[0-9]{4}
    //Pattern NumberThousand = Pattern.compile("\\d* \\w Thousand");
    //Pattern PRICE_DOU_DOLLAR = Pattern.compile( "$" + "\\d*" +" " + "(billion|million|Million|Billion)");
    private static Pattern NUMBER_ADDS = Pattern.compile("\\d+" + " " + "(Thousand|Million|Billion|Trillion|percent|percentage|Dollars)");
    private static Pattern PRICE_MBT_US_DOLLARS = Pattern.compile("\\d+" + " " + "(million|billion|trillion)" + " " + "U.S" + " " + "dollars");
    private static Pattern PRICE_DOU = Pattern.compile("\\d+" + "(m|bn) " + "(Dollars)");
    private static Pattern PRICE_FRACTION_DOLLARS = Pattern.compile("[0-9]*" + " " + "[0-9]*" + "/" + "[0-9]*" + " " + "Dollars");
    private static Pattern DATE_DD_MONTH = Pattern.compile(/*"(3[01]|[0-2][0-9])"*/"(3[0-1]|[0-2][0-9]|[0-9])" + " " + "(january|february|march|april|may|june|july|august|september|october|november|december|jan|fab|mar|apr|jun|jul|aug|sep|oct|nov|dec)");
    private static Pattern DATE_MONTH_DD = Pattern.compile("(january|february|march|april|may|june|july|august|september|october|november|december|jan|fab|mar|apr|jun|jul|aug|sep|oct|nov|dec)" + " " + "(3[0-1]|[0-2][0-9]|[0-9])$" /*"[0-9]{1,2}" /*"(3[0-1]|[0-2][0-9])" */);
    private static Pattern PRICE_SIMPLE = Pattern.compile( "\\$"+ "\\d+");
    private static Pattern FRACTURE_SIMPLE = Pattern.compile("[0-9]*" + " " + "[0-9]*" + "/" + "[0-9]*$");
    private static Pattern DATE_MONTH_YYYY = Pattern.compile("(january|february|march|april|may|june|july|august|september|october|november|december|jan|fab|mar|apr|jun|jul|aug|sep|oct|nov|dec)" + " " + "([1-2][0-9][0-9][0-9]|[0-9][0-9][0-9] )$"); /*"[0-9]{4}");*/
    private static Pattern REGULAR_NUM = Pattern.compile("^[0-9]*$");
    private static Pattern DOUBLE_NUM = Pattern.compile("^[0-9]*$" + "." + "^[0-9]*$");
    private static Pattern BETWEEN = Pattern.compile("\\d+" + "and" + "\\d+");


    private SegmentFile segmantFile;
    private int termPosition;



    public Parse(SegmentFile segmantFile) {
        try {
//            FileReader stopwords_fr = new FileReader("src\\Engine\\resources\\stop_words.txt"); // read stop words from the file
//            FileReader specialwords_fr = new FileReader("src\\Engine\\resources\\special_words.txt"); // read stop words from the file
//            FileReader specialchars_fr= new FileReader("src\\Engine\\resources\\special_chars.txt");
            BufferedReader stopwords_br = new BufferedReader(stopwords_fr);
            BufferedReader specialwords_br = new BufferedReader(specialwords_fr);
            BufferedReader specialchars_br = new BufferedReader(specialchars_fr);
            BufferedReader months_br = new BufferedReader(months_fr);

            this.segmantFile = segmantFile;
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
            while ((curr_line = months_br.readLine()) != null) {
                months.add(curr_line);
            }
            //SegmentFile parserSegmentFile = new SegmentFile();
            termPosition = 0;

        } catch (Exception e) {

        }


    }

    public HashSet<String> parse(String text, Document currDoc) {
        termPosition = 0;
        //text = remove_stop_words(text);
        String[] tokens;
        tokens = StringUtils.split(text , " \\/");
        SortedMap<String, Term> AllTerms = getTerms(tokens, currDoc);
        currDoc.updateAfterParsing();
       // segmantFile.signToSpecificPartition(AllTerms , currDoc);
        return null;
    }

    /**
     * check witch pattars the tokens match
     *
     * @param tokensArray
     * @param currDoc
     */
    private SortedMap<String, Term> getTerms(String[] tokensArray, Document currDoc) {
        TreeMap<String, Term> docTerms = new TreeMap<String, Term>((Comparator) (o1, o2) -> {
            String s1 = ((String)(o1)).toLowerCase();
            String s2 = ((String)(o2)).toLowerCase();
            return s1.compareTo(s2);
        });  // < str_term , obj_term >  // will store all the terms in curpos
        String addTerm = "" ;
        for (int i = 0; i < tokensArray.length; ) {

            addTerm = "" ;
            //First law - save " phrase" - will be saved as phrase and single words
            if ( tokensArray[i].startsWith("\"") && !tokensArray[i].endsWith("\"")){
                int j = i ;
                StringBuilder phrase = new StringBuilder(tokensArray[j]);
                j++ ;
                while ( j < tokensArray.length && ( j - i ) < 6 ) {
                    if (tokensArray[j].endsWith("\"")) { // end of phrase
                        phrase = phrase.append(" " + tokensArray[j]);
                        String phrase_temp  = phrase.toString();
                        phrase_temp = cleanToken(phrase_temp) ;
                        System.out.println(phrase_temp);
                        if (docTerms.containsKey(phrase_temp)) {
                            Term tmp = docTerms.get(phrase_temp);
                            tmp.advanceTf();
                            tmp.addPosition(termPosition);
                            currDoc.addTerm(tmp);
                            termPosition++;
                            break;
                        } else { // new term
                            Term obj_term = new Term(termPosition, 1, phrase_temp);
                            termPosition++;
                            currDoc.addTerm(obj_term);
                            docTerms.put(phrase_temp, obj_term);
                            break;
                        } // ***** adding to doc terms ****
                    } else {
                        phrase = phrase.append(" "+ tokensArray[j]);
                        j++ ;
                    }

                }
            }
            // Second law  - save terms of capitals letters - Ashley Cummins Brittingham
            String temp_token = cleanToken(tokensArray[i] ) ;
            if ( i < tokensArray.length-1 && Character.isUpperCase(temp_token.charAt(0)) // check first letter is a capital
                    && !specialchars.contains(tokensArray[i].charAt(tokensArray[i].length()-1)) //check Cummins,
                    && !specialchars.contains(tokensArray[i+1].charAt(0)) // check ,Cummins
                    && Character.isUpperCase(cleanToken(tokensArray[i+1]).charAt(0)) // check capital of the second word
            ){
                int j = i ;
                StringBuilder long_term = new StringBuilder();
                //j++ ;
                 boolean stop = false ;
                 String what_to_add = "";
                while ( j < tokensArray.length && ( j - i ) < 6 ) {
                    temp_token = cleanToken(tokensArray[j] ) ;
                    if ( !specialchars.contains(tokensArray[j+1].charAt(0))
                            && Character.isUpperCase(temp_token.charAt(0))
                            && ( j < tokensArray.length-1
                            && !(months.contains(tokensArray[j]) && isNumber(tokensArray[j+1])))
                    ){  // add one word term
                        long_term = long_term.append(temp_token + " ");
                        what_to_add = temp_token ;
                        j++;
                    } else { // end of long term
                        if ( long_term.length() < 2) {
                            i=j ;
                            break;
                        }
                        what_to_add  = long_term.toString();
                       what_to_add = cleanToken(what_to_add) ;
                        stop = true;
                        i = j ;
                    }
                        System.out.println(what_to_add);
                        if (docTerms.containsKey(what_to_add)) {
                            Term tmp = docTerms.get(what_to_add);
                            tmp.advanceTf();
                            tmp.addPosition(termPosition);
                            currDoc.addTerm(tmp);
                            termPosition++;
                           if ( stop) break;
                        } else { // new term
                            Term obj_term = new Term(termPosition, 1,what_to_add);
                            termPosition++;
                            currDoc.addTerm(obj_term);
                            docTerms.put( what_to_add, obj_term);
                           if ( stop ) break;
                        } // ***** adding to doc terms ****
                }
            }
            if (addTerm.equals("") && ! tokensArray[i].equals("") && tokensArray[i] != null)
                tokensArray[i] = cleanToken(tokensArray[i]);
            //tokensArray[i] = remove_stop_words(tokensArray[i]);
            if (tokensArray[i].equals("")  || tokensArray[i].length() < 2  ) { // not a term
                i += 1;
                continue;
            }
            // check stop word
            if (!tokensArray[i].equals( "may") && stopwords.contains(tokensArray[i])) {
                i += 1;
                continue;
            }
            // check number with no special term
            if (isNumber(tokensArray[i]) && ( i == tokensArray.length-1 ||(i < tokensArray.length - 1  && (!specialwords.contains(cleanToken(tokensArray[i + 1].toLowerCase())) || !tokensArray[i+1].contains("/"))))) {

                if ( addTerm.equals("")) addTerm = check1WordPattern(tokensArray[i]) ; //regular num
                addTerm = "" ;
            }
            // check between
            if (addTerm.equals("")&& (tokensArray[i].equals("Between") || tokensArray[i].equals("between") ) && i < tokensArray.length-3  ){
                Matcher bet = BETWEEN.matcher( tokensArray[i+1]+tokensArray[i+2]+tokensArray[i+3] ) ;
                if ( bet.find()){
                    addTerm = tokensArray[i] +" "+ tokensArray[i+1]+" " + tokensArray[i+2]+" "+ tokensArray[i+3] ;
                    i= i+3 ;
                }
            }
            //  check if its date first ..
            if (addTerm.equals("")&& i < tokensArray.length - 1 && !isNumber(tokensArray[i])  ) {
                String temp_token1 = cleanToken(tokensArray[i + 1]);
                //date - < Month + decimal >
                Matcher dateFormatMatcher2 = DATE_MONTH_DD.matcher(tokensArray[i].toLowerCase() + " " + temp_token1.toLowerCase());
                if (dateFormatMatcher2.find()) {
                    String term = PairTokensIsDate2Format(tokensArray[i].toLowerCase(), temp_token1.toLowerCase());
                    //System.out.println("Term added: " + term);
                    addTerm = term ;

                }
                //date - < Month + YYYY >
                Matcher dateFormatMatcherYear = DATE_MONTH_YYYY.matcher(tokensArray[i].toLowerCase() + " " + temp_token.toLowerCase());
                if (addTerm.equals("") && dateFormatMatcherYear.find()) {
                    String term = PairTokensIsDate3Format(tokensArray[i].toLowerCase(), temp_token.toLowerCase());
                    //System.out.println("Term added: " + term);
                    addTerm = term ;

                }
                if (!addTerm.equals("")) i += 1;
                if ( tokensArray[i].equals("may") && addTerm.equals("") ) // stop word  - fix may
                {
                    i++;
                    continue;
                }
            }
            //  check if its $ or % ..
            if (addTerm.equals("") && (tokensArray[i].startsWith("$") || tokensArray[i].startsWith("%")) && i < tokensArray.length) {
                if (i < tokensArray.length - 1) {
                    String temp_token1 = cleanToken(tokensArray[i + 1]);
                    addTerm = check2WordsPattern(tokensArray[i], temp_token1) ;
                    if (!addTerm.equals("")) i += 1;
                }
                if (addTerm.equals(""))  addTerm = check1WordPattern(tokensArray[i]);

            }
            // check a term with  num
            // Matcher regularNUMmatcher = REGULAR_NUM.matcher(tokensArray[i]);
            Matcher regularNUMmatcher2 = REGULAR_NUM.matcher(cleanToken(tokensArray[i].replaceAll("[mbn]", "")));
            if (addTerm.equals("") && (isNumber(tokensArray[i]) || regularNUMmatcher2.find()|| isNumber(tokensArray[i].replaceAll("[mbn]", "")))) {  // change the term only if the first token is a number !!!!

                if (i < tokensArray.length - 3) {
                    String temp_token3 = cleanToken(tokensArray[i + 3]);
                    String temp_token2 = cleanToken(tokensArray[i + 2]);
                    String temp_token1 = cleanToken(tokensArray[i + 1]);
                    addTerm = check4WordsPattern(tokensArray[i], temp_token1, temp_token2, temp_token3) ;
                    if (!addTerm.equals("")) i += 3;

                }
                if (addTerm.equals("") && i < tokensArray.length - 2) {
                    String temp_token1 = cleanToken(tokensArray[i + 1]);
                    String temp_token2 = cleanToken(tokensArray[i + 2]);
                    addTerm = check3WordsPattern(tokensArray[i], temp_token1, temp_token2) ;
                    if (!addTerm.equals("")) i += 2;
                }
                if (addTerm.equals("") &&  i < tokensArray.length - 1) {
                    String temp_token1= cleanToken(tokensArray[i + 1]);
                    addTerm = check2WordsPattern(tokensArray[i], temp_token1) ;
                    if (!addTerm.equals("")) i += 1;
                }
                if (addTerm.equals("") && i < tokensArray.length) {
                    addTerm = check1WordPattern(tokensArray[i]) ;
                }

            }
            //REGULAR WORD
            if (addTerm.equals("")){
                if ( !tokensArray [i].equals( "F><F"))
                    addTerm = tokensArray[i] ;
            }

            if ( addTerm.equals("")){ i++; continue;}

            if (docTerms.containsKey(addTerm)) {
                System.out.println(addTerm);
                Term tmp = docTerms.get(addTerm);
                tmp.advanceTf();
                tmp.addPosition(termPosition);
                currDoc.addTerm(tmp);
                termPosition++;
            } else { // new term

                // mutex
                //StringUtils.replaceAll(addTerm, "/'?,", "");
                Term obj_term = new Term(termPosition, 1, addTerm);
                termPosition++;
                currDoc.addTerm(obj_term);
                docTerms.put(addTerm, obj_term);
                //obj_term.addDoc(currDoc);
                //obj_term.addDoc(currDoc);
                System.out.println(addTerm);
            }
                i++;
        } //end for
        return docTerms ;
    }

    public static boolean isNumber(String string) {

        if (string == null || string.isEmpty()) {
            return false;
        }
        string = string.replaceAll("," , "") ;
        if ( string.equals(""))
            return  false ;
        int i = 0;
        if (string.charAt(0) == '-') {
            if (string.length() > 1) {
                i++;
            } else {
                return false;
            }
        }
        for (; i < string.length(); i++) {
            if (string.charAt(i) == '.') continue;
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String cleanToken(String token) {
        StringBuilder s = null;
        boolean changed = true  ;
        while (  token != null  && token.length() >0  &&!token.equals("") && changed ) {
            changed = false;
            s = new StringBuilder(token);
            if (specialchars.contains(""+s.charAt(0))) {
                s.deleteCharAt(0);
                token = s.toString() ;
                changed = true ;
            }
            if ( token != null  && token.length() >0  && !token.equals("")   && specialchars.contains(""+s.charAt(s.length()-1))) {
                s.deleteCharAt(s.length() - 1);
                changed = true;
            }
            token = s.toString() ;
        }
        return  token;
        //return s.toString();
    }

    private boolean isExpression(String token) {
        if (token.equals("U.S."))
            return true;
        return false;
    }

    private String check1WordPattern(String token) {
        if (token.equals("")) return  "" ;

        String term;
        String originalToken = token;
        token = cleanToken(token);
        // < $number >

        if (token.startsWith("$")) {

            String temp = token.replace("$", "");
            temp = temp.replaceAll("," , "");
            //Matcher regularNUMmatcher = REGULAR_NUM.matcher(temp);
            if (isNumber(temp)) {
                term = get_term_from_simple_price(temp, originalToken);
                return term ;
            }
        }
        //< number + % >
        if (token.endsWith("%")) {
            term = token;
            //System.out.println("Term added: " + term);
            return term ;
        }

        // < number >
        //Matcher regularNUMmatcher = REGULAR_NUM.matcher(token);
        token = token.replaceAll("," , "") ;
        if (isNumber(token)) {
            //if (Character.isDigit(token.charAt(0))) {
            term = get_term_from_simple_number(token);
            //System.out.println("Term added: " + term);
            return term;
        }

        // < simple token - just add as is >
        term = token;
        //System.out.println("Term added: " + term);
        return  term ;
    }

    private String get_term_from_simple_price(String token, String originalToken) {
        originalToken = originalToken.replace("$", "");
        token = token.replaceAll("," , "");

        double value = 0;
        if (isNumber(token)) {
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

    private String check2WordsPattern(String token1, String token2) {
        String term = "";
        String saved_original = token1;
//        token1 = cleanToken(token1);
//        token2 = cleanToken(token2);

        //datre < decimal + decimal\decimal  >
        Matcher fractureMatcher = FRACTURE_SIMPLE.matcher(token1 + " " + token2);
        if (fractureMatcher.find()) {
            term = token1 + " " + token2;
            //System.out.println("Term added: " + term);

            return term;
        }

        // check < $ + Decimal + million|billion >
        Matcher priceDouMatcher$ = PRICE_SIMPLE.matcher(token1);
        if (priceDouMatcher$.find() && token2.equals("million") || token2.equals("billion")) {
            String temp = "";
            BigDecimal value = new BigDecimal(0);
            temp = token1.replace("$", "");

            if (token2.equals("billion")) {
                temp = temp.replaceAll("," , "");
                if (isNumber(temp)) value = new BigDecimal(Double.parseDouble(temp) * BILLION);
            }
            if (token2.equals("million")) {
                temp = temp.replaceAll("," , "");
                if (isNumber(temp)) value = new BigDecimal( Double.parseDouble(temp) * MILLION);
            }

            term = get_term_from_simple_price(value+ "", "");
            //System.out.println("Term added: " + term);
            return term ;
        }

        // check < Decimal+m|bn + Dollars >
        Matcher priceDouMatcher = PRICE_DOU.matcher(token1 + " " + token2);
        if (priceDouMatcher.find()) {
            String temp = "";
            BigDecimal value = new BigDecimal(0);
            if (token1.endsWith("bn")) {
                temp = saved_original.replaceAll("bn", "");
                Matcher regularNUMmatcher = REGULAR_NUM.matcher(temp);
                if (regularNUMmatcher.find()) value = new BigDecimal(Double.parseDouble(temp) * BILLION) ;

            }
            if (token1.endsWith("m")) {
                temp = saved_original.replaceAll("m", "");

                if (isNumber(temp)) value = new BigDecimal(Double.parseDouble(temp) * MILLION) ;
            }

            term = get_term_from_simple_price(value + "", "");
            //System.out.println("Term added: " + term);
            return term ;
        }

        // check <decimal + NumberSize >
        Matcher numberSizeMatcher = NUMBER_ADDS.matcher(token1 + " " + token2);
        if (numberSizeMatcher.find()) {
            term = PairTokensIsNumberFormat(token1, token2);
            //System.out.println("Term added: " +term);
            return  term ;
        }
        //date < decimal + Month >
        Matcher dateFormatMatcher = DATE_DD_MONTH.matcher(token1 + " " + token2.toLowerCase());
        if (dateFormatMatcher.find()) {
            term = PairTokensIsDateFormat(token1, token2.toLowerCase());
            //System.out.println("Term added: " + term);
            return  term ;
        }
        return term;
    }

    /**
     * Check if term exits , and updates fields accordingly
     *
     * @param term
     */
    private void addToDocTerms(String term, Document currDoc) {

    }

    private String check3WordsPattern(String token1, String token2, String token3) {
        String term = "";
        token1 = cleanToken(token1);
        token2 = cleanToken(token2);
        token3 = cleanToken(token3);

        // check <decimal + fraction + dollars>
        Matcher decFractionDollarsMatcher = PRICE_FRACTION_DOLLARS.matcher(token1 + " " + token2 + " " + token3);
        if (decFractionDollarsMatcher.find()) {
            term = token1 + " " + token2 + " " + token3;
            //System.out.println("Term added: " + term);

            return term;
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
        return term ;


    }

    private String  check4WordsPattern(String token1, String token2, String token3, String token4) {
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
                    term = token1 + " M Dollars";
                    break;
                case "billion":
                    token1=token1.replaceAll("," ,"");
                    if (isNumber(token1))
                        term = (( checkVal(Double.parseDouble(token1) * THOUSAND)) ) + " M Dollars";
                    break;
                case "trillion":
                    token1= token1.replaceAll("," ,"");
                    if (isNumber(token1)) ;
                    term = (  (checkVal(Double.parseDouble(token1) * MILLION ) )) + " M Dollars";
                    break;

            }
            //System.out.println(term);

            return term ;
        }
        return term;
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
                token= token.replaceAll("," ,"");
                if (isNumber(token)) ;
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
        if (anotherToken.length() == 1) anotherToken = "0"+anotherToken ;
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
        if (token.length() == 1) token = "0"+token ;
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

        String[] words = StringUtils.split(str , " ");
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
