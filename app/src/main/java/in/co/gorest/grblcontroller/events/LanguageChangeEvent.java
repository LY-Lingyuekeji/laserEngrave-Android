package in.co.gorest.grblcontroller.events;

public class LanguageChangeEvent {

    private String message;

    public LanguageChangeEvent(String message){
        this.message = message;
    }

    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

}
