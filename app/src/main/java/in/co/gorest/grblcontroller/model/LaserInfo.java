package in.co.gorest.grblcontroller.model;

import java.io.Serializable;

public class LaserInfo implements Serializable {
    private String power;
    private String printName;
    private String printingPower;
    private String printingSpeed;
    private String progress;
    private String speed;
    private String status;
    private String xPos;
    private String yPos;

    public void setStatus(String str) {
        this.status = str;
    }

    public String getStatus() {
        return this.status;
    }

    public String getPower() {
        return this.power;
    }

    public String getPrintName() {
        return this.printName;
    }

    public String getProgress() {
        return this.progress;
    }

    public String getSpeed() {
        return this.speed;
    }

    public String getxPos() {
        return this.xPos;
    }

    public String getyPos() {
        return this.yPos;
    }

    public void setPower(String str) {
        this.power = str;
    }

    public void setPrintName(String str) {
        this.printName = str;
    }

    public void setProgress(String str) {
        this.progress = str;
    }

    public void setSpeed(String str) {
        this.speed = str;
    }

    public void setxPos(String str) {
        this.xPos = str;
    }

    public void setyPos(String str) {
        this.yPos = str;
    }

    public String getPrintingPower() {
        return this.printingPower;
    }

    public String getPrintingSpeed() {
        return this.printingSpeed;
    }

    public void setPrintingPower(String str) {
        this.printingPower = str;
    }

    public void setPrintingSpeed(String str) {
        this.printingSpeed = str;
    }
}
