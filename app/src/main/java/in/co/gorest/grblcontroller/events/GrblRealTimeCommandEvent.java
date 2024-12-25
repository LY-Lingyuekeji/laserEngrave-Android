package in.co.gorest.grblcontroller.events;

public class GrblRealTimeCommandEvent {

    private final byte command;

    public GrblRealTimeCommandEvent(byte command){
        this.command = command;
    }

    public byte getCommand(){
        return this.command;
    }

}
