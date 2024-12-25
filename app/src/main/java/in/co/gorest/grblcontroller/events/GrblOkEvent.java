package in.co.gorest.grblcontroller.events;

public class GrblOkEvent {

    private String message;

    public GrblOkEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

}
