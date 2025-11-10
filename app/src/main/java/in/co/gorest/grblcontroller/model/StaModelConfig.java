package in.co.gorest.grblcontroller.model;

import java.io.Serializable;

public class StaModelConfig implements Serializable {
    private String mode;
    private String machineName;
    private String configSSID;


    public StaModelConfig() {}

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getConfigSSID() {
        return configSSID;
    }

    public void setConfigSSID(String configSSID) {
        this.configSSID = configSSID;
    }
}
