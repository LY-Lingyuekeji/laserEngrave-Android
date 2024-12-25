package in.co.gorest.grblcontroller.model;

import java.io.Serializable;

public class CommandSetInfo implements Serializable {
    private String currentPower;
    private String currentSpeed;
    private String distance;
    private String option;
    private String power;
    private String printFileName;
    private String speed;

    public String getDistance() {
        return this.distance;
    }

    public void setDistance(String str) {
        this.distance = str;
    }

    public String getCurrentPower() {
        return this.currentPower;
    }

    public String getCurrentSpeed() {
        return this.currentSpeed;
    }

    public void setCurrentPower(String str) {
        this.currentPower = str;
    }

    public void setCurrentSpeed(String str) {
        this.currentSpeed = str;
    }

    public String getPrintFileName() {
        return this.printFileName;
    }

    public void setPrintFileName(String str) {
        this.printFileName = str;
    }

    public void setSpeed(String str) {
        this.speed = str;
    }

    public String getSpeed() {
        return this.speed;
    }

    public void setPower(String str) {
        this.power = str;
    }

    public String getPower() {
        return this.power;
    }

    public String getOption() {
        return this.option;
    }

    public void setOption(String str) {
        this.option = str;
    }
}
