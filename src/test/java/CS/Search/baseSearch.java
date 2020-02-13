package CS.Search;


import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.StringUtil;
import CS.methods.base.baseQueryAnalyzer;
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
    static String QAPath = ConfigUtil.QASet;
    static String evalResultPath = ConfigUtil.BaseLuceneEvaluateResult;
    static String searchResultPath = ConfigUtil.BaseLuceneSearchResult;
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
        parser = new MultiFieldQueryParser(new String[]{"methbody", "methname"},
                new baseQueryAnalyzer());
    }

    /**
     * Search function that returns evaluation result.
     * @return metrics of evaluation result
     * @throws Exception
     */
    public static void evaluation() throws Exception {
        List<String> querys = DatasetUtil.loadQuerysFromJson(QAPath);
        EvaluateUtil eu = new EvaluateUtil(DatasetUtil.loadTrueResults(QAPath),searcher);

        int i = 0;
        for (String rawQuery: querys) {
            Query query = parser.parse(QueryParser.escape(rawQuery));

            Date start = new Date();
            TopDocs docs = searcher.search(query, topK);
            Date end = new Date();

            eu.setResult(docs, (end.getTime() - start.getTime()), i++, query);
            System.out.print(i+"'th query search finished\n");
        }

        eu.writeSearchResultTXT(searchResultPath);
        eu.writeEvaluateResultCSV(evalResultPath);
    }

    public static List<String> search(String rawQuery) throws Exception {
        Query query = parser.parse(QueryParser.escape(StringUtil.replaceReservedWords(rawQuery)));
        TopDocs docs = searcher.search(query, topK);
        List<String> resultList = new ArrayList<>();
        for (ScoreDoc sd : docs.scoreDocs) {
            resultList.add(searcher.doc(sd.doc).get("methbody"));
        }
        return resultList;
    }


}