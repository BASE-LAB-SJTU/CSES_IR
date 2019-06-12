package CS.methods.base;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.*;

public class baseSnippetAnalyzer extends Analyzer {
    /**
     * Code snippet preprocess. Include: removing the stopwords for code, splitting compound words, lowering and
     * stemming.
     * @param fieldName
     * @return
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new LetterTokenizer();//new StandardTokenizer();
        int flags = SPLIT_ON_CASE_CHANGE | SPLIT_ON_NUMERICS | GENERATE_NUMBER_PARTS | GENERATE_WORD_PARTS;
        TokenStream result = new WordDelimiterGraphFilter(source, flags, null);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, codeStopWord.CODE_STOP_WORDS_SET);
        result = new PorterStemFilter(result);
        return new Analyzer.TokenStreamComponents(source, result);
    }
}
