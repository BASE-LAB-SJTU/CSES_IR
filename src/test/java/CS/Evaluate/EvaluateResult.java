package CS.Evaluate;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import static java.lang.Math.min;

public class EvaluateResult {
    static String QAPath = ConfigUtil.QASet;
    static String evalResultPath = ConfigUtil.BaseLuceneEvaluateResult;
    static String searchResult = ConfigUtil.BaseLuceneSearchResult;
    static int topK = ConfigUtil.TopK;


    public static void main(String[] args) throws Exception {
        evaluation();
    }


    static void evaluation() throws Exception {
        EvaluateUtil eu = new EvaluateUtil(DatasetUtil.loadTrueResults(QAPath),null);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(searchResult));
            String str = "";
            int i=0;
            while((str= reader.readLine()) != null){
                String[] qts = str.split(",");
                qts = Arrays.copyOfRange(qts, 0, min(qts.length, topK));
                eu.setSearchResult(qts,i);
                i+=1;
            }
            eu.writeEvaluateResultCSV(evalResultPath);

        } catch (FileNotFoundException ex) {
            System.out.print(ex.getMessage());
        }
    }

}
