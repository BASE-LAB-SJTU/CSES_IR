package CS.Search;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.StringUtil;
import CS.methods.QECK.ExpandQueryBuilder;
import CS.methods.base.baseQueryAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QECKSearch {
    static String QAPath = ConfigUtil.QASet;
    static String evalResultPath = ConfigUtil.QECKEvaluateResult;
    static String codebaseIndexPath = ConfigUtil.codebaseIndex;
    static String SOIndexPath = ConfigUtil.SOIndex;
    static String searchResultPath = ConfigUtil.QECKSearchResult;
    static int topK = ConfigUtil.TopK;
    static ExpandQueryBuilder expandQueryBuilder = null;
    static IndexSearcher searcher = null;
    static QueryParser parser = null;

    public static void main(String[] args) throws Exception {
        prepare();
        evaluation();
    }

    public static void prepare() throws Exception {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(codebaseIndexPath)));
        searcher = new IndexSearcher(reader);
        parser = new MultiFieldQueryParser(new String[] {"methbody", "methname"},
                new baseQueryAnalyzer());
        expandQueryBuilder = new ExpandQueryBuilder(SOIndexPath, topK);
    }

    public static void evaluation() throws Exception {
        List<String> querys = DatasetUtil.loadQuerysFromJson(QAPath);
        EvaluateUtil eu = new EvaluateUtil(DatasetUtil.loadTrueResults(QAPath),searcher);

        int i = 0;
        for (String rawQuery : querys) {
            Date start = new Date();
            rawQuery = StringUtil.replaceReservedWords(rawQuery);
            String expandedQuery = expandQueryBuilder.getexpandedquery(rawQuery);
            Query query = parser.parse(QueryParser.escape(expandedQuery));
            TopDocs docs = searcher.search(query, topK);
            Date end = new Date();

            eu.setResult(docs, (end.getTime() - start.getTime()), i++, query);
            System.out.print(i+"'th query finished\n");
        }
        eu.writeSearchResultTXT(searchResultPath);
        eu.writeEvaluateResultCSV(evalResultPath);
    }

    public static List<String> search(String rawQuery) throws Exception {
        // prepare query
        rawQuery = StringUtil.replaceReservedWords(rawQuery);

        // expandedQuery
        String expandedQuery = expandQueryBuilder.getexpandedquery(rawQuery);
        Query query = parser.parse(QueryParser.escape(expandedQuery));
        TopDocs docs = searcher.search(query, topK);
        List<String> results = new ArrayList<>();
        for (ScoreDoc sd: docs.scoreDocs) {
            results.add(searcher.doc(sd.doc).get("methbody"));
        }
        return results;
    }
}