package Engine.Model ;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;



public class Parse {






    public void parse(String str) {
        str = remove_stop_words(str) ;




        return ;
    }


    private String remove_stop_words(String str) {
        String res = "" ;
        int k = 0, i, j;
        ArrayList<String> wordsList = new ArrayList<String>();
        ArrayList<String> result = new ArrayList<String>();
        String sCurrentLine;
        String[] stopwords = new String[2000];
        try {
            FileReader fr = new FileReader("src\\resources\\stop_words.txt"); // read stop words from the file
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




