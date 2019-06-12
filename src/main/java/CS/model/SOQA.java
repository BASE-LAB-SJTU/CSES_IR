package CS.model;

public class SOQA {
    String Question = "";
    String Answer = "";
    long qScore = 0;
    long aScore = 0;

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        this.Question = question;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        this.Answer = answer;
    }

    public long getqScore() {
        return qScore;
    }

    public void setqScore(long qScore) {
        this.qScore = qScore;
    }

    public long getaScore() {
        return aScore;
    }

    public void setaScore(long aScore) {
        this.aScore = aScore;
    }
}
