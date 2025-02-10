package in.co.gorest.grblcontroller.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;

public class GCodeDrawer {
    private List<DrawCommand> commands;
    private float minX, maxX, minY, maxY;
    private float canvasWidth, canvasHeight;
    private float maxPower, minPower;

    public GCodeDrawer(List<DrawCommand> commands, float minX, float minY, float maxX, float maxY,
                       float canvasWidth, float canvasHeight, float maxPower, float minPower) {
        this.commands = commands;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.maxPower = maxPower;
        this.minPower = minPower;
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        Path path = new Path();

        if (commands.isEmpty()) return;

        DrawCommand start = commands.get(0);
        path.moveTo(scaleX(start.x), scaleY(start.y));

        for (int i = 1; i < commands.size(); i++) {
            DrawCommand command = commands.get(i);
            paint.setColor(getColorForPower(command.power));  // 根据功率设置颜色
            path.lineTo(scaleX(command.x), scaleY(command.y));
            canvas.drawPath(path, paint);
        }
    }

    // 根据 S 值设置颜色，使用渐变映射
    private int getColorForPower(float power) {
        // 如果功率小于最大功率的一半，则用灰色（低功率）
        if (power < maxPower / 2) {
            int colorValue = (int) (255 * (power / (maxPower / 2)));  // 映射到 [0, 255]
            return Color.rgb(colorValue, colorValue, colorValue);  // 灰色
        } else {
            int colorValue = (int) (255 * ((power - (maxPower / 2)) / (maxPower / 2)));  // 映射到 [0, 255]
            return Color.rgb(0, 0, colorValue);  // 黑色到深红色
        }
    }

    private float scaleX(float x) {
        return (x - minX) / (maxX - minX) * canvasWidth;
    }

    private float scaleY(float y) {
        return (y - minY) / (maxY - minY) * canvasHeight;
    }
}
