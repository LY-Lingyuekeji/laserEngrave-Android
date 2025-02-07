package in.co.gorest.grblcontroller.events;

public class SpeedLevelUpdateMessageEvent {
    private int speedLevel;

    public SpeedLevelUpdateMessageEvent(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }
}
