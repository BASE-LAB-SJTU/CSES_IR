package CS.Index;

import CS.Util.DatasetUtil;
import CS.Util.xml2jsonUtil;
import CS.methods.standard.standardSnippetAnalyzer;
import CS.model.SOqa;
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

public class QECKSoIndex {
    public static void main(String[] args) throws Exception {
        String indexPath = "/mnt/sdb/yh/luceneSeries/SOindex/";
        String jsonSplitsPath = "/mnt/sdb/yansh/largeSplits/jsonfiles/";
        buildStackoverflowIndex(jsonSplitsPath, indexPath);
    }

    static void buildStackoverflowIndex(String jsonSplitsPath, String indexPath) throws Exception {
        Date start = new Date();
        try {
            // create SO first retrieval index
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new standardSnippetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            xml2jsonUtil getJson = new xml2jsonUtil();
            List<String> soSplitsJson = getJson.getJsonFilenameList(jsonSplitsPath);
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
        List<SOqa> soData = DatasetUtil.loadSoData(path);
        System.out.println("sodata length:" + soData.size());
        FieldType ff = new FieldType();
        ff.setStoreTermVectors(true);
        ff.setStored(true);
        ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        FieldType scoreType = new FieldType();
        scoreType.setStoreTermVectors(false);
        scoreType.setStored(true);
        scoreType.setIndexOptions(IndexOptions.DOCS);
        for(SOqa so: soData){
            Document document = new Document();
            String soBlock = so.getQ() + "\t" + so.getA();
//            System.out.println("qs:" + so.getqScore() + " as:" + so.getaScore() + " so:" + soBlock);
            StoredField soField = new StoredField("content", soBlock, ff);
            document.add(soField);
            document.add(new NumericDocValuesField("qScore", so.getqScore()));
            document.add(new NumericDocValuesField("aScore", so.getaScore()));
            writer.addDocument(document);
        }
    }

    static void QECKindexDocs(final IndexWriter writer, String path) throws Exception {
        List<SOqa> soData = DatasetUtil.loadSoData(path);
        FieldType ff = new FieldType();
        ff.setStoreTermVectors(true);
        ff.setStored(true);
        ff.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        for(SOqa so: soData){
            Document document = new Document();
            String soBlock = so.getQ() + "\t" + so.getA();
            StoredField soField = new StoredField("snippet", soBlock, ff);
            document.add(soField);
            writer.addDocument(document);
        }

    }
}
