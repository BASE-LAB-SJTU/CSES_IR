package CS.Util;

import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    /**
     * Concate seperated method name from CodeParser into original one
     * @param raw
     * @return
     */
    public static String concateMethname(String raw) {
        String[] wordList = raw.trim().split("\\s+");
        List<String> concatList = new ArrayList<String>();
        for (int i = 0; i < wordList.length; i++) {
            if (i == 0) {
                concatList.add(wordList[i]);
            } else {
                String hump = "" + wordList[i].charAt(0);
                hump = hump.toUpperCase();
                hump += wordList[i].length() > 1 ? wordList[i].substring(1): "";
                concatList.add(hump);
            }
        }
        return StringUtils.join(concatList, "");
    }

    /**
     * Replace all reserved word 'AND', 'OR', 'NOT' with lowercase
     * @param str
     * @return
     */
    public static String replaceReservedWords(String str) {
        return str
                .replace("OR", "or")
                .replace("AND", "and")
                .replace("NOT", "not");
    }
}
