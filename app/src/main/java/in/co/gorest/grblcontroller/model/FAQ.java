package in.co.gorest.grblcontroller.model;

public class FAQ {
    private String answer;
    private String question;

    public FAQ(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
    public String getQuestion() {
        return this.question;
    }

    public String getAnswer() {
        return this.answer;
    }
}
