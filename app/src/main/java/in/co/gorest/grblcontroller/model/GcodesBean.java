package in.co.gorest.grblcontroller.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.util.FileUtils;

public class GcodesBean implements Serializable {
    /**
     * 叠加图片的uri
     */
    private String superpositionUri = "";
    private String recordName;
    private boolean isSave;
    private List<GcodesItemBean> gcodesItemBeans = new ArrayList<>();

    public String getSuperpositionUri() {
        return superpositionUri;
    }

    public void setSuperpositionUri(String superpositionUri) {
        this.superpositionUri = superpositionUri;
    }

    public String getRecordName() {
        if (recordName == null && gcodesItemBeans != null && gcodesItemBeans.size() > 0)
            recordName = gcodesItemBeans.get(0).fileName.replace(".nc", "");
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public List<GcodesItemBean> getGcodesItemBeans() {
        return gcodesItemBeans;
    }

    public void setGcodesItemBeans(List<GcodesItemBean> gcodesItemBeans) {
        this.gcodesItemBeans = gcodesItemBeans;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public static class GcodesItemBean implements Serializable{
        private boolean isMater = false;
        private String fileTime;
        private String available;
        private int editHighY;
        private int editWideX;
//        private List<String> gcode;
        private String uri = "";
        private int Wide;
        private String types;
        private int height;
        private String depthProgress;
        private String speedProgress;
        private String fileName;
        private String ncFilePath;

        public String getNcFilePath() {
            return ncFilePath;
        }

        public void setNcFilePath(String ncFilePath) {
            this.ncFilePath = ncFilePath;
        }

        public boolean isMater() {
            return isMater;
        }

        public void setMater(boolean mater) {
            isMater = mater;
        }

        public int getEditHighY() {
            return editHighY;
        }

        public void setEditHighY(int editHighY) {
            this.editHighY = editHighY;
        }

        public int getEditWideX() {
            return editWideX;
        }

        public void setEditWideX(int editWideX) {
            this.editWideX = editWideX;
        }

        public String getFileTime() {
            return fileTime;
        }

        public void setFileTime(String fileTime) {
            this.fileTime = fileTime;
        }

        public String getAvailable() {
            return available;
        }

        public void setAvailable(String available) {
            this.available = available;
        }

        public String getTypes() {
            return types;
        }

        public void setTypes(String types) {
            this.types = types;
        }

        public int getWide() {
            return Wide;
        }

        public void setWide(int wide) {
            Wide = wide;
        }

        public int getHeight() {
            return this.height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public List<String> getGcode() {
            List<String> gcode = FileUtils.getFileContents(new File(ncFilePath));
            return gcode;
        }
//
//        public void setGcode(List<String> gcode) {
//            this.gcode = gcode;
//        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getDepthProgress() {
            return depthProgress;
        }

        public void setDepthProgress(String depthProgress) {
            this.depthProgress = depthProgress;
        }

        public String getSpeedProgress() {
            return speedProgress;
        }

        public void setSpeedProgress(String speedProgress) {
            this.speedProgress = speedProgress;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;

        }

    }

    @Override
    public String toString() {
        return "GcodesBean{" +
                "superpositionUri='" + superpositionUri + '\'' +
                ", recordName='" + recordName + '\'' +
                ", isSave=" + isSave +
                ", gcodesItemBeans=" + gcodesItemBeans +
                '}';
    }
}
