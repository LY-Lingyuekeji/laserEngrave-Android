package in.co.gorest.grblcontroller.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class StrokeTextView extends AppCompatTextView {
    private boolean isHollow = false;
    private int strokeWidth = 3;
    private int extraPadding = 10; // 防裁剪用的额外 padding

    public StrokeTextView(Context context) {
        super(context);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 添加额外 padding 防止裁剪
        setPadding(getPaddingLeft() + extraPadding,
                getPaddingTop() + extraPadding,
                getPaddingRight() + extraPadding,
                getPaddingBottom() + extraPadding);
        setClipToOutline(false);
    }

    public void setHollowText(boolean hollow) {
        this.isHollow = hollow;
        invalidate();
    }

    public boolean isHollowText() {
        return isHollow;
    }

    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        int originalColor = paint.getColor();
        float originalStrokeWidth = paint.getStrokeWidth();
        Paint.Style originalStyle = paint.getStyle();

        if (isHollow) {
            // 设置描边属性
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            super.onDraw(canvas); // 描边文字
        } else {
            paint.setStyle(Paint.Style.FILL);
            super.onDraw(canvas); // 普通文字
        }

        // 恢复画笔属性
        paint.setStyle(originalStyle);
        paint.setStrokeWidth(originalStrokeWidth);
        paint.setColor(originalColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 手动增加控件尺寸防止描边或倾斜后裁剪
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int extra = strokeWidth + extraPadding;

        setMeasuredDimension(width + extra, height + extra);
    }
}
