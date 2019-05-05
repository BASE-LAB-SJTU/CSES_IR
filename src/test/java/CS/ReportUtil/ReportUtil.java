package CS.ReportUtil;

import CS.Search.CodeHowSearch;
import CS.Search.QECKSearch;
import CS.Search.WordnetSearch;
import CS.Search.baseSearch;
import CS.Util.ConfigUtil;
import CS.evaluation.MetricsSet;
import CS.Index.*;
import com.csvreader.CsvWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportUtil {

    public static void Init() {
        reportName.put(algorithm.Lucene, "/mnt/sdb/yh/luceneSeries/eval_result/BaseLucene.csv");
        reportName.put(algorithm.CodeHow, "/mnt/sdb/yh/luceneSeries/eval_result/CodeHow.csv");
        reportName.put(algorithm.QECK, "/mnt/sdb/yh/luceneSeries/eval_result/QECK.csv");
        reportName.put(algorithm.WordNet, "/mnt/sdb/yh/luceneSeries/eval_result/WordNet.csv");
        reportName.put(algorithm.ALL, "/mnt/sdb/yh/luceneSeries/eval_result/All4.csv");

    }

    static Map<algorithm, String> reportName = new HashMap<algorithm, String>();

    public enum algorithm {
        Lucene, WordNet, QECK, CodeHow, ALL;
    }

    public static void main(String args[]) {
        Init();
        final String xlsxPath = "/mnt/sdb/heyq/query_2.0.xlsx";
        writeCsvPerFile(xlsxPath, algorithm.ALL);
    }

    /**
     * Generate csv report file through overall evaluation.
     * @param xlsxPath path that stores query excel file
     * @param al indicate which algorithm to run
     */
    public static void writeCsvPerFile(String xlsxPath, algorithm al) {
        try {
            String csvFilePath = reportName.get(al);
            switch (al) {
                case Lucene: {
                    baseSearch bs = new baseSearch();
                    //MetricsSet fileResult = bs.base1000JavaProjSearch(xlsxPath, csvFilePath);
                    break;
                }
                case CodeHow: {
                    CodeHowAPIIndex apiIndex = new CodeHowAPIIndex();
                    CodeHowSearch chSearch = new CodeHowSearch();
                    apiIndex.codeHowBuildIndex("/mnt/sdb/yh/luceneSeries/APIindex/", ConfigUtil.APIDocPath);
                    MetricsSet fileResult = chSearch.evaluateCodeHowSearch(xlsxPath, csvFilePath, "");
                    break;
                }
                case WordNet:{
                    WordnetSearch ws = new WordnetSearch();
                    MetricsSet fileResult = ws.WordNetSearch(xlsxPath, csvFilePath,"");
                    break;
                }
                case QECK:{
                    QECKSearch qeck = new QECKSearch();
                    MetricsSet fileResult = qeck.qeckSearch(xlsxPath, csvFilePath, "");
                    break;
                }
                case ALL: {
                    // evaluate each test file and record into report
                    CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("UTF-8"));
                    String[] csvHeaders = new MetricsSet().METRICS;
                    csvWriter.writeRecord(csvHeaders);
                    MetricsSet total = new MetricsSet();
                    // base
                    baseSearch bs = new baseSearch();
                    MetricsSet fileResult = bs.base1000JavaProjSearch(xlsxPath, reportName.get(algorithm.Lucene),"");
                    fileResult.filename = "Base Lucene";
                    writeCsvLine(csvWriter, fileResult, csvHeaders.length, fileResult.filename);
                    total.add(fileResult);
                    // wordnet
                    WordnetSearch ws = new WordnetSearch();
                    fileResult = ws.WordNetSearch(xlsxPath, reportName.get(algorithm.WordNet),"");
                    fileResult.filename = "WordNet";
                    writeCsvLine(csvWriter, fileResult, csvHeaders.length, fileResult.filename);
                    total.add(fileResult);
                    // qeck
                    QECKSearch qeck = new QECKSearch();
                    fileResult = qeck.qeckSearch(xlsxPath, reportName.get(algorithm.QECK), "");
                    fileResult.filename = "QECK";
                    writeCsvLine(csvWriter, fileResult, csvHeaders.length, fileResult.filename);
                    total.add(fileResult);
                    // api
                    CodeHowSearch chSearch = new CodeHowSearch();
                    fileResult = chSearch.evaluateCodeHowSearch(xlsxPath, reportName.get(algorithm.CodeHow), "");
                    fileResult.filename = "CodeHow";
                    writeCsvLine(csvWriter, fileResult, csvHeaders.length, fileResult.filename);
                    total.add(fileResult);
                    // write csv
                    writeTotalCsvLine(csvWriter, total, csvHeaders.length);
                    csvWriter.close();
                    System.out.println("write csv report finish:" + csvFilePath);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get filename list in one directory
     * @param dirPath
     * @return
     */
    static List<String> getFilenameList(String dirPath) {
        // scan target directory and get file list
        File[] allFiles = new File(dirPath).listFiles();
        List<String> filenameList = new ArrayList<String>();
        for (int i = 0; i < allFiles.length; i++) {
            File file = allFiles[i];
            if (file.isFile())  filenameList.add(file.getName());
        }
        return filenameList;
    }

    public static List<String> getTypeFilenameList(String dirPath, String type) {
        // scan target directory and get file list
        File[] allFiles = new File(dirPath).listFiles();
        List<String> filenameList = new ArrayList<String>();
        for (int i = 0; i < allFiles.length; i++) {
            File file = allFiles[i];
            if (file.isFile() && file.getName().substring(file.getName().length() -5).contains(type))
                filenameList.add(file.getName());
        }
        return filenameList;
    }

    // Write a line with file metric data
    private static void writeCsvLine(CsvWriter csvWriter, MetricsSet fileResult, int length, String filename) throws IOException {
        String[] line = new String[length];
        line[0] = filename;
        line[1] = String.valueOf(fileResult.topk);     // topk is integer
        line[2] = new DecimalFormat("0.0000").format(fileResult.precision.sum/fileResult.precision.count);
        line[3] = new DecimalFormat("0.0000").format(fileResult.recall.sum/fileResult.recall.count);
        line[4] = new DecimalFormat("0.0000").format(fileResult.fMeasure.sum/fileResult.fMeasure.count);
        line[5] = new DecimalFormat("0.0000").format(fileResult.MAP.sum/fileResult.MAP.count);
        line[6] = new DecimalFormat("0.0000").format(fileResult.MRR.sum/fileResult.MRR.count);
        line[7] = new DecimalFormat("0.0000").format(fileResult.time.sum/fileResult.time.count);
        line[8] = String.valueOf(fileResult.queryNumb);
        line[9] = String.valueOf(fileResult.dataSize);
        csvWriter.writeRecord(line);
    }

    // Write total result with all file data
    private static void writeTotalCsvLine(CsvWriter csvWriter, MetricsSet total, int length) throws IOException {
        // evaluate total records
        String[] line = new String[length];
        line[0] = "total";
        line[1] = String.valueOf(total.topk);
        line[2] = new DecimalFormat("0.0000").format(total.precision.sum/total.precision.count);
        line[3] = new DecimalFormat("0.0000").format(total.recall.sum/total.recall.count);
        line[4] = new DecimalFormat("0.0000").format(total.fMeasure.sum/total.fMeasure.count);
        line[5] = new DecimalFormat("0.0000").format(total.MAP.sum/total.MAP.count);
        line[6] = new DecimalFormat("0.0000").format(total.MRR.sum/total.MRR.count);
        line[7] = new DecimalFormat("0.0000").format(total.time.sum/total.time.count);
        line[8] = String.valueOf(total.queryNumb);
        line[9] = String.valueOf(total.dataSize);
        csvWriter.writeRecord(line);
        line = new String[length];
        line[0] = "valid count";
        line[2] = String.valueOf(total.precision.count);
        line[3] = String.valueOf(total.recall.count);
        line[4] = String.valueOf(total.fMeasure.count);
        line[5] = String.valueOf(total.MAP.count);
        line[6] = String.valueOf(total.MRR.count);
        line[7] = String.valueOf(total.time.count);
        csvWriter.writeRecord(line);
    }
}
