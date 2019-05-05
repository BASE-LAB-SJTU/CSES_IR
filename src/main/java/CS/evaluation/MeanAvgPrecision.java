package CS.evaluation;

import java.util.List;
import java.util.Map;


public class MeanAvgPrecision {
    public static double getAveragePrecision(String[] ranking, List<String> trueResult, int k) {
        double avPrec = 0.0;
        double j = 1;
        for (int i = 0; i < k; i++) {
            if (trueResult.contains(ranking[i])) {
                avPrec += j / (i + 1);
                j++;
            }
        }

        return avPrec / trueResult.size();
    }


    public static double getAveragePrecision(String[] ranking, List<String> trueResult) {
        return getAveragePrecision(ranking,trueResult,ranking.length);
    }

}
