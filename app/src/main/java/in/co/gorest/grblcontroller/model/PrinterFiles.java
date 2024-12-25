package in.co.gorest.grblcontroller.model;

import java.io.Serializable;

public class PrinterFiles implements Serializable {
    private String fileName;
    private String fileSize;
    private String fullPath;
    private int localIndex;

    public PrinterFiles() {
    }

    public PrinterFiles(String str, String str2, String str3) {
        this.fileName = str;
        this.fileSize = str2;
        this.fullPath = str3;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String str) {
        this.fileName = str;
    }

    public String getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(String str) {
        this.fileSize = str;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public void setFullPath(String str) {
        this.fullPath = str;
    }

    public int getLocalIndex() {
        return this.localIndex;
    }

    public void setLocalIndex(int i) {
        this.localIndex = i;
    }
}
