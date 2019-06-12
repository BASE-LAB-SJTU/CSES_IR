package CS.methods.base;


import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.*;

public class baseQueryAnalyzer extends Analyzer {
    /**
     * Query preprocess. Include: removing the stopwords, splitting compound words, lowering and stemming.
     * @param fieldName
     * @return
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        int flags = SPLIT_ON_CASE_CHANGE | SPLIT_ON_NUMERICS | GENERATE_NUMBER_PARTS | GENERATE_WORD_PARTS;
        TokenStream result = new WordDelimiterGraphFilter(source, flags, null);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, codeQueryStopWord.CODE_QUERY_STOP_WORDS_SET);
        result = new PorterStemFilter(result);
        return new TokenStreamComponents(source, result);
    }
}
