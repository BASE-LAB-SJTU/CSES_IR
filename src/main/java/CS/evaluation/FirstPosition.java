package CS.evaluation;

import java.util.List;

public class FirstPosition {
    /**
     * Get the position of the first RIGHT result from returned list from ranking model
     * @param ranking   ranking result
     * @param trueResult    the true result of the query
     * @return first position n
     */
    public static  int getFirstPos(int[] ranking, List<String> trueResult) {
        for (int i = 0; i < ranking.length; i++) {
            if (trueResult.contains(ranking[i])) {
                return i;
            }
        }
        return -1;
    }
}
