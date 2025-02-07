package in.co.gorest.grblcontroller.model;

public class Material {
    private String name;
    private int imageResId;

    public Material(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
