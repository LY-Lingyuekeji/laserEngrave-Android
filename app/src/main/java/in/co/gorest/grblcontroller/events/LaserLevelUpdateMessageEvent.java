package in.co.gorest.grblcontroller.events;

public class LaserLevelUpdateMessageEvent {
    private int laserLevel;

    public LaserLevelUpdateMessageEvent(int laserLevel) {
        this.laserLevel = laserLevel;
    }

    public int getLaserLevel() {
        return laserLevel;
    }
}
