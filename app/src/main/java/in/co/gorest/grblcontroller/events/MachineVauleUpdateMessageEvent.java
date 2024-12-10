package in.co.gorest.grblcontroller.events;

public class MachineVauleUpdateMessageEvent {
    private String message;

    public MachineVauleUpdateMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
