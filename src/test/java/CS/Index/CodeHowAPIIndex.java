package CS.Index;

import CS.Util.DatasetUtil;
import CS.methods.base.baseSnippetAnalyzer;
import CS.model.APIdata;
import CS.similarity.TfidfSimilarity;
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

import CS.Util.ConfigUtil;

public class CodeHowAPIIndex {
    public static void main(String[] args) {
        codeHowBuildIndex(ConfigUtil.APIIndex , ConfigUtil.APIOrigin );
    }

    public static void  codeHowBuildIndex(String indexPath, String docsPath){
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new baseSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            iwc.setSimilarity(new TfidfSimilarity());
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

}
