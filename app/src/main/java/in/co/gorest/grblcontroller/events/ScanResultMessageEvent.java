package in.co.gorest.grblcontroller.events;

public class ScanResultMessageEvent {

    private String message;

    public ScanResultMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
