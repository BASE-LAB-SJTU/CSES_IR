package CS.ReportUtil;

import CS.Search.CodeHowSearch;
import CS.Search.QECKSearch;
import CS.Search.WordnetSearch;
import CS.Search.baseSearch;

import java.util.ArrayList;
import java.util.List;

public class APIUtil {

    public static void main(String[] args) throws Exception {
        String query = "Convert double to decimal";
        int K = 5;
        List<String> results = getTopKCode(query, K, ReportUtil.algorithm.Lucene);
        for(String i : results) {
            System.out.println("result:" +i);
        }
    }

    static List<String> getTopKCode(String nlQuery, int K,ReportUtil.algorithm al) throws Exception {
        if (K <= 0 ) {
            throw new Exception("K must be above zero!");
        }
        List<String> results = new ArrayList<>();
        switch (al) {
            case Lucene: {
                baseSearch bs = new baseSearch();
                results = bs.search(nlQuery);
                break;
            }
            case WordNet: {
                WordnetSearch ws = new WordnetSearch();
                results = ws.search(nlQuery);
                break;
            }
            case CodeHow: {
                CodeHowSearch cs = new CodeHowSearch();
                results = cs.search(nlQuery);
                break;
            }
            case QECK: {
                QECKSearch qeckSearch = new QECKSearch();
                results = qeckSearch.search(nlQuery);
                break;
            }
            default:
                throw new Exception("Algorithm not implemented!");
        }
        return results.subList(0, K);
    }
}
