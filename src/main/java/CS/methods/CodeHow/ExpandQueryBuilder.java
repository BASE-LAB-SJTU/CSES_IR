package CS.methods.CodeHow;

import CS.Util.ConfigUtil;
import CS.methods.base.baseQueryAnalyzer;
import CS.model.OverlapTopDocs;
import CS.model.QueryTerm;
import CS.model.QueryTestCase;
import edu.stanford.nlp.util.StringUtils;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpandQueryBuilder {
    IndexSearcher APISearcher = null;
    QueryParser APIParser = null;
    QueryParser parser = null;
    int topK = 10;
    double maxTF = 0;

    /**
     * init
     * @param SOIndexPath
     * @param topk
     * @param searcher
     * @throws Exception
     */
    public ExpandQueryBuilder(String SOIndexPath, int topk, IndexSearcher searcher) throws Exception{
        IndexReader APIReader = DirectoryReader.open(FSDirectory.open(Paths.get(SOIndexPath)));
        APISearcher = new IndexSearcher(APIReader);
        APIParser = new QueryParser("content", new baseQueryAnalyzer());
        topK = topk;
        maxTF = getMaxTF(searcher.getIndexReader());
        String[] CodeHowTestField = {"methbody", "methname", "api"};
        parser = new MultiFieldQueryParser(CodeHowTestField, new baseQueryAnalyzer());
    }

    /**
     * get Expanded Query by raw query.
     * @param rawQuery
     * @return
     * @throws Exception
     */
    public Query getExpandedQuery(String rawQuery) throws Exception {
        QueryTerm qt = new QueryTerm(parser.parse(QueryParser.escape(rawQuery)));

        List<?> APIrank = apiUnderstanding(APIParser, APISearcher, rawQuery);
        // Implement Extended Boolean Query
        List<?> extbQueryList = getExtBoolQueryList(qt, APIrank);

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
        String fullTerm = StringUtils.join(qt.getTermList());
        String queryTermExpr = "methbody:(" + fullTerm + ") methname:(" + fullTerm + ")";
        Query query = parser.parse(queryTermExpr);

        Expression expr = JavascriptCompiler.compile(sourceText);
        FunctionScoreQuery sumQuery = new FunctionScoreQuery(query, expr.getDoubleValuesSource(bindings));
        return sumQuery;
    }


    /**
     * From raw query text generate Qtext, Qapi, Using FunctionScoreQuery. Return a list containing all relative query
     * @param qt
     * @param APIrank
     * @return
     * @throws ParseException
     * @throws java.text.ParseException
     */
    public List<FunctionScoreQuery> getExtBoolQueryList(QueryTerm qt, List<?> APIrank) throws ParseException,
            java.text.ParseException {

        List<FunctionScoreQuery> queryList = new ArrayList<>();
        for (Object FQNrank : APIrank) {     // Qapi1...k
            String fqn = ((Map.Entry<String, Double>) FQNrank).getKey().replace("/", ".");
            double apiscore =  ((Map.Entry<String, Double>) FQNrank).getValue();
            FunctionScoreQuery funcQuery = getApiQueryViaTerm(fqn, qt, parser, apiscore, maxTF);
            queryList.add(funcQuery);
        }
        return queryList;
    }


    /**
     * Get max term frequency for normalization
     * @param reader
     * @return double value of term freq
     */
    public double getMaxTF(IndexReader reader) {
        try {
            double maxTF = 0;
            for (int docId = 0; docId < reader.numDocs(); docId ++) {
                Terms terms = reader.getTermVector(docId, "methbody");
                if (terms == null)
                    continue;
                TermsEnum termsEnum = terms.iterator();
                while (termsEnum.next() != null) {
                    PostingsEnum docEnums = termsEnum.postings(null);
                    while ((docEnums.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        if (docEnums.freq() > maxTF) {
                            maxTF = docEnums.freq();
                        }
                    }
                }
            }
            return maxTF;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get functionScoreQuery from query term, the query is scored via combined API score
     * @param fqn
     * @param qt
     * @param parser
     * @param apiscore
     * @param maxTF
     * @return
     * @throws java.text.ParseException
     * @throws ParseException
     */
    private FunctionScoreQuery getApiQueryViaTerm(String fqn, QueryTerm qt, QueryParser parser, double apiscore,
                                                  double maxTF) throws java.text.ParseException, ParseException {
        SimpleBindings bindings = new SimpleBindings();
        bindings.add(fqn, DoubleValuesSource.SCORES);   // use api query for base
        List<String> textList = qt.getFilteredTermList(fqn);
        for (String termText : textList) {
            bindings.add(termText, DoubleValuesSource.fromQuery(
                    parser.parse("(methbody:" + termText + ")OR(methname:" + termText + ")")));
        }
        String sourceText = qt.getScoreExpressionStr(true, fqn, true, apiscore, maxTF);
        Expression expr = JavascriptCompiler.compile(sourceText);
        FunctionScoreQuery funcQuery = new FunctionScoreQuery( parser.parse("api:" + QueryParser.escape(fqn)),
                expr.getDoubleValuesSource(bindings));
        return funcQuery;
    }

    public static List<String> getApiTokens(String api) {
        List<String> tokens = new ArrayList<String>();
        Matcher tokenMcr = Pattern.compile("\\w+").matcher(api);
        while(tokenMcr.find()) {
            tokens.add(tokenMcr.group());
        }
        return tokens;
    }

    /**
     * First Retrieval with API index
     * @param APIParser
     * @param APISearcher
     * @param rawQuery
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public List<?> apiUnderstanding(QueryParser APIParser, IndexSearcher APISearcher, String rawQuery)
            throws ParseException, IOException {
        // Get TopDocs by field <FQN>
        Query FQNquery = APIParser.parse("FQN:(" + QueryParser.escape(rawQuery) + ")");
        TopDocs FQNtd = APISearcher.search(FQNquery, ConfigUtil.TopK);
        Query descQuery = APIParser.parse("description:(" + QueryParser.escape(rawQuery) + ")");
        TopDocs descTd = APISearcher.search(descQuery, ConfigUtil.TopK);
        // Get overlap
        OverlapTopDocs olTopDocs = new OverlapTopDocs(FQNtd.scoreDocs, descTd.scoreDocs);
        List<?> APIrank = olTopDocs.getAPIRank(APISearcher, ConfigUtil.TopkAPI);
        return APIrank;
    }

}
