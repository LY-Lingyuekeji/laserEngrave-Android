package in.co.gorest.grblcontroller.events;

public class AfterUploadFileEvent {

    private String message;

    public AfterUploadFileEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
