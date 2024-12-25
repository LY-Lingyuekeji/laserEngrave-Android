package in.co.gorest.grblcontroller.events;

public class ConsoleMessageEvent {

    private final String message;

    public ConsoleMessageEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

}
