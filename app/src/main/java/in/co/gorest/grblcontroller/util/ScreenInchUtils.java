package in.co.gorest.grblcontroller.util;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import in.co.gorest.grblcontroller.GrblController;

public class ScreenInchUtils {
    private static double mInch = 0;

    public static double getScreenInch(Activity activity) {
        if (mInch != 0.0d) {
            return mInch;
        }

        try {
            int realWidth = 0, realHeight = 0;
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            if (Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                display.getRealSize(size);
                realWidth = size.x;
                realHeight = size.y;
            } else if (Build.VERSION.SDK_INT < 17
                    && Build.VERSION.SDK_INT >= 14) {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } else {
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }

            mInch = formatDouble(Math.sqrt((realWidth / metrics.xdpi) * (realWidth / metrics.xdpi) + (realHeight / metrics.ydpi) * (realHeight / metrics.ydpi)), 1);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return mInch;
    }


    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    public static double formatDouble(double d, int newScale) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 获取DPI，图像每英寸长度内的像素点数
     * DPI = 宽 / ((尺寸2 × 宽2) / (宽2 + 高2))1/2 = 长 / ((尺寸2 × 高2) / (宽2 + 高2))1/2
     *
     * @return
     */
    public static float getDPI(Activity activity) {
        //获取屏幕尺寸
        double screenSize = ScreenInchUtils.getScreenInch(activity);
        //获取宽高大小
        int widthPx = GrblController.getInstance().getResources().getDisplayMetrics().widthPixels;
        int heightPx = GrblController.getInstance().getResources().getDisplayMetrics().heightPixels;
        float dpi = (float) (widthPx / Math.sqrt((screenSize * screenSize * widthPx * widthPx) / (widthPx * widthPx + heightPx * heightPx)));
        return dpi;
    }

    //px转毫米
    public static int pxWidthToMm(Activity activity,int value) {

        float inch = value / getDPI(activity);
        int c_value = (int) (inch * 25.4f);
        return c_value;
    }

    //px转毫米
    public static int pxHeightToMm(Activity activity,int value) {

        float inch = value / getDPI(activity);
        int c_value = (int) (inch * 25.4f);
        return c_value;
    }

    //毫米转px
    public static int mmToPx(Activity activity,int value) {
        float inch = value / 25.4f;
        int c_value = (int)(inch * getDPI(activity));
        return c_value;
    }

}
