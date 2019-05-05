package CS.methods.standard;

import CS.Util.ConfigUtil;
import org.apache.lucene.analysis.CharArraySet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class codeStopWord {
    public static CharArraySet CODE_STOP_WORDS_SET = null;

    static {
        try {
            ArrayList<String> stopWords = new ArrayList<>();
            FileReader fr = new FileReader(ConfigUtil.codeStopWord);
            BufferedReader bf = new BufferedReader(fr);
            String str = "";
            while ((str = bf.readLine()) != null) {
                stopWords.add(str);
            }
            bf.close();
            fr.close();

            final CharArraySet stopSet = new CharArraySet(stopWords, false);
            CODE_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
