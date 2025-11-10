package in.co.gorest.grblcontroller.model;

import java.io.Serializable;

public class DeviceConnectRecord implements Serializable {
    private String machineName;
    private String ssid;
    private String ipAddress;
    private String mode;
    private String time;
    private String size;
    private String laserModule;

    public DeviceConnectRecord() {}

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLaserModule() {
        return laserModule;
    }

    public void setLaserModule(String laserModule) {
        this.laserModule = laserModule;
    }
}
