package in.co.gorest.grblcontroller.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ZoomView extends RelativeLayout {
    // 属性变量
    private float translationX, translationXst, scaleTranX = 0; // 移动X
    private float translationY, translationYst, scaleTranY = 0; // 移动Y
    private float translationXs = 0.0f; // 最后一次移动X
    private float translationYs = 0.0f; // 最后一次移动Y
    private float scaleX = 1;
    private float scaleY = 1;

    public float getBig() {
        return big;
    }

    public void setBig(float big) {
        this.big = big;
    }

    private float big = 1.0f; // 伸缩比例
    private float rotation; // 旋转角度

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    private ImageView imageView;
    // 移动过程中临时变量
    private float actionX;
    private float actionY;
    private float spacing;
    private float degree;

    public void initTranslation(float translationX, float translationY, float translationXt) {
        this.translationX = translationX;
        this.translationY = translationY;
        translationYst = translationY;
        translationXst = translationXt;
    }

    public void initTranslationX(float translationX) {
        this.translationX = translationX- scaleTranX;
        setTranslationX(this.translationX);
        imageView.setTranslationX(translationX + scaleTranX);
    }

    public void initTranslationY(float translationy) {
        translationy = -translationy;
        this.translationY = translationy+scaleTranY;
        setTranslationY(translationY);
        imageView.setTranslationY(translationY - imageView.getHeight() / 2 + scaleTranY);
    }

    public void setZoom(OnZoom zoom) {
        this.zoom = zoom;
    }

    private OnZoom zoom;

    public interface OnZoom {
        void onZoom(int i, float scaleX, float scaleY);
    }

    public int getMoveType() {
        return moveType;
    }

    private int moveType; // 0=未选择，1=拖动，2=缩放

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    private boolean isSelected;


    public ZoomView(Context context) {
        this(context, null);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (zoom != null) {
            zoom.onZoom(-1, 0, 0);
        }
        if (!isSelected) {
            moveType = 4;
            return super.onTouchEvent(event);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                moveType = 1;
                actionX = event.getRawX();
                actionY = event.getRawY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                moveType = 2;
                spacing = getSpacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (moveType == 1) {
                    translationX = translationX + event.getRawX() - actionX;
                    translationY = translationY + event.getRawY() - actionY;
                    Log.e("ZoomView", translationX + "=====" + translationY + "==" + getX() + "==" + getY() + "==" + getRight() + "==" + getBottom() + "=" + scaleX + "==" + scaleY + "===" + getTop() + "==" + getLeft());
                    updateTranslationX();
                    updateTranslationY();
                    setTranslationX(translationX);
                    setTranslationY(translationY);
                    imageView.setTranslationX(translationX + scaleTranX);
                    imageView.setTranslationY(translationY - imageView.getHeight() / 2 + scaleTranY);
                    actionX = event.getRawX();
                    actionY = event.getRawY();
                    if (zoom != null) {
                        zoom.onZoom(moveType, scaleX, scaleY);
                    }

                } else if (moveType == 2) {
                    scaleX = scaleX * getSpacing(event) / spacing;
                    scaleY = scaleY * getSpacing(event) / spacing;
                    if (scaleX > big) {
                        scaleX = big;
                    }
                    if (scaleY > big) {
                        scaleY = big;
                    }
                    setScaleX(scaleX);
                    setScaleY(scaleY);
                    if (scaleX == 1) {
                        scaleTranX = 0;
                    } else if (scaleX > 1) {
                        scaleTranX = getWidth() * (scaleX - 1) / 2 * -1;
                    } else {
                        scaleTranX = getWidth() * (1 - scaleX) / 2;
                    }
                    if (scaleY == 1) {
                        scaleTranY = 0;
                    } else if (scaleY > 1) {
                        scaleTranY = getHeight() * (scaleY - 1) / 2 * -1;
                    } else {
                        scaleTranY = getHeight() * (1 - scaleY) / 2;
                    }
                    imageView.setTranslationX(translationX + scaleTranX);
                    imageView.setTranslationY(translationY - imageView.getHeight() / 2 + scaleTranY);
                    if (zoom != null) {
                        zoom.onZoom(2, scaleX, scaleY);
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (moveType == 1) {
                    if (translationXs == translationX && translationYs == translationY) {
                        moveType = 4;
                    } else {
                        translationXs = translationX;
                        translationYs = translationY;
                    }
                } else moveType = 0;
        }
        return super.onTouchEvent(event);
    }

    private void updateTranslationX() {
        if (scaleX == 1) {//没缩放过
            translationX = translationX < 0 ? 0 : translationX > translationXst ? translationXst : translationX;
        } else if (scaleX < 1) {//缩小
            float narrowX = getRight() * (1.0f - scaleX) / -2;
            translationX = translationX < narrowX ? narrowX : translationX > translationXst - narrowX ? translationXst - narrowX : translationX;
        } else if (scaleX > 1) {//放大
            float narrowX = getRight() * (scaleX - 1.0f) / 2 + 5;
            translationX = translationX < narrowX ? narrowX : translationX > translationXst - narrowX ? translationXst - narrowX : translationX;
        }
    }

    private void updateTranslationY() {
        if (scaleY == 1) {//没缩放过
            translationY = translationY > 0 ? 0 : translationY < -getTop() ? -getTop() : translationY;
        } else if (scaleY < 1) {//缩小
            float narrowY = (getBottom() - getTop()) * (1.0f - scaleY) / 2;
            translationY = translationY > narrowY ? narrowY : translationY < getTop() * -1 - narrowY ? getTop() * -1 - narrowY : translationY;
        } else if (scaleY > 1) {//放大
            float narrowY = (getBottom() - getTop()) * (scaleY - 1.0f) / -2;
            translationY = translationY > narrowY ? narrowY : translationY < getTop() * -1 - narrowY ? getTop() * -1 - narrowY : translationY;

        }
    }

    // 触碰两点间距离
    private float getSpacing(MotionEvent event) {
        //通过三角函数得到两点间的距离
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取旋转角度
    private float getDegree(MotionEvent event) {
        //得到两个手指间的旋转角度
        double delta_x = event.getX(0) - event.getX(1);
        double delta_y = event.getY(0) - event.getY(1);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void setScalesX(float scalex) {
        scaleX = scalex;
        setScaleX(scaleX);
        if (scaleX == 1) {
            scaleTranX = 0;
        } else if (scaleX > 1) {
            scaleTranX = getWidth() * (scaleX - 1) / 2 * -1;
        } else {
            scaleTranX = getWidth() * (1 - scaleX) / 2;
        }
        dealAfterScale();
    }

    public void setScalesY(float scaley) {
        scaleY = scaley;
        setScaleY(scaleY);
        if (scaleY == 1) {
            scaleTranY = 0;
        } else if (scaleY > 1) {
            scaleTranY = getHeight() * (scaleY - 1) / 2 * -1;
        } else {
            scaleTranY = getHeight() * (1 - scaleY) / 2;
        }
        dealAfterScale();
    }

    public void dealAfterScale() {
        if (scaleTranX < 0) {
            scaleTranX = 0;
            initTranslationX(scaleTranX);
        } else {
            imageView.setTranslationX(translationX + scaleTranX);
        }
        if (scaleTranY < 0) {
            scaleTranY = 0;
            initTranslationY(scaleTranY);
        } else {
            imageView.setTranslationY(translationY - imageView.getHeight() / 2 + scaleTranY);
        }
        if (scaleX == 1) {
            scaleTranX = 0;
        } else if (scaleX > 1) {
            scaleTranX = getWidth() * (scaleX - 1) / 2 * -1;
        } else {
            scaleTranX = getWidth() * (1 - scaleX) / 2;
        }
        if (scaleY == 1) {
            scaleTranY = 0;
        } else if (scaleY > 1) {
            scaleTranY = getHeight() * (scaleY - 1) / 2 * -1;
        } else {
            scaleTranY = getHeight() * (1 - scaleY) / 2;
        }
        imageView.setTranslationX(translationX + scaleTranX);
        imageView.setTranslationY(translationY - imageView.getHeight() / 2 + scaleTranY);
    }

    public void setScalesxY(float scale) {
        scaleY = scale;
        scaleX = scale;
        setScaleX(scaleX);
        setScaleY(scaleY);
    }
}
