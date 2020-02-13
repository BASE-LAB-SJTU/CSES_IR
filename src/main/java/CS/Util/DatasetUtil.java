package CS.Util;

import CS.model.APIdata;
import CS.model.QueryTestCase;
import CS.model.SOQA;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import CS.model.IndexedCode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasetUtil {

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

    public static List<SOQA> loadSoData(String path) throws Exception {
        List<SOQA> soDataList = new ArrayList<SOQA>();
        JSONParser parser = new JSONParser();
        JSONArray QAs = (JSONArray) parser.parse(new FileReader(path));

        for (Object qa : QAs) {
            JSONObject jqa = (JSONObject) qa;
            SOQA so = new SOQA();
            so.setAnswer(((String) jqa.get("a")).replace("\n", ""));
            so.setQuestion(((String) jqa.get("q")).replace("\n", ""));
            so.setaScore((long) jqa.get("aScore"));
            so.setqScore((long) jqa.get("qScore"));
            soDataList.add(so);
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
            System.out.println("load query finished:" + path);

            return  querys;
        } catch (FileNotFoundException ex) {
            System.out.print(ex.getMessage());
        }
        return null;
    }

    public static List<List<String>> loadTrueResults(String path) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            QueryTestCase[] codes = gson.fromJson(reader, QueryTestCase[].class);
            List<List<String>> trueResults = new ArrayList<>();
            for (QueryTestCase qtc: codes) {
                trueResults.add(Arrays.asList(qtc.answerList));
            }
            return  trueResults;
        } catch (FileNotFoundException ex) {

        }
        return null;
    }

    public static List<String> getTypeFilenameList(String dirPath, String type) {
        // scan target directory and get file list
        File[] allFiles = new File(dirPath).listFiles();
        List<String> filenameList = new ArrayList<String>();

        for (File file: allFiles) {
            if (file.isFile() && file.getName().substring(file.getName().length() - type.length()).contains(type))
                filenameList.add(file.getName());
        }
        return filenameList;
    }

    public static IndexedCode[]  getParsedCodeFromJson(String jsonPath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonPath));
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            IndexedCode[] codes = gson.fromJson(reader, IndexedCode[].class);
            return codes;
        } catch (FileNotFoundException ex) {
            System.out.print(ex.toString());
        }
        return null;
    }
}
