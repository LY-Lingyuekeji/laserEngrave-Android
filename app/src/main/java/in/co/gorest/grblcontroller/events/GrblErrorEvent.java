package in.co.gorest.grblcontroller.events;


import androidx.annotation.NonNull;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.util.GrblLookups;

public class GrblErrorEvent {

    private final String message;

    private int errorCode;
    private String errorName;
    private String errorDescription;

    public GrblErrorEvent(GrblLookups lookups, String message){
        this.message = message;

        String[] inputParts = message.split(":");
        if(inputParts.length == 2){
            String[] lookup = lookups.lookup(inputParts[1].trim());
            if(lookup != null){
                this.errorCode = Integer.parseInt(lookup[0]);
                this.errorName = lookup[1];
                this.errorDescription = lookup[2];
            }
        }
    }

    @NonNull
    @Override
    public String toString(){
        return GrblController.getInstance().getString(R.string.text_grbl_error_format, errorCode, errorDescription);
    }

    public String getMessage(){ return this.message; }

    public int getErrorCode(){
        return this.errorCode;
    }

    public String getErrorName(){
        return this.errorName;
    }

    public String getErrorDescription(){
        return this.errorDescription;
    }
}
