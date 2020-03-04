package CS.similarity;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

import java.io.IOException;

public class ExtBoolSimilarity extends Similarity {


    @Override
    public long computeNorm(FieldInvertState state) {
        final int numTerms;
        numTerms = state.getLength();
        if (state.getIndexCreatedVersionMajor() >= 7) {
            return SmallFloat.intToByte4(numTerms);
        } else {
            return SmallFloat.floatToByte315(lengthNorm(numTerms));
        }
    }

    @Override
    public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        final Explanation idf = idfExplain(collectionStats, termStats[0]);
        float[] normTable = new float[256];
        for (int i = 1; i < 256; ++i) {
            int length = SmallFloat.byte4ToInt((byte) i);
            float norm = lengthNorm(length);
            normTable[i] = norm;
        }
        normTable[0] = 1f / normTable[255];
        return new IDFStats(collectionStats.field(), boost, idf, normTable);
    }

    @Override
    public SimScorer simScorer(SimWeight weight, LeafReaderContext context) throws IOException {
        IDFStats idfstats = (IDFStats) weight;
        boolean isAPI = false;
        float apiscore = 0;
        return new ExtBoolSimScorer(idfstats, isAPI, apiscore );
    }

    /**
     * Computes a score factor for a simple term and returns an explanation
     * for that score factor.
     *
     * <p>
     * The default implementation uses:
     *
     * <pre class="prettyprint">
     * idf(docFreq, docCount);
     * </pre>
     *
     * Note that {@link CollectionStatistics#docCount()} is used instead of
     * {@link org.apache.lucene.index.IndexReader#numDocs() IndexReader#numDocs()} because also
     * {@link TermStatistics#docFreq()} is used, and when the latter
     * is inaccurate, so is {@link CollectionStatistics#docCount()}, and in the same direction.
     * In addition, {@link CollectionStatistics#docCount()} does not skew when fields are sparse.
     *
     * @param collectionStats collection-level statistics
     * @param termStats term-level statistics for the term
     * @return an Explain object that includes both an idf score factor
    and an explanation for the term.
     */
    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
        final long df = termStats.docFreq();
        final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
        final float idf = idf(df, docCount);
        return Explanation.match(idf, "idf(docFreq=" + df + ", docCount=" + docCount + ")");
    }

    /**
     * Compute an index-time normalization value for this field instance.
     *
     * @param length the number of terms in the field
     * @return a length normalization value
     */
    public float lengthNorm(int length) {
        return (float) (1.0 / Math.sqrt(length));
    }

    /*
     * @param freq the frequency of a term within a document
     * @return a score factor based on a term's within-document frequency
     */
    public  float tf(float freq)  {
        return (float) Math.sqrt(freq);
    };

    /*
     * @param docFreq the number of documents which contain the term
     * @param docCount the total number of documents in the collection
     * @return a score factor based on the term's document frequency
     */
    public float idf(long docFreq, long docCount) {
        return (float)(Math.log((double)docCount / (double)(docFreq + 1)) + 1.0D);
    }


    /**
     * Implement specialized Score setting for extended boolean model
     */
    private final class ExtBoolSimScorer extends Similarity.SimScorer{

        private final IDFStats stats;
        private final float weightValue;
        private final boolean isAPI;
        private final float APIscore;

        ExtBoolSimScorer(IDFStats stats, boolean termIsAPI, float APIscore) {
            this.stats = stats;
            this.weightValue = stats.queryWeight;
            this.isAPI = termIsAPI;
            this.APIscore = APIscore;
        }

        @Override
        public float score(int doc, float freq) throws IOException {
            final float raw = tf(freq) * weightValue; // compute tf(f)*weight
            float notInAPIScore = (float) (0.5 + (1-0.5) * raw);
            return (isAPI) ? APIscore : notInAPIScore;
        }

        /**
         * Computes the amount of a sloppy phrase match, based on an edit distance.
         *
         * @param distance
         */
        @Override
        public float computeSlopFactor(int distance) {
            return sloppyFreq(distance);    // same to TfidfSimilarity
        }

        /**
         * Calculate a scoring factor based on the data in the payload.
         *
         * @param doc
         * @param start
         * @param end
         * @param payload
         */
        @Deprecated
        @Override
        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            return 1;       // same to TfidfSimilarity
        }

        /** Computes the amount of a sloppy phrase match, based on an edit distance.
         * This value is summed for each sloppy phrase match in a document to form
         * the frequency to be used in scoring instead of the exact term count.
         *
         * <p>A phrase match with a small edit distance to a document passage more
         * closely matches the document, so implementations of this method usually
         * return larger values when the edit distance is small and smaller values
         * when it is large.
         *
         * @see PhraseQuery#getSlop()
         * @param distance the edit distance of this sloppy phrase match
         * @return the frequency increment for this match
         */
        @Deprecated
        public float sloppyFreq(int distance) {
            return 1.0F / (float)(distance + 1);
        }

    }


    /** Collection statistics for the TF-IDF model. The only statistic of interest
     * to this model is idf. */
    static class IDFStats extends Similarity.SimWeight {
        private final String field;
        /** The idf and its explanation */
        private final Explanation idf;
        private final float boost;
        private final float queryWeight;
        final float[] normTable;

        public IDFStats(String field, float boost, Explanation idf, float[] normTable) {
            this.field = field;
            this.idf = idf;
            this.boost = boost;
            this.queryWeight = boost * idf.getValue();
            this.normTable = normTable;
        }
    }
}