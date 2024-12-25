package in.co.gorest.grblcontroller.events;

public class ServiceMessageEvent {

    private final String message;

    public ServiceMessageEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

}
