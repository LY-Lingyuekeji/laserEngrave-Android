package in.co.gorest.grblcontroller.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.WifiNetworkAdapter;
import in.co.gorest.grblcontroller.events.UiToastEvent;


public class WifiListActivity extends Activity implements WifiNetworkAdapter.OnItemClickListener {

    // TAG
    private final String TAG = WifiListActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // wifi
    private RecyclerView wifiRecyclerView;

    // 用于在 Intent 中传递或接收 Wi-Fi 网络的 IP
    public static final String EXTRA_WIFI_IP = "wifi_ip";
    // 用于在 Intent 中传递或接收 Wi-Fi 网络的 Port
    public static final String EXTRA_WIFI_PORT = "wifi_port";
    // 本地设备的 Wi-Fi 管理器
    private WifiManager wifiManager;
    // Wi-Fi 网络适配器
    private WifiNetworkAdapter wifiNetworkAdapter;

    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        setResult(Activity.RESULT_CANCELED);

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
        // Wi-Fi 列表
        wifiRecyclerView = findViewById(R.id.recycler_view_wifi);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化 Wi-Fi 列表
        wifiRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wifiNetworkAdapter = new WifiNetworkAdapter(new ArrayList<>(), this);
        wifiRecyclerView.setAdapter(wifiNetworkAdapter);
        // 获取 Wi-Fi 管理器实例
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_bluetooth_adapter_error), true, true));
            finish();
        }

        // 检查并请求 Wi-Fi 权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // 显示可用的 Wi-Fi 网络
            displayAvailableWifiNetworks();
        }
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 显示可用的 Wi-Fi 网络
     */
    @SuppressLint("MissingPermission")
    private void displayAvailableWifiNetworks() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // 注册广播接收器以监听 Wi-Fi 扫描结果
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);

        // 启动 Wi-Fi 扫描
        wifiManager.startScan();
    }


    /**
     * Wi-Fi 扫描广播接收器
     */
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();
                wifiNetworkAdapter.updateDevices(scanResults);
            } else {
                Log.e(TAG, "Failed to scan Wi-Fi networks.");
            }
        }
    };

    @Override
    public void onItemClick(ScanResult wifiNetwork) {
        // 处理点击事件
        Log.d(TAG, "SSID: " + wifiNetwork.SSID);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_WIFI_IP, "192.168.0.1");
        intent.putExtra(EXTRA_WIFI_PORT, 23);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * 权限请求结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayAvailableWifiNetworks();
            } else {
                Toast.makeText(getApplicationContext(), "请手动开启权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }
}