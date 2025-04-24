package in.co.gorest.grblcontroller.events;

public class CommonCommandValueMessageEvent {
    private String message;

    public CommonCommandValueMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
