package in.co.gorest.grblcontroller.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageProcessingUtils {

    // 1. 将Bitmap转换为灰度图
    public static Bitmap toGrayscale(Bitmap bitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0); // 将饱和度设置为0实现灰度效果

        return applyColorMatrix(bitmap, colorMatrix);
    }

    // 2. 将Bitmap转换为黑白图
    public static Bitmap toBlackAndWhite(Bitmap bitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0); // 先转换为灰度
        ColorMatrix contrastMatrix = new ColorMatrix(new float[] {
                255, 0, 0, 0, -128,
                0, 255, 0, 0, -128,
                0, 0, 255, 0, -128,
                0, 0, 0, 1, 0
        });

        colorMatrix.postConcat(contrastMatrix); // 通过设置对比度调整为黑白效果

        return applyColorMatrix(bitmap, colorMatrix);
    }

    // 3. 轮廓检测 (Sobel 算子实现)
    public static Bitmap detectEdges(Bitmap bitmap) {
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        for (int x = 1; x < bitmap.getWidth() - 1; x++) {
            for (int y = 1; y < bitmap.getHeight() - 1; y++) {
                int pixel = bitmap.getPixel(x, y);
                int leftPixel = bitmap.getPixel(x - 1, y);
                int rightPixel = bitmap.getPixel(x + 1, y);
                int topPixel = bitmap.getPixel(x, y - 1);
                int bottomPixel = bitmap.getPixel(x, y + 1);

                int edge = Math.abs(Color.red(leftPixel) - Color.red(rightPixel)) +
                        Math.abs(Color.red(topPixel) - Color.red(bottomPixel));

                if (edge > 128) {
                    resultBitmap.setPixel(x, y, Color.BLACK);
                } else {
                    resultBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }

        return resultBitmap;
    }

    // 4. 素描效果
    public static Bitmap sketchEffect(Bitmap bitmap) {
        Bitmap grayscale = toGrayscale(bitmap);
        Bitmap inverted = invertColors(grayscale);
        Bitmap blurred = applyGaussianBlur(inverted);

        return colorDodgeBlend(blurred, grayscale);
    }

    // 5. 调整对比度
    public static Bitmap adjustContrast(Bitmap bitmap, float contrast) {
        float scale = contrast + 1.f;
        float translate = (-0.5f * scale + 0.5f) * 255.f;

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });

        return applyColorMatrix(bitmap, colorMatrix);
    }

    // 6. 调整亮度
    public static Bitmap adjustBrightness(Bitmap bitmap, float brightness) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[] {
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });

        return applyColorMatrix(bitmap, colorMatrix);
    }

    // 7. 调整锐化 (使用卷积核实现锐化)
    public static Bitmap sharpen(Bitmap bitmap) {
        // 简单的锐化卷积核
        float[] sharpenMatrix = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };

        return applyConvolution(bitmap, sharpenMatrix);
    }

    // 8. 调整高光
    public static Bitmap adjustHighlights(Bitmap bitmap, float highlight) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[] {
                highlight, 0, 0, 0, 0,
                0, highlight, 0, 0, 0,
                0, 0, highlight, 0, 0,
                0, 0, 0, 1, 0
        });

        return applyColorMatrix(bitmap, colorMatrix);
    }

    // 辅助方法：应用ColorMatrix
    private static Bitmap applyColorMatrix(Bitmap bitmap, ColorMatrix colorMatrix) {
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return resultBitmap;
    }

    // 辅助方法：颜色反转（用于素描效果）
    private static Bitmap invertColors(Bitmap bitmap) {
        Bitmap invertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int pixel = bitmap.getPixel(x, y);
                int r = 255 - Color.red(pixel);
                int g = 255 - Color.green(pixel);
                int b = 255 - Color.blue(pixel);
                invertedBitmap.setPixel(x, y, Color.rgb(r, g, b));
            }
        }

        return invertedBitmap;
    }

    // 辅助方法：应用高斯模糊
    private static Bitmap applyGaussianBlur(Bitmap bitmap) {
        // 高斯模糊实现（可以用 RenderScript 或 OpenCV 等库来更高效处理）
        // 这里只是作为占位符简单说明
        return bitmap; // 假设已经模糊处理
    }

    // 辅助方法：颜色减淡混合，用于素描效果
    private static Bitmap colorDodgeBlend(Bitmap blurred, Bitmap original) {
        Bitmap resultBitmap = Bitmap.createBitmap(blurred.getWidth(), blurred.getHeight(), blurred.getConfig());

        for (int x = 0; x < blurred.getWidth(); x++) {
            for (int y = 0; y < blurred.getHeight(); y++) {
                int origPixel = original.getPixel(x, y);
                int blurPixel = blurred.getPixel(x, y);

                int r = colorDodge(Color.red(origPixel), Color.red(blurPixel));
                int g = colorDodge(Color.green(origPixel), Color.green(blurPixel));
                int b = colorDodge(Color.blue(origPixel), Color.blue(blurPixel));

                resultBitmap.setPixel(x, y, Color.rgb(r, g, b));
            }
        }

        return resultBitmap;
    }

    // 颜色减淡算法
    private static int colorDodge(int base, int blend) {
        if (blend == 255) return blend;
        return Math.min(255, ((base << 8) / (255 - blend)));
    }

    // 应用卷积操作（用于锐化）
    private static Bitmap applyConvolution(Bitmap bitmap, float[] matrix) {
        // 卷积操作，处理图像的锐化（可以使用ScriptIntrinsicConvolve3x3）

        // 卷积操作的具体实现可以根据需要来处理

        return bitmap; // 假设已经应用卷积
    }
}

