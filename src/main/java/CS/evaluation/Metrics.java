package CS.evaluation;


public class Metrics {
    public int count;
    public double sum;

    public Metrics(int count, double sum) {
        this.count = count;
        this.sum = sum;
    }

    public Metrics() {
        this.count = 0;
        this.sum = 0;
    }
}