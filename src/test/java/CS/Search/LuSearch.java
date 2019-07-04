package CS.Search;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.StringUtil;
import CS.evaluation.MetricsSet;
import CS.methods.LuSearch.LuSearchQueryAnalyzer;
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

public class LuSearch {
    static String QAPath = ConfigUtil.QASet;
    static String evalResultPath = ConfigUtil.luSearchResult;
    static String codebaseIndexPath = ConfigUtil.codebaseIndex;
    static int topK = ConfigUtil.TopK;
    static IndexSearcher searcher = null;
    static QueryParser parser = null;
    public static void main(String[] args) throws Exception {
        prepare();
        evaluation();
    }
    public static void prepare() throws Exception {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(codebaseIndexPath)));
        searcher = new IndexSearcher(reader);
        Analyzer analyzer = new LuSearchQueryAnalyzer();
        parser = new MultiFieldQueryParser(new String[] {"methbody", "methname"}, analyzer);
    }
    /**
     * Search and return evaluation result on csv test files
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static void evaluation() throws Exception {

        List<String> querys = DatasetUtil.loadQuerysFromJson(QAPath);
        EvaluateUtil eu = new EvaluateUtil(DatasetUtil.loadTrueResults(QAPath),searcher);
        int i = 0;
        for (String rawQuery : querys) {
            Date start = new Date();
            Query query = parser.parse(QueryParser.escape(rawQuery));
            TopDocs docs = searcher.search(query, topK);
            Date end = new Date();

            eu.setResult(docs, (end.getTime() - start.getTime()), i++, query);
            System.out.print(i+"'th query finished\n");

        }
        eu.writeDefaultCSV(evalResultPath);
    }

    public static List<String> search(String rawQuery) throws Exception {
        rawQuery = StringUtil.replaceReservedWords(rawQuery);
        Query query = parser.parse(QueryParser.escape(rawQuery));
        TopDocs docs = searcher.search(query, topK);
        List<String> resultList = new ArrayList<>();
        for (ScoreDoc sd : docs.scoreDocs) {
            resultList.add(searcher.doc(sd.doc).get("methbody"));
        }
        return resultList;
    }
}