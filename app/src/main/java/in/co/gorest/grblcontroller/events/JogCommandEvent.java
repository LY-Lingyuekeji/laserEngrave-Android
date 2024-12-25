package in.co.gorest.grblcontroller.events;

public class JogCommandEvent {

    private String command;
    private String status;

    public JogCommandEvent(String command){
        this.command = command;
    }

    public String getCommand(){ return this.command; }
    public void setCommand(String command){ this.command = command; }

    public String getStatus(){ return this.status; }
    public void setStatus(String status){ this.status = status; }

}
