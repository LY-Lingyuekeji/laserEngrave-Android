package in.co.gorest.grblcontroller.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.util.DimensionUtil;


/**
 * 作者: liuhuaqian on 2020-12-18.
 */
public class MySeekBar extends View {

    /********************** 参数 **********************/

    private float progress_max; // 进度条最大值
    private float progress_min; // 进度条最小值
    private float progress_default; // 进度条默认值

    private int track_left_height; // 进度条左边高度
    private int track_right_height; // 进度条右边高度

    private int track_left_color; // 进度条左边颜色
    private int track_right_color; // 进度条右边颜色

    private int thumb_color_default; // 拖动滑块默认颜色
    private int thumb_radius_default; // 拖动滑块半径

    private int thumb_color_on_dragging; // 拖动滑块拖动中颜色
    private int thumb_radius_on_dragging; // 拖动滑块拖动中半径

    /********************** 绘制相关 **********************/

    private Paint paint; // 画笔
    private int xLeft; // 实际的绘图区域按距离父布局左边 padding 算起
    private int xRight; // 到距离父布局右边的的 padding 结束
    private int yCenter; // 确定绘制进度条Y轴意义上的中点

    private int thumb_radius; // 滑动滑块半径
    private onProgressChanged onProgressChanged;

    /********************** 构造函数 **********************/

    public MySeekBar(Context context) {
        this(context, null);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MySeekBar, defStyleAttr, 0);

        this.progress_max = typedArray.getFloat(R.styleable.MySeekBar_progress_max, 100);
        this.progress_min = typedArray.getFloat(R.styleable.MySeekBar_progress_min, 1);
        this.progress_default = typedArray.getFloat(R.styleable.MySeekBar_progress_default, progress_min);

        this.track_left_height = typedArray.getDimensionPixelSize(R.styleable.MySeekBar_track_left_height, DimensionUtil.dp2px(8));
        this.track_right_height = typedArray.getDimensionPixelSize(R.styleable.MySeekBar_track_right_height, track_left_height - DimensionUtil.dp2px(2));

        this.track_left_color = typedArray.getColor(R.styleable.MySeekBar_track_left_color, Color.parseColor("#1E853A"));
        this.track_right_color = typedArray.getColor(R.styleable.MySeekBar_track_right_color, Color.parseColor("#3D3D3D"));

        this.thumb_color_default = typedArray.getColor(R.styleable.MySeekBar_thumb_color_default, track_left_color);
        this.thumb_radius_default = typedArray.getDimensionPixelSize(R.styleable.MySeekBar_thumb_radius_default, track_left_height + DimensionUtil.dp2px(2));

        this.thumb_color_on_dragging = typedArray.getColor(R.styleable.MySeekBar_thumb_color_on_dragging, thumb_color_default);
        this.thumb_radius_on_dragging = typedArray.getDimensionPixelSize(R.styleable.MySeekBar_thumb_radius_on_dragging, thumb_radius_default + DimensionUtil.dp2px(2));

        typedArray.recycle();

        thumb_radius = thumb_radius_default;

