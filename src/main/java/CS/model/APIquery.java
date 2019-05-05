package CS.model;

import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

public class APIquery extends CustomScoreQuery {
    /**
     * Create a CustomScoreQuery over input subQuery.
     *
     * @param subQuery the sub query whose scored is being customized. Must not be null.
     */
    public APIquery(Query subQuery) {
        super(subQuery);
    }

    /**
     * Prints a query to a string, with <code>field</code> assumed to be the
     * default field and omitted.
     *
     * @param field
     */
    @Override
    public String toString(String field) {
        return null;
    }

    /**
     * Override and implement query instance equivalence properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     * <p>
     * Typically a query will be equal to another only if it's an instance of
     * the same class and its document-filtering properties are identical that other
     * instance. Utility methods are provided for certain repetitive code.
     *
     * @param obj
     * @see #sameClassAs(Object)
     * @see #classHash()
     */
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    /**
     * Override and implement query hash code properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     *
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return 0;
    }
}
