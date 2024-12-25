package in.co.gorest.grblcontroller.events;

public class BluetoothDisconnectEvent {

    private String message;

    public BluetoothDisconnectEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }

    public void setMessage(String message){
        this.message = message;
    }

}
