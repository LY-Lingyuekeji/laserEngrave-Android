package in.co.gorest.grblcontroller.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import cn.wandersnail.commons.util.ShellUtils;
import in.co.gorest.grblcontroller.model.LaserInfo;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



/* loaded from: classes2.dex */
public class ToolUtil {

    public static LaserInfo getLaserInfoFromString(String str) {
        LaserInfo laserInfo = new LaserInfo();
        for (String str2 : str.split(ShellUtils.COMMAND_LINE_END)) {
            if (str2.contains("<") && str2.contains(">")) {
                String[] split = str2.split("\\|");
                for (int i = 0; i < split.length; i++) {
                    String str3 = split[i];
                    if (i == 0) {
                        if (str3.toLowerCase().contains("idle") || str3.toLowerCase().contains("jog")) {
                            laserInfo.setStatus("IDLE");
                        } else if (str3.toLowerCase().contains("run")) {
                            laserInfo.setStatus("PRINTING");
                        } else if (str3.toLowerCase().contains("hold")) {
                            laserInfo.setStatus("PAUSE");
                        }
                    }
                    if (str3.contains("MPos:") && str3.contains(",")) {
                        String[] split2 = str3.split(",");
                        laserInfo.setxPos(split2[0].split(":")[1]);
                        laserInfo.setyPos(split2[1]);
                    }
                    if (str3.contains("FS:") && str3.contains(",")) {
                        String[] split3 = str3.split(",");
                        laserInfo.setSpeed(split3[0].split(":")[1]);
                        laserInfo.setPower(split3[1]);
                    }
                    if (str3.contains("Ov:")) {
                        String[] split4 = str3.split(",");
                        if (split4.length > 2) {
                            laserInfo.setPrintingPower(split4[2].replace(">", "").replace("\r", "").replace(ShellUtils.COMMAND_LINE_END, ""));
                            laserInfo.setPrintingSpeed(split4[0].substring(3));
                        }
                    }
                    if (str3.contains("SD:") && str3.contains(",")) {
                        String[] split5 = str3.split(",");
                        laserInfo.setProgress(split5[0].split(":")[1]);
                        String[] split6 = split5[1].split("/");
                        laserInfo.setPrintName(split6[split6.length - 1].replace(">", "").replace("\r", "").replace(ShellUtils.COMMAND_LINE_END, ""));
                    }
                }
            }
        }
        if (laserInfo.getStatus() == null) {
            laserInfo.setStatus("");
        }
        return laserInfo;
    }

    /* JADX WARN: Not initialized variable reg: 1, insn: 0x0057: MOVE (r0 I:??[OBJECT, ARRAY]) = (r1 I:??[OBJECT, ARRAY]), block:B:44:0x0057 */


    public static File createImageFile(Context context) {
        try {
            return File.createTempFile("JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveNCFile(Context context, StringBuffer stringBuffer, String str) {
        if (stringBuffer == null) {
            return null;
        }
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), str + ".nc");
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(stringBuffer.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            return "content://makerbase.com.mkslaser.fileprovider/root" + file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int i, int i2) {
        if (bitmap == null) {
            return null;
        }
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(i / width, i2 / height);
        matrix.postScale(1.0f, -1.0f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        Log.i("-----", "scaleBitmap:new--- " + i + "---" + i2);
        Log.i("-----", "scaleBitmap: " + height + "---" + width);
        return createBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, float f) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(f, f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        if (createBitmap.equals(bitmap)) {
            return createBitmap;
        }
        bitmap.recycle();
        return createBitmap;
    }

    public static Bitmap cropBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width < height) {
            height = width;
        }
        int i = height / 2;
        return Bitmap.createBitmap(bitmap, width / 3, 0, i, (int) (i / 1.2d), (Matrix) null, false);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, float f) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        if (createBitmap.equals(bitmap)) {
            return createBitmap;
        }
        bitmap.recycle();
        return createBitmap;
    }

    public static Bitmap skewBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postSkew(-0.6f, -0.3f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        if (createBitmap.equals(bitmap)) {
            return createBitmap;
        }
        bitmap.recycle();
        return createBitmap;
    }

    public static Bitmap generateBitmap(String str, int i, int i2) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(i);
        textPaint.setColor(i2);
        int ceil = (int) Math.ceil(textPaint.measureText(str));
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        Bitmap createBitmap = Bitmap.createBitmap(ceil, (int) Math.ceil(Math.abs(fontMetrics.bottom) + Math.abs(fontMetrics.top)), Bitmap.Config.ARGB_8888);
        new Canvas(createBitmap).drawText(str, 0.0f, Math.abs(fontMetrics.ascent), textPaint);
        return createBitmap;
    }


    /* loaded from: classes2.dex */
    public class JsonKey {
        public static final String JSON_DATAS = "info";
        public static final String JSON_MSG = "msg";
        public static final String JSON_SUCCESS_KEY = "success";

        public JsonKey() {
        }
    }

    public static Locale getLan(String str, SharedPreferences.Editor editor) {
        Log.e("getLan---", "---" + str + "---" + Locale.getDefault().toString());
        String locale = Locale.getDefault().toString();
        Locale locale2 = Locale.SIMPLIFIED_CHINESE;
        if (str.contains("zh")) {
            editor.putString("LANGUAGE", "zh");
            return Locale.SIMPLIFIED_CHINESE;
        }
        if (str.contains("en")) {
            editor.putString("LANGUAGE", "en");
            return Locale.ENGLISH;
        }
        if (str.contains("de")) {
            editor.putString("LANGUAGE", "de");
            return Locale.GERMANY;
        }
        if (str.contains("ru")) {
            editor.putString("LANGUAGE", "ru");
            return new Locale("RU", "ru", "");
        }
        if (str.contains("jp")) {
            editor.putString("LANGUAGE", "jp");
            return Locale.JAPANESE;
        }
        if (str.contains("ko")) {
            editor.putString("LANGUAGE", "ko");
            return Locale.KOREA;
        }
        if (locale.contains("zh")) {
            Locale locale3 = Locale.SIMPLIFIED_CHINESE;
            editor.putString("LANGUAGE", "auto");
            return locale3;
        }
        if (locale.contains("en")) {
            editor.putString("LANGUAGE", "auto");
            return Locale.ENGLISH;
        }
        if (locale.contains("de")) {
            editor.putString("LANGUAGE", "auto");
            return Locale.GERMANY;
        }
        if (locale.contains("ru")) {
            editor.putString("LANGUAGE", "auto");
            return new Locale("RU", "ru", "");
        }
        if (locale.contains("ko")) {
            editor.putString("LANGUAGE", "auto");
            return Locale.KOREA;
        }
        if (locale.contains("jp") || locale.contains("ja")) {
            editor.putString("LANGUAGE", "auto");
            return Locale.JAPANESE;
        }
        Locale locale4 = Locale.ENGLISH;
        editor.putString("LANGUAGE", "auto");
        return locale4;
    }

    public static byte[] HexToByte(String str) {
        int length = str.length();
        byte[] bArr = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }
}
