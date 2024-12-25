package in.co.gorest.grblcontroller.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DrawingView extends View {
    private List<Shape> shapes = new ArrayList<>();
    private Paint paint;
    private Shape selectedShape;
    private float lastX, lastY;

    private static float initialX;
    private static float initialY;  // 记录初始坐标，用于三角形绘制和大小调整

    private ScaleGestureDetector scaleGestureDetector; // 缩放手势检测器

    public static final int TOOL_TEXT = 0; // 文字工具
    public static final int TOOL_BARCODE = 1; // 条形码工具
    public static final int TOOL_QR_CODE = 2; // 二维码工具
    public static final int TOOL_PEN = 3; // 画笔工具
    public static final int TOOL_LINE = 4; // 直线工具
    public static final int TOOL_TRIANGLE = 5; // 三角形工具
    public static final int TOOL_RECTANGLE = 6; // 矩形工具
    public static final int TOOL_CIRCLE = 7; // 圆形工具
    public static final int TOOL_ERASER = 8; // 橡皮擦工具

    private int currentTool = -1;  // 当前选中的工具

    private Path penPath;  // 当前路径
    private List<Path> penPaths;  // 存储所有绘制的路径
    private List<Paint> penPaints;  // 存储所有路径对应的画笔


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.BLACK);  // 默认颜色为黑色
        paint.setStrokeWidth(5f);  // 默认粗细为5
        paint.setStyle(Paint.Style.STROKE);  // 画笔为描边
        paint.setStrokeCap(Paint.Cap.ROUND);  // 圆形的端点
        paint.setStrokeJoin(Paint.Join.ROUND);  // 圆形的连接点

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        penPaths = new ArrayList<>();
        penPaints = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制所有形状
        for (Shape shape : shapes) {
            shape.draw(canvas, paint);
        }

        for (int i = 0; i < penPaths.size(); i++) {
            canvas.drawPath(penPaths.get(i), penPaints.get(i));
        }
        if (penPath != null) {
            canvas.drawPath(penPath, paint);  // 绘制当前路径
        }
    }

    public void drawShape(Shape shape) {
        shapes.add(shape);
        invalidate();  // 重新绘制
    }

    public void clear() {
        shapes.clear();  // 清除所有形状
        penPaths.clear();  // 清除画笔路径
        penPaints.clear();  // 清除画笔
        invalidate();     // 重新绘制画布
    }


    public void setCurrentTool(int tool) {
        currentTool = tool;
    }

    // 处理触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event); // 处理缩放手势

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (currentTool == TOOL_PEN) {
                    // 画笔工具，开始绘制路径
                    penPath = new Path();
                    penPaths.add(penPath);  // 添加路径
                    penPaints.add(new Paint(paint));  // 添加画笔
                    penPath.moveTo(x, y);  // 移动到触摸点
                } else if (currentTool == TOOL_LINE) {
                    // 创建直线，初始时终点和起点相同
                    LineShape line = new LineShape(x, y, x, y, paint);
                    shapes.add(line);
                    selectedShape = line;
                } else if (currentTool == TOOL_TRIANGLE) {
                    // 记录起始坐标作为三角形的起始点
                    initialX = x;
                    initialY = y;

                    // 创建一个新的三角形，起始位置为 (initialX, initialY)
                    TriangleShape triangle = new TriangleShape(initialX, initialY, initialX + 100, initialY + 100, initialX + 200, initialY, paint);
                    drawShape(triangle);
                    selectedShape = triangle;  // 设置为当前选中的形状
                } else if (currentTool == TOOL_RECTANGLE) {
                    // 创建矩形，初始对角线的两个点
                    RectangleShape rectangle = new RectangleShape(x, y, x, y, paint);
                    shapes.add(rectangle);
                    selectedShape = rectangle;
                } else if (currentTool == TOOL_CIRCLE) {
                    // 创建圆形，初始半径为 0
                    CircleShape circle = new CircleShape(x, y, 0, paint);
                    shapes.add(circle);
                    selectedShape = circle;
                } else if (currentTool == TOOL_ERASER) {
                    // 橡皮擦工具，擦除形状
                    eraseShape(x, y);  // 尝试擦除形状
                } else {
                    // 选中某个形状（文字、二维码等）
                    selectedShape = getShapeAt(x, y);
                    if (selectedShape != null) {
                        lastX = x;
                        lastY = y;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (currentTool == TOOL_PEN && penPath != null) {
                    penPath.lineTo(x, y);  // 画笔工具，添加路径线段
                } else if (selectedShape instanceof LineShape) {
                    // 更新直线的结束点
                    ((LineShape) selectedShape).resize(x, y);
                } else if (selectedShape instanceof TriangleShape) {
                    // 计算拖动的距离，并动态改变三角形的大小
                    TriangleShape triangle = (TriangleShape) selectedShape;
                    triangle.resize(x, y);  // 更新三角形大小
                } else if (selectedShape instanceof RectangleShape) {
                    ((RectangleShape) selectedShape).resize(x, y);  // 调整矩形大小
                } else if (selectedShape instanceof CircleShape) {
                    ((CircleShape) selectedShape).resize(x, y);  // 调整圆的半径
                } else if (currentTool == TOOL_ERASER) {
                    eraseShape(x, y);  // 橡皮擦工具，擦除形状
                } else if (selectedShape != null) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    selectedShape.move(dx, dy);
                    lastX = x;
                    lastY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (currentTool == TOOL_PEN) {
                    penPath = null;  // 结束当前路径
                }
                selectedShape = null;
                break;
        }
        invalidate();  // 重新绘制
        return true;
    }

    // 查找点击位置是否有形状
    private Shape getShapeAt(float x, float y) {
        for (Shape shape : shapes) {
            if (shape.contains(x, y)) {
                return shape;
            }
        }
        return null;
    }

    // 画笔工具设置
    public void setPenColor(int color) {
        paint.setColor(color);  // 更新画笔颜色
    }

    public void setPenWidth(float width) {
        paint.setStrokeWidth(width);  // 更新画笔粗细
    }

    // 形状接口
    public interface Shape {
        void draw(Canvas canvas, Paint paint);

        boolean contains(float x, float y);  // 判断是否包含该点

        void move(float dx, float dy);  // 移动形状

        void scale(float scaleFactor);  // 缩放形状
    }

    // 缩放手势监听器
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (selectedShape != null) {
                float scaleFactor = detector.getScaleFactor();
                selectedShape.scale(scaleFactor);  // 对选中的形状进行缩放
                invalidate();  // 重新绘制
            }
            return true;
        }
    }

    // 文本形状
    public static class TextShape implements Shape {
        private String text;
        private float x, y;
        private Paint paint;
        private float scaleFactor = 1f; // 缩放比例

        // 修改此构造函数，直接设置文本大小为 48
        public TextShape(String text, float x, float y, Paint paint) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.paint = new Paint(paint);  // 创建新的 Paint 对象，避免直接修改传入的 Paint
            this.paint.setTextSize(48f);    // 设置默认的文本大小为 48
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            canvas.save();
            canvas.scale(scaleFactor, scaleFactor, x, y); // 只在绘制时应用缩放
            canvas.drawText(text, x, y, this.paint);
            canvas.restore();
        }

        @Override
        public boolean contains(float x, float y) {
            // 简单的矩形包围框判断
            RectF bounds = new RectF(this.x, this.y - paint.getTextSize(), this.x + paint.measureText(text), this.y);
            return bounds.contains(x, y);
        }

        @Override
        public void move(float dx, float dy) {
            this.x += dx;
            this.y += dy;
        }

        @Override
        public void scale(float scaleFactor) {
            // 缩放时更新位置和缩放比例
            this.scaleFactor *= scaleFactor;

            // 限制缩放比例范围 (最小 0.1，最大 3.0)
            this.scaleFactor = Math.max(0.1f, Math.min(this.scaleFactor, 2.0f));
        }
    }

    // 条形码形状
    public static class BarcodeShape implements Shape {
        private Bitmap barcodeBitmap;
        private float x, y;
        private float scaleFactor = 1f;

        public BarcodeShape(String content, float x, float y, Paint paint) {
            this.x = x;
            this.y = y;
            this.barcodeBitmap = generateBarcode(content);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            canvas.save();
            canvas.scale(scaleFactor, scaleFactor, x, y); // 根据缩放比例绘制
            if (barcodeBitmap != null) {
                canvas.drawBitmap(barcodeBitmap, x, y, null);
            }
            canvas.restore();
        }

        @Override
        public boolean contains(float x, float y) {
            // 简单的矩形包围框判断
            RectF bounds = new RectF(this.x, this.y, this.x + barcodeBitmap.getWidth(), this.y + barcodeBitmap.getHeight());
            return bounds.contains(x, y);
        }

        @Override
        public void move(float dx, float dy) {
            this.x += dx;
            this.y += dy;
        }

        @Override
        public void scale(float scaleFactor) {
            // 平滑缩放比例变化并限制范围
            this.scaleFactor *= scaleFactor;

            // 限制缩放比例范围 (最小 0.1，最大 3.0)
            this.scaleFactor = Math.max(0.1f, Math.min(this.scaleFactor, 2.0f));
        }

        private Bitmap generateBarcode(String content) {
            try {
                MultiFormatWriter writer = new MultiFormatWriter();
                Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
                hints.put(EncodeHintType.MARGIN, 1);  // 设置条形码的边距
                BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, 200, 100, hints);

                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
                return bitmap;
            } catch (WriterException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // 二维码形状
    public static class QRCodeShape implements Shape {
        private Bitmap qrCodeBitmap;
        private float x, y;
        private float scaleFactor = 1f; // 当前缩放比例

        public QRCodeShape(String content, float x, float y, Paint paint) {
            this.x = x;
            this.y = y;
            this.qrCodeBitmap = generateQRCode(content);  // 生成二维码
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            canvas.save();
            // 计算缩放中心点并应用缩放
            canvas.scale(scaleFactor, scaleFactor, x + qrCodeBitmap.getWidth() / 2, y + qrCodeBitmap.getHeight() / 2);
            if (qrCodeBitmap != null) {
                canvas.drawBitmap(qrCodeBitmap, x, y, null);
            }
            canvas.restore();
        }

        @Override
        public boolean contains(float x, float y) {
            // 简单的矩形包围框判断
            RectF bounds = new RectF(this.x, this.y, this.x + qrCodeBitmap.getWidth() * scaleFactor, this.y + qrCodeBitmap.getHeight() * scaleFactor);
            return bounds.contains(x, y);
        }

        @Override
        public void move(float dx, float dy) {
            this.x += dx;
            this.y += dy;
        }

        @Override
        public void scale(float scaleFactor) {
            // 平滑缩放比例变化并限制范围
            this.scaleFactor *= scaleFactor;

            // 限制缩放比例范围 (最小 0.1，最大 3.0)
            this.scaleFactor = Math.max(0.1f, Math.min(this.scaleFactor, 2.0f));
        }

        private Bitmap generateQRCode(String content) {
            try {
                com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // 直线
    public static class LineShape implements Shape {
        private float startX, startY, endX, endY;  // 直线的起始点和结束点
        private Paint paint;

        public LineShape(float startX, float startY, float endX, float endY, Paint paint) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.paint = new Paint(paint);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            canvas.drawLine(startX, startY, endX, endY, this.paint);  // 绘制直线
        }

        @Override
        public boolean contains(float x, float y) {
            // 判断点是否在直线上。考虑到直线的宽度，可以通过距离计算
            float lineLength = (float) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            float distToStart = (float) Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
            float distToEnd = (float) Math.sqrt(Math.pow(x - endX, 2) + Math.pow(y - endY, 2));

            return Math.abs(distToStart + distToEnd - lineLength) < 10;  // 误差范围内算点在直线上
        }

        @Override
        public void move(float dx, float dy) {
            this.startX += dx;
            this.startY += dy;
            this.endX += dx;
            this.endY += dy;
        }

        // 根据拖动的距离调整直线的结束点
        public void resize(float newX, float newY) {
            this.endX = newX;
            this.endY = newY;
        }

        @Override
        public void scale(float scaleFactor) {
            // 直线缩放时，通过比例调整起点和终点
            float centerX = (startX + endX) / 2;
            float centerY = (startY + endY) / 2;

            this.startX = centerX + (startX - centerX) * scaleFactor;
            this.startY = centerY + (startY - centerY) * scaleFactor;
            this.endX = centerX + (endX - centerX) * scaleFactor;
            this.endY = centerY + (endY - centerY) * scaleFactor;
        }
    }

    // 三角形形状
    public static class TriangleShape implements Shape {
        private float x1, y1, x2, y2, x3, y3;
        private Paint paint;

        public TriangleShape(float x1, float y1, float x2, float y2, float x3, float y3, Paint paint) {
            this.x1 = x1;  // 顶部点
            this.y1 = y1;
            this.x2 = x2;  // 底部左点
            this.y2 = y2;
            this.x3 = x3;  // 底部右点
            this.y3 = y3;
            this.paint = new Paint(paint);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            Path path = new Path();
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
            path.lineTo(x3, y3);
            path.close();

            canvas.drawPath(path, this.paint);  // 绘制三角形
        }

        @Override
        public boolean contains(float x, float y) {
            // 使用点是否在三角形内部的算法判断
            float alpha = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
            float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
            float gamma = 1 - alpha - beta;

            return (alpha > 0 && beta > 0 && gamma > 0);  // 判断是否在三角形内部
        }

        @Override
        public void move(float dx, float dy) {
            this.x1 += dx;
            this.y1 += dy;
            this.x2 += dx;
            this.y2 += dy;
            this.x3 += dx;
            this.y3 += dy;
        }

        // 根据拖动距离动态调整三角形大小
        public void resize(float newX, float newY) {
            float width = Math.abs(newX - initialX);
            float height = Math.abs(newY - initialY);

            // 更新三角形的坐标，按拖动的距离重新计算顶点
            this.x2 = initialX - width / 2; // 底部左点
            this.y2 = initialY + height;  // 底部左点
            this.x3 = initialX + width / 2; // 底部右点
            this.y3 = initialY + height;  // 底部右点
        }

        @Override
        public void scale(float scaleFactor) {
            // 不做缩放处理，因为我们是通过拖动改变大小
        }
    }

    // 矩形形状
    public static class RectangleShape implements Shape {
        private float x1, y1, x2, y2;  // 对角线的两个点
        private Paint paint;

        public RectangleShape(float x1, float y1, float x2, float y2, Paint paint) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.paint = new Paint(paint);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            float left = Math.min(x1, x2);
            float top = Math.min(y1, y2);
            float right = Math.max(x1, x2);
            float bottom = Math.max(y1, y2);

            canvas.drawRect(left, top, right, bottom, this.paint);  // 绘制矩形
        }

        @Override
        public boolean contains(float x, float y) {
            float left = Math.min(x1, x2);
            float top = Math.min(y1, y2);
            float right = Math.max(x1, x2);
            float bottom = Math.max(y1, y2);

            return x >= left && x <= right && y >= top && y <= bottom;  // 判断点是否在矩形内
        }

        @Override
        public void move(float dx, float dy) {
            this.x1 += dx;
            this.y1 += dy;
            this.x2 += dx;
            this.y2 += dy;
        }

        // 根据拖动的距离调整矩形大小
        public void resize(float newX, float newY) {
            this.x2 = newX;
            this.y2 = newY;
        }

        @Override
        public void scale(float scaleFactor) {
            // 缩放时按比例调整对角线
            float centerX = (x1 + x2) / 2;
            float centerY = (y1 + y2) / 2;

            this.x1 = centerX + (x1 - centerX) * scaleFactor;
            this.y1 = centerY + (y1 - centerY) * scaleFactor;
            this.x2 = centerX + (x2 - centerX) * scaleFactor;
            this.y2 = centerY + (y2 - centerY) * scaleFactor;
        }
    }

    // 圆形形状
    public static class CircleShape implements Shape {
        private float cx, cy;  // 圆心坐标
        private float radius;  // 半径
        private Paint paint;

        private float initialX, initialY; // 初始点击位置

        public CircleShape(float cx, float cy, float radius, Paint paint) {
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.paint = new Paint(paint);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            canvas.drawCircle(cx, cy, radius, this.paint);  // 绘制圆形
        }

        @Override
        public boolean contains(float x, float y) {
            float dx = x - cx;
            float dy = y - cy;
            return dx * dx + dy * dy <= radius * radius;  // 判断点是否在圆内
        }

        @Override
        public void move(float dx, float dy) {
            this.cx += dx;
            this.cy += dy;
        }

        // 根据拖动的距离调整圆的半径
        public void resize(float newX, float newY) {
            this.radius = (float) Math.sqrt(Math.pow(newX - cx, 2) + Math.pow(newY - cy, 2));  // 计算半径
        }

        @Override
        public void scale(float scaleFactor) {
            this.radius *= scaleFactor;  // 缩放半径
        }
    }

    // 擦除形状
    private void eraseShape(float x, float y) {
        // 遍历所有形状，检查每个形状是否包含在擦除区域内
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.contains(x, y)) {
                // 如果形状包含触摸点，则从列表中删除该形状
                shapes.remove(i);
            }
        }
    }


}