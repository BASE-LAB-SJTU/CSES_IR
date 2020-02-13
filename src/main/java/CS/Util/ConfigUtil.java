package CS.Util;

public class ConfigUtil {
    //luSearch
    public static final String wordnetPath = "src/test/resources/lusearch/WordNet/wn_s.pl";

    //QECK
    public static final int QECKTopNWords = 5;
    public static final String SOIndex = "src/test/resources/QECK/SOindex/";
    public static final String SOOrigin = "src/test/resources/QECK/SOorigin/";

    //Codehow
    public static final int TopkAPI = 10;
    // FunctionScoreQuery expression config
    public static final String w_ti_q_13 = "1.5";
    public static final String w_ti_q_2 = "1.0";
    public static final String p_norm = "3";
    public static final String p_norm_exp = "1/3";
    //api data
    public static final String APIOrigin = "src/test/resources/Codehow/API/javaAPI.json";
    public static final String APIIndex = "src/test/resources/Codehow/API/index";

    //config for all methods
    public static final int TopK = 3;
    public static final String codebaseOrigin = "src/test/resources/CosBench/codebase/";
    public static final String codebaseIndex = "src/test/resources/CosBench/index";
    public static final String QASet = "src/test/resources/CosBench/query2answer.json";
    public static final String codeStopWord = "src/test/resources/stopwords/codeStopWord";
    public static final String codeQueryStopWord = "src/test/resources/stopwords/codeQueryStopWord";

    //evaluation result
    public static final String BaseLuceneEvaluateResult = "src/test/resources/evaluateResult/BaseLuceneEvaluateResult_"+ConfigUtil.TopK+".csv";
    public static final String QECKEvaluateResult = "src/test/resources/evaluateResult/QECKEvaluateResult_"+ConfigUtil.TopK+".csv";
    public static final String LuSearchEvaluateResult = "src/test/resources/evaluateResult/luSearchEvaluateResult_"+ConfigUtil.TopK+".csv";
    public static final String YeSearchEvaluateResult = "src/test/resources/evaluateResult/YeSearchEvaluateResult_"+ConfigUtil.TopK+".csv";
    public static final String CodeHowEvaluateResult = "src/test/resources/evaluateResult/CodeHowEvaluateResult_"+ConfigUtil.TopK+".csv";
    public static final String CodennEvaluateResult = "src/test/resources/evaluateResult/CodennEvaluateResult_"+ConfigUtil.TopK+".csv";

    //search result
    public static final String BaseLuceneSearchResult = "src/test/resources/searchResult/BaseLuceneSearchResult.txt";
    public static final String QECKSearchResult = "src/test/resources/searchResult/QECKSearchResult.txt";
    public static final String LuSearchSearchResult = "src/test/resources/searchResult/LuSearchSearchResult.txt";
    public static final String CodeHowSearchResult = "src/test/resources/searchResult/CodeHowSearchResult.txt";
    public static final String CodennSearchResult = "src/test/resources/searchResult/CodennSearchResult.txt";
    public static final String YeSearchSearchResult = "src/test/resources/searchResult/YeSearchSearchResult.txt";

}
