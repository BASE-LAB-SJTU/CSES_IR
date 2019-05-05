package CS.Index;

import CS.Util.DatasetUtil;
import CS.methods.standard.standardSnippetAnalyzer;
import CS.model.APIdata;
import CS.similarity.TfidfSimilarity;
import cses.parser.JavaParser;
import cses.parser.ParserOutput;
import edu.stanford.nlp.util.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static CS.Util.ConfigUtil.APIindex;
import static CS.methods.CodeHow.CodeHow.concateMethname;

public class CodeHowAPIIndex {
    public static void main(String[] args) {
        codeHowBuildIndex(APIindex , "/mnt/sdb/yh/luceneSeries/data/result.json" );
    }

    public static void  codeHowBuildIndex(String indexPath, String docsPath){
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new standardSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            iwc.setSimilarity(new TfidfSimilarity());
            System.out.println("simi:"  +iwc.getSimilarity());
            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docsPath);
            writer.close();

        } catch (Exception var12) {
            System.out.println(" caught a " + var12.getClass() + "\n with message: " + var12.getMessage());
            var12.printStackTrace();
        }

        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }

    static void indexDocs(final IndexWriter writer, String path) throws Exception {
        List<APIdata> apis = DatasetUtil.loadAPIData(path);
        for(APIdata API: apis){
            Document document = new Document();
            FieldType ff = new FieldType();
            ff.setStoreTermVectors(true);
            ff.setStored(true);
            ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            StoredField FQNField = new StoredField("FQN", API.getFuncFullName(), ff);
            StoredField desField = new StoredField("description", API.getFuncDesc(), ff);
            document.add(FQNField);
            document.add(desField);
            writer.addDocument(document);
        }

    }

    public static void buildBaseIndexWithApi(String testPath, String indexPath) {
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new standardSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocsWithApi(writer, testPath);
            writer.close();
        } catch (Exception var12) {
            System.out.println(" caught a " + var12.getClass() + "\n with message: " + var12.getMessage());
        }
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }


    static void indexDocsWithApi(final IndexWriter writer,  String csvPath) throws Exception {
        List<String> csvSnippets = DatasetUtil.loadPureCsvCodeSnippet(csvPath);
        for(String snippet: csvSnippets){
            Document document = new Document();
            FieldType ff = new FieldType();
            ff.setStoreTermVectors(true);
            ff.setStored(true);
            ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            JavaParser codeParser = new JavaParser();
            ParserOutput result = codeParser.parseOne(snippet);
            String methbody = result.getInBraceLine(snippet);
            String methname = result.getMethodname();
            String apiSeq = StringUtils.join(result.getApiseq(), " ");
            StoredField code = new StoredField("methbody", methbody, ff);
            StoredField name = new StoredField("methname", concateMethname(methname), ff);
            StoredField apis = new StoredField("api", apiSeq, ff);
            document.add(code);
            document.add(name);
            document.add(apis);
            writer.addDocument(document);
        }
    }

}
