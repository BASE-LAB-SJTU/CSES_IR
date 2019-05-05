package CS.Util;

import java.io.*;
import java.util.*;

import com.google.gson.GsonBuilder;
import org.dom4j.*;
import org.dom4j.io.*;
import com.google.gson.Gson;
import org.json.simple.JSONValue;


public class xml2jsonUtil {

    private class item {
        int id = 0;
        String body;
        String title;
        boolean isQuestion;
        int refId = 0; // for answer, it refers to parent question; for question, it refers to accepted answer id
        int score = 0;
    }

    private class QApair {
        String question;
        String answer;
        int qid = 0;
        int aid = 0;
        int qscore = 0;
        int ascore = 0;

        public int setItemValue(item I) {
            if (I.isQuestion) {
                this.qid = I.id;
                this.question = I.title;
                this.aid = I.refId;
                this.qscore = I.score;
                return 1;
            } else {
                this.aid = I.id;
                this.answer = I.body;
                return 2;
            }
        }
    }

    private class QApairJson {
        public QApairJson(QApair pair) {
            this.id = pair.qid; //take question id in the xml file as json pair id
            this.q = pair.question;
            this.a = pair.answer;
            this.qScore = pair.qscore;
            this.aScore = pair.ascore;
        }
        int id = 0;
        String q;
        String a;
        int qScore;
        int aScore;
    }


    public static void main(String[] args) {
        String xmlDir = "/mnt/sdb/yansh/largeSplits/xmlfiles/";
        String jsonDir = "/mnt/sdb/yansh/largeSplits/jsonfiles/";
        xml2jsonUtil x2j= new xml2jsonUtil();
        List<String> xmls = getXmlFilenameList(xmlDir);
        int i = 0;
        for (String xml: xmls) {
            x2j.xml2json(xmlDir + xml, jsonDir + xml.replace("xml", "json"));
            System.out.println("XML file [" + xml + "] translation completed");
            i ++;
        }
    }

    static List<String> getXmlFilenameList(String dirPath) {
        // scan target directory and get file list
        File[] allFiles = new File(dirPath).listFiles();
        List<String> filenameList = new ArrayList<String>();
        for (int i = 0; i < allFiles.length; i++) {
            File file = allFiles[i];
            if (file.isFile() && file.getName().contains(".xml"))
                filenameList.add(file.getName());
        }
        return filenameList;
    }


    /**
     * Return a list of filename of XML file from a directory
     * @param dirPath
     * @return
     */
    public static List<String> getJsonFilenameList(String dirPath) {
        // scan target directory and get file list
        File[] allFiles = new File(dirPath).listFiles();
        List<String> filenameList = new ArrayList<String>();
        for (int i = 0; i < allFiles.length; i++) {
            File file = allFiles[i];
            if (file.isFile() && file.getName().contains(".json"))
                filenameList.add(file.getName());
        }
        return filenameList;
    }


    public void xml2json(String xmlpath, String jsonpath) {
        try {
            File xmlFile = new File(xmlpath);
            SAXReader reader = new SAXReader();
            Document doc = reader.read(xmlFile);
            Element root = doc.getRootElement();
            Element rowNode;
            Map<Integer, item> answerMap = new HashMap<>();
            List<QApair> pairs = new ArrayList<>();
            /*
                First time of iteration: store All ANSWER item into a MAP, duplicate the answer with their key as parentid
                                        for those parent who has no accpted answer yet, these question take the highest score
                                        of answer as question
                duplicate 1:    Key: AnswerID   Value: AnswerInfo
                duplicate 2:    Key: parentsID  Value: AnswerInfo with higher score
             */
            for (Iterator rowI = root.elementIterator("row"); rowI.hasNext();) {
                rowNode = (Element) rowI.next();
                item rowItem = parseRow(rowNode);
                if (!rowItem.isQuestion) {
                    answerMap.put(rowItem.id, rowItem);
                    if ( !answerMap.containsKey(rowItem.refId) ||
                            (answerMap.containsKey(rowItem.refId) && answerMap.get(rowItem.refId).score < rowItem.score)) {
                        answerMap.put(rowItem.refId, rowItem);
                    }
                }
            }
            /*
             *  Second time of iteration: for each QUESTION, seatch its accepted answer in the map;
             *                            or just search its own id for key to get the answer with highest score if it has
             *                            not get an accepted answer.
             */
            for (Iterator rowI = root.elementIterator("row"); rowI.hasNext();) {
                rowNode = (Element) rowI.next();
                item rowItem = parseRow(rowNode);
                if (rowItem.isQuestion) {
                    QApair tmpPair = new QApair();
                    tmpPair.setItemValue(rowItem);
                    String answerText = "ANSWER OUT OF FILE";
                    int answerScore = 0;
                    if (rowItem.refId == 0) {
                        if (!answerMap.containsKey(rowItem.id)) {    //question without any answer
                            continue;
                        }
                        answerText = answerMap.get(rowItem.id).body;  // question without accepted answer, choose higest socre answer
                        answerScore = answerMap.get(rowItem.id).score;
                    } else if (answerMap.containsKey(rowItem.refId)){
                        answerText = answerMap.get(rowItem.refId).body; // question with accepted answer
                        answerScore = answerMap.get(rowItem.refId).score;
                    }
                    tmpPair.ascore = answerScore;
                    tmpPair.answer = answerText;
                    pairs.add(tmpPair);
                    answerMap.put(rowItem.id, rowItem);
                }
            }

            // generate JSON file from QA pairs
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            List<QApairJson> jsonList = new ArrayList<>();
            int count = 0;      //  test control count
            for (QApair pr : pairs) {
                count ++;
                QApairJson json = new QApairJson(pr);
                json.id = count;
                json.q = JsonFormatUtil.purifyHTML(json.q).replaceAll("\\s+", " ")
                        .replace("\"", "\\\"")
                        .replace("\'", "\\\'");
                json.a = JsonFormatUtil.purifyHTML(json.a).replaceAll("\\s+", " ")
                        .replace("\"", "\\\"")
                        .replace("\'", "\\\'");
                jsonList.add(json);
            }
            System.out.println("List size:" + jsonList.size());
            //String formattedJsonStr = JsonFormatUtil.formatJson(gson.toJson(jsonList));
            String formattedJsonStr = gson.toJson(jsonList);
//            formattedJsonStr = JsonFormatUtil.purifyHTML(formattedJsonStr);
            Writer write = new OutputStreamWriter(new FileOutputStream(jsonpath), "UTF-8");
            write.write(formattedJsonStr);
            write.flush();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private item parseRow(Element rowNode) {
        item newItem = new item();
        for(Iterator attriI = rowNode.attributeIterator();attriI.hasNext();){
            Attribute attribute = (Attribute) attriI.next();
            String name = attribute.getName();
            String value = attribute.getValue();
            if (name.equals("Id")) {
                newItem.id = Integer.parseInt(value);
            } else if (name.equals("PostTypeId")) {
                newItem.isQuestion = value.equals("1");
            } else if (name.equals("Body")) {
                newItem.body = value;
            } else if (name.equals("Title")) {
                newItem.title = value;
            } else if (name.equals("AcceptedAnswerId") || name.equals("ParentId")) {
                newItem.refId = Integer.parseInt(value);
            } else if (name.equals("Score")) {
                newItem.score = Integer.parseInt(value);
            }
            //System.out.println("name:" + name + "\n value:" + value);
        }
        return newItem;
    }

}
