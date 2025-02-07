package in.co.gorest.grblcontroller.events;

public class LaserTypeUpdateMessageEvent {
    private int laserType;

    public LaserTypeUpdateMessageEvent(int laserType) {
        this.laserType = laserType;
    }

    public int getLaserType() {
        return laserType;
    }
}
