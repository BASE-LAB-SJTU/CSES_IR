package CS.Search;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.Util.EvaluateUtil;
import CS.Util.JsonFormatUtil;
import CS.evaluation.MetricsSet;
import CS.methods.standard.standardQueryAnalyzer;
import CS.model.QueryTerm;
import CS.similarity.ExtBoolSimilarity;
import CS.similarity.TfidfSimilarity;
import edu.stanford.nlp.util.StringUtils;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import java.nio.file.Paths;
import java.util.*;


import static CS.methods.CodeHow.CodeHow.apiUnderstanding;
import static CS.methods.CodeHow.CodeHow.getExtBoolQueryList;

public class CodeHowSearch {

    public static void main(String[] args) throws Exception {
        String exp = "CosBench/";
        String xlsxPath = "src/test/resources/"+exp+"query2answer.xlsx";
        String resPath = "src/test/resources/"+exp+"codeHowRes_"+ConfigUtil.TopK+".csv";
        String indexPath = "src/test/resources/"+exp+"index";
        evaluateCodeHowSearch(xlsxPath,resPath,indexPath);
    }

    public static MetricsSet evaluateCodeHowSearch(String csvPath, String outputCsvPath, String indexPath) throws Exception {
        // Create Index searcher
        IndexSearcher APISearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(ConfigUtil.APIIndexPath))));
        APISearcher.setSimilarity(new TfidfSimilarity());
        String[] APIField = {"FQN", "description"};
        QueryParser APIParser = new MultiFieldQueryParser(APIField, new standardQueryAnalyzer());
        IndexReader normalReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(normalReader);
        // set custom similarity
        //searcher.setSimilarity(new ExtBoolSimilarity());
        String[] CodeHowTestField = {"methbody", "methname", "api"};
        QueryParser parser = new MultiFieldQueryParser(CodeHowTestField, new standardQueryAnalyzer());
        String[] querys = null;
        List<String>[] trueResults = null;
        querys = (String[]) DatasetUtil.loadQuerysFromJson(csvPath).toArray();
        trueResults = DatasetUtil.loadJsonTrueResults(csvPath);
        EvaluateUtil eu = new EvaluateUtil(trueResults);
        int i = 0;
        for (String rawQuery : querys) {
            Date start = new Date();
            List<?> APIrank = apiUnderstanding(APIParser, APISearcher, rawQuery);
            // Implement Extended Boolean Query
            List<?> extbQueryList = getExtBoolQueryList(rawQuery, parser, APIrank, searcher);
            // get expanded query from sum(api1, api2...  api_text)
            String sourceText = "";     // initialize score expression
            SimpleBindings bindings = new SimpleBindings();
            bindings.add("text", DoubleValuesSource.SCORES);
            for (Object qi : extbQueryList) {
                FunctionScoreQuery queryi = (FunctionScoreQuery) qi;
                int apiIdx = extbQueryList.indexOf(qi);
                String bindingName = "api" + String.valueOf(apiIdx);
                bindings.add(bindingName, DoubleValuesSource.fromQuery(queryi));
                sourceText += (sourceText.length() == 0) ? bindingName : " + " + bindingName;
            }
            sourceText = sourceText.length() == 0? "text" : "text +" + sourceText;
            Expression expr = JavascriptCompiler.compile(sourceText);
            QueryTerm qt = new QueryTerm(parser.parse(QueryParser.escape(rawQuery)));
            String fullTerm = StringUtils.join(qt.getTermList());
            String queryTermExpr = "methbody:(" + fullTerm + ") methname:(" + fullTerm + ")";
            Query query = parser.parse(queryTermExpr);
            FunctionScoreQuery sumQuery = new FunctionScoreQuery(query, expr.getDoubleValuesSource(bindings));
            TopDocs extbTD = searcher.search(sumQuery, ConfigUtil.TopK);
            Date end = new Date();
            int numsHit = searcher.count(sumQuery);
            eu.setFirstPos(numsHit,i, searcher.search(sumQuery, numsHit));
            eu.setResult(extbTD, (end.getTime() - start.getTime()), i++, searcher);
        }
        eu.writeDefaultCSV(outputCsvPath);
        MetricsSet testResult = eu.getTotalResultPerFile();
        testResult.dataSize = normalReader.maxDoc();
        return testResult;
    }

    public static List<String> search(String rawQuery) throws Exception {
        // Create Index searcher
        IndexSearcher APISearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(ConfigUtil.APIindex))));
        APISearcher.setSimilarity(new TfidfSimilarity());
        String[] APIField = {"FQN", "description"};
        QueryParser APIParser = new MultiFieldQueryParser(APIField, new standardQueryAnalyzer());
        IndexReader normalReader = DirectoryReader.open(FSDirectory.open(Paths.get(ConfigUtil.index)));
        IndexSearcher searcher = new IndexSearcher(normalReader);
        // set custom similarity
        searcher.setSimilarity(new ExtBoolSimilarity());
        String[] CodeHowTestField = {"methbody", "methbody", "api"};
        QueryParser parser = new MultiFieldQueryParser(CodeHowTestField, new standardQueryAnalyzer());
        //  prepare query
        rawQuery = JsonFormatUtil.replaceReservedWords(rawQuery);
        List<String> resultList = new ArrayList<>();
        rawQuery = rawQuery.replaceAll("\\d+" , "");
        List<?> APIrank = apiUnderstanding(APIParser, APISearcher, rawQuery);
        // Implement Extended Boolean Query
        List<FunctionScoreQuery> extbQueryList = getExtBoolQueryList(rawQuery, parser, APIrank, searcher);
        // get expanded query from sum(api1, api2...  api_text)
        String sourceText = "";     // initialize score expression
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("text", DoubleValuesSource.SCORES);
        for (Object qi : extbQueryList) {
            FunctionScoreQuery queryi = (FunctionScoreQuery) qi;
            int apiIdx = extbQueryList.indexOf(qi);
            String bindingName = "api" + String.valueOf(apiIdx);
            bindings.add(bindingName, DoubleValuesSource.fromQuery(queryi));
            sourceText += (sourceText.length() == 0) ? bindingName : " + " + bindingName;
        }
        sourceText = sourceText.length() == 0? "text" : "text +" + sourceText;
        Expression expr = JavascriptCompiler.compile( sourceText);
        QueryTerm qt = new QueryTerm(parser.parse(QueryParser.escape(rawQuery)));
        String fullTerm = StringUtils.join(qt.getTermList());
        String queryTermExpr = "methbody:(" + fullTerm + ") methname:(" + fullTerm + ")";
        Query textQuery = parser.parse(queryTermExpr);
        FunctionScoreQuery sumQuery = new FunctionScoreQuery(textQuery, expr.getDoubleValuesSource(bindings));
        TopDocs extbTD = searcher.search(sumQuery, ConfigUtil.TopK);
        for (ScoreDoc sd : extbTD.scoreDocs) {
            resultList.add(searcher.doc(sd.doc).get("methbody"));
        }
        return resultList;
    }
}