package in.co.gorest.grblcontroller.events;

public class TelnetConnectEvent {

    private String message;

    public TelnetConnectEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
