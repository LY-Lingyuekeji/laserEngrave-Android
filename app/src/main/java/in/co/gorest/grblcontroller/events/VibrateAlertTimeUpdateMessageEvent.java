package in.co.gorest.grblcontroller.events;

public class VibrateAlertTimeUpdateMessageEvent {
    private String message;

    public VibrateAlertTimeUpdateMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
