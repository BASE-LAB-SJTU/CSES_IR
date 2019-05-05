package CS.Util;

import CS.model.APIdata;
import CS.model.QueryDataRow;
import CS.model.QueryTestCase;
import CS.model.SOqa;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cses.parser.ParserOutput;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasetUtil {

    public enum DataFormat {
        CSV, XLSX, TXT, JSON
    }

    public class APIData {
        public String FQN;
        public String description;

        public APIData(String f, String d) {
            FQN = f;
            description = d;
        }

    }

    public static List<APIdata> loadAPIData(String path) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray QAs = (JSONArray) parser.parse(new FileReader(path));

        int size = QAs.size();
        List<APIdata> apis = new ArrayList<>(size);
        for(Object qa: QAs) {
            JSONObject jqa = (JSONObject) qa;
            String family = (String) jqa.get("name");
            JSONArray funcs = (JSONArray) jqa.get("func");
            for (Object func: funcs) {
                APIdata api = new APIdata();
                api.setFamilyName(family);
                JSONObject jsonFunc = (JSONObject) func;
                String funcName = (String) jsonFunc.get("funcName");
                String funcDes = (String) jsonFunc.get("funcDes");
                api.setFuncFullName(family + "/" + funcName);
                api.setFuncDesc(funcDes);
                apis.add(api);
            }
        }
        return apis;
    }

    public static List<SOqa> loadSoData(String path) throws Exception {
        List<SOqa> soDataList = new ArrayList<SOqa>();
        JSONParser parser = new JSONParser();
        JSONArray QAs = (JSONArray) parser.parse(new FileReader(path));

        int size = QAs.size();
        //String[] soData = new String[size];
        int i = 0;
        for (Object qa : QAs) {
            JSONObject jqa = (JSONObject) qa;
            SOqa so = new SOqa();
            so.setA(((String) jqa.get("a")).replace("\n", ""));
            so.setQ(((String) jqa.get("q")).replace("\n", ""));
            so.setaScore((long) jqa.get("aScore"));
            so.setqScore((long) jqa.get("qScore"));
            soDataList.add(so);
            i++;
        }
        return soDataList;
    }


    public static List<String> loadQuerysFromJson(String path) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            QueryTestCase[] codes = gson.fromJson(reader, QueryTestCase[].class);
            List<String> querys = new ArrayList<String>();
            for (QueryTestCase qtc: codes) {
                querys.add(qtc.query);
            }
            return  querys;
        } catch (FileNotFoundException ex) {

        }
        return null;
    }

    public static List<String>[] loadJsonTrueResults(String path) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            QueryTestCase[] codes = gson.fromJson(reader, QueryTestCase[].class);
            List<List<String>> trueResults = new ArrayList<>();
            for (QueryTestCase qtc: codes) {
                trueResults.add(Arrays.asList(qtc.answerList));
            }
            return  (List<String>[]) trueResults.toArray(new ArrayList[trueResults.size()]);
        } catch (FileNotFoundException ex) {

        }
        return null;
    }

    public static Workbook getWorkbook(InputStream in, File file) throws IOException{
        Workbook wb = null;
        if(file.getName().endsWith("xls")){  //Excel 2003
            wb = new HSSFWorkbook(in);
        }else if(file.getName().endsWith("xlsx")){ // Excel 2007/2010
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }

    public static List<QueryDataRow> getDataList(String filepath) {
        try {
            File excelFile = new File(filepath);
            FileInputStream in = new FileInputStream(excelFile);
            Workbook workbook = getWorkbook(in, excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            List<QueryDataRow> rowList = new ArrayList<>();
            for (Row row : sheet) {
                QueryDataRow qdr = new QueryDataRow(row);
                rowList.add(qdr);
            }
            return rowList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ParserOutput.IndexedCode[]  getParsedCodeFromJson(String jsonPath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(jsonPath));
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            ParserOutput.IndexedCode[] codes = gson.fromJson(reader, ParserOutput.IndexedCode[].class);
            return codes;
        } catch (FileNotFoundException ex) {

        }
        return null;
    }
}
