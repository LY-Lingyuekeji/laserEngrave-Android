package in.co.gorest.grblcontroller.events;

public class RemoteFileLineJugdeCommandMessageEvent {
    private float maxX;
    private float maxY;

    public RemoteFileLineJugdeCommandMessageEvent(float maxX, float maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }
}
