package in.co.gorest.grblcontroller.events;

public class FilePathEvent {

    private int from;
    private String path;

    public FilePathEvent(int from, String path) {
        this.from = from;
        this.path = path;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
