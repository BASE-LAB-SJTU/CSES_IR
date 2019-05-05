package CS.Search;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.JsonFormatUtil;
import CS.evaluation.MetricsSet;
import CS.methods.Wordnet.WordnetQueryAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WordnetSearch {

    public static void main(String[] args) throws Exception {

        String exp = "CosBench/";//"within/guava/";
        String xlsxPath = "src/test/resources/"+exp+"query2answer.xlsx";
        String resPath = "src/test/resources/"+exp+"wordnetRes_"+ConfigUtil.TopK+".csv";
        String indexPath = "src/test/resources/"+exp+"index";

        WordNetSearch(xlsxPath,resPath,indexPath);
    }

    /**
     * Search and return evaluation result on csv test files
     * @param csvPath
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static MetricsSet WordNetSearch(String csvPath, String outputCsvPath, String indexPath) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new WordnetQueryAnalyzer();
        QueryParser parser = new MultiFieldQueryParser(new String[] {"methbody", "methname"}, analyzer);
        String[] querys = null;
        List<String>[] trueResults = null;
        querys = (String[]) DatasetUtil.loadQuerysFromJson(csvPath).toArray();
        trueResults = DatasetUtil.loadJsonTrueResults(csvPath);
        EvaluateUtil eu = new EvaluateUtil(trueResults);
        int i = 0;
        for (String rawQuery : querys) {
            Query query = parser.parse(QueryParser.escape(rawQuery));
            Date start = new Date();
            TopDocs docs = searcher.search(query, ConfigUtil.TopK);
            Date end = new Date();
            int numsHit = searcher.count(query);
            eu.setFirstPos(numsHit, i, searcher.search(query, numsHit));
            eu.setResult(docs, (end.getTime() - start.getTime()), i++, searcher);
        }
        eu.writeDefaultCSV(outputCsvPath);
        MetricsSet testResult = eu.getTotalResultPerFile();
        testResult.dataSize = reader.maxDoc();
        return testResult;
    }

    public static List<String> search(String rawQuery) throws Exception {
        String indexPath = ConfigUtil.index;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new WordnetQueryAnalyzer();
        QueryParser parser = new MultiFieldQueryParser(new String[] {"methbody", "methname", "api"}, analyzer);
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