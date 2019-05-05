package CS.evaluation;

import java.util.List;
import java.util.Map;


public class PRF {

    double tp = 0;
    int topK = 0;
    int trSize = 0;

    private final double BETA_ONE = 1.0;

    public PRF(String[] ranking, List<String> trueResult, int k) {
        if (ranking.length < k) throw new Error("k is bigger than topNDoc");
        double tp = 0;
        for (int i = 0; i < k; i++) {
            if (trueResult.contains(ranking[i])) {
                tp++;
            }
        }
        this.trSize = trueResult.size();
        this.topK = Math.min(trSize,k);
        this.tp = tp;
    }

    public PRF(String[] ranking, List<String> trueResult) {
        this(ranking, trueResult, ranking.length);
    }

    public double computePrecisionAtK() {
        return tp / topK;
    }

    public double computeRecalls() {
        return tp / trSize;
    }

    public double getFmeasure() {
        return getFmeasure(BETA_ONE);
    }

    public double getFmeasure(double beta) {
        double p = computePrecisionAtK();
        double r = computeRecalls();
        beta *= beta;
        return (1 + beta) * p * r / (beta * (p + r));
    }


}
