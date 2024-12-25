package in.co.gorest.grblcontroller.events;

public class APModelUploadEvent {

    private String message;

    public APModelUploadEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
