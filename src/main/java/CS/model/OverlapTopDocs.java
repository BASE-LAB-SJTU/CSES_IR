package CS.model;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.*;

// design for CodeHow : API understanding
public class OverlapTopDocs {

    private ScoreDoc[] FQNsd;
    private ScoreDoc[] descSd;
    private List<Integer> overlapDocId;

    public OverlapTopDocs(ScoreDoc[] sdlist1, ScoreDoc[] sdlist2) {
        this.FQNsd = sdlist1;
        this.descSd = sdlist2;
        overlapDocId = new ArrayList<Integer>();
        for (ScoreDoc sd1: sdlist1) {
            for (ScoreDoc sd2: sdlist2) {
                if(sd2.doc == sd1.doc) {
                    overlapDocId.add(sd1.doc);
                    break;
                }
            }
        }
    }

    public double minOverlapScore () {
        double minOlScore = Integer.MAX_VALUE;
        for (int docid : overlapDocId) {
            double addScore = getAddScoreFromOverlap(docid);
            if (addScore < minOlScore) {
                minOlScore = addScore;
            }
        }
        return minOlScore;
    }

    public boolean isInOverlap(int docid) {
        return overlapDocId.contains(docid);
    }

    public double maxNotOverlapScore() {
        double maxNO = 0;
        for(ScoreDoc sd: FQNsd) {
            if (!overlapDocId.contains(sd.doc) && sd.score > maxNO) {
                maxNO = sd.score;
            }
        }
        for(ScoreDoc sd: descSd) {
            if (!overlapDocId.contains(sd.doc) && sd.score > maxNO) {
                maxNO = sd.score;
            }
        }
        return maxNO;
    }

    /*
     *  Get API score from overlap set;
     *  if API is in overlap, return added value;
     *  else return (min * api) / (maxNo + a)
     */
    public double getAPIscore( int docid ) {
        double score = 0;
        double a = 0.1;
        if(overlapDocId.size() > 0) {
            if (isInOverlap(docid)) {
                score = getAddScoreFromOverlap(docid);
            } else {
                double x = minOverlapScore();
                double y = maxNotOverlapScore();
                score = (minOverlapScore() * getAddScoreFromOverlap(docid)) / (maxNotOverlapScore() + a * getMaxScore());
            }
        } else {
            score = getAddScoreFromOverlap(docid);
        }
        return score;
    }

    /*
     * Get API rank list by desc order;
     */
    public List<?> getAPIRank (IndexSearcher searcher, int topK) {
        Map<String, Double> rank = new HashMap<>();
        Set<Map.Entry<String, Double>> apiRankSet = new HashSet<>();
        try {
            for(ScoreDoc sd: FQNsd) {
                Double score = getAPIscore(sd.doc);
                String FQN = searcher.doc(sd.doc).getField("FQN").stringValue();
                if (!apiRankSet.contains(FQN)) {
                    rank.put(FQN, score);
                }
            }
            for (ScoreDoc sd: descSd) {
                Double score = getAPIscore(sd.doc);
                String description = searcher.doc(sd.doc).getField("FQN").stringValue();
                if (!apiRankSet.contains(description)) {
                    rank.put(description, score);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Map.Entry<String, Double>> rawList = getSortedListDesc(rank);
        List<Map.Entry<String, Double>> rtnList = new ArrayList<>();
        int min = topK < rawList.size() ?  topK : rawList.size();
        for(int i = 0; i <min; i++) {
            rtnList.add(rawList.get(i));
        }
        return rtnList;
    }

    /*
     *  Get added score from overlap set
     *  return respective score if not in overlap
     *  return added score if in
     */
    private double getAddScoreFromOverlap(int docid) {
        double addScore = 0;
        for (ScoreDoc sd: FQNsd) {
            if (sd.doc == docid) {
                addScore += sd.score;
            }
        }
        for (ScoreDoc sd: descSd) {
            if (sd.doc == docid) {
                addScore += sd.score;
            }
        }
        return addScore;
    }

    private double getMaxScore() {
        double max = 0, score = 0;
        for (ScoreDoc sd: FQNsd) {
            score = getAddScoreFromOverlap(sd.doc);
            if (score > max) max = score;
        }
        for (ScoreDoc sd: descSd) {
            score = getAddScoreFromOverlap(sd.doc);
            if (score > max) max = score;
        }
        return max;
    }

    /*
     * MAP SORT BY VALUE DESCEND ORDER
     */
    private List<Map.Entry<String, Double>> getSortedListDesc(Map<String, Double> inputMap) {

        List<Map.Entry<String, Double>> list =
                new ArrayList<Map.Entry<String, Double>>(inputMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return ((o2.getValue() - o1.getValue()) >= 0) ? 1 : -1;
            }
        });
        return list;
    }
}
