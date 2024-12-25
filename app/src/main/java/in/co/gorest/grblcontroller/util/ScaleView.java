package in.co.gorest.grblcontroller.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 刻度尺
 */
public class ScaleView extends View {

    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mRulerPaint;
    private float progrees = 10;
    private int mLineWidth=1;
    private int max;

    private int min = 0;
    //    private int width;
    private boolean isCanMove;
    private float startXs[];

    public ScaleView(Context context) {
        super(context);
        init();
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setMax(int ma) {
        max = ma + 1;
        startXs = new float[max + 1];
        invalidate();
    }

    private void init() {
        max = 85 + 1;
        startXs = new float[max + 1];
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
        int sd = (85 + 2) * (mLineWidth + ScreenInchUtils.mmToPx((Activity) getContext(),1)) + 5;
        int sdd = setMeasureWidth(widthMeasureSpec);
        Log.e("ScaleView", sd + "mmToPx=" + ScreenInchUtils.mmToPx((Activity) getContext(),1));
        setMeasuredDimension(sd , setMeasureHeight(heightMeasureSpec));
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
        float startX = 5;
        for (int i = min; i < max; i++) {
            startX += mLineWidth;
            if (i % 10 == 0) {
                canvas.drawLine(startX, 0, startX, 72, mLinePaint);
                String text = i + "";
                Rect rect = new Rect();
                float txtWidth = mTextPaint.measureText(text);
                mTextPaint.getTextBounds(text, 0, text.length(), rect);
                canvas.drawText(text, startX - txtWidth / 2, 72 + rect.height() + 10, mTextPaint);
            } else if (i % 5 == 0) {
                canvas.drawLine(startX, 36, startX, 72, mLinePaint);
            } else {
                canvas.drawLine(startX, 48, startX, 72, mLinePaint);
            }
            startXs[i] = startX;
            startX = startX + translate;
        }

    }

    public float getStartX(int i) {
        return startXs[i];
    }
}
