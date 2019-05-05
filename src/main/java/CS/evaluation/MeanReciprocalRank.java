package CS.evaluation;

import java.util.List;
import java.util.Map;


public class MeanReciprocalRank {

    public static double getReciprocalRank(String[] ranking, List<String> trueResult) {
        double reciprocalRank = 0;
        for (int i = 0; i < ranking.length; i++) {
            if (trueResult.contains(ranking[i])) {
                reciprocalRank = 1.0 / (i + 1);
                break;
            }
        }
        return reciprocalRank;
    }

    public static double getMeanReciprocalRank(final List<String[]> rankings, final List<List<String>> trueResults) {
        double mrr = 0;

        for (int i = 0; i < rankings.size(); i++) {
            mrr += getReciprocalRank(rankings.get(i), trueResults.get(i));
        }
        mrr /= rankings.size();
        return mrr;
    }

}
