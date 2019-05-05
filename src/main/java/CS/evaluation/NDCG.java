package CS.evaluation;

import java.util.List;

public class NDCG {
    public static double IDCG(int TureResultSize) {
        double idcg = 0;
        final int itemRelevance = 1;
        for (int i = 0 ; i < TureResultSize; i++) {
            idcg += (Math.pow(2, itemRelevance) - 1) * (Math.log(2) / Math.log(i + 2));
        }
        return idcg;
    }

    public  static double NDCG(String[] ranking, List<String> trueResult) {
        double dcg = 0;
        final double itemRelevance = 1;
        final double idcg = IDCG(trueResult.size());
        for (int i = 0; i < ranking.length; i ++) {
            if (trueResult.contains(ranking[i])) {
                int rank = i + 1;
                dcg += (Math.pow(2, itemRelevance) - 1.0) * (Math.log(2) / Math.log(rank + 1));
            }
        }
        return dcg / idcg;
    }
}
