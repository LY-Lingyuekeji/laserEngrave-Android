package in.co.gorest.grblcontroller.model;

public class Device {
    private String name;
    private String travel;

    public Device(String name, String travel) {
        this.name = name;
        this.travel = travel;
    }

    public String getName() {
        return name;
    }

    public String getTravel() {
        return travel;
    }
}
