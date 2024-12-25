package in.co.gorest.grblcontroller.events;


import androidx.annotation.NonNull;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.util.GrblLookups;

public class GrblAlarmEvent {

    private final String message;

    private int alarmCode;
    private String alarmName;
    private String alarmDescription;

    public GrblAlarmEvent(GrblLookups lookups, String message){
        this.message = message;

        String[] inputParts = message.split(":");
        if(inputParts.length == 2){
            String[] lookup = lookups.lookup(inputParts[1].trim());
            if(lookup != null){
                this.alarmCode = Integer.parseInt(lookup[0]);
                this.alarmName = lookup[1];
                this.alarmDescription = lookup[2];
            }
        }
    }

    @NonNull
    @Override
    public String toString(){
        return GrblController.getInstance().getString(R.string.text_grbl_alarm_format, alarmCode, alarmDescription);
    }

    public String getMessage(){ return this.message; }

    public int getAlarmCode(){return this.alarmCode; }

    public String getAlarmName(){
        return this.alarmName;
    }

    public String getAlarmDescription(){
        return this.alarmDescription;
    }

}
