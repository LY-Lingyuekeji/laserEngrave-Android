package in.co.gorest.grblcontroller.events;

public class OperationModeSetupEvent {

    private String message;

    public OperationModeSetupEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

    public void setMessage(String message){
        this.message = message;
    }

}
