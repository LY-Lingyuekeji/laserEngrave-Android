package in.co.gorest.grblcontroller.events;

public class ViewPagerItemEvent {

    private int postion;

    public ViewPagerItemEvent(int postion){
        this.postion = postion;
    }

    public int getPostion() {
        return postion;
    }

    public void setPostion(int postion) {
        this.postion = postion;
    }
}
