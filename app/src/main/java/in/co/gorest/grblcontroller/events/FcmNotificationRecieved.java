package in.co.gorest.grblcontroller.events;

import in.co.gorest.grblcontroller.model.GrblNotification;

public class FcmNotificationRecieved {

    private final GrblNotification grblNotification;

    public FcmNotificationRecieved(GrblNotification grblNotification){
        this.grblNotification = grblNotification;
    }

    public GrblNotification getGrblNotification(){
        return this.grblNotification;
    }


}
