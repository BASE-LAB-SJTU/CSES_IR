package CS.methods.QECK;

import CS.Util.ConfigUtil;
import CS.Util.StringUtil;
import CS.methods.base.baseQueryAnalyzer;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class ExpandQueryBuilder {
    IndexReader soReader = null;
    IndexSearcher soSearcher = null;
    QueryParser soParser = null;
    int topK = 10;

    public ExpandQueryBuilder(String SOIndexPath, int k) throws Exception{
        soReader = DirectoryReader.open(FSDirectory.open(Paths.get(SOIndexPath)));
        soSearcher = new IndexSearcher(soReader);
        soParser = new QueryParser("content", new baseQueryAnalyzer());
        topK = k;
    }

    public String getexpandedquery(String rawQuery) throws Exception {
        Query soQuery = soParser.parse(QueryParser.escape(StringUtil.replaceReservedWords(rawQuery)));

        SimpleBindings bindings = new SimpleBindings();
        bindings.add("score", DoubleValuesSource.SCORES);
        bindings.add("soAscore", DoubleValuesSource.fromLongField("aScore"));
        bindings.add("soQscore", DoubleValuesSource.fromLongField("qScore"));

        // get highest text similarity score for normalization
        double maxTextScore = soSearcher.search(soQuery, topK).getMaxScore();
        double maxSOscore = soSearcher.search(
                new FunctionScoreQuery(
                        soQuery,
                        JavascriptCompiler.compile("soAscore * 0.3 + soQscore * 0.7")
                                .getDoubleValuesSource(bindings)
                ),
                topK
        ).getMaxScore();

        bindings.add("maxTextScore", DoubleValuesSource.constant( maxTextScore));
        bindings.add("maxSOscore", DoubleValuesSource.constant( maxSOscore));
        Expression expr = JavascriptCompiler.compile(
                "score / maxTextScore + (soAscore * 0.3 + soQscore * 0.7)/maxSOscore");
        FunctionScoreQuery funcQuery = new FunctionScoreQuery(soQuery, expr.getDoubleValuesSource(bindings));
        TopDocs soDocs = soSearcher.search(funcQuery, topK);
        String keywords = GetTopTerms.getTopTermByTFIDF(soDocs, soReader, ConfigUtil.QECKTopNWords);
        String queryStr = rawQuery + keywords;

        return queryStr;
    }
}
