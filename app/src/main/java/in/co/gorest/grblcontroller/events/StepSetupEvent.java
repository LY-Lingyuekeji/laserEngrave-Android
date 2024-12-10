package in.co.gorest.grblcontroller.events;

public class StepSetupEvent {

    private String message;

    public StepSetupEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

    public void setMessage(String message){
        this.message = message;
    }

}
