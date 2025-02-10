package in.co.gorest.grblcontroller.activity;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.util.DrawCommand;
import in.co.gorest.grblcontroller.util.GCodeDrawer;
import in.co.gorest.grblcontroller.util.GCodeParser;

public class GCodeTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GCodeView(this));
    }

    private class GCodeView extends android.view.View {
        private List<DrawCommand> commands;
        float maxPower;
        float minPower;

        public GCodeView(android.content.Context context) {
            super(context);
            try {
                // 加载文件
                InputStream inputStream = getAssets().open("111.nc");
                // 解析 GCode 文件并获取最大功率值
                GCodeParser.MaxPowerResult result = GCodeParser.parseGCode(inputStream);
                commands = result.commands;
                maxPower = result.maxPower;
                minPower = result.minPower;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 使用边界值和画布大小创建绘制器
            float minX = 0, minY = 0, maxX = 21.0f, maxY = 21.0f; // 你可以根据实际文件中的边界值设置
            float canvasWidth = 800, canvasHeight = 800;

            // 创建 GCodeDrawer 实例
            GCodeDrawer drawer = new GCodeDrawer(commands, minX, minY, maxX, maxY, canvasWidth, canvasHeight, maxPower, minPower);

            drawer.draw(canvas);
        }
    }
}
