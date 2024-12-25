package in.co.gorest.grblcontroller.events;

public class ControltoPreViewMessageEvent {

    private final String message;

    public ControltoPreViewMessageEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

}
