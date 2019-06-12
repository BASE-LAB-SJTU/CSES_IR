package CS.similarity;

import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class TfidfSimilarity extends TFIDFSimilarity {
    @Override
    public float tf(float freq) {
        return (float) Math.sqrt(freq);
    }

    @Override
    public float idf(long docFreq, long docCount) {
        return (float)(Math.log((double)docCount / (double)(docFreq + 1)) + 1.0D);
    }

    @Override
    public float lengthNorm(int length) {
        return (float) (1.0 / Math.sqrt(length));
    }

    @Override
    public float sloppyFreq(int distance) {
        return 1.0F / (float)(distance + 1);
    }

    @Override
    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1;
    }
}
