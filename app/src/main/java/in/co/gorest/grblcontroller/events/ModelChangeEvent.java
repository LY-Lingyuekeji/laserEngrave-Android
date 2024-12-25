package in.co.gorest.grblcontroller.events;

public class ModelChangeEvent {

    private String message;

    public ModelChangeEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

}
