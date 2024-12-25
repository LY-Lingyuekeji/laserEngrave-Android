package in.co.gorest.grblcontroller.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageProcess {

    private static String TAG = "ImageProcess";
    /**
     * 将彩色图转换为黑白图
     */
    protected static Bitmap convert2GreyImg(Bitmap img, int threshold) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);

                if ((threshold != -1) && (threshold >=0) && (threshold <= 255))
                {
                    if (grey < threshold) {
                        grey = 0;
                    } else {
                        grey = 255;
                    }
                }

                if (threshold == -1)
                {
                    if (grey > 235)
                    {
                        grey = 255;
                    }
                }

                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 将彩色图转换为灰度图
     */
    public static Bitmap convert2GreyImg(Bitmap img) {
        return convert2GreyImg(img, -1);
    }

    /**
     * 将彩色图转换为灰度图，这个函数需要捕获OOM的异常
     */
    public static Bitmap convertToGreyImage(Bitmap image, float printWidth, float printHeight, float resol) {
        if (image == null) {
            return null;
        }
        Log.e(TAG, "resol:" + resol);
        // 先调整大小
        Bitmap adjustBitmap = imageResize(image, (int) (printWidth / resol), (int) (printHeight / resol));
        if (adjustBitmap == null) {
            return null;
        }
        // 转换成灰度图或者黑白图
        adjustBitmap = convert2GreyImg(adjustBitmap);
        if (adjustBitmap == null) {
            return null;
        }

        return adjustBitmap;
    }


    /**
     * 将图片进行缩放
     */
    public static Bitmap imageResize(Bitmap bm, int newWidth, int newHeight) {
        return imageResize(bm, newWidth, newHeight, 1);
    }

    /**
     * 将图片进行缩放
     */
    public static Bitmap imageResize(Bitmap bm, int printWidth, int printHeight, float resol) {
        Log.e(TAG, "printWidth=" + printWidth + "==" + printHeight+"=="+resol);
        Bitmap newbm = null;
        int newWidth = (int) (printWidth / resol);
        int newHeight = (int) (printHeight / resol);
        Log.e(TAG, "newWidth=" + newWidth + "==" + newHeight);

        if (newWidth >= 2500 || newHeight >= 2500) {
            Log.e(TAG, "图片尺寸过大，降低质量");
            return imageResize(bm, printWidth, printHeight, 1f / ((1 / resol) - 1));
        }

        if (bm != null) {
            // 获得图片的宽高
            int width = bm.getWidth();
            int height = bm.getHeight();

            // 计算缩放比例
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            // 得到新的图片
            newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        }
        return newbm;
    }

    /**
     * 将彩色图转换为黑白图，这个函数需要捕获OOM的异常
     */
    public static Bitmap convertToBlackWhiteImage(Bitmap image, float printWidth, float printHeight, float resol, int thresold) {
        if (image == null) {
            return null;
        }
        Log.d(TAG, String.format("origin image, width : %d, height : %d", image.getWidth(), image.getHeight()));

        // 先调整大小
        Bitmap adjustBitmap = imageResize(image, (int) (printWidth / resol), (int) (printHeight / resol));
        if (adjustBitmap == null) {
            return null;
        }
        Log.d(TAG, String.format("adjust image, width : %d, height : %d", adjustBitmap.getWidth(), adjustBitmap.getHeight()));

        // 转换成灰度图或者黑白图
        adjustBitmap = convert2GreyImg(adjustBitmap, thresold);
        if (adjustBitmap == null) {
            return null;
        }

        return adjustBitmap;
    }

    public static Bitmap convertToOutlineImage(Bitmap image) {

        // 通过potrace进行轮廓提取
        PotraceJ.turdsize = 2;
        PotraceJ.alphamax = 0.0;
        PotraceJ.opttolerance = 0.2;
        PotraceJ.curveoptimizing = true;

        ArrayList<ArrayList<PotraceJ.Curve>> plist = PotraceJ.PotraceTrace(image);

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        Arrays.fill(pixels, -1);
        Bitmap outlineImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        outlineImage.setPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        Canvas canvas = new Canvas(outlineImage);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        //线条结束处绘制一个半圆
        paint.setStrokeCap(Paint.Cap.ROUND);

        Path path = new Path();

        for (ArrayList<PotraceJ.Curve> list : plist) {
            for (int i = 0; i < list.size(); i++) {
                PotraceJ.Curve curve = list.get(i);

                if (curve.Kind == PotraceJ.CurveKind.Line) {
                    path.reset();
                    path.moveTo((float) curve.A.X, (float) curve.A.Y);
                    path.quadTo((float) curve.A.X, (float) curve.A.Y, (float) curve.B.X, (float) curve.B.Y);
                    canvas.drawPath(path, paint);
                } else {
                    path.reset();
                    path.moveTo((float) curve.A.X, (float) curve.A.Y);
                    path.rCubicTo((float) curve.A.X, (float) curve.A.Y, (float) curve.ControlPointA.X, (float) curve.ControlPointA.Y, (float) curve.B.X, (float) curve.B.Y);
                    canvas.drawPath(path, paint);
                }
            }
        }

        return outlineImage;
    }

    public static Bitmap ImageBalance(Bitmap bitmap, int brightness, float contrast, int whiteLimitValue) {
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,// 改变亮度
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0});

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return ImageAdjustWhilteLimit(bmp, whiteLimitValue);
    }

    /**
     * 调整图像的白色限制值
     *
     * @param img
     * @param whiteLimitValue 白色限制值
     * @return
     */
    protected static Bitmap ImageAdjustWhilteLimit(Bitmap img, int whiteLimitValue) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int value = pixels[width * i + j];
                int grey = (value & 0x000000FF);

                if (grey >= (255 - whiteLimitValue)) {
                    grey = 255;
                }

                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        img.setPixels(pixels, 0, width, 0, 0, width, height);

        return img;
    }

    public static Bitmap ImageDithering(Bitmap img, float resol, boolean lean) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                gray[width * i + j] = red;
            }
        }


        int e = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                if (g >= 128) {
                    pixels[width * i + j] = 0xffffffff;
                    e = g - 255;
                } else {
                    pixels[width * i + j] = 0xff000000;
                    e = g - 0;
                }
                if (j < width - 1 && i < height - 1) {
                    //右边像素处理
                    gray[width * i + j + 1] += 3 * e / 8;
                    //下
                    gray[width * (i + 1) + j] += 3 * e / 8;
                    //右下
                    gray[width * (i + 1) + j + 1] += e / 4;
                } else if (j == width - 1 && i < height - 1) {//靠右或靠下边的像素的情况
                    //下方像素处理
                    gray[width * (i + 1) + j] += 3 * e / 8;
                } else if (j < width - 1 && i == height - 1) {
                    //右边像素处理
                    gray[width * (i) + j + 1] += e / 4;
                }
            }
        }

        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return lean ? imageResize(mBitmap, (int) (width / resol), (int) (height / resol)) : mBitmap;
    }

    public static Bitmap convertToOutlineImage(Bitmap image, float resol, boolean lean) {
        // 通过potrace进行轮廓提取
        PotraceJ.turdsize = 2;
        PotraceJ.alphamax = 0.0;
        PotraceJ.opttolerance = 0.2;
        PotraceJ.curveoptimizing = true;

        ArrayList<ArrayList<PotraceJ.Curve>> plist = PotraceJ.PotraceTrace(image);

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        Arrays.fill(pixels, -1);
        Bitmap outlineImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        outlineImage.setPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        Canvas canvas = new Canvas(outlineImage);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        //线条结束处绘制一个半圆
        paint.setStrokeCap(Paint.Cap.ROUND);

        Path path = new Path();

        for (ArrayList<PotraceJ.Curve> list : plist) {
            for (int i = 0; i < list.size(); i++) {
                PotraceJ.Curve curve = list.get(i);

                if (curve.Kind == PotraceJ.CurveKind.Line) {
                    path.reset();
                    path.moveTo((float) curve.A.X, (float) curve.A.Y);
                    path.quadTo((float) curve.A.X, (float) curve.A.Y, (float) curve.B.X, (float) curve.B.Y);
                    canvas.drawPath(path, paint);
                } else {
                    path.reset();
                    path.moveTo((float) curve.A.X, (float) curve.A.Y);
                    path.rCubicTo((float) curve.A.X, (float) curve.A.Y, (float) curve.ControlPointA.X, (float) curve.ControlPointA.Y, (float) curve.B.X, (float) curve.B.Y);
                    canvas.drawPath(path, paint);
                }
            }
        }

        return lean ? imageResize(outlineImage, (int) (resol), (int) (outlineImage.getHeight() * (resol / outlineImage.getWidth()))) : outlineImage;
    }

    public static Bitmap convertToOutlineImage(Bitmap image, float resol, boolean lean, boolean isCenterlineMode) {
        // 如果是中心线模式，提取中心线
        if (isCenterlineMode) {
            // 提取并返回中心线图像
            return extractCenterlineFromOutline(image);
        }

        // 否则，进行常规的轮廓提取
        PotraceJ.turdsize = 2;
        PotraceJ.alphamax = 0.0;
        PotraceJ.opttolerance = 0.2;
        PotraceJ.curveoptimizing = true;

        ArrayList<ArrayList<PotraceJ.Curve>> plist = PotraceJ.PotraceTrace(image);

        // 初始化轮廓图像
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        Arrays.fill(pixels, -1);  // 设置为白色
        Bitmap outlineImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        outlineImage.setPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        // 创建 Canvas 和 Paint 用于绘制轮廓
        Canvas canvas = new Canvas(outlineImage);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);  // 线条结束处绘制一个半圆

        Path path = new Path();

        // 根据potrace提取的曲线数据绘制轮廓
        for (ArrayList<PotraceJ.Curve> list : plist) {
            for (int i = 0; i < list.size(); i++) {
                PotraceJ.Curve curve = list.get(i);

                if (curve.Kind == PotraceJ.CurveKind.Line) {
                    path.reset();
                    path.moveTo((float) curve.A.X, (float) curve.A.Y);
                    path.quadTo((float) curve.A.X, (float) curve.A.Y, (float) curve.B.X, (float) curve.B.Y);
                    canvas.drawPath(path, paint);
                } else {
                    path.reset();
                    path.moveTo((float) curve.A.X, (float) curve.A.Y);
                    path.rCubicTo((float) curve.A.X, (float) curve.A.Y, (float) curve.ControlPointA.X, (float) curve.ControlPointA.Y, (float) curve.B.X, (float) curve.B.Y);
                    canvas.drawPath(path, paint);
                }
            }
        }

        // 如果是需要缩放的模式，进行缩放处理
        return lean ? imageResize(outlineImage, (int) (resol), (int) (outlineImage.getHeight() * (resol / outlineImage.getWidth()))) : outlineImage;
    }

    // 提取中心线的函数，将轮廓图转为中心线图像
    private static Bitmap extractCenterlineFromOutline(Bitmap image) {
        // 将 Bitmap 转换为 Mat 以便进行 OpenCV 处理
        Mat mat = new Mat();
        Utils.bitmapToMat(image, mat);

        // 转换为灰度图像
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

        // 创建二值化图像，确保背景是白色，轮廓是黑色
        Mat binaryMat = new Mat();
        Imgproc.threshold(mat, binaryMat, 128, 255, Imgproc.THRESH_BINARY_INV);  // 反转图像，背景白色，轮廓黑色

        // 执行骨架化操作来提取中心线
        Mat skeleton = new Mat(binaryMat.size(), CvType.CV_8UC1, new Scalar(0));
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));

        boolean done;
        do {
            // 腐蚀操作
            Mat eroded = new Mat();
            Imgproc.erode(binaryMat, eroded, element);

            // 计算骨架化图像
            Mat temp = new Mat();
            Core.subtract(binaryMat, eroded, temp);
            Core.bitwise_or(skeleton, temp, skeleton);

            // 复制腐蚀后的图像
            eroded.copyTo(binaryMat);

            done = Core.countNonZero(binaryMat) == 0;  // 判断是否已经完成骨架化
        } while (!done);

        // 将结果转换回 Bitmap，创建带透明背景的 Bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(skeleton.width(), skeleton.height(), Bitmap.Config.ARGB_8888);

        // 将骨架化的图像转换成 Bitmap，透明部分将保持透明
        Utils.matToBitmap(skeleton, resultBitmap);

        // 释放资源
        mat.release();
        binaryMat.release();
        skeleton.release();
        element.release();

        return resultBitmap;
    }



    public static Bitmap thresholdImage(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap thresholded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = original.getPixel(x, y);
                int gray = (int) (0.299 * Color.red(pixelColor) + 0.587 * Color.green(pixelColor) + 0.114 * Color.blue(pixelColor));
                int threshold = gray > 128 ? 255 : 0;  // 这里采用简单的阈值化方法，128为阈值
                thresholded.setPixel(x, y, Color.rgb(threshold, threshold, threshold));
            }
        }
        return thresholded;
    }


    /**
     * 反转黑白图，传进来的bitmap本身需要是黑白图
     *
     * @param bmp
     * @return
     */
    public static Bitmap ReverseBlackAndWhiteImage(Bitmap bmp) {
        int m_ImageWidth, m_ImageHeigth;

        m_ImageWidth = bmp.getWidth();

        m_ImageHeigth = bmp.getHeight();

        int[] m_BmpPixel = new int[m_ImageWidth * m_ImageHeigth];

        bmp.getPixels(m_BmpPixel, 0, m_ImageWidth, 0, 0, m_ImageWidth, m_ImageHeigth);

        for (int i = 0; i < m_ImageWidth * m_ImageHeigth; i++) {
            if ((m_BmpPixel[i] & 0xff) == 0xff) {
                m_BmpPixel[i] = 0xff000000;

            } else if ((m_BmpPixel[i] & 0xff) == 0x0) {
                m_BmpPixel[i] = 0xffffffff;
            }
        }

        bmp.setPixels(m_BmpPixel, 0, m_ImageWidth, 0, 0, m_ImageWidth, m_ImageHeigth);

        return bmp;
    }

    public static Bitmap transparentImage(Bitmap bmp) {
        int m_ImageWidth, m_ImageHeigth;

        m_ImageWidth = bmp.getWidth();

        m_ImageHeigth = bmp.getHeight();

        int[] m_BmpPixel = new int[m_ImageWidth * m_ImageHeigth];

        bmp.getPixels(m_BmpPixel, 0, m_ImageWidth, 0, 0, m_ImageWidth, m_ImageHeigth);

        for (int i = 0; i < m_ImageWidth * m_ImageHeigth; i++) {
            if ((m_BmpPixel[i] & 0x00ffffff) == 0x00ffffff) {
                m_BmpPixel[i] = 0xff000000;

            } else {
                m_BmpPixel[i] = 0xffffffff;
            }
        }

        bmp.setPixels(m_BmpPixel, 0, m_ImageWidth, 0, 0, m_ImageWidth,

                m_ImageHeigth);

        return bmp;

    }

    public static Bitmap transparentImageto(Bitmap bmp) {
        int m_ImageWidth, m_ImageHeigth;

        m_ImageWidth = bmp.getWidth();

        m_ImageHeigth = bmp.getHeight();

        int[] m_BmpPixel = new int[m_ImageWidth * m_ImageHeigth];

        bmp.getPixels(m_BmpPixel, 0, m_ImageWidth, 0, 0, m_ImageWidth, m_ImageHeigth);

        for (int i = 0; i < m_ImageWidth * m_ImageHeigth; i++) {
            if ((m_BmpPixel[i] & 0x00ffffff) == 0x00ffffff) {
                m_BmpPixel[i] = 0x00ffffff;
            }
        }

        bmp.setPixels(m_BmpPixel, 0, m_ImageWidth, 0, 0, m_ImageWidth, m_ImageHeigth);

        return bmp;

    }

    //把白色转换成透明
    public static Bitmap getImageToChange(Bitmap mBitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        if (mBitmap != null) {
            int mWidth = mBitmap.getWidth();
            int mHeight = mBitmap.getHeight();
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    int color = mBitmap.getPixel(j, i);
                    int g = Color.green(color);
                    int r = Color.red(color);
                    int b = Color.blue(color);
                    int a = Color.alpha(color);
                    if (g >= 250 && r >= 250 && b >= 250) {
                        a = 0;
                    }
                    color = Color.argb(a, r, g, b);
                    createBitmap.setPixel(j, i, color);
                }
            }
        }
        return createBitmap;
    }

    //把白色转换成透明
    public static Bitmap getImageToChange(Bitmap mBitmap, int vale) {
        Bitmap createBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        if (mBitmap != null) {
            int mWidth = mBitmap.getWidth();
            int mHeight = mBitmap.getHeight();
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    int color = mBitmap.getPixel(j, i);
                    int g = Color.green(color);
                    int r = Color.red(color);
                    int b = Color.blue(color);
                    int a = Color.alpha(color);
                    if (g < vale || r < vale || b < vale) {
                        a = 250;
                    }
                    color = Color.argb(a, r, g, b);
                    createBitmap.setPixel(j, i, color);
                }
            }
        }
        return createBitmap;
    }

    /*
     * 4.图片素描效果
     */
    public static Bitmap SuMiaoImage(Bitmap adjustBitmap, float resol) {
        // 先调整大小
        Bitmap bmp = imageResize(adjustBitmap, (int) (adjustBitmap.getWidth() / resol), (int) (adjustBitmap.getHeight() / resol));
        if (bmp == null) {
            return null;
        }
        //创建新Bitmap
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];    //存储变换图像
        int[] linpix = new int[width * height];     //存储灰度图像
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        //灰度图像
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++)   //拉普拉斯算子模板 { 0, -1, 0, -1, -5, -1, 0, -1, 0
            {
                //获取前一个像素颜色
                pixColor = pixels[width * i + j];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                //灰度图像
                int gray = (int) (0.3 * pixR + 0.59 * pixG + 0.11 * pixB);
                linpix[width * i + j] = Color.argb(255, gray, gray, gray);
                //图像反向
                gray = 255 - gray;
                pixels[width * i + j] = Color.argb(255, gray, gray, gray);
            }
        }
        int radius = Math.min(width / 2, height / 2);
        int[] copixels = gaussBlur(pixels, width, height, 10, 10 / 3);   //高斯模糊 采用半径10
        int[] result = colorDodge(linpix, copixels);   //素描图像 颜色减淡
        bitmap.setPixels(result, 0, width, 0, 0, width, height);
        return bitmap;
    }

    //高斯模糊
    public static int[] gaussBlur(int[] data, int width, int height, int radius, float sigma) {

        float pa = (float) (1 / (Math.sqrt(2 * Math.PI) * sigma));
        float pb = -1.0f / (2 * sigma * sigma);
        // generate the Gauss Matrix
        float[] gaussMatrix = new float[radius * 2 + 1];
        float gaussSum = 0f;
        for (int i = 0, x = -radius; x <= radius; ++x, ++i) {
            float g = (float) (pa * Math.exp(pb * x * x));
            gaussMatrix[i] = g;
            gaussSum += g;
        }
        for (int i = 0, length = gaussMatrix.length; i < length; ++i) {
            gaussMatrix[i] /= gaussSum;
        }

        // x direction
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = x + j;
                    if (k >= 0 && k < width) {
                        int index = y * width + k;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }
                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }

        // y direction
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = y + j;
                    if (k >= 0 && k < height) {
                        int index = k * width + x;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }
                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }
        return data;
    }

    //颜色减淡
    public static int[] colorDodge(int[] baseColor, int[] mixColor) {
        for (int i = 0, length = baseColor.length; i < length; ++i) {
            int bColor = baseColor[i];
            int br = (bColor & 0x00ff0000) >> 16;
            int bg = (bColor & 0x0000ff00) >> 8;
            int bb = (bColor & 0x000000ff);

            int mColor = mixColor[i];
            int mr = (mColor & 0x00ff0000) >> 16;
            int mg = (mColor & 0x0000ff00) >> 8;
            int mb = (mColor & 0x000000ff);

            int nr = colorDodgeFormular(br, mr);
            int ng = colorDodgeFormular(bg, mg);
            int nb = colorDodgeFormular(bb, mb);

            baseColor[i] = nr << 16 | ng << 8 | nb | 0xff000000;
        }
        return baseColor;
    }

    private static int colorDodgeFormular(int base, int mix) {
        int result = base + (base * mix) / (255 - mix);
        result = result > 255 ? 255 : result;
        return result;
    }

    /*
     * 5.图像锐化处理 拉普拉斯算子处理
     */
    public static Bitmap SharpenImage(Bitmap bmp, float alpha) {
        /*
         * 锐化基本思想是加强图像中景物的边缘和轮廓,使图像变得清晰
         * 而图像平滑是使图像中边界和轮廓变得模糊
         *
         * 拉普拉斯算子图像锐化
         * 获取周围9个点的矩阵乘以模板9个的矩阵 卷积
         */
        //拉普拉斯算子模板 { 0, -1, 0, -1, -5, -1, 0, -1, 0 } { -1, -1, -1, -1, 9, -1, -1, -1, -1 }
        int[] laplacian = new int[]{-1, -1, -1, -1, 9, -1, -1, -1, -1};
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int pixColor = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int idx = 0;
//        float alpha = 0.3F;  //图片透明度
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        //图像处理
        for (int i = 1; i < height - 1; i++) {
            for (int k = 1; k < width - 1; k++) {
                idx = 0;
                newR = 0;
                newG = 0;
                newB = 0;
                for (int n = -1; n <= 1; n++)   //取出图像3*3领域像素
                {
                    for (int m = -1; m <= 1; m++)  //n行数不变 m列变换
                    {
                        pixColor = pixels[(i + n) * width + k + m];  //当前点(i,k)
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);
                        //图像像素与对应摸板相乘
                        newR = newR + (int) (pixR * laplacian[idx] * alpha);
                        newG = newG + (int) (pixG * laplacian[idx] * alpha);
                        newB = newB + (int) (pixB * laplacian[idx] * alpha);
                        idx++;
                    }
                }
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                //赋值
                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 素描效果
     *
     * @param bmp
     * @return
     */
    public static Bitmap convertToSketch(Bitmap bmp) {
        int pos, row, col, clr;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixSrc = new int[width * height];
        int[] pixNvt = new int[width * height];
        // 先对图象的像素处理成灰度颜色后再取反
        bmp.getPixels(pixSrc, 0, width, 0, 0, width, height);
        for (row = 0; row < height; row++) {
            for (col = 0; col < width; col++) {
                pos = row * width + col;
                pixSrc[pos] = (Color.red(pixSrc[pos])
                        + Color.green(pixSrc[pos]) + Color.blue(pixSrc[pos])) / 3;
                pixNvt[pos] = 255 - pixSrc[pos];
            }
        }
        // 对取反的像素进行高斯模糊, 强度可以设置，暂定为5.0
        gaussGray(pixNvt, 5.0, 5.0, width, height);
        // 灰度颜色和模糊后像素进行差值运算
        for (row = 0; row < height; row++) {
            for (col = 0; col < width; col++) {
                pos = row * width + col;
                clr = pixSrc[pos] << 8;
                clr /= 256 - pixNvt[pos];
                clr = Math.min(clr, 255);
                pixSrc[pos] = Color.rgb(clr, clr, clr);
            }
        }
        bmp.setPixels(pixSrc, 0, width, 0, 0, width, height);
        return bmp;
    }

    private static int gaussGray(int[] psrc, double horz, double vert,
                                 int width, int height) {
        int[] dst, src;
        double[] n_p, n_m, d_p, d_m, bd_p, bd_m;
        double[] val_p, val_m;
        int i, j, t, k, row, col, terms;
        int[] initial_p, initial_m;
        double std_dev;
        int row_stride = width;
        int max_len = Math.max(width, height);
        int sp_p_idx, sp_m_idx, vp_idx, vm_idx;
        val_p = new double[max_len];
        val_m = new double[max_len];
        n_p = new double[5];
        n_m = new double[5];
        d_p = new double[5];
        d_m = new double[5];
        bd_p = new double[5];
        bd_m = new double[5];
        src = new int[max_len];
        dst = new int[max_len];
        initial_p = new int[4];
        initial_m = new int[4];
        // 垂直方向
        if (vert > 0.0) {
            vert = Math.abs(vert) + 1.0;
            std_dev = Math.sqrt(-(vert * vert) / (2 * Math.log(1.0 / 255.0)));
            // 初试化常量
            findConstants(n_p, n_m, d_p, d_m, bd_p, bd_m, std_dev);
            for (col = 0; col < width; col++) {
                for (k = 0; k < max_len; k++) {
                    val_m[k] = val_p[k] = 0;
                }
                for (t = 0; t < height; t++) {
                    src[t] = psrc[t * row_stride + col];
                }
                sp_p_idx = 0;
                sp_m_idx = height - 1;
                vp_idx = 0;
                vm_idx = height - 1;
                initial_p[0] = src[0];
                initial_m[0] = src[height - 1];
                for (row = 0; row < height; row++) {
                    terms = (row < 4) ? row : 4;
                    for (i = 0; i <= terms; i++) {
                        val_p[vp_idx] += n_p[i] * src[sp_p_idx - i] - d_p[i]
                                * val_p[vp_idx - i];
                        val_m[vm_idx] += n_m[i] * src[sp_m_idx + i] - d_m[i]
                                * val_m[vm_idx + i];
                    }
                    for (j = i; j <= 4; j++) {
                        val_p[vp_idx] += (n_p[j] - bd_p[j]) * initial_p[0];
                        val_m[vm_idx] += (n_m[j] - bd_m[j]) * initial_m[0];
                    }
                    sp_p_idx++;
                    sp_m_idx--;
                    vp_idx++;
                    vm_idx--;
                }
                transferGaussPixels(val_p, val_m, dst, 1, height);
                for (t = 0; t < height; t++) {
                    psrc[t * row_stride + col] = dst[t];
                }
            }
        }
        // 水平方向
        if (horz > 0.0) {
            horz = Math.abs(horz) + 1.0;
            if (horz != vert) {
                std_dev = Math.sqrt(-(horz * horz)
                        / (2 * Math.log(1.0 / 255.0)));
                // 初试化常量
                findConstants(n_p, n_m, d_p, d_m, bd_p, bd_m, std_dev);
            }
            for (row = 0; row < height; row++) {
                for (k = 0; k < max_len; k++) {
                    val_m[k] = val_p[k] = 0;
                }
                for (t = 0; t < width; t++) {
                    src[t] = psrc[row * row_stride + t];
                }
                sp_p_idx = 0;
                sp_m_idx = width - 1;
                vp_idx = 0;
                vm_idx = width - 1;
                initial_p[0] = src[0];
                initial_m[0] = src[width - 1];
                for (col = 0; col < width; col++) {
                    terms = (col < 4) ? col : 4;
                    for (i = 0; i <= terms; i++) {
                        val_p[vp_idx] += n_p[i] * src[sp_p_idx - i] - d_p[i]
                                * val_p[vp_idx - i];
                        val_m[vm_idx] += n_m[i] * src[sp_m_idx + i] - d_m[i]
                                * val_m[vm_idx + i];
                    }
                    for (j = i; j <= 4; j++) {
                        val_p[vp_idx] += (n_p[j] - bd_p[j]) * initial_p[0];
                        val_m[vm_idx] += (n_m[j] - bd_m[j]) * initial_m[0];
                    }
                    sp_p_idx++;
                    sp_m_idx--;
                    vp_idx++;
                    vm_idx--;
                }
                transferGaussPixels(val_p, val_m, dst, 1, width);
                for (t = 0; t < width; t++) {
                    psrc[row * row_stride + t] = dst[t];
                }
            }
        }
        return 0;
    }

    private static void transferGaussPixels(double[] src1, double[] src2,
                                            int[] dest, int bytes, int width) {
        int i, j, k, b;
        int bend = bytes * width;
        double sum;
        i = j = k = 0;
        for (b = 0; b < bend; b++) {
            sum = src1[i++] + src2[j++];
            if (sum > 255)
                sum = 255;
            else if (sum < 0)
                sum = 0;
            dest[k++] = (int) sum;
        }
    }

    private static void findConstants(double[] n_p, double[] n_m, double[] d_p,
                                      double[] d_m, double[] bd_p, double[] bd_m, double std_dev) {
        double div = Math.sqrt(2 * 3.141593) * std_dev;
        double x0 = -1.783 / std_dev;
        double x1 = -1.723 / std_dev;
        double x2 = 0.6318 / std_dev;
        double x3 = 1.997 / std_dev;
        double x4 = 1.6803 / div;
        double x5 = 3.735 / div;
        double x6 = -0.6803 / div;
        double x7 = -0.2598 / div;
        int i;
        n_p[0] = x4 + x6;
        n_p[1] = (Math.exp(x1)
                * (x7 * Math.sin(x3) - (x6 + 2 * x4) * Math.cos(x3)) + Math
                .exp(x0) * (x5 * Math.sin(x2) - (2 * x6 + x4) * Math.cos(x2)));
        n_p[2] = (2
                * Math.exp(x0 + x1)
                * ((x4 + x6) * Math.cos(x3) * Math.cos(x2) - x5 * Math.cos(x3)
                * Math.sin(x2) - x7 * Math.cos(x2) * Math.sin(x3)) + x6
                * Math.exp(2 * x0) + x4 * Math.exp(2 * x1));
        n_p[3] = (Math.exp(x1 + 2 * x0)
                * (x7 * Math.sin(x3) - x6 * Math.cos(x3)) + Math.exp(x0 + 2
                * x1)
                * (x5 * Math.sin(x2) - x4 * Math.cos(x2)));
        n_p[4] = 0.0;
        d_p[0] = 0.0;
        d_p[1] = -2 * Math.exp(x1) * Math.cos(x3) - 2 * Math.exp(x0)
                * Math.cos(x2);
        d_p[2] = 4 * Math.cos(x3) * Math.cos(x2) * Math.exp(x0 + x1)
                + Math.exp(2 * x1) + Math.exp(2 * x0);
        d_p[3] = -2 * Math.cos(x2) * Math.exp(x0 + 2 * x1) - 2 * Math.cos(x3)
                * Math.exp(x1 + 2 * x0);
        d_p[4] = Math.exp(2 * x0 + 2 * x1);
        for (i = 0; i <= 4; i++) {
            d_m[i] = d_p[i];
        }
        n_m[0] = 0.0;
        for (i = 1; i <= 4; i++) {
            n_m[i] = n_p[i] - d_p[i] * n_p[0];
        }
        double sum_n_p, sum_n_m, sum_d;
        double a, b;
        sum_n_p = 0.0;
        sum_n_m = 0.0;
        sum_d = 0.0;
        for (i = 0; i <= 4; i++) {
            sum_n_p += n_p[i];
            sum_n_m += n_m[i];
            sum_d += d_p[i];
        }
        a = sum_n_p / (1.0 + sum_d);
        b = sum_n_m / (1.0 + sum_d);
        for (i = 0; i <= 4; i++) {
            bd_p[i] = d_p[i] * a;
            bd_m[i] = d_m[i] * b;
        }
    }

    public static Bitmap ImageDithering(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                gray[width * i + j] = red;
            }
        }


        int e = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                if (g >= 128) {
                    pixels[width * i + j] = 0xffffffff;
                    e = g - 255;
                } else {
                    pixels[width * i + j] = 0xff000000;
                    e = g - 0;
                }
                if (j < width - 1 && i < height - 1) {
                    //右边像素处理
                    gray[width * i + j + 1] += 3 * e / 8;
                    //下
                    gray[width * (i + 1) + j] += 3 * e / 8;
                    //右下
                    gray[width * (i + 1) + j + 1] += e / 4;
                } else if (j == width - 1 && i < height - 1) {//靠右或靠下边的像素的情况
                    //下方像素处理
                    gray[width * (i + 1) + j] += 3 * e / 8;
                } else if (j < width - 1 && i == height - 1) {
                    //右边像素处理
                    gray[width * (i) + j + 1] += e / 4;
                }
            }
        }

        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return mBitmap;
    }

    /**
     * 去除多余白框
     *
     * @param bitmap 灰度图
     * @return 当输入为全白图时返回为null
     */
    public static Bitmap ImageWhiteEdgeRemoval(Bitmap bitmap, int cropHeights, int cropWidths) {
        int[] imgThePixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        bitmap.getPixels(imgThePixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int top = 0;  // 上边框白色高度
        int left = 0; // 左边框白色高度
        int right = 0; // 右边框白色高度
        int bottom = 0; // 底边框白色高度

        for (int h = 0; h < bitmap.getHeight(); h++) {
            boolean holdBlackPix = false;
            for (int w = 0; w < bitmap.getWidth(); w++) {
                if ((bitmap.getPixel(w, h) & 0xff) != 255) {
                    holdBlackPix = true;
                    break;
                }
            }

            if (holdBlackPix) {
                break;
            }
            top++;
        }

        for (int w = 0; w < bitmap.getWidth(); w++) {
            boolean holdBlackPix = false;
            for (int h = 0; h < bitmap.getHeight(); h++) {
                if ((bitmap.getPixel(w, h) & 0xff) != 255) {
                    holdBlackPix = true;
                    break;
                }
            }
            if (holdBlackPix) {
                break;
            }
            left++;
        }

        for (int w = bitmap.getWidth() - 1; w >= 0; w--) {
            boolean holdBlackPix = false;
            for (int h = 0; h < bitmap.getHeight(); h++) {
                if ((bitmap.getPixel(w, h) & 0xff) != 255) {
                    holdBlackPix = true;
                    break;
                }
            }
            if (holdBlackPix) {
                break;
            }
            right++;
        }

        for (int h = bitmap.getHeight() - 1; h >= 0; h--) {
            boolean holdBlackPix = false;
            for (int w = 0; w < bitmap.getWidth(); w++) {
                if ((bitmap.getPixel(w, h) & 0xff) != 255) {
                    holdBlackPix = true;
                    break;
                }
            }
            if (holdBlackPix) {
                break;
            }
            bottom++;
        }

        // 获取内容区域的宽高
        int cropHeight = bitmap.getHeight() - bottom - top;
        int cropWidth = bitmap.getWidth() - left - right;

        if ((cropHeight <= 0) || (cropWidth <= 0)) {
            return null;
        }

        // 获取内容区域的像素点
        int[] newPix = new int[cropWidth * cropHeight];

        int i = 0;
        for (int h = top; h < top + cropHeight; h++) {
            for (int w = left; w < left + cropWidth; w++) {
                newPix[i++] = bitmap.getPixel(w, h);
            }
        }
        bitmap = Bitmap.createBitmap(newPix, cropWidth, cropHeight, Bitmap.Config.ARGB_8888);
        Bitmap finalBitmap = Bitmap.createBitmap(bitmap.getWidth() + 10, bitmap.getHeight() + 10, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(finalBitmap);
        Paint paint = new Paint();
        canvas.drawRGB(255, 255, 255);
        canvas.drawBitmap(bitmap, 5, 5, paint);

        return finalBitmap;
    }

    public static Bitmap addWhiteBg(Bitmap bitmap) {
        Bitmap finalBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(finalBitmap);
        Paint paint = new Paint();
        canvas.drawRGB(255, 255, 255);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return finalBitmap;
    }
}
