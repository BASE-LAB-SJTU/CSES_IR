package CS.methods.CodeHow;

import CS.Util.ConfigUtil;
import CS.model.OverlapTopDocs;
import CS.model.QueryTerm;
import edu.stanford.nlp.util.StringUtils;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeHow {

    /*
     * From raw query text generate Qtext, Qapi, using FunctionScoreQuery
     * return a list containing all relative query
     */
    public static List<FunctionScoreQuery> getExtBoolQueryList(String rawQuery, QueryParser parser, List<?> APIrank, IndexSearcher searcher) throws ParseException, java.text.ParseException, IOException {
        Query fullQuery = parser.parse(QueryParser.escape(rawQuery));   // base escape and api parser
        QueryTerm qt = new QueryTerm(fullQuery);
        List<FunctionScoreQuery> queryList = new ArrayList<>();
        for (Object FQNrank : APIrank) {     // Qapi1...k
            String fqn = ((Map.Entry<String, Double>) FQNrank).getKey().replace("/", ".");
            double apiscore =  ((Map.Entry<String, Double>) FQNrank).getValue();
            FunctionScoreQuery funcQuery = getApiQueryViaTerm(fqn, qt, parser, rawQuery, apiscore, getMaxTF(searcher.getIndexReader()));
            queryList.add(funcQuery);
            TopDocs td = searcher.search(funcQuery, ConfigUtil.TopK);
        }
        return queryList;
    }


    /**
     *  Get max term frequency for normalization
     * @param reader
     * @return double value of term freq
     */
    public static double getMaxTF(IndexReader reader) {
        try {
            double maxTF = 0;
            for (int docId = 0; docId < reader.numDocs(); docId ++) {
                Terms terms = reader.getTermVector(docId, "methbody");
                if (terms == null)
                    continue;
                TermsEnum termsEnum = terms.iterator();
                BytesRef thisTerm = null;
                while ((thisTerm = termsEnum.next()) != null) {
                    String termText = thisTerm.utf8ToString();
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
     */
    private static FunctionScoreQuery getApiQueryViaTerm(String fqn, QueryTerm qt, QueryParser parser, String rawQuery, double apiscore, double maxTF) throws java.text.ParseException, ParseException {
        String sourceText = "";     // initialize score expression
        SimpleBindings bindings = new SimpleBindings();
        bindings.add(fqn, DoubleValuesSource.SCORES);   // use api query for base
        List<String> textList = qt.getFilteredTermList(fqn);
        for (String termText : textList) {
            bindings.add(termText, DoubleValuesSource.fromQuery(parser.parse("(methbody:" + termText + ")OR(methname:" + termText + ")")));
        }
        sourceText = qt.getScoreExpressionStr(true, fqn, true, apiscore, maxTF);
        Expression expr = JavascriptCompiler.compile(sourceText);
        FunctionScoreQuery funcQuery = new FunctionScoreQuery( parser.parse("api:" + QueryParser.escape(fqn)), expr.getDoubleValuesSource(bindings));//new TermQuery(new Term("api",fqn))
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

    /*
     * First Retrieval with API index
     */
    public static List<?> apiUnderstanding(QueryParser APIParser, IndexSearcher APISearcher, String rawQuery) throws ParseException, IOException {
        Query APIQuery = APIParser.parse(QueryParser.escape(rawQuery));
        // Get TopDocs by field <FQN>
        Query FQNquery = APIParser.parse("FQN:(" + QueryParser.escape(rawQuery) + ")");
        TopDocs FQNtd = APISearcher.search(FQNquery, ConfigUtil.TopK);
        Query descQuery = APIParser.parse("description:(" + QueryParser.escape(rawQuery) + ")");
        TopDocs descTd = APISearcher.search(descQuery, ConfigUtil.TopK);
        // Get overlap
        OverlapTopDocs olTopDocs = new OverlapTopDocs(FQNtd.scoreDocs, descTd.scoreDocs);
        List<?> APIrank = olTopDocs.getAPIRank(APISearcher, ConfigUtil.apiUsTopk);
        return APIrank;
    }

    /**
     * Concate seperated method name from CodeParser into origional one
     * @param raw
     * @return
     */
    public static String concateMethname(String raw) {
        String[] wordList = raw.trim().split("\\s+");
        List<String> concatList = new ArrayList<String>();
        for (int i = 0; i < wordList.length; i++) {
            if (i == 0) {
                concatList.add(wordList[i]);
            } else {
                String hump = "" + wordList[i].charAt(0);
                hump = hump.toUpperCase();
                hump += wordList[i].length() > 1 ? wordList[i].substring(1): "";
                concatList.add(hump);
            }
        }
        return StringUtils.join(concatList, "");
    }

}
