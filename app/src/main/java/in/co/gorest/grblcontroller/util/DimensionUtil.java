package in.co.gorest.grblcontroller.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * 作者: liuhuaqian on 2020-12-18.
 */
public class DimensionUtil {
    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    public static int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

}
