package in.co.gorest.grblcontroller.events;

public class FragmentCommandEvent {

    private String message;

    public FragmentCommandEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

}
