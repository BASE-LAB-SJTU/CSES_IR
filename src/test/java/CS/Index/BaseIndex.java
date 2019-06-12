package CS.Index;

import CS.Util.DatasetUtil;
import CS.methods.base.baseSnippetAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import CS.model.IndexedCode;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import CS.Util.ConfigUtil;
import CS.Util.StringUtil;


public class BaseIndex {
    public static void main(String[] args) {
        final String codebaseIndexPath = ConfigUtil.codebaseIndex;
        final String codebaseJsonPath = ConfigUtil.codebaseOrigin;
        final String mode = args[0];
        buildIndex(codebaseJsonPath, codebaseIndexPath, mode);
    }

    static void buildIndex(String jsonPath, String indexPath, String mode) {
        List<String> jsonFileNames = DatasetUtil.getTypeFilenameList(jsonPath, ".json");
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new baseSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if (mode.equals('a')) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
            } else if (mode.equals('c')){
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);

            int i = 0;
            for (String name: jsonFileNames) {
                indexDocs(writer, jsonPath + name);
                System.out.println(name + " indexed OK -- " + i++ + "/" + jsonFileNames.size());
            }
            writer.close();

        } catch (Exception var12) {
            System.out.println(" caught a " + var12.getClass() + "\n with message: " + var12.getMessage());
            var12.printStackTrace();
        }

        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }

    static void indexDocs(final IndexWriter writer, final String jsonPath) throws Exception {
        IndexedCode[] queryRows = DatasetUtil.getParsedCodeFromJson(jsonPath);
        for(IndexedCode row: queryRows){
            FieldType ff = new FieldType();
            ff.setStoreTermVectors(true);
            ff.setStored(true);
            ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            Document document = new Document();
            StoredField code = new StoredField("methbody", row.methbody, ff);
            StoredField name = new StoredField("methname", StringUtil.concateMethname(row.methname), ff);
            StoredField apis = new StoredField("api", row.apiseq, ff);
            StringField ids = new StringField("id", row.id,Field.Store.YES);
            document.add(code);
            document.add(name);
            document.add(apis);
            document.add(ids);
            writer.addDocument(document);
        }
    }

}
