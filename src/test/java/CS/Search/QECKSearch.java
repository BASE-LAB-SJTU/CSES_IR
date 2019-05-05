package CS.Search;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.JsonFormatUtil;
import CS.evaluation.MetricsSet;
import CS.methods.QECK.GetTopTerms;
import CS.methods.standard.standardQueryAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.Expression;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QECKSearch {

    public static void main(String[] args) throws Exception {
        String exp = "within/guava/";
        String xlsxPath = "src/test/resources/"+exp+"query2answer.xlsx";
        String resPath = "src/test/resources/"+exp+"QECKRes_"+ConfigUtil.TopK+".csv";
        String indexPath = "src/test/resources/"+exp+"index";
        qeckSearch(xlsxPath,resPath,indexPath);
    }


    public static MetricsSet qeckSearch(String csvPath, String outputCsvPath, String indexPath) throws Exception {
        // build test document searcher
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new MultiFieldQueryParser(new String[] {"methbody", "methname"}, new standardQueryAnalyzer());
        // build so first query searcher
        IndexReader soReader = DirectoryReader.open(FSDirectory.open(Paths.get(ConfigUtil.SOindex)));
        IndexSearcher soSearcher = new IndexSearcher(soReader);
        QueryParser soParser = new QueryParser("content", new standardQueryAnalyzer());
        // load test query and evaluation setting from csv file or xlsx file
        String[] querys = null;
        List<String>[] trueResults = null;
        querys = (String[]) DatasetUtil.loadQuerysFromJson(csvPath).toArray();
        trueResults = DatasetUtil.loadJsonTrueResults(csvPath);
        EvaluateUtil eu = new EvaluateUtil(trueResults);
        int j = 0;
        for (String rawQuery : querys) {
            Date start = new Date();
            rawQuery = JsonFormatUtil.replaceReservedWords(rawQuery);
            // Query on stackoverflow dataset to expand query
            Query soQuery = soParser.parse(QueryParser.escape(JsonFormatUtil.replaceReservedWords(rawQuery)));
            SimpleBindings bindings = new SimpleBindings();
            bindings.add("score", DoubleValuesSource.SCORES);
            bindings.add("soAscore", DoubleValuesSource.fromLongField("aScore"));
            bindings.add("soQscore", DoubleValuesSource.fromLongField("qScore"));
            // get highest text similarity score for normalization
            double maxTextScore = soSearcher.search(soQuery, ConfigUtil.TopK).getMaxScore();
            double maxSOscore = soSearcher.search(
                    new FunctionScoreQuery(
                            soQuery,
                            JavascriptCompiler.compile("soAscore * 0.3 + soQscore * 0.7")
                                    .getDoubleValuesSource(bindings)
                    ),
                    ConfigUtil.TopK
            ).getMaxScore();
            bindings.add("maxTextScore", DoubleValuesSource.constant( maxTextScore));
            bindings.add("maxSOscore", DoubleValuesSource.constant( maxSOscore));
            Expression expr = JavascriptCompiler.compile("score / maxTextScore + (soAscore * 0.3 + soQscore * 0.7)/maxSOscore");
            FunctionScoreQuery funcQuery = new FunctionScoreQuery(soQuery, expr.getDoubleValuesSource(bindings));
            TopDocs soDocs = soSearcher.search(funcQuery, ConfigUtil.TopK);
            String keywords = GetTopTerms.printIDF(soDocs, soSearcher,soReader, ConfigUtil.QECKTopNWords);
            // secondary query
            String queryStr = rawQuery + keywords;
            Query query = parser.parse(QueryParser.escape(queryStr));
            TopDocs docs = searcher.search(query, ConfigUtil.TopK);
            Date end = new Date();
            for(int i = 0;i<docs.scoreDocs.length;i++){
                System.out.print(docs.scoreDocs[i].doc+" ");
            }
            System.out.print("\n");

            int numsHit = searcher.count(query);
            if (numsHit > 0) {
                eu.setFirstPos(searcher.search(query, numsHit), j, false);
            } else {
                eu.setFirstPos(null, j, true);
            }
            eu.setResult(docs, (end.getTime() - start.getTime()), j++, searcher);
        }
        eu.writeDefaultCSV(outputCsvPath);
        MetricsSet testResult = eu.getTotalResultPerFile();
        testResult.dataSize = reader.maxDoc();
        return testResult;
    }

    public static List<String> search(String rawQuery) throws Exception {
        // build test document searcher
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(ConfigUtil.index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new MultiFieldQueryParser(new String[] {"methbody", "methname", "api"}, new StandardAnalyzer());
        // build so first query searcher
        IndexReader soReader = DirectoryReader.open(FSDirectory.open(Paths.get(ConfigUtil.SOindex)));
        IndexSearcher soSearcher = new IndexSearcher(soReader);
        QueryParser soParser = new QueryParser("content", new standardQueryAnalyzer());
        // prepare query
        rawQuery = JsonFormatUtil.replaceReservedWords(rawQuery);
        // Query on stackoverflow dataset to expand query
        Query soQuery = soParser.parse(QueryParser.escape(JsonFormatUtil.replaceReservedWords(rawQuery)));
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("score", DoubleValuesSource.SCORES);
        bindings.add("soAscore", DoubleValuesSource.fromLongField("aScore"));
        bindings.add("soQscore", DoubleValuesSource.fromLongField("qScore"));
        // get highest text similarity score for normalization
        double maxTextScore = soSearcher.search(soQuery, ConfigUtil.TopK).getMaxScore();
        double maxSOscore = soSearcher.search(
                new FunctionScoreQuery(
                        soQuery,
                        JavascriptCompiler.compile("soAscore * 0.3 + soQscore * 0.7")
                                .getDoubleValuesSource(bindings)
                ),
                ConfigUtil.TopK
        ).getMaxScore();
        bindings.add("maxTextScore", DoubleValuesSource.constant( maxTextScore));
        bindings.add("maxSOscore", DoubleValuesSource.constant( maxSOscore));
        Expression expr = JavascriptCompiler.compile("score / maxTextScore + (soAscore * 0.3 + soQscore * 0.7)/maxSOscore");
        FunctionScoreQuery funcQuery = new FunctionScoreQuery(soQuery, expr.getDoubleValuesSource(bindings));
        TopDocs soDocs = soSearcher.search(funcQuery, ConfigUtil.TopK);
        String keywords = GetTopTerms.printIDF(soDocs, soSearcher,soReader, ConfigUtil.QECKTopNWords);
        // secondary query
        String queryStr = rawQuery + keywords;
        Query query = parser.parse(QueryParser.escape(queryStr));
        TopDocs docs = searcher.search(query, ConfigUtil.TopK);
        List<String> results = new ArrayList<>();
        for (ScoreDoc sd: docs.scoreDocs) {
            results.add(searcher.doc(sd.doc).get("methbody"));
        }
        return results;
    }
}