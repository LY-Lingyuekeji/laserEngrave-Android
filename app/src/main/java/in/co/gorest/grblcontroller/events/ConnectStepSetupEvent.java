package in.co.gorest.grblcontroller.events;

public class ConnectStepSetupEvent {

    private String message;

    public ConnectStepSetupEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

    public void setMessage(String message){
        this.message = message;
    }

}