        initPaint(); // 初始化画笔
    }

    /********************** Getter and Setter **********************/

    public float getProgressMax() {
        return progress_max;
    }

    public MySeekBar setProgressMax(float progressMax) {
        this.progress_max = progressMax;
        return this;
    }

    public float getProgressMin() {
        return progress_min;
    }

    public MySeekBar setProgressMin(float progressMin) {
        this.progress_min = progressMin;
        return this;
    }

    public float getProgressDefault() {
        return progress_default;
    }

    public MySeekBar setProgressDefault(float progressDefault) {
        this.progress_default = progressDefault;
        return this;
    }
    public void setProgress(float progressDefault) {
        this.progress_default = progressDefault;
        invalidate();
    }
    public void setProgressChanged(onProgressChanged onChanged){
        onProgressChanged=onChanged;
    }
    public int getTrackLeftHeight() {
        return track_left_height;
    }

    public MySeekBar setTrackLeftHeight(int trackLeftHeight) {
        this.track_left_height = trackLeftHeight;
        return this;
    }

    public int getTrackRightHeight() {
        return track_right_height;
    }

    public MySeekBar setTrackRightHeight(int trackRightHeight) {
        this.track_right_height = trackRightHeight;
        return this;
    }

    public int getTrackLeftColor() {
        return track_left_color;
    }

    public MySeekBar setTrackLeftColor(int trackLeftColor) {
        this.track_left_color = trackLeftColor;
        return this;
    }

    public int getTrackRightColor() {
        return track_right_color;
    }

    public MySeekBar setTrackRightColor(int trackRightColor) {
        this.track_right_color = trackRightColor;
        return this;
    }

    public int getThumbColorDefault() {
        return thumb_color_default;
    }

    public MySeekBar setThumbColorDefault(int thumbColorDefault) {
        this.thumb_color_default = thumbColorDefault;
        return this;
    }

    public int getThumbRadiusDefault() {
        return thumb_radius_default;
    }

    public MySeekBar setThumbRadiusDefault(int thumbRadiusDefault) {
        this.thumb_radius_default = thumbRadiusDefault;
        return this;
    }

    public int getThumbColorOnDragging() {
        return thumb_color_on_dragging;
    }

    public MySeekBar setThumbColorOnDragging(int thumbColorOnDragging) {
        this.thumb_color_on_dragging = thumbColorOnDragging;
        return this;
    }

    public int getThumbRadiusOnDragging() {
        return thumb_radius_on_dragging;
    }

    public MySeekBar setThumbRadiusOnDragging(int thumbRadiusOnDragging) {
        this.thumb_radius_on_dragging = thumbRadiusOnDragging;
        return this;
    }

    /********************** 绘制相关 **********************/

    private void initPaint(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 仅当 android_layout_width = wrap_content 或未指定时生效，若测出来的size大于你所指定的size (譬如这里是180dp)，则使用所指定的size
        int width = resolveSize(DimensionUtil.dp2px(180), widthMeasureSpec);
        int height = thumb_radius_on_dragging * 2; // 控件高度按拖动时的滑块直径
        setMeasuredDimension(width, height); // 强制指定控件大小

        xLeft = getPaddingLeft() + thumb_radius_on_dragging; // 实际的绘图区域按距离父布局左边 padding 算起
        xRight = getMeasuredWidth() - getPaddingRight() - thumb_radius_on_dragging; // 到距离父布局右边的的 padding 结束
        yCenter = getPaddingTop() + thumb_radius_on_dragging; // 确定绘制进度条Y轴意义上的中点
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int currentProgress = (int) (progress_default / progress_max * (xRight-xLeft));
        drawRightTrack(canvas, currentProgress);
        drawLeftTrack(canvas, currentProgress);
        drawThumb(canvas, currentProgress);
    }

    /**
     * 绘制进度条左边
     */
    private void drawLeftTrack(Canvas canvas, int currentProgress){
        paint.setColor(track_left_color);
        paint.setStrokeWidth(track_left_height);
        canvas.drawLine(xLeft, yCenter, xLeft + currentProgress, yCenter, paint);
    }

    /**
     * 绘制拖动滑块
     */
    private void drawThumb(Canvas canvas, int currentProgress){
        paint.setColor(thumb_color_default);
        canvas.drawCircle(xLeft + currentProgress, yCenter, thumb_radius, paint);
    }

    /**
     * 绘制进度条右边
     */
    private void drawRightTrack(Canvas canvas, int currentProgress){
        paint.setColor(track_right_color);
        paint.setStrokeWidth(track_right_height);
        canvas.drawLine(xLeft + currentProgress, yCenter, xRight, yCenter, paint);
    }

    public void startAnim(){
        final ValueAnimator animator = ValueAnimator.ofInt(0,100);
        animator.setDuration(100*500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress_default = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                performClick(); // 若 SeekBar设置了 OnClickListener，可以在此处唤醒监听器
                getParent().requestDisallowInterceptTouchEvent(true); // 不允许父组件拦截触摸事件
                thumb_radius = thumb_radius_on_dragging;
                progress_default = calculateDraggingX(event.getX());
                if(onProgressChanged!=null)
                    onProgressChanged.onProgress((int)progress_default);
//                LogUtils.e(event.getActionMasked()+"progress_default="+progress_default);
                break;
            case MotionEvent.ACTION_UP:

//                LogUtils.e(event.getActionMasked()+"progress_default=+="+progress_default);

                break;
            default:
                thumb_radius = thumb_radius_default;
        }
//        LogUtils.e("progress_default=="+progress_default);
        invalidate();
        return true;
    }

    /**
     * 计算拖动值
     *
     * @param x 屏幕上的event.getX()
     * @return 经转换后对应拖动条的进度值
     */
    private float calculateDraggingX(float x){
        if(x < xLeft){
            return progress_min;
        }
        if(x > xRight){
            return progress_max;
        }
        return x / getMeasuredWidth() * progress_max;
    }
    public interface  onProgressChanged{
        void onProgress(int Progress);
        void onStop(int Progress);
    }
}
