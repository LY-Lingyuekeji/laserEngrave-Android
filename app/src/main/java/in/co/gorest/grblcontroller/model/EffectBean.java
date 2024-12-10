package in.co.gorest.grblcontroller.model;

import java.io.Serializable;

public class EffectBean implements Serializable {
    /**
     * 1镜像，2旋转
     */
    private int effectType;
    private int rotate;

    public EffectBean(int effectType, int rotate) {
        this.effectType = effectType;
        this.rotate = rotate;
    }

    public int getEffectType() {
        return effectType;
    }

    public int getRotate() {
        return rotate;
    }
}
