package CS.evaluation;

public class MetricsSet{
    public static final String[] METRICS = {"algorithm", "topk", "precision", "recall",
            "f-measure", "map", "mrr", "NDCG", "firstPos","time(ms)", "query number", "data size"};

    public static final String[] METRICS_PER_FILE = {"id", "topk", "p", "ap", "rr", "time"};

    public MetricsSet() {
        this.filename = "";
        this.topk = 5;
        this.precision = new Metrics();
        this.recall = new Metrics();
        this.fMeasure = new Metrics();
        this.MAP = new Metrics();
        this.MRR = new Metrics();
        this.NDCG = new Metrics();
        this.firstPos = new Metrics();
        this.time = new Metrics();
        this.queryNumb = 0;
        this.dataSize = 0;
    }

    public void add(MetricsSet another) {
        if (this.topk == another.topk) {
            this.precision.count += another.precision.count;
            this.precision.sum += another.precision.sum;
            this.recall.count += another.recall.count;
            this.recall.sum += another.recall.sum;
            this.fMeasure.count += another.fMeasure.count;
            this.fMeasure.sum += another.fMeasure.sum;
            this.MAP.count += another.MAP.count;
            this.MAP.sum += another.MAP.sum;
            this.MRR.count += another.MRR.count;
            this.MRR.sum += another.MRR.sum;
            this.NDCG.count += another.NDCG.count;
            this.NDCG.sum += another.NDCG.sum;
            this.firstPos.count += another.firstPos.count;
            this.firstPos.sum += another.firstPos.sum;
            this.time.count += another.time.count;
            this.time.sum += another.time.sum;
            this.queryNumb += another.queryNumb;
            this.dataSize += another.dataSize;
        } else {
            System.out.println("Top K doesn't match!");
            System.out.println(String.format("Error occured in file '%s' and '%s'", this.filename, another.filename));
            System.out.println(this.filename + " top K:" + this.topk);
            System.out.println(another.filename + " top K:" + another.topk);
        }
    }

    public String filename;
    public int topk;
    public Metrics precision;
    public Metrics recall;
    public Metrics fMeasure;
    public Metrics MAP;
    public Metrics MRR;
    public Metrics NDCG;
    public Metrics time;
    public Metrics firstPos;
    public int queryNumb;
    public int dataSize;

}