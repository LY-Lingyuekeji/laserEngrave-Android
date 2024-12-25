package in.co.gorest.grblcontroller.events;

public class DeviceConnectEvent {

    private String type;
    private String name;
    private String address;

    public DeviceConnectEvent(String type, String name, String address) {
        this.type = type;
        this.name = name;
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
