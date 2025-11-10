package in.co.gorest.grblcontroller.model;

import java.util.List;

public class ZoomViewRecord {
    private float resols;
    private int depthProgress;
    private int speedProgress;
    private int speedLevel;
    private int laserLevel;
    private int operationMode;
    private float scaleX;
    private float scaleY;
    private boolean andReverse;
    private int sharp;
    private int wide;
    private int height;
    private int editScrollX;
    private int editWideX;
    private int editHighY;
    private List<String> gcodes;
    private String types;
    private String imagePath;

    public ZoomViewRecord() {}

    public float getResols() {
        return resols;
    }

    public void setResols(float resols) {
        this.resols = resols;
    }

    public int getDepthProgress() {
        return depthProgress;
    }

    public void setDepthProgress(int depthProgress) {
        this.depthProgress = depthProgress;
    }

    public int getSpeedProgress() {
        return speedProgress;
    }

    public void setSpeedProgress(int speedProgress) {
        this.speedProgress = speedProgress;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    public int getLaserLevel() {
        return laserLevel;
    }

    public void setLaserLevel(int laserLevel) {
        this.laserLevel = laserLevel;
    }

    public int getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(int operationMode) {
        this.operationMode = operationMode;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public boolean isAndReverse() {
        return andReverse;
    }

    public void setAndReverse(boolean andReverse) {
        this.andReverse = andReverse;
    }

    public int getSharp() {
        return sharp;
    }

    public void setSharp(int sharp) {
        this.sharp = sharp;
    }

    public int getWide() {
        return wide;
    }

    public void setWide(int wide) {
        this.wide = wide;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getEditScrollX() {
        return editScrollX;
    }

    public void setEditScrollX(int editScrollX) {
        this.editScrollX = editScrollX;
    }

    public int getEditWideX() {
        return editWideX;
    }

    public void setEditWideX(int editWideX) {
        this.editWideX = editWideX;
    }

    public int getEditHighY() {
        return editHighY;
    }

    public void setEditHighY(int editHighY) {
        this.editHighY = editHighY;
    }

    public List<String> getGcodes() {
        return gcodes;
    }

    public void setGcodes(List<String> gcodes) {
        this.gcodes = gcodes;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
