package CS.Index;

import CS.Util.DatasetUtil;
import CS.methods.standard.standardSnippetAnalyzer;
import CS.model.QueryDataRow;
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

import static CS.ReportUtil.ReportUtil.getTypeFilenameList;
import static CS.methods.CodeHow.CodeHow.concateMethname;


public class baseIndex {
    public static void main(String[] args) {
        final String indexPath = args[1];
        final String jsonPath = args[0];
        final String mode = args[2];
        buildTop1000JavaIndex(jsonPath, indexPath, mode);
    }


    static void buildTop1000JavaIndex(String jsonPath, String indexPath, String mode) {
        List<String> jsonFileNames = getTypeFilenameList(jsonPath, ".json");
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new standardSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if (mode.equals('a')) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.APPEND); //originally CREATE_or_APPEND TODO: CHANGE IWC MODE
            } else if (mode.equals('c')){
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }
            IndexWriter writer = new IndexWriter(dir, iwc);
//            indexQueryDocs(writer, xlsxPath);
            int i = 0;
            for (String name: jsonFileNames) {
                i++;
//                indexJsonDocs(writer, jsonPath);
                indexJsonDocs(writer, jsonPath + name);
                System.out.println(name + " indexed OK -- " + i + "/" + jsonFileNames.size());
                System.gc();
            }

            writer.close();

        } catch (Exception var12) {
            System.out.println(" caught a " + var12.getClass() + "\n with message: " + var12.getMessage());
            var12.printStackTrace();
        }

        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }

    static void indexQueryDocs(final IndexWriter writer, final String xslxPath) throws Exception {
        List<QueryDataRow> queryRows = DatasetUtil.getDataList(xslxPath);
        for(QueryDataRow row: queryRows){
//            System.out.println("snippet:" + snippet);
            FieldType ff = new FieldType();
            ff.setStoreTermVectors(true);
            ff.setStored(true);
            ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            // Parse source code and get threee fields
            JavaParser codeParser = new JavaParser();
            for (String snippet: row.getAnswerList()) {
                ParserOutput result = codeParser.parseOnePureBody(snippet);
                String methbody = result.getInBraceLine(snippet);
                String methname = result.getMethodname();
                String apiSeq = StringUtils.join(result.getApiseq(), " ");
                StoredField code = new StoredField("methbody", methbody, ff);
                StoredField name = new StoredField("methname", concateMethname(methname), ff);
                StoredField apis = new StoredField("api", apiSeq, ff);
                Document document = new Document();
                document.add(code);
                document.add(name);
                document.add(apis);
                writer.addDocument(document);
            }
        }
    }

    static void indexJsonDocs(final IndexWriter writer, final String jsonPath) throws Exception {
        ParserOutput.IndexedCode[] queryRows = DatasetUtil.getParsedCodeFromJson(jsonPath);
        for(ParserOutput.IndexedCode row: queryRows){
            FieldType ff = new FieldType();
            ff.setStoreTermVectors(true);
            ff.setStored(true);
            ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            Document document = new Document();
            StoredField code = new StoredField("methbody", row.methbody, ff);
            StoredField name = new StoredField("methname", concateMethname(row.methname), ff);
            StoredField apis = new StoredField("api", row.apiseq, ff);
            StoredField ids = new StoredField("id", row.id, ff);
            document.add(code);
            document.add(name);
            document.add(apis);
            document.add(ids);
            writer.addDocument(document);
        }
    }
}
