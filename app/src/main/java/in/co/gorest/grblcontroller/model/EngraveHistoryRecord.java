package in.co.gorest.grblcontroller.model;

import java.util.List;

import in.co.gorest.grblcontroller.util.ZoomViewBean;

public class EngraveHistoryRecord {
    private String timestamp;
    private String machineName;
    private List<ZoomViewRecord> zoomViewRecords;
    private int totalEngraveCount;
    private float locations;
    private String imagePath;

    public EngraveHistoryRecord() {}

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public List<ZoomViewRecord> getZoomViewRecords() {
        return zoomViewRecords;
    }

    public void setZoomViewRecords(List<ZoomViewRecord> zoomViewRecords) {
        this.zoomViewRecords = zoomViewRecords;
    }

    public int getTotalEngraveCount() {
        return totalEngraveCount;
    }

    public void setTotalEngraveCount(int totalEngraveCount) {
        this.totalEngraveCount = totalEngraveCount;
    }

    public float getLocations() {
        return locations;
    }

    public void setLocations(float locations) {
        this.locations = locations;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
