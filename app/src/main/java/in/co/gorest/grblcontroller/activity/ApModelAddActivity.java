
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.AddDeviceAdapter;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.model.Device;
import in.co.gorest.grblcontroller.util.JsonParser;

public class ApModelAddActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = ApModelAddActivity.class.getSimpleName();
    // 获取 WifiManager 实例
    private WifiManager wifiManager;
    // 返回
    private ImageView ivBack;
    // 我已连接热点
    private TextView tvConnectedHotspot;
    // 去连接热点
    private TextView tvToConnectHotspot;


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
        DataBindingUtil.setContentView(this, R.layout.activity_ap_model_add);

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
        // 我已连接热点
        tvConnectedHotspot = findViewById(R.id.tv_connected_hotspot);
        // 去连接热点
        tvToConnectHotspot = findViewById(R.id.tv_to_connect_hotspot);

    }

    /**
     * 初始化数据
     */
    private void initData() {

        // 获取WifiManager实例
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        // 我已连接热点
        tvConnectedHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查Wi-Fi连接状态
                checkWifiConnection();
            }
        });

        // 去连接热点
        tvToConnectHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, 1000); // 使用自定义的请求码
            }
        });
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
     * 检查当前Wi-Fi连接
     */
    private void checkWifiConnection() {
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            // 检查SSID是否包含 "MKS"
            if (ssid != null && ssid.contains("MKS")) {
                Log.d(TAG, "Connected to MKS network");
                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent("AP", ssid.substring(1, ssid.length() - 1), "192.168.4.1" ));
                finish();
            } else {
                // 创建自定义弹窗对象
                BaseAlertDialog baseAlertDialog = new BaseAlertDialog(ApModelAddActivity.this);

                // 显示弹窗并传入标题、内容以及确认按钮的点击事件
                baseAlertDialog.show("温馨提示", "当前并未连接至\"MKS_XXX\"的热点，请检查后重试！", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 点击确认按钮后执行的操作
                        Log.d(TAG, "用户点击了确认按钮");
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 判断是否从 Wi-Fi 设置页面返回
        if (requestCode == 1000) { // 你可以自定义一个请求码
            // Wi-Fi 设置页面返回后进行检查
            checkWifiConnection();
        }
    }

}
