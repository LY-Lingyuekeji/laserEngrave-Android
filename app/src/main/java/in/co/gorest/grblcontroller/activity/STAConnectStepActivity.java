
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.baoyachi.stepview.VerticalStepView;
import com.baoyachi.stepview.bean.StepBean;
import com.king.view.circleprogressview.CircleProgressView;

import org.greenrobot.eventbus.EventBus;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.FlowViewVertical;

public class STAConnectStepActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = STAModelActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 用于管理Wi-Fi状态的WifiManager
    private WifiManager wifiManager;
    // SSID
    private static String ssid;
    // password
    private String password;
    // MAC地址
    private String macAddress;

    // 返回
    private ImageView ivBack;
    // 圆形进度动画控件
    private CircleProgressView staStpeCpv;

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
        DataBindingUtil.setContentView(this, R.layout.activity_sta_connect_step);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));

        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 注册广播
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 注销广播
        unregisterReceiver(wifiScanReceiver);
    }


    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 圆形进度动画控件
        staStpeCpv = findViewById(R.id.sta_stpe_cpv);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 管理Wi-Fi状态的WifiManager
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // 获取传递的参数
        if (getIntent() != null) {
            // ssid
            ssid = getIntent().getStringExtra("ssid");
            // password
            password = getIntent().getStringExtra("password");
            // macAddress
            macAddress = getIntent().getStringExtra("macAddress");

            Log.d(TAG, "ssid=" + ssid + "------password=" + password + "-----macAddress=" + macAddress);

        }


//        //显示进度动画，进度，动画时长
//        staStpeCpv.showAnimation(100,3000);
//        //设置当前进度
//        staStpeCpv.setProgress(100);
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
     * WIFI扫描广播
     */
    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            @SuppressLint("MissingPermission") List<ScanResult> results = wifiManager.getScanResults();
            for (ScanResult scanResult : results) {
                if (scanResult.frequency > 2400 && scanResult.frequency < 2500 && !scanResult.SSID.isEmpty()) {
                   Log.d(TAG, "scanResult SSID =" + scanResult.SSID);
                   // TODO 扫描WiFi
                   if (scanResult.SSID.contains(ssid)) {
                       // TODO 通过SSID和password连接配置的WiFi
                       if (!TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(password)) {
                           Log.d(TAG, "SSID=" + ssid + "----- Password=" + password);

                           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                               connectToWifiForAndroidQ(STAConnectStepActivity.this, ssid, password);
                           } else {
                               connectToWifi(STAConnectStepActivity.this, ssid, password);
                           }
                       }
                   }
                }
            }
        }
    };


    /**
     * 通过SSID和密码连接WIFI（适用于Android版本大于Android Q）
     *
     * @param context  上下文
     * @param ssid     SSID
     * @param password 密码
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectToWifiForAndroidQ(Context context, String ssid, String password) {
        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
        builder.setSsid(ssid);
        builder.setWpa2Passphrase(password); // WPA2 passphrase

        WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

        NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);

        NetworkRequest networkRequest = networkRequestBuilder.build();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Connected to the network
                connectivityManager.bindProcessToNetwork(network); // This line sets the network for all outgoing data

                // 获取IP
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = Formatter.formatIpAddress(ipAddress);
                Log.d(TAG, "Connected Wi-Fi IP Address: " + ip);
                // 移除IP地址最后一个点后的数字
                String modifiedIp = removeLastSegment(ip);
                Log.d(TAG, "modifiedIp: " + modifiedIp);
                // 扫描指定的IP网段
                scanIP(modifiedIp);
            }

            @Override
            public void onUnavailable() {
                // Connection failed
                Toast.makeText(STAConnectStepActivity.this, "连接失败，请重新连接！", Toast.LENGTH_SHORT).show();

            }
        });
    }


    /**
     * 通过SSID和密码连接WIFI
     *
     * @param context  上下文
     * @param ssid     SSID
     * @param password 密码
     */
    public void connectToWifi(Context context, String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid); // Quotes are required
        wifiConfig.preSharedKey = String.format("\"%s\"", password); // Quotes are required for the password

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Add the new configuration to the system
        int netId = wifiManager.addNetwork(wifiConfig);

        // Enable the network and attempt to connect
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        // Register a network callback to listen for network connection status
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Network is available
                super.onAvailable(network);
                connectivityManager.unregisterNetworkCallback(this);

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = Formatter.formatIpAddress(ipAddress);
                Log.d(TAG, "Connected Wi-Fi IP Address: " + ip);
                // 移除IP地址最后一个点后的数字
                String modifiedIp = removeLastSegment(ip);
                Log.d(TAG, "modifiedIp: " + modifiedIp);

                // 扫描指定的IP网段
                scanIP(modifiedIp);
            }

            @Override
            public void onLost(Network network) {
                // Network is lost
                super.onLost(network);
                connectivityManager.unregisterNetworkCallback(this);

                // Connection failed
                Toast.makeText(STAConnectStepActivity.this, "连接失败，请重新连接！", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 移除IP地址最后一个(.)后的数字
     * @param ipAddress IP地址
     * @return 格式化后的IP字符串
     */
    private String removeLastSegment(String ipAddress) {
        // 使用点（.）作为分隔符分割IP地址
        String[] parts = ipAddress.split("\\.");
        // 如果IP地址格式正确，返回除了最后一部分之外的字符串
        // 使用StringBuilder来构建新的IP地址字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 2) { // 在最后一部分之前添加点（.）
                sb.append(".");
            }
        }
        return sb.toString();
    }

    /**
     * 扫描指定的网段
     * @param ip 指定的网段
     */
    private void scanIP(String ip) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanNetworkInParallel(ip);
            }
        }).start();
    }

    /**
     * 创建线程池并分段扫描
     */
    public void scanNetworkInParallel(String subnet) {
        ExecutorService executor = Executors.newFixedThreadPool(16); // 创建 16 个线程的线程池

        // 为每个线程分配扫描任务
        int segmentSize = 255 / 16; // 每个线程扫描的 IP 段大小

        for (int i = 0; i < 16; i++) {
            int start = i * segmentSize + 1; // 计算每个线程的起始 IP
            int end = (i + 1) * segmentSize; // 计算每个线程的结束 IP

            // 处理最后一个线程，确保覆盖到 255
            if (i == 15) {
                end = 255;
            }

            // 为每个段提交一个任务
            final int segmentStart = start;
            final int segmentEnd = end;

            executor.submit(() -> scanNetwork(subnet, segmentStart, segmentEnd)); // 提交扫描任务
        }

        // 关闭线程池
        executor.shutdown();
    }

    /**
     * 扫描指定子网范围的IP
     */
    public void scanNetwork(String subnet, int start, int end) {
        for (int i = start; i <= end; i++) {
            String host = subnet + "." + i;
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address.isReachable(500)) { // 超时设置为 500ms
                    Log.d(TAG, "Device Found: " + host + " - " + address.getHostName());
                    macAddress = macAddress.substring(macAddress.length() - 6);
                    // 只保留后四位
                    if (address.getHostName().contains(macAddress) || address.getHostName().toString().contains("mks_grbl")) {
                        // 连接Telnet
                        EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, host));
                        // 保存ssid 和 host
                        sharedPref.edit().putString(getString(R.string.preference_sta_ssid), ssid).apply();
                        sharedPref.edit().putString(getString(R.string.preference_sta_host), host).apply();

                        //重启应用
                        Intent launchIntent = getApplication().getPackageManager().getLaunchIntentForPackage(getPackageName());
                        if (launchIntent != null) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(launchIntent);
                                    //添加activity切换动画效果
                                    overridePendingTransition(0, 0);
                                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                                    am.killBackgroundProcesses(getPackageName());
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(0);
                                    finish();
                                }
                            }, 400);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
