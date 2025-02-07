package in.co.gorest.grblcontroller.events;

public class MaterialTypeUpdateMessageEvent {
    private int material;

    public MaterialTypeUpdateMessageEvent(int material) {
        this.material = material;
    }

    public int getMaterialType() {
        return material;
    }
}
