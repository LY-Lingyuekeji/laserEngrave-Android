package in.co.gorest.grblcontroller.model;

public class LaserParameter {
    private String materialType;
    private String laserModel;
    private int recommendedSpeed; // 推荐雕刻速度
    private int recommendedPower; // 推荐激光功率
    private String operationMode;  // 添加加工模式（"engraving" 或 "cutting"）

    // 构造方法
    public LaserParameter(String materialType, String laserModel, int recommendedSpeed, int recommendedPower, String operationMode) {
        this.materialType = materialType;
        this.laserModel = laserModel;
        this.recommendedSpeed = recommendedSpeed;
        this.recommendedPower = recommendedPower;
        this.operationMode = operationMode;
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
}
