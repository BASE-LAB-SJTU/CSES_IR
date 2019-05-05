package CS.methods.QECK;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CS.Util.ConfigUtil;
import CS.methods.standard.standardSnippetAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class GetTopTerms {

    public static void main(String[] args) {
    }

    public static String getTopN(List<Entry<String, Float>> sortedMap, int N) {
        String keywords = "";
        for (int i = 0; i < N; i++) {
            keywords += " ";
            keywords += sortedMap.get(i).getKey();
        }
        return keywords;
    }

    public static String printIDF(TopDocs td, IndexSearcher searcher, IndexReader reader, int topn) {
        //final String QECKpath = ConfigUtil.QECKTempIndexPath;     //origianlly
        //final String QECKpath = ConfigUtil.demoIndexPath;       //new added
        try {

          /*  IndexWriterConfig iwc = new IndexWriterConfig(null);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(
                    FSDirectory.open(Paths.get(QECKpath)), iwc);
            for (ScoreDoc sd : td.scoreDocs) {

                System.out.println("doc number:" + writer.numDocs());
                System.out.println("writer:" + writer.toString());
                Document dd = searcher.doc(sd.doc);
                System.out.println("doc:" + dd);
                writer.addDocument(dd);
                System.out.println("after Writer:" + writer.toString());
            }
            writer.commit();
            writer.close();*/

            //Directory directory = FSDirectory.open(Paths.get(QECKpath));
//            Directory directory = FSDirectory.open(Paths.get(soIndexPath));     //new added
//            IndexReader reader = DirectoryReader.open(directory);
            List<LeafReaderContext> list = reader.leaves();
            Map<String, Float> map = new HashMap<String, Float>();

            for (ScoreDoc sd : td.scoreDocs) {
                Terms terms = reader.getTermVector(sd.doc, "content");
                //System.out.println("sd doc:" + sd.doc);
                //if (terms == null)  continue;
                TermsEnum tn = terms.iterator();
                BytesRef thisTerm = null;
                BytesRef text;
                String maxtext = "";
                float maxtf = 0;
                float maxidf = 0;
                while ((text = tn.next()) != null) {
                    float tf = (float) Math.sqrt((double) tn.totalTermFreq());
                    //float idf = (float) (Math.log((double) (reader.numDocs() + 1L) / (double) (tn.docFreq() + 1L)) + 1.0D);     //original
                    float idf = (float) (Math.log((double) (td.scoreDocs.length + 1L) / (double) (tn.docFreq() + 1L)) + 1.0D);     //original
                    String termText = text.utf8ToString();
                    //System.out.println("term:" + termText + " tf:" + tf + " idf:" + idf + " total:" + tf * idf);
                    map.put(termText, tf * idf);
                    if(tf * idf > maxtf * maxidf) {
                        maxtf = tf;
                        maxidf = idf;
                        maxtext = termText;
                    }
                }
                //System.out.println("term:" + maxtext + " tf:" + maxtf + " idf:" + maxidf + " total:" + maxtf * maxidf);

//                System.out.println("\n\n\n");
            }
//            for (LeafReaderContext ar : list) {
//                Terms term = ar.reader().terms("snippet");
//                if (term == null) continue;
//                TermsEnum tn = term.iterator();
//                BytesRef text;
//                while ((text = tn.next()) != null) {
//                    float tf = (float) Math.sqrt((double) tn.totalTermFreq());
//                    float idf = (float) (Math.log((double) (reader.numDocs() + 1L) / (double) (tn.docFreq() + 1L)) + 1.0D);
//                    String termText = text.utf8ToString();
//                    map.put(termText, tf * idf);
//                }
//            }
//            reader.close();
//            directory.close();
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

    public static String getTFIDF(TopDocs td, IndexSearcher searcher, int topn) {

        return null;
    }

    public static String getTF(TopDocs td, IndexSearcher searcher, int topn) {
        try {
            for (ScoreDoc sd : td.scoreDocs) {
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}