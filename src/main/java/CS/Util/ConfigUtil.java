package CS.Util;

public class ConfigUtil {
    //luSearch
    public static final String wordnetPath = "src/test/resources/lusearch/WordNet/wn_s.pl";

    //QECK
    public static final int QECKTopNWords = 5;
    public static final String SOIndex = "src/test/resources/lusearch/QECK/SOindex/";
    public static final String SOOrigin = "src/test/resources/lusearch/QECK/SOorigin/";

    //Codehow
    public static final int TopkAPI = 5;
    // FunctionScoreQuery expression config
    public static final String w_ti_q_13 = "1.5";
    public static final String w_ti_q_2 = "1.0";
    public static final String p_norm = "3";
    public static final String p_norm_exp = "1/3";
    //api data
    public static final String APIOrigin = "src/test/resources/ExpandQueryBuilder/API/javaAPI.json";
    public static final String APIIndex = "src/test/resources/ExpandQueryBuilder/API/index";

    //config for all methods
    public static final int TopK = 20;
    public static final String codebaseOrigin = "src/test/resources/CosBench/codebase";
    public static final String codebaseIndex = "src/test/resources/CosBench/index";
    public static final String QASet = "src/test/resources/CosBench/query2answer.json";
    public static final String codeStopWord = "src/test/resources/stopwords/codeStopWord";
    public static final String codeQueryStopWord = "src/test/resources/stopwords/codeQueryStopWord";
    //evaluation result
    public static final String baseResult = "src/test/resources/result/baseResult_"+ConfigUtil.TopK+".csv";
    public static final String QECKResult = "src/test/resources/result/QECKResult_"+ConfigUtil.TopK+".csv";
    public static final String luSearchResult = "src/test/resources/result/luSearchResult_"+ConfigUtil.TopK+".csv";
    public static final String CodeHowResult = "src/test/resources/result/CodeHowResult_"+ConfigUtil.TopK+".csv";

}
