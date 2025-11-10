
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.DeviceConnectRecordAdapter;
import in.co.gorest.grblcontroller.adapters.HistoryAdapter;
import in.co.gorest.grblcontroller.model.DeviceConnectRecord;
import in.co.gorest.grblcontroller.model.EngraveHistoryRecord;
import in.co.gorest.grblcontroller.util.FileUtil;

public class DeviceConnectRecordActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = DeviceConnectRecordActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 添加
    private TextView tvAddDevice;
    // 历史记录
    private RecyclerView rvDeviceConnectHistory;
    // Laser列表适配器
    private DeviceConnectRecordAdapter adapter;

    private List<DeviceConnectRecord> recordList = new ArrayList<>();


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
        DataBindingUtil.setContentView(this, R.layout.activity_device_connect_history);

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
        // 添加
        tvAddDevice = findViewById(R.id.tv_add_device);
        // 历史记录
        rvDeviceConnectHistory = findViewById(R.id.rv_device_connect_history);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置LayoutManager
        rvDeviceConnectHistory.setLayoutManager(new LinearLayoutManager(this));
        // 加载连接记录
        loadRecords();


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

        // 添加
        tvAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeviceConnectRecordActivity.this, AddDeviceActivity.class));
            }
        });
    }

    /**
     * 加载连接记录
     */
    private void loadRecords() {
        File file = new File(GrblController.getInstance().getExternalFilesDir(null) + "/connect/", "device_record.json");
        if (!file.exists()) {
            Log.d(TAG, "文件不存在");
            return;
        }

        try {
            String json = FileUtil.readFile(file);
            Log.d(TAG, "读取到的文件内容: " + json); // 输出文件内容进行调试
            recordList = new Gson().fromJson(json, new TypeToken<List<DeviceConnectRecord>>(){}.getType());
            // 创建适配器，将设备数据传入适配器
            adapter = new DeviceConnectRecordAdapter(this, recordList);
            // 设置Adpter
            rvDeviceConnectHistory.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
