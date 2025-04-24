
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.AddDeviceAdapter;
import in.co.gorest.grblcontroller.model.Device;
import in.co.gorest.grblcontroller.util.JsonParser;

public class AddDeviceActivity extends AppCompatActivity {

    // 返回
    private ImageView ivBack;
    // Laser列表
    private RecyclerView laserRecyclerView;
    // CNC列表
    private RecyclerView cncRecyclerView;
    // Laser列表适配器
    private AddDeviceAdapter laserAdapter;
    // CNC列表适配器
    private AddDeviceAdapter cncAdapter;

    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 绑定视图
        DataBindingUtil.setContentView(this, R.layout.activity_add_device);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // Laser列表
        laserRecyclerView = findViewById(R.id.laser_recycler_view);
        // CNC列表
        cncRecyclerView = findViewById(R.id.cnc_recycler_view);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 使用 GridLayoutManager 设置每行显示 2 个设备
        laserRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        cncRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 从文件中读取 JSON 数据
        String json = loadJSONFromAsset(this, "devices.txt");

        // 解析 JSON 数据，获取 Laser 和 CNC 设备列表
        List<Device> laserDevices = JsonParser.parseDevicesFromJson(json, "laser");
        List<Device> cncDevices = JsonParser.parseDevicesFromJson(json, "cnc");

        // 创建适配器，将设备数据传入适配器
        laserAdapter = new AddDeviceAdapter(this,laserDevices);
        cncAdapter = new AddDeviceAdapter(this,cncDevices);

        // 设置适配器到 RecyclerView
        laserRecyclerView.setAdapter(laserAdapter);
        cncRecyclerView.setAdapter(cncAdapter);
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        // 返回
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    /**
     * 从 assets 目录加载 JSON 文件
     */
    public String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename); // 打开 assets 目录中的文件
            int size = is.available(); // 获取文件大小
            byte[] buffer = new byte[size]; // 创建字节数组读取文件
            is.read(buffer); // 读取文件内容
            is.close(); // 关闭文件流
            json = new String(buffer, "UTF-8"); // 转换为 UTF-8 编码的字符串
        } catch (IOException ex) {
            ex.printStackTrace(); // 捕获文件读取错误
            return null;
        }
        return json; // 返回读取的 JSON 字符串
    }
}
