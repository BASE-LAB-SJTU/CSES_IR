package CS.Search;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.StringUtil;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Paths;
import java.util.*;


import CS.methods.CodeHow.ExpandQueryBuilder;

public class CodeHowSearch {
    static String QAPath = ConfigUtil.QASet;
    static String evalResultPath = ConfigUtil.CodeHowResult;
    static String codebaseIndexPath = ConfigUtil.codebaseIndex;
    static String APIIndexPath = ConfigUtil.APIIndex;
    static int topK = ConfigUtil.TopK;

    static IndexSearcher searcher = null;
    static ExpandQueryBuilder expandQueryBuilder = null;

    public static void main(String[] args) throws Exception {
        prepare();
        evaluation();
    }

    public static void prepare() throws Exception{
        IndexReader normalReader = DirectoryReader.open(FSDirectory.open(Paths.get(codebaseIndexPath)));
        searcher = new IndexSearcher(normalReader);
        // searcher.setSimilarity(new ExtBoolSimilarity());

        expandQueryBuilder = new ExpandQueryBuilder(APIIndexPath, topK, searcher);
    }

    public static void evaluation() throws Exception {
        List<String> querys = DatasetUtil.loadQuerysFromJson(QAPath);
        EvaluateUtil eu = new EvaluateUtil(DatasetUtil.loadTrueResults(QAPath),searcher);

        int i = 0;
        for (String rawQuery : querys) {

            Date start = new Date();
            Query expandedQuery = expandQueryBuilder.getExpandedQuery(rawQuery);
            TopDocs extbTD = searcher.search(expandedQuery, topK);
            Date end = new Date();

            eu.setResult(extbTD, (end.getTime() - start.getTime()), i++, expandedQuery);
            System.out.print(i+"'th query finished\n");

        }
        eu.writeDefaultCSV(evalResultPath);
    }

    public static List<String> search(String rawQuery) throws Exception {
        //  prepare query
        List<String> resultList = new ArrayList<>();
        Query expandedQuery = expandQueryBuilder.getExpandedQuery(StringUtil.replaceReservedWords(rawQuery));
        TopDocs extbTD = searcher.search(expandedQuery, topK);
        for (ScoreDoc sd : extbTD.scoreDocs) {
            resultList.add(searcher.doc(sd.doc).get("methbody"));
        }
        return resultList;
    }
}