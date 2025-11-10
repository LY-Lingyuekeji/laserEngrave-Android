package in.co.gorest.grblcontroller.model;

public class WifiNetwork {
    private String ssid;
    private String ipAddress;

    private String mode;

    // 构造函数
    public WifiNetwork(String ssid, String ipAddress, String mode) {
        this.ssid = ssid;
        this.ipAddress = ipAddress;
        this.mode = mode;
    }

    // Getter 和 Setter 方法
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
}
