package CS.Search;


import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.JsonFormatUtil;
import CS.evaluation.MetricsSet;
import CS.methods.standard.standardQueryAnalyzer;
import CS.similarity.TfidfSimilarity;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class baseSearch {

    public static void main(String[] args) throws Exception {
        String exp = "CosBench/";
        String xlsxPath = "src/test/resources/"+exp+"query2answer.xlsx";
        String resPath = "src/test/resources/"+exp+"baseRes_"+ConfigUtil.TopK+".csv";
        String indexPath = "src/test/resources/"+exp+"index";

        base1000JavaProjSearch(xlsxPath, resPath,indexPath);

    }

    /**
     * Search function that returns evaluation result (for EXCEL format).
     * @param xlsxPath test file path
     * @param outputResultPath the path to store output csv file
     * @return metrics of evaluation result
     * @throws Exception
     */
    public static MetricsSet base1000JavaProjSearch(String xlsxPath, String outputResultPath,String indexPath) throws Exception {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        //searcher.setSimilarity(new TfidfSimilarity());
        QueryParser parser = new MultiFieldQueryParser(new String[]{"methbody", "methname"}, new standardQueryAnalyzer());
        List<String> querys = DatasetUtil.loadQuerysFromJson(xlsxPath);
        EvaluateUtil eu = new EvaluateUtil(DatasetUtil.loadJsonTrueResults(xlsxPath));
        int i = 0;
        for (String rawQuery: querys) {
            rawQuery = JsonFormatUtil.replaceReservedWords(rawQuery);
            Query query = parser.parse(QueryParser.escape(rawQuery));
            Date start = new Date();
            TopDocs docs = searcher.search(query, ConfigUtil.TopK);
            Date end = new Date();
            int numsHit = searcher.count(query);
            if (numsHit > 0) {
                eu.setFirstPos(searcher.search(query, numsHit), i, false);
            } else {
                eu.setFirstPos(null, i, true);
            }
            eu.setResult(docs, (end.getTime() - start.getTime()), i++, searcher);
        }
        eu.writeDefaultCSV(outputResultPath);
        MetricsSet testResult = eu.getTotalResultPerFile();
        testResult.dataSize = reader.maxDoc();
        return testResult;
    }


    public static List<String> search(String rawQuery) throws Exception {
        String indexPath = ConfigUtil.index;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new TfidfSimilarity());
        QueryParser parser = new MultiFieldQueryParser(new String[]{"methbody", "methname", "api"}, new StandardAnalyzer());
        rawQuery = JsonFormatUtil.replaceReservedWords(rawQuery);
        Query query = parser.parse(QueryParser.escape(rawQuery));
        TopDocs docs = searcher.search(query, ConfigUtil.TopK);
        List<String> resultList = new ArrayList<>();
        for (ScoreDoc sd : docs.scoreDocs) {
            resultList.add(searcher.doc(sd.doc).get("methbody"));
        }
        return resultList;
    }


}