package CS.Util;

import CS.evaluation.*;
import com.csvreader.CsvWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;

import static CS.evaluation.NDCG.NDCG;

public class EvaluateUtil {

    String[][] ranks = null;
    float[][] scores = null;
    List<String>[] trueResults = null;
    int firstPos[] = null;
    long[] times = null;
    int size = 0;
    IndexSearcher searcher = null;
    public EvaluateUtil(List<String>[] trueResults,IndexSearcher s) {
        if (trueResults == null) throw new Error("true result is null");
        this.trueResults = trueResults;
        this.size = trueResults.length;
        this.times = new long[size];
        this.ranks = new String[size][];
        this.scores = new float[size][];
        this.firstPos = new int[size];
        this.searcher = s;
    }

    public String[][] evaluates() {
        String[][] results = new String[size][];
        for (int i = 0; i < size; i++) {
            results[i] = evaluate(i);
        }
        return results;
    }

    private String[] evaluate(int i) {
        String[] rank = ranks[i];
        List<String> trueResult = trueResults[i];
        PRF prf = new PRF(rank, trueResult);
        String p = String.valueOf(prf.computePrecisionAtK());
        String r = String.valueOf(prf.computeRecalls());
        String f = String.valueOf(prf.getFmeasure());
        String ap = String.valueOf(MeanAvgPrecision.getAveragePrecision(rank, trueResult));
        String rr = String.valueOf(MeanReciprocalRank.getReciprocalRank(rank, trueResult));
        String NDCG = String.valueOf(NDCG(rank, trueResult));
        String firstPos = String.valueOf(this.firstPos[i]);
        return new String[]{String.valueOf(i+1), String.valueOf(ConfigUtil.TopK),p,r,f,ap,rr, NDCG, firstPos, String.valueOf(times[i])};
    }

    private void setFirstPos(int queryId, Query query) throws Exception{
        int numsHit = searcher.count(query);
        if (numsHit > 0) {
            TopDocs docs = searcher.search(query, numsHit);
            int[] rank = new int[docs.scoreDocs.length];
            for (int i = 0; i < rank.length; i ++) {
                rank[i] = docs.scoreDocs[i].doc;
            }
            firstPos[queryId] = FirstPosition.getFirstPos(rank, trueResults[queryId]);
        } else {
            firstPos[queryId] = -1;
        }
    }

    public void setResult(TopDocs docs, long time, int queryId, Query query) throws Exception {
        setFirstPos(queryId, query);

        ScoreDoc[] sds = docs.scoreDocs;
        int size = sds.length;
        String[] rank = new String[size];
        float[] score = new float[size];
        for (int i = 0; i < size; i++) {
            rank[i] = searcher.doc(sds[i].doc).get("id");
            score[i] = sds[i].score;
        }
        ranks[queryId] = rank;
        scores[queryId] = score;
        times[queryId] = time;
    }

    private void writeCSV(String csvFilePath, String[][] contents) {
        try {
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("UTF-8"));
            String[] csvHeaders = MetricsSet.METRICS_PER_FILE;
            csvWriter.writeRecord(csvHeaders);
            for (String[] content : contents) {
                csvWriter.writeRecord(content);
            }
            MetricsSet total = getTotalResultPerFile(contents);
            String[] totalLine = new String[] {"total", contents[0][1],
                    new DecimalFormat("0.0000").format(total.precision.sum / total.precision.count),
                    new DecimalFormat("0.0000").format(total.recall.sum / total.recall.count),
                    new DecimalFormat("0.0000").format(total.fMeasure.sum / total.fMeasure.count),
                    new DecimalFormat("0.0000").format(total.MAP.sum / total.MAP.count),
                    new DecimalFormat("0.0000").format(total.MRR.sum / total.MRR.count),
                    new DecimalFormat("0.0000").format(total.NDCG.sum / total.NDCG.count),
                    new DecimalFormat("0.0000").format(total.firstPos.sum / total.firstPos.count),
                    new DecimalFormat("0.0000").format(total.time.sum / total.time.count),
            };
            csvWriter.writeRecord(totalLine);
            csvWriter.close();
            System.out.println("write csv finish:" + csvFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call writeCSV with default value
     * @param resultFilePath the path of result file
     */
    public void writeDefaultCSV(String resultFilePath) {
        writeCSV(resultFilePath, evaluates());
    }

    /**
     * Print metrics and evaluate results in each file.
     * @param contents every metric with each code snippet
     * @return metrics of a  test file
     */
    private MetricsSet getTotalResultPerFile(String[][] contents) {
        String[] csvHeaders = MetricsSet.METRICS_PER_FILE;
        MetricsSet thisFileResult = new MetricsSet();
        int caseSize = csvHeaders.length - 2;
        Double[] RowSum = new Double[caseSize];
        Integer[] RowCount = new Integer[caseSize];
        for (int i = 0; i < caseSize; i ++) {
            RowSum[i] = 0.0;
            RowCount[i] = 0;
        }
        for (String[] row: contents) {
            for(int i = 0; i < caseSize; i++) {
                if (row[i + 2].equals("NaN")) continue;
                Double tmp = Double.parseDouble(row[i + 2]);
                if (tmp < 0) continue;
                RowSum[i] += tmp;
                RowCount[i] += 1;
            }
        }
        thisFileResult.topk = Integer.parseInt(contents[1][1]);
        thisFileResult.precision = new Metrics(RowCount[0], RowSum[0]);
        thisFileResult.recall = new Metrics(RowCount[1], RowSum[1]);
        thisFileResult.fMeasure = new Metrics(RowCount[2], RowSum[2]);
        thisFileResult.MAP = new Metrics(RowCount[3], RowSum[3]);
        thisFileResult.MRR = new Metrics(RowCount[4], RowSum[4]);
        thisFileResult.NDCG = new Metrics(RowCount[5], RowSum[5]);
        thisFileResult.firstPos = new Metrics(RowCount[6], RowSum[6]);
        thisFileResult.time = new Metrics(RowCount[7], RowSum[7]);
        thisFileResult.queryNumb = this.ranks.length;
        return thisFileResult;
    }


}
