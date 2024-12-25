package in.co.gorest.grblcontroller.events;

public class WifiNameEvent {

    private String message;

    public WifiNameEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

    public void setMessage(String message){
        this.message = message;
    }

}
