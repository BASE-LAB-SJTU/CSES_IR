package CS.Util;

public class ConfigUtil {
    //wordnet data
    public static final String wordnetPath = "src/test/resources/WordNet/wn_s.pl";

    public static final int QECKTopNWords = 10;

    //api data
    public static final String APIDocPath = "src/test/resources/API/result.json";
    public static final String APIIndexPath = "src/test/resources/API/index";

    //test
    public static final int TopK = 20;
    public static final int apiUsTopk = 5; // 10

    // FunctionScoreQuery expression config
    public static final String w_ti_q_13 = "1.5";
    public static final String w_ti_q_2 = "1.0";
    public static final String w_ti_q = "w_ti_q";
    public static final String p_norm = "3";
    public static final String p_norm_exp = "1/3";

    // sever position
    public static final String SOindex = "/mnt/sdb/yh/luceneSeries/SOindex/";
    public static final String APIindex = "/mnt/sdb/yh/luceneSeries/APIindex/";
    public static final String index = "/mnt/sdb/yh/luceneSeries/index/";

    public static final String codeStopWord = "src/test/resources/codeStopWord";
    public static final String codeQueryStopWord = "src/test/resources/codeQueryStopWord";


}
