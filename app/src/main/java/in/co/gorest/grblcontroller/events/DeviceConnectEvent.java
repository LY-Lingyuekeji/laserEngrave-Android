package in.co.gorest.grblcontroller.events;

public class DeviceConnectEvent {

    private String connectType;
    private String machineName;
    private String wifiName;
    private String ipAddress;

    public DeviceConnectEvent(String connectType, String machineName, String wifiName, String ipAddress) {
        this.connectType = connectType;
        this.machineName = machineName;
        this.wifiName = wifiName;
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getConnectType() {
        return connectType;
    }

    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }
}
