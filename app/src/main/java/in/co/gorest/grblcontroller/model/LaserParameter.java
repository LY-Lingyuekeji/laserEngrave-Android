package in.co.gorest.grblcontroller.model;

public class LaserParameter {
    private String materialType;
    private String laserModel;
    private int recommendedSpeed; // 推荐雕刻速度
    private int recommendedPower; // 推荐激光功率
    private String operationMode;  // 添加加工模式（"engraving" 或 "cutting"）
    private float recommendedGap;  // 扫描间隙
    private ScanDirection  scanDirection;  // 扫描方向
    private boolean isAir;  // 是否开启气泵
    private int zDown;  // 下沉距离

    // 构造方法
    public LaserParameter(String materialType, String laserModel, int recommendedSpeed, int recommendedPower, String operationMode, float recommendedGap, ScanDirection scanDirection, boolean isAir, int zDown) {
        this.materialType = materialType;
        this.laserModel = laserModel;
        this.recommendedSpeed = recommendedSpeed;
        this.recommendedPower = recommendedPower;
        this.operationMode = operationMode;
        this.recommendedGap = recommendedGap;
        this.scanDirection = scanDirection;
        this.isAir = isAir;
        this.zDown = zDown;
    }

    // Getter和Setter方法
    public String getMaterialType() {
        return materialType;
    }

    public String getLaserModel() {
        return laserModel;
    }

    public int getRecommendedSpeed() {
        return recommendedSpeed;
    }

    public int getRecommendedPower() {
        return recommendedPower;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public float getRecommendedGap() {
        return recommendedGap;
    }

    public ScanDirection getScanDirection() {
        return scanDirection;
    }

    public boolean isAir() {
        return isAir;
    }

    public int getzDown() {
        return zDown;
    }
}
