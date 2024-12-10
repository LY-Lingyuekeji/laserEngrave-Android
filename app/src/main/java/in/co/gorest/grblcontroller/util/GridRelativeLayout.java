package in.co.gorest.grblcontroller.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GridRelativeLayout extends View {
    public final static int NONE = 0;
    /**
     * 按下
     */
    public final static int PRESS = 1;
    /**
     * 左移
     */
    public final static int LEFT = 2;
    /**
     * 右移
     */
    public final static int RIGHT = 3;
    /**
     * 上移
     */
    public final static int UP = 4;
    /**
     * 下移
     */
    public final static int DOWN = 5;
    /**
     * 长按
     */
    public final static int LONG_PRESS = 6;
    /**
     * 放大
     */
    public final static int AMPLIFICATION = 7;
    /**
     * 缩小
     */
    public final static int NARROW = 8;

    private static final float MIN_MOVE_DISTANCE = 15;

    private int mTouchMode;
    private float mStartX, mStartY;
    private long mStartTime;
    private float mFingerSpace;


    //=================

    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mRulerPaint;
    private float progrees = 10;
    private int mLineWidth=1;
    private int max, maxs;
    private int min = -1;
    private int width;


    public int getWidths() {
        return width;
    }

    public int getHigh() {
        return high;
    }

    private int high;
    int cur_x;
    int cur_y;
    private boolean isCanMove;
    private float[] startY, startXs;

    private int limitwidth, limithigh;

    public void setlimit(int width, int high) {
        limitwidth = width;
        limithigh = high;
    }

    public GridRelativeLayout(Context context) {
        super(context);
        init();
    }

    public GridRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setMax(int ma) {
        maxs = ma + 1;
        startXs = new float[maxs + 1];
        invalidate();
    }

    private void init() {
        max = 85;
        startY = new float[max + 1];
        maxs = 85;
        startXs = new float[maxs + 1];
        mLineWidth = 1;
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);//抗锯齿
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mLineWidth);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(25);
        mRulerPaint = new Paint();
        mRulerPaint.setAntiAlias(true);
        mRulerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRulerPaint.setColor(Color.WHITE);
        mRulerPaint.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wh = 85 * (mLineWidth + ScreenInchUtils.mmToPx((Activity) getContext(),1));
        int hg =  85 * (mLineWidth + ScreenInchUtils.mmToPx((Activity) getContext(),1))+10;
        int whs = getMeasuredWidth();
        int hgs = getMeasuredHeight();
        width = wh < whs ? whs : wh;
        high = hg < hgs ? hgs : hg;
        setMeasuredDimension(width, high);
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

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        float translate = ScreenInchUtils.mmToPx((Activity) getContext(),1);
        float starty = 10;
        for (int i = max; i > min; i--) {
            starty += mLineWidth;
            if (i % 10 == 0) {
                mLinePaint.setColor(Color.parseColor("#000000"));
                canvas.drawLine(0, starty, width, starty, mLinePaint);
            } else {
                mLinePaint.setColor(Color.parseColor("#F0F0F0"));
                canvas.drawLine(0, starty, width, starty, mLinePaint);
            }
            startY[i] = starty;
            starty = starty + translate;

        }
        float translates = ScreenInchUtils.mmToPx((Activity) getContext(),1);
        Log.e("GridRelativeLayout",width + "=translate=" + translate + "=" + (maxs * 4));
        float startX = 0;
        for (int i = 0; i < maxs; i++) {
            startX += mLineWidth;
            if (i % 10 == 0) {
                mLinePaint.setColor(Color.parseColor("#000000"));
                canvas.drawLine(startX, 0, startX, high, mLinePaint);
            } else {
                mLinePaint.setColor(Color.parseColor("#F0F0F0"));
                canvas.drawLine(startX, 0, startX, high, mLinePaint);
            }
            startXs[i] = startX;
            startX = startX + translates;
        }
    }

    public float getStartY(int i) {
        return startY[i];
    }
    private int lastX;
    private int lastY;

    private void getGestures(MotionEvent event) {
        // 2个手指以上
        if (event.getPointerCount() >= 2) {
            //先判断是否是缩放
            float x1 = event.getX(0) - event.getX(1);
            float y1 = event.getY(0) - event.getY(1);
            //第一个手指和第二个手指的间距
            float value = (float) Math.sqrt(x1 * x1 + y1 * y1);
            if (mFingerSpace == 0) {
                mFingerSpace = value;
            } else {
                //一段时间内，如果两值间的变化不大，则认为是移动，否则是；加时间限制是为了防止反应过快
                if (System.currentTimeMillis() - mStartTime > 50) {
                    //移动后两指的间距的变化值
                    float fingerDistanceChange = value - mFingerSpace;
                    //同时手指间的间距变化大于最小距离时就认为是缩放
                    if (Math.abs(fingerDistanceChange) > MIN_MOVE_DISTANCE) {
                        float scale = value / mFingerSpace;
                        if (scale > 1) {
                            mTouchMode = AMPLIFICATION;
                        } else {
                            mTouchMode = NARROW;
                        }
                        mStartTime = System.currentTimeMillis();
                        mStartX = event.getX();
                        mStartY = event.getY();
                        mFingerSpace = value;
                        //当第一个手指200毫秒内移动MIN_MOVE_DISTANCE倍距离，同时手指间的间距变化小于4倍距离时为移动
                    }
                }
                return;
            }
        }
        //判断是否长按,x方向移动小于最小距离同时y方向小于最小距离，
        float offsetX = Math.abs(event.getX() - mStartX);
        float offsetY = Math.abs(event.getY() - event.getY());
        long time = System.currentTimeMillis() - mStartTime;
        if (time > 1500 && Math.abs(offsetX) < MIN_MOVE_DISTANCE && Math.abs(offsetY) < MIN_MOVE_DISTANCE) {
            mTouchMode = LONG_PRESS;
            return;
        }
        //移动时 区分上下还是左右
        if (System.currentTimeMillis() - mStartTime > 50) {
            float xDistance = event.getX() - mStartX;
            float yDistance = event.getY() - mStartY;
            if (Math.abs(xDistance) > Math.abs(yDistance)) {
                if (xDistance > 0) {
                    /**右移*/
                    mTouchMode = RIGHT;

                } else {
                    /**左移*/
                    mTouchMode = LEFT;
                }
            } else {
                if (yDistance > 0) {
                    /**下移*/
                    mTouchMode = DOWN;
                } else {
                    /**上移*/
                    mTouchMode = UP;
                }
            }
            scrollBy((int) xDistance, (int) yDistance);
            mStartTime = System.currentTimeMillis();
            mStartX = event.getX();
            mStartY = event.getY();
        }
    }

    @Override

    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);

    }
}
