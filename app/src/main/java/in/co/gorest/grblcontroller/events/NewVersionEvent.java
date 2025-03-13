package in.co.gorest.grblcontroller.events;

public class NewVersionEvent {

    private String message;

    public NewVersionEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

}
