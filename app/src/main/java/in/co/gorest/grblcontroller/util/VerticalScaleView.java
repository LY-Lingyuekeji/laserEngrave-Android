package in.co.gorest.grblcontroller.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VerticalScaleView extends View {

    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mRulerPaint;
    private float progrees = 10;
    private int mLineWidth=1;
    private int max;
    private int min = -1;
    private int width, high;
    private boolean isCanMove;
    private float[] startY;

    public VerticalScaleView(Context context) {
        super(context);
        init();
    }

    public VerticalScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        max = 85;
        startY = new float[max + 1];
        mLineWidth = 1;
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setAntiAlias(true);//抗锯齿
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mLineWidth);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(25);
        mRulerPaint = new Paint();
        mRulerPaint.setAntiAlias(true);
        mRulerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRulerPaint.setColor(Color.BLACK);
        mRulerPaint.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        high = (85 + 1) * (mLineWidth + ScreenInchUtils.mmToPx((Activity) getContext(),1)) + 10;
        setMeasuredDimension(setMeasureWidth(widthMeasureSpec), high);
    }

    private int setMeasureHeight(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        int result = Integer.MAX_VALUE;
        switch (mode) {
            case MeasureSpec.AT_MOST:
                size = Math.min(result, size);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                size = result;
                break;
        }
        return size;
    }

    private int setMeasureWidth(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        int result = Integer.MAX_VALUE;
        switch (mode) {
            case MeasureSpec.AT_MOST:
                size = Math.min(result, size);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                size = result;
                break;
        }
        return size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        float translate = ScreenInchUtils.mmToPx((Activity) getContext(),1);
        float starty = 10;
        for (int i = max; i > min; i--) {
            starty += mLineWidth;
            if (i % 10 == 0) {
                canvas.drawLine(width / 3, starty, width, starty, mLinePaint);
                String text = i + "";
                Rect rect = new Rect();
                float txtWidth = mTextPaint.measureText(text);
                mTextPaint.getTextBounds(text, 0, text.length(), rect);
                if (i == 0) {
                    canvas.drawText(text, 0 + txtWidth, starty + text.length() * 8, mTextPaint);
                } else
                    canvas.drawText(text, 0, starty + text.length() * 3, mTextPaint);
            } else if (i % 5 == 0) {
                canvas.drawLine(width / 3, starty, (float) (width * 0.65), starty, mLinePaint);
            } else {
                canvas.drawLine(width / 3, starty, (float) (width * 0.55), starty, mLinePaint);
            }
            startY[i] = starty;
            starty = starty + translate;
        }
    }

    public float getStartY(int i) {
        return startY[i];
    }
}
