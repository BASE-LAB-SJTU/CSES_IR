package CS.methods.QECK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.*;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

public class GetTopTerms {

    private static String getTopN(List<Entry<String, Float>> sortedMap, int N) {
        String keywords = "";
        for (int i = 0; i < N; i++) {
            keywords += " ";
            keywords += sortedMap.get(i).getKey();
        }
        return keywords;
    }

    /**
     * get Top Term By TFIDF value.
     * @param td TopDocs
     * @param reader
     * @param topn top N
     * @return
     */
    public static String getTopTermByTFIDF(TopDocs td, IndexReader reader, int topn) {
        try {
            Map<String, Float> map = new HashMap<String, Float>();

            for (ScoreDoc sd : td.scoreDocs) {
                Terms terms = reader.getTermVector(sd.doc, "content");

                TermsEnum tn = terms.iterator();
                BytesRef text;
                float maxtf = 0;
                float maxidf = 0;
                while ((text = tn.next()) != null) {
                    float tf = (float) Math.sqrt((double) tn.totalTermFreq());
                    float idf = (float) (Math.log((double) (td.scoreDocs.length + 1L) /
                            (double) (tn.docFreq() + 1L)) + 1.0D);
                    String termText = text.utf8ToString();
                    map.put(termText, tf * idf);
                    if(tf * idf > maxtf * maxidf) {
                        maxtf = tf;
                        maxidf = idf;
                    }
                }
            }

            List<Entry<String, Float>> sortedMap = new ArrayList<Entry<String, Float>>(map.entrySet());
            Collections.sort(sortedMap, new Comparator<Entry<String, Float>>() {
                public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
                    return (int) ((o2.getValue() - o1.getValue()) * 1000000.0);
                }
            });

            return getTopN(sortedMap, topn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}