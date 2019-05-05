package CS.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import static org.apache.poi.ss.usermodel.Cell.*;

public class QueryDataRow {
    private int id;
    private String question;
    private int answerNumb;
    private String[] answerList;
    private int[] answerIDList;

    public QueryDataRow(Row row){
        // get id
        Cell cell = row.getCell(0);
        id = (int) cell.getNumericCellValue();
        // get question
        cell = row.getCell(1);
        question = cell.getStringCellValue();
        // get answerNumb
        cell = row.getCell(2);
        answerNumb = (int) cell.getNumericCellValue();
        answerIDList = new int[answerNumb];
        for (int i = 3; i < row.getLastCellNum(); i++) {
            cell = row.getCell(i);
            answerIDList[i-3] = (int)cell.getNumericCellValue();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getAnswerNumb() {
        return answerNumb;
    }

    public void setAnswerNumb(int answerNumb) {
        this.answerNumb = answerNumb;
    }

    public String[] getAnswerList() {
        return answerList;
    }

    public int[] getAnswerIDList() {
        return answerIDList;
    }

    public void setAnswerList(String[] answerList) {
        this.answerList = answerList;
    }


    private static Object getValue(Cell cell) {
        Object obj = null;
        switch (cell.getCellType()) {
            case CELL_TYPE_BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            case CELL_TYPE_ERROR:
                obj = cell.getErrorCellValue();
                break;
            case CELL_TYPE_NUMERIC:
                obj = cell.getNumericCellValue();
                break;
            case CELL_TYPE_STRING:
                obj = cell.getStringCellValue();
                break;
            default:
                break;
        }
        return obj;
    }
}
