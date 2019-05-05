package CS.model;

import CS.Util.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static CS.Util.ConfigUtil.p_norm_exp;

public class QueryTerm {

    Query query;

    public QueryTerm(Query inputQuery) {
        this.query = inputQuery;
    }

    public List<String> getTermList() {
        String[] termList;
        List<String> result = new ArrayList<>();
        String rawScript = this.query.toString();
        termList = (rawScript.split("\\)"));
        for(String st: termList) {
            result.add(st.substring(st.lastIndexOf(":")+1));
        }
        return result;
    }

    public List<String> getFilteredTermList(String filterFQN) {
        List<String> fullTerm = getTermList();
        List<String> filteredTerm = new ArrayList<>();
        for(String term: fullTerm) {
            term = term.toLowerCase();
            if(!filterFQN.toLowerCase().contains(term)) {
                filteredTerm.add(term);
            }
        }
        return filteredTerm;
    }

    /*
     * From term list get expression designed in CodeHow
     * @return SourceText used in JavascriptCompression
     */
    public String getScoreExpressionStr(boolean needFQN, String FQN, boolean isAnd, double apiscore, double maxTF) {
        String sim = "";
        List<String> terms;
        List<String> stemTerms = new ArrayList<String>();
        if (needFQN) {
            terms = getFilteredTermList(FQN);
            for(String term: terms) {
                String stemTerm = term.replace("_name", "").replace("_body", "");
                if (!stemTerms.contains(stemTerm)) {
                    stemTerms.add(stemTerm);
                }
            }
            terms.add(FQN);
            stemTerms.add(FQN);
        } else {
            terms = getTermList();
        }
        String demoninator = "", numerator = "";
        String zeroFactor = "";
        if (isAnd) {
            String maxNormal = Double.toString(maxTF);
            for (String term : terms) {
                String w_ti_q = String.format("pow(%s, %s)", ConfigUtil.w_ti_q_2, ConfigUtil.p_norm);
                String w_ti_d = String.format("pow(1 - %s, %s)", term + "/" + maxNormal, ConfigUtil.p_norm);
                if (term.equals(FQN)) {
                    if(needFQN)  w_ti_q = String.format("pow(%s, %s)", ConfigUtil.w_ti_q_13, ConfigUtil.p_norm);
                    String apiTerm = "(" + term + "-" + term + "+" + String.valueOf(apiscore) + "/" + maxNormal + ")";
                    w_ti_d = String.format("pow(1 - %s, %s)", apiTerm, ConfigUtil.p_norm);
                }
                String multi = String.format("%s * %s", w_ti_d, w_ti_q);
                numerator += (numerator.length() == 0) ? multi : " + " + multi;
                demoninator += (demoninator.length() == 0) ? w_ti_q : " + " + w_ti_q;
                String zeroItem = String.format("(((%s - 0.5) * %s == 0 )? 0 : 1)", term, term);
                if (term.equals(FQN)) {
                    zeroItem = String.format("(((%s - 0.5) * %s == 0 )? 0 : 1)", term, term);
                }
//                String zeroItem = String.format("(((%s - 0.5)== 0 )? 0 : 1)",  term);
                zeroFactor += (zeroFactor.length() == 0) ? zeroItem : " * " + zeroItem;
            }
            String sim_and = String.format("%s * (1 - pow ((%s) / (%s) , %s))", zeroFactor, numerator, demoninator, p_norm_exp );
            sim_and = String.format("1 - pow ((%s) / (%s) , %s)", numerator, demoninator, p_norm_exp );
            sim = sim_and;
        }else {
            String sim_or = "";
            sim = sim_or;
        }
        return sim;
    }
}
