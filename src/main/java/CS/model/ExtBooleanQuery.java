package CS.model;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;

public class ExtBooleanQuery extends CustomScoreQuery {
    /**
     * Create a CustomScoreQuery over input subQuery.
     *
     * @param subQuery the sub query whose scored is being customized. Must not be null.
     */
    public ExtBooleanQuery(Query subQuery) {
        super(subQuery);
    }

    /**
     * Create a CustomScoreQuery over input subQuery and a {@link FunctionQuery}.
     *
     * @param subQuery     the sub query whose score is being customized. Must not be null.
     * @param scoringQuery a value source query whose scores are used in the custom score
     */
    public ExtBooleanQuery(Query subQuery, FunctionQuery scoringQuery) {
        super(subQuery, scoringQuery);
    }

    /**
     * Create a CustomScoreQuery over input subQuery and a {@link FunctionQuery}.
     *
     * @param subQuery       the sub query whose score is being customized. Must not be null.
     * @param scoringQueries value source queries whose scores are used in the custom score
     */
    public ExtBooleanQuery(Query subQuery, FunctionQuery... scoringQueries) {
        super(subQuery, scoringQueries);
    }


//    @Override
//    protected CustomScoreProvider getCustomScoreProvider(
//            LeafReaderContext context) throws IOException {
//        return new RecencyBoostCustomScoreProvider(context,multiplier,day,maxDaysAgo,dayField);
//
//    }
}
