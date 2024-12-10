package in.co.gorest.grblcontroller.model;

import android.net.Uri;

import java.util.List;

/**
 * 作者: liuhuaqian on 2020-12-16.
 */
public class PictureBean {
    private Uri url;
private List<String> ggoden;

    public List<String> getGgoden() {
        return ggoden;
    }

    public void setGgoden(List<String> ggoden) {
        this.ggoden = ggoden;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName;



    public Uri getUrl() {
        return url;
    }

    public void setUrl(Uri url) {
        this.url = url;
    }
}
