package in.co.gorest.grblcontroller.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class UsualTools {
    private static long lastClick;
    public static boolean isShowPrintMsg = false;
    private static Toast toast;

    /**
     * 判断手机号码是否规则
     *
     * @param input 输入的电话号码
     * @return
     */
    public static boolean isPhoneNumber(String input) {
        String regex = "(1[0-9][0-9]|15[0-9]|18[0-9])\\d{8}";
        Pattern p = Pattern.compile(regex);
        return p.matches(regex, input);
    }

    /**
     * 判断两次输入的密码是否一致
     *
     * @param password
     * @param checkPassword
     * @return
     */
    public static boolean checkPassword(String password, String checkPassword) {
        return password.equals(checkPassword);
    }

    /**
     * 设置斜体字
     *
     * @param word 字符串
     * @return SpannableString
     */
    public static SpannableString setSpanWord(String word) {
        SpannableString sp = new SpannableString(word);
        // 设置斜体
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0,
                word.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return sp;
    }

    /**
     * 设置字体颜色
     *
     * @param word       字符串
     * @param changePart 要改变的颜色部分
     * @param color      要改变的颜色编码
     * @return SpannableStringBuilder
     */
    public static SpannableStringBuilder setStringColor(String word,
                                                        String changePart, String color) {
        char[] c = changePart.toCharArray();
        SpannableStringBuilder style = new SpannableStringBuilder(word);
        if (c.length != 0) {
            int lastIndex = c.length - 1;
            int start = word.indexOf(c[0]);
            int end = start + lastIndex + 1;
            style.setSpan(new ForegroundColorSpan(Color.parseColor(color)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return style;
        } else {
            return style;
        }

    }

    /**
     * @param word       字符串
     * @param changePart 要改变的颜色部分
     * @param colorId    要改变的颜色id
     * @return
     */
    public static SpannableStringBuilder setStringColor(String word,
                                                        String changePart, int colorId) {
        char[] c = changePart.toCharArray();
        SpannableStringBuilder style = new SpannableStringBuilder(word);
        if (c.length != 0) {
            int lastIndex = c.length - 1;
            int start = word.indexOf(c[0]);
            int end = start + lastIndex + 1;
            style.setSpan(new ForegroundColorSpan(colorId), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return style;
        } else {
            return style;
        }

    }

    /**
     * 设置两种字体的颜色
     *
     * @param word        字符串
     * @param changePart  要改变的颜色部分1
     * @param color       要设置的颜色1
     * @param changePart2 要改变的颜色部分2
     * @param color2      要设置的颜色2
     * @return SpannableStringBuilder
     */
    public static SpannableStringBuilder setTwoStringColor(String word,
                                                           String changePart, String color, String changePart2, String color2) {
        char[] c = changePart.toCharArray();
        char[] c2 = changePart2.toCharArray();
        SpannableStringBuilder style = new SpannableStringBuilder(word);
        if (c.length != 0) {
            int lastIndex = c.length - 1;
            int start = word.indexOf(c[0]);
            int end = start + lastIndex + 1;
            style.setSpan(new ForegroundColorSpan(Color.parseColor(color)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            int lastIndex2 = c2.length - 1;
            int start2 = word.indexOf(c2[0]);
            int end2 = start2 + lastIndex2 + 1;
            style.setSpan(new ForegroundColorSpan(Color.parseColor(color2)),
                    start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return style;
        } else {
            return style;
        }

    }

    /**
     * 改变字体大小
     *
     * @param word       要改变的字体
     * @param changePart 需要改变的部分
     * @param size       大小
     * @return
     */
    public static SpannableStringBuilder setStringFontSize(String word,
                                                           String changePart, int size) {
        char[] c = changePart.toCharArray();
        SpannableStringBuilder style = new SpannableStringBuilder(word);
        if (c.length != 0) {
            int lastIndex = c.length - 1;
            int start = word.indexOf(c[0]);
            int end = start + lastIndex + 1;
            // style 为0 即是正常的，还有Typeface.BOLD(粗体) Typeface.ITALIC(斜体)等
            style.setSpan(new TextAppearanceSpan(null, 0, size, null, null),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return style;
        } else {
            return style;
        }

    }

    /**
     * 显示网络连接异常的toast
     *
     * @param context
     */
    public static void showNetErrorToast(Context context) {
        if (context != null) {
            Toast.makeText(context, "网络连接异常", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示数据解析出错的toast
     *
     * @param context
     */
    public static void showDataErrorToast(Context context) {
        Toast.makeText(context, "数据解析出错", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示没有数据的toast
     *
     * @param context
     */
    public static void showNoDataErrorToast(Context context) {
        Toast.makeText(context, "没有数据", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示最后一页的toast
     *
     * @param context
     */
    public static void showLastPageToast(Context context) {
        Toast.makeText(context, "没有更多数据了", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示最后一页的toast
     *
     * @param context
     */
    public static void showNoFunctionToast(Context context) {
        Toast.makeText(context, "该功能暂未开通，敬请期待", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示短时间的toast
     *
     * @param context
     * @param text
     */
    public static void showShortToast(Context context, String text) {
        if (toast!=null){
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }else {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * 防止多次点击
     */
    public static void stopTooMuchClick() {
        // 大于一秒方个通过
        if (System.currentTimeMillis() - lastClick <= 1000) {
            return;
        }
        lastClick = System.currentTimeMillis();

    }

    /**
     * 判断字符串是否只有数字
     *
     * @param str 字符串
     * @return 是则返回true，否则返回false
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 把时间戳转换为指定格式的字符串
     *
     * @param time       时间戳
     * @param dateFormat 转换格式
     * @return 转换后的字符串
     */
    public static String TimestampToDate(String time, String dateFormat) {
        if (isNumeric(time) == true && !time.equals("")) {
            int length = time.length();
            if (length == 13) {
                time = time.substring(0, 10);
            }
            long timeI = Integer.parseInt(time);
            long temp = timeI * 1000;
            Timestamp ts = new Timestamp(temp);
            Date date = new Date();
            try {
                date = ts;
            } catch (Exception e) {
                e.printStackTrace();
            }
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            String dateString = formatter.format(date);
            return dateString;
        } else {
            return time;
        }

    }

    /**
     * 把指定格式的时间转换为时间戳
     *
     * @param time       时间
     * @param dateFormat 时间格式
     * @return
     */
    public static long dataToTime(String time, String dateFormat) {
        SimpleDateFormat sdr = new SimpleDateFormat(dateFormat, Locale.CHINA);
        try {
            Date date = sdr.parse(time);
            long l = date.getTime();
            return l;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * 把字符串"null"转为""
     *
     * @param nullString
     * @return 如果字符串为"null"，则返回"",否则，返回原字符串
     */
    public static String changeNullToEmpty(String nullString) {
        return nullString.equals("null") ? "" : nullString;
    }

    /**
     * 把字符串"null"转为"0"
     *
     * @param nullString
     * @return 如果字符串为"null"，则返回"0",否则，返回原字符串
     */
    public static String changeNullToZero(String nullString) {
        return nullString.equals("null") ? "0" : nullString;
    }

    /**
     * 拨打号码
     *
     * @param context
     * @param phone
     */
    public static void telNumber(Context context, String phone) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.CALL");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("tel:" + phone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 展示软键盘
     *
     * @param edit
     * @param context
     */
    public static void showSoftKeyboard(EditText edit, Context context) {
        final InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            imm.hideSoftInputFromWindow(edit.getApplicationWindowToken(), 0);
        }

    }

    /**
     * 获取当前活动界面的名称
     *
     * @param context
     * @return 界面名称
     */
    public static String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    /**
     * 界面跳转
     *
     * @param context
     * @param toClass
     */
    public static void jumpActivity(Context context, Class<?> toClass) {
        Intent intent = new Intent(context, toClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 界面跳转
     *
     * @param context
     * @param toClass
     * @param bundle
     */
    public static void jumpActivity(Context context, Class<?> toClass,
                                    Bundle bundle) {
        Intent intent = new Intent(context, toClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    /**
     * 带返回结果的界面跳转
     *
     * @param context
     * @param toClass
     * @param requestCode
     */
    public static void jumpActivityForResult(Context context, Class<?> toClass,
                                             int requestCode) {
        Intent intent = new Intent(context, toClass);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 带返回结果的界面跳转
     *
     * @param context
     * @param toClass
     * @param bundle
     * @param requestCode
     */
    public static void jumpActivityForResult(Context context, Class<?> toClass,
                                             Bundle bundle, int requestCode) {
        Intent intent = new Intent(context, toClass);
        intent.putExtra("bundle", bundle);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 退出登录
     *
     * @param context
     * @param toClass
     */
    public static void loginOut(Context context, Class<?> toClass, Bundle bundle) {
        Intent intent = new Intent(context, toClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (bundle != null) {
            intent.putExtra("bundle", bundle);
        }
        context.startActivity(intent);
    }

    /**
     * 获取bundle
     *
     * @param activity
     * @return
     */
    public static Bundle getIntentBundle(Activity activity) {
        Bundle bundle = activity.getIntent().getBundleExtra("bundle");
        return bundle;
    }

    /**
     * 获取组件高度
     *
     * @param view
     * @return 高度
     */
    public static int getViewMeasureHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int height = view.getMeasuredHeight();
        return height;
    }

    /**
     * 获取组件宽度
     *
     * @param view
     * @return 宽度
     */
    public static int getViewMeasureWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int width = view.getMeasuredWidth();
        return width;
    }

    /**
     * 获取组件高度
     *
     * @param view
     * @return 高度
     */
    public static int getViewHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int height = view.getHeight();
        return height;
    }

    /**
     * 获取组件宽度
     *
     * @param view
     * @return 宽度
     */
    public static int getViewWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int width = view.getWidth();
        return width;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param context
     * @param pxValue
     * @return dp值
     */
    public static int pxToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return 宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return 高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int height = display.getHeight();
        return height;
    }

    /**
     * 获取app版本名称
     *
     * @param context
     * @return 版本号
     */
    public static String getAppVerson(Context context) {
        PackageManager manager;
        PackageInfo info = null;
        manager = context.getPackageManager();
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取app版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersonCode(Context context) {
        PackageManager manager;
        PackageInfo info = null;
        manager = context.getPackageManager();
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 隐藏键盘
     *
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View localView = activity.getCurrentFocus();
            if (localView != null && localView.getWindowToken() != null) {
                IBinder windowToken = localView.getWindowToken();
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
            }
        }
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    /**
     * 保留小数点位数
     *
     * @param number     数字
     * @param stayNumber 要保留的点数
     * @return 字符串
     */
    public static String stayPoint(float number, int stayNumber) {
        // DecimalFormat decimalFormat = new DecimalFormat("0.0");//
        // 构造方法的字符格式这里如果小数不足1位,会以0补足.
        // String p = decimalFormat.format(number);// format 返回的是字符串
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        ddf1.setMaximumFractionDigits(stayNumber);
        String s = ddf1.format(number);
        return s;
    }

    /**
     * 发送短信
     *
     * @param context
     * @param phone   手机号码
     */
    public static void sendMessage(Context context, String phone) {
        // 调用Android系统API发送短信
        Uri uri = Uri.parse("smsto:" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", "");
        context.startActivity(intent);
    }

    /**
     * 获取随机整数
     *
     * @param maxValue 最大值（不包含该值）
     * @param minValue 最小值（不包含该值）
     * @return
     */
    public static int getRandomNumber(int maxValue, int minValue) {
        // (数据类型)(最小值+Math.random()*(最大值-最小值+1))
        int random = (int) (minValue + Math.random()
                * (maxValue - minValue + 1));
        return random;
    }

    public static void showPrintMsg(String msg) {
        if (isShowPrintMsg) {
            if (msg.length() > 3000) {
                String show = msg.substring(0, 3000);
                Log.d("showPrintMsg", show);
                if ((msg.length() - 3000) > 3000) {// 剩下的文本还是大于规定长度
                    String partLog = msg.substring(3000, msg.length());
                    showPrintMsg(partLog);
                } else {
                    String surplusLog = msg.substring(3000, msg.length());
                    Log.d("showPrintMsg", surplusLog + "");
                }
            } else {
                Log.d("showPrintMsg", msg);
            }
        }
    }

    /**
     * 通过蔡勒公式算出某一天是星期几
     *
     * @param year        年
     * @param monthOfYear 月
     * @param dayOfMonth  日
     * @return 星期几
     */
    public static String getWeekday(int year, int monthOfYear, int dayOfMonth) {
        int c = Integer.parseInt((year + "").substring(0, 2));
        int y = Integer.parseInt((year + "").substring(2, 4));
        int m = monthOfYear;
        int d = dayOfMonth;
        if (m == 1) {
            m = 13;
            y--;
        }
        if (m == 2) {
            m = 14;
            y--;
        }
        int w = y + (y / 4) + (c / 4) - 2 * c + (26 * (m + 1) / 10) + d - 1;
        if (w >= 0) {
            w = w % 7;
        } else {
            w = (w % 7 + 7) % 7;
        }
        String myWeek = null;
        switch (w) {
            case 0:
                myWeek = "日";
                break;
            case 1:
                myWeek = "一";
                break;
            case 2:
                myWeek = "二";
                break;
            case 3:
                myWeek = "三";
                break;
            case 4:
                myWeek = "四";
                break;
            case 5:
                myWeek = "五";
                break;
            case 6:
                myWeek = "六";
                break;
            default:

                break;
        }
        return myWeek;

    }

    public static String getStackTraceInfo(Exception e) {

        StringWriter sw = null;
        PrintWriter pw = null;

        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);//将出错的栈信息输出到printWriter中
            pw.flush();
            sw.flush();

            return sw.toString();
        } catch (Exception ex) {

            return "发生错误";
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }

    }
}

