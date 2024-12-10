package in.co.gorest.grblcontroller.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageUtils {

    public static Bitmap adjustContrast(Bitmap src, float contrast) {
        // 创建一个新的Bitmap
        Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        // 创建一个画笔
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();


        // 设置对比度
        cm.postConcat(new ColorMatrix(new float[]{
                contrast, 0, 0, 0, 128 * (1 - contrast),
                0, contrast, 0, 0, 128 * (1 - contrast),
                0, 0, contrast, 0, 128 * (1 - contrast),
                0, 0, 0, 1, 0
        }));

        // 应用颜色矩阵
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, 0, 0, paint);

        return dst;
    }

    public static Bitmap adjustBrightness(Bitmap src, float brightness) {
        // 创建一个新的Bitmap
        Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        // 创建一个画笔
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        // 设置亮度
        cm.set(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });

        // 应用颜色矩阵
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, 0, 0, paint);

        return dst;
    }
}
