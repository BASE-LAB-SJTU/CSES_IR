package CS.Index;

import CS.Util.ConfigUtil;
import CS.Util.DatasetUtil;
import CS.methods.base.baseSnippetAnalyzer;
import CS.model.SOQA;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class QECKSOIndex {
    public static void main(String[] args) {
        String indexPath = ConfigUtil.SOIndex;
        String SOOriginPath = ConfigUtil.SOOrigin;
        buildStackoverflowIndex(SOOriginPath, indexPath);
    }

    static void buildStackoverflowIndex(String jsonSplitsPath, String indexPath){
        Date start = new Date();
        try {
            // create SO first retrieval index
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new baseSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            List<String> soSplitsJson = DatasetUtil.getTypeFilenameList(jsonSplitsPath,".json");
            IndexWriter writer = new IndexWriter(dir, iwc);
            for (String jfile: soSplitsJson) {
                System.out.println("json index builed:" +jfile);
                indexDocs(writer, jsonSplitsPath + jfile);
            }
            writer.close();
        } catch (Exception var12) {
            System.out.println(" caught a " + var12.getClass() + "\n with message: " + var12.getMessage());
            var12.printStackTrace();
        }
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }


    static void indexDocs(final IndexWriter writer, String path) throws Exception {
        List<SOQA> soData = DatasetUtil.loadSoData(path);
        System.out.println("sodata length:" + soData.size());
        FieldType ff = new FieldType();
        ff.setStoreTermVectors(true);
        ff.setStored(true);
        ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        FieldType scoreType = new FieldType();
        scoreType.setStoreTermVectors(false);
        scoreType.setStored(true);
        scoreType.setIndexOptions(IndexOptions.DOCS);
        for(SOQA so: soData){
            Document document = new Document();
            String soBlock = so.getQuestion() + "\t" + so.getAnswer();
            StoredField soField = new StoredField("content", soBlock, ff);
            document.add(soField);
            document.add(new NumericDocValuesField("qScore", so.getqScore()));
            document.add(new NumericDocValuesField("aScore", so.getaScore()));
            writer.addDocument(document);
        }
    }
}
