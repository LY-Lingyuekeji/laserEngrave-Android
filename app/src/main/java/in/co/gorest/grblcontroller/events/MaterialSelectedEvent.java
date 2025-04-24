package in.co.gorest.grblcontroller.events;

public class MaterialSelectedEvent {

    private String message;

    public MaterialSelectedEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

    public void setMessage(String message){
        this.message = message;
    }

}
