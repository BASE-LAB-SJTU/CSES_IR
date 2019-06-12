package CS.methods.LuSearch;


import CS.methods.base.codeQueryStopWord;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.*;
import java.text.ParseException;

import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import CS.Util.ConfigUtil;

import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.*;

public class LuSearchQueryAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        int flags = SPLIT_ON_CASE_CHANGE | SPLIT_ON_NUMERICS | GENERATE_NUMBER_PARTS | GENERATE_WORD_PARTS;
        TokenStream result = new WordDelimiterGraphFilter(source, flags, null);        result = new LowerCaseFilter(result);
        result = new StopFilter(result, codeQueryStopWord.CODE_QUERY_STOP_WORDS_SET);
        result = new SynonymGraphFilter(result, buildSynonym(), false);
        result = new PorterStemFilter(result);
        return new TokenStreamComponents(source, result);
    }

    private SynonymMap buildSynonym() {
        try {
            String wordnetPath = ConfigUtil.wordnetPath;
            File file = new File(wordnetPath);
            InputStream stream = new FileInputStream(file);
            Reader rulesReader = new InputStreamReader(stream);
            SynonymMap.Builder parser = new WordnetSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
            ((WordnetSynonymParser) parser).parse(rulesReader);
            return parser.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
