package in.co.gorest.grblcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.hailong.appupdate.AppUpdateManager;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.yalantis.ucrop.UCrop;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import in.co.gorest.grblcontroller.activity.EditActivity;
import in.co.gorest.grblcontroller.adapters.DevicePagerAdapter;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.NewVersionEvent;
import in.co.gorest.grblcontroller.fragment.HomeFragment;
import in.co.gorest.grblcontroller.fragment.SettingFragment;
import in.co.gorest.grblcontroller.model.StaModelConfig;
import in.co.gorest.grblcontroller.model.WifiNetwork;
import in.co.gorest.grblcontroller.util.FileUtil;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.WebSocketManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("CustomSplashScreen")
public class MainActivity extends BaseActivity {

    /**
     * 用于日志记录的标签
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * WifiManager 用来管理 Wi-Fi 连接和扫描
     */
    private WifiManager wifiManager;
    /**
     * fragment数组
     */
    private ArrayList<Fragment> fragments = new ArrayList<>();
    /**
     * MainViewPagerAdapter
     */
    private ViewPagerAdapter adapter;
    /**
     * ViewPager
     */
    private ViewPager2 mainViewPager;
    /**
     * 雕刻
     */
    private LinearLayout mainHome;
    /**
     * 控制
     */
    private LinearLayout mainSettings;
    /**
     * 是否设置电池优化
     */
    private boolean isShowCheckPowerDialog = false;

    /**
     * 更新内容
     */
    private static String[] arrayContent = new String[]{""};
    /**
     * 服务器版本号
     */
    private int serverVersionCode;
    /**
     * 服务器版本名
     */
    private String serverVersionName;
    /**
     * 当前版本
     */
    private int currentVersionCode;

    /**
     * Wi-Fi 网络的信息
     */
    private List<WifiNetwork> wifiNetworkList = new ArrayList<>();


    /**
     * 注册权限请求结果的回调
     */
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    /**
     *  启用矢量图支持，确保在应用中可以正确显示矢量图形
     */
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定视图
        DataBindingUtil.setContentView(this, R.layout.activity_main);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 申请权限
        requestPermissions();
        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化点击事件
        initListeners();

        // 注册EventBus
        EventBus.getDefault().register(this);

    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        // 注销广播接收器，避免内存泄漏
//        unregisterReceiver(wifiScanReceiver);
//    }


    @Override
    protected void onResume() {
        super.onResume();
        // TODO 替换WebSocket
        WebSocketManager webSocketManager = WebSocketManager.getInstance();
        boolean isConnect = webSocketManager.isConnected();
        Log.d(TAG, "isConnect=" + isConnect);
    }


    /**
     * 申请权限
     */
    private void requestPermissions() {
        List<String> requiredPermissions = new ArrayList<>();

        // 读写权限, Android 13 及以上版本跳转到系统文件访问页面，手动赋予
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Environment.isExternalStorageManager()) {
                // 跳转到系统文件访问页面，手动赋予
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        } else {
            // 读写权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // 蓝牙权限，Android 12 及以上版本需要请求 BLUETOOTH_SCAN 和 BLUETOOTH_CONNECT 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 请求 BLUETOOTH_SCAN 权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            // 请求 BLUETOOTH_CONNECT 权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            // 旧版蓝牙权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH);
            }
        }

        // WiFi 权限，Android 13 及以上版本需要请求 NEARBY_WIFI_DEVICES 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 请求 NEARBY_WIFI_DEVICES 权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        } else {
            // 旧版 WiFi 权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.CHANGE_WIFI_STATE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.CHANGE_NETWORK_STATE);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.CAMERA);
        }

        // 申请所有必要的权限
        if (!requiredPermissions.isEmpty()) {
            requestPermissionsLauncher.launch(requiredPermissions.toArray(new String[0]));
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 分页
        mainViewPager = findViewById(R.id.main_view_pager);
        // 首页
        mainHome = findViewById(R.id.main_home);
        // 设置
        mainSettings = findViewById(R.id.main_setting);
    }

    /**
     * 初始化数据
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initData() {
        // 添加数据源
        fragments.add(new HomeFragment());
        fragments.add(new SettingFragment());
        // 适配器
        adapter = new ViewPagerAdapter(this, fragments);
        // 绑定适配器
        mainViewPager.setAdapter(adapter);
        mainViewPager.setUserInputEnabled(false);
        mainViewPager.setOffscreenPageLimit(3);
        mainViewPager.setCurrentItem(0);
        // 设置选中项
        mainHome.setSelected(true);

        //  电池优化
        isShowCheckPowerDialog = sharedPref.getBoolean(getString(R.string.preference_show_check_power_dialog), false);
        if (!isShowCheckPowerDialog) {
            checkPowerManagement();
        }

        // 获取服务器版本信息
        fetchVersionInfo();

        // 自动搜索设备
//        scanDevice();


    }

    /**
     * 初始化点击事件
     */
    private void initListeners() {
        // 首页
        mainHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainViewPager.setCurrentItem(0);
                mainHome.setSelected(true);
                mainSettings.setSelected(false);
            }
        });

        // 设置
        mainSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainViewPager.setCurrentItem(1);
                mainHome.setSelected(false);
                mainSettings.setSelected(true);
            }
        });
    }

    /**
     * 处理权限请求结果
     */
    private void onPermissionsResult(@NonNull Map<String, Boolean> permissions) {
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String permission = entry.getKey();
            Boolean isGranted = entry.getValue();
            if (Boolean.TRUE.equals(isGranted)) {
                onPermissionGranted(permission);
            } else {
                onPermissionDenied(permission);
            }
        }
    }

    /**
     * 权限被授予
     *
     * @param permission 权限
     */
    private void onPermissionGranted(String permission) {
        // 处理权限被授予后的逻辑
        Log.d(TAG, permission + " 权限已授予");
    }

    /**
     * 权限被拒绝
     *
     * @param permission 权限
     */
    private void onPermissionDenied(String permission) {
        // 处理权限被拒绝后的逻辑
        Toast.makeText(this, permission + " 权限被拒绝", Toast.LENGTH_SHORT).show();

        // 跳转到系统设置页面
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * 电池优化弹窗
     */
    private void checkPowerManagement() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                BaseDialog.showCustomDialog(this,
                        "温馨提示", "电池优化警告\r\n\r\n为了确保此应用程序能稳定长时间运行，请关闭此应用程序的Android电池优化\r\n\r\n取消后可在设置页面进行设置",
                        "确定", "取消",
                        v -> {
                            try {
                                Intent myIntent = new Intent();
                                myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                startActivity(myIntent);
                            } catch (RuntimeException ignored) {
                            }

                            sharedPref.edit().putBoolean(getString(R.string.preference_show_check_power_dialog), true).apply();

                        },
                        v -> {
                            sharedPref.edit().putBoolean(getString(R.string.preference_show_check_power_dialog), true).apply();
                        });
            }
        }
    }

    /**
     * 结果回调
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri destinationUri = getImageOutputUri();
            if (requestCode == ImgUtil.CHOOSE_PHOTO) {
                Uri selectedImageUri = data.getData();
                UCrop.of(selectedImageUri, destinationUri)
                        .start(this);
            } else if (requestCode == ImgUtil.TAKE_PHOTO) {
                UCrop.of(ImgUtil.imageUri, destinationUri)
                        .start(this);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", resultUri);
                intent.putExtra("businessType", 1);
                startActivity(intent);
            }

        }
    }

    /**
     * 读取 versionInfo 模拟接口
     */
    public void fetchVersionInfo() {
        OkHttpClient client = new OkHttpClient();

        String url = "http://47.243.173.178/version-info.txt"; // 接口地址
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // 使用 UTF-8 编码读取文件内容
                        String versionInfo = new String(response.body().bytes(), StandardCharsets.UTF_8);
                        // 处理 versionInfo
                        parseVersionInfo(versionInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 处理 versionInfo
     *
     * @param content 内容
     */
    private void parseVersionInfo(String content) {
        String[] lines = content.split("\n");
        String versionCode = lines[0].split("=")[1].trim();
        String versionName = lines[1].split("=")[1].trim();
        String versionContent = lines[2].split("=")[1].trim();
        // 1. 去掉首尾的中括号（即删除第一个和最后一个字符）
        versionContent = versionContent.substring(1, versionContent.length() - 1).trim();

        // 2. 按照逗号分割字符串
        String[] contentArray = versionContent.split(",");

        // 3. 去掉引号并将处理后的内容添加到一个 ArrayList 中
        ArrayList<String> arrayList = new ArrayList<>();
        for (String item : contentArray) {
            // 去掉每个元素的引号并添加到 ArrayList
            arrayList.add(item.replace("\"", "").trim());
        }

        // 在这里根据 versionCode, versionName 和 versionContent 进行版本检查等逻辑
        Log.d(TAG, "Version Code: " + versionCode);
        Log.d(TAG, "Version Name: " + versionName);
        Log.d(TAG, "Content: " + versionContent);

        serverVersionCode = Integer.parseInt(versionCode);
        serverVersionName = versionName;
        // 4. 将 ArrayList 转换为 String[] 并赋值给 arrayContent
        arrayContent = arrayList.toArray(new String[0]);

        // 获取当前版本
        getCurrentAppVersion();

    }

    /**
     * 获取当前应用的 versionCode 和 versionName
     */
    private void getCurrentAppVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            currentVersionCode = packageInfo.versionCode;
            String currentVersionName = packageInfo.versionName;

            // 打印当前应用的 versionCode 和 versionName
            Log.d(TAG, "Current versionCode: " + currentVersionCode);
            Log.d(TAG, "Current versionName: " + currentVersionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // 获取当前日期并转换为yyyyMMdd格式
        String formattedDate = getCurrentDate();
        Log.d(TAG, "CurrentDate：" + formattedDate);  // 输出当前日期

        // 版本对比
        if (serverVersionCode > currentVersionCode) {
            // TODO 通知有新版本

            EventBus.getDefault().post(new NewVersionEvent("new"));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppUpdateManager.Builder builder = new AppUpdateManager.Builder(MainActivity.this);
                            builder.apkUrl("http://47.243.173.178/apk/iklestar-" + serverVersionName + "-" + formattedDate + ".apk")
                                    .updateContent(arrayContent)
                                    .updateForce(false)
                                    .build();
                        }
                    });
                }
            }).start();

        } else {
            Toast.makeText(MainActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取当前日期并格式化为yyyyMMdd
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();  // 获取当前日期
        return sdf.format(date);  // 返回格式化后的日期
    }

    /**
     * 输出裁剪的图片文件路径
     *
     * @return 图片文件路径
     */
    private Uri getImageOutputUri() {
        File file = new File(getExternalCacheDir(), "cropped_image.jpg"); // 指定输出文件路径
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
    }


    /**
     * 扫描设备
     */
    private void scanDevice() {
        // 获取系统的 WifiManager 实例
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Log.d(TAG, "wifi是否可用：" + wifiManager.isWifiEnabled());
        // 检查 Wi-Fi 是否已启用，如果没有启用，则启用 Wi-Fi
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);  // 启用 Wi-Fi
        }

        // 检查必要的权限
        if (checkPermissions()) {
            // 权限已授权，执行 Wi-Fi 扫描
            // 注册接收器
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(wifiScanReceiver, filter);
            // 开始搜索WIFI
            wifiManager.startScan();
        } else {
            // 权限未授权，向用户请求权限
            requestPermissionsForWiFi();
        }
    }

    /**
     * 权限检测
     */
    private boolean checkPermissions() {
        // 检查 Wi-Fi 和位置权限
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * 请求权限
     */
    private void requestPermissionsForWiFi() {
        // 请求必要的权限
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                1002); // 请求码，可以自定义
    }

    /**
     * 权限请求结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            // 判断权限请求结果
            if (grantResults.length > 0) {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    // 权限已授予，执行 Wi-Fi 扫描
                    Log.d(TAG, "权限已授予");
                    // 获取系统的 WifiManager 实例
                    wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    Log.d(TAG, "wifi是否可用：" + wifiManager.isWifiEnabled());
                    // 检查 Wi-Fi 是否已启用，如果没有启用，则启用 Wi-Fi
                    if (!wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(true);  // 启用 Wi-Fi
                    }

                    // 检查必要的权限
                    if (checkPermissions()) {
                        // 权限已授权，执行 Wi-Fi 扫描
                        // 注册接收器
                        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        registerReceiver(wifiScanReceiver, filter);
                        // 开始搜索WIFI
                        wifiManager.startScan();
                    } else {
                        // 权限未授权，向用户请求权限
                        requestPermissionsForWiFi();
                    }
                } else {
                    // 权限未授予，向用户显示提示
                    Log.d(TAG, "权限未授予");
                    Toast.makeText(this, "需要 Wi-Fi 和位置权限才能扫描设备", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * 广播接收器，用于接收扫描结果
     */
    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 获取扫描结果，这里返回一个 List<ScanResult>，每个 ScanResult 代表一个 Wi-Fi 网络
            @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();

            // 检查当前 Wi-Fi 连接状态
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null) {
                String currentSSID = wifiInfo.getSSID().replace("\"", ""); // 移除引号
                Log.d(TAG, "当前已连接的 Wi-Fi SSID: " + currentSSID);
            }

            // 清空旧的数据
            wifiNetworkList.clear();

            // 遍历扫描结果，检查每个网络的 SSID 是否包含 "MSK"
            for (ScanResult result : scanResults) {
                // 搜索AP模式的机器
                if (result.SSID.startsWith("Laser") || result.SSID.startsWith("CNC")) {  // 如果 SSID 包含 "Laser" 或 "CNC"
                    // 构造 WifiNetwork 对象，并添加到列表
                    String ipAddress = "192.168.4.1";  // 这里假设 IP 地址为默认值
                    wifiNetworkList.add(new WifiNetwork(result.SSID, ipAddress, "AP"));
                }
            }

            // 搜索STA模式的机器
            // TODO 第一步：获取配置列表，查看是否存在配置项，如果无配置项则直接return
            // 创建路径
            File directory = new File(GrblController.getInstance().getExternalFilesDir(null) + "/config");
            if (!directory.exists()) directory.mkdirs();
            File file = new File(directory, "sta_model_config.json");
            // 读取已有记录
            List<StaModelConfig> staConfigList = FileUtil.readStaModelConfigList(file);
            if (staConfigList == null || staConfigList.isEmpty()) {
                Log.d(TAG, "无 STA 模式配置，跳过 STA 扫描，扫描当前连接的网络");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                // 子线程中处理 ping 检测
                executor.execute(() -> {
                    try {
                        String subnet = getLocalSubnet(); // 例如返回 192.168.1
                        // 扫描指定的IP网段
                        scanIP(subnet, "Laser-T4", "STA");
                    } catch (Exception e) {
                        Log.e(TAG, "Ping 扫描失败", e);
                    }
                });
            } else {
                // TODO 第二步：获取配置项中保存的Wi-Fi名字，并且根据名字获取此WiFi下的所有局域网设备名称及IP地址
                ExecutorService executor = Executors.newSingleThreadExecutor();
                for (StaModelConfig config : staConfigList) {
                    String targetSSID = config.getConfigSSID();
                    String targetMachineName = config.getMachineName();
                    String targetMode = config.getMode();
                    if (TextUtils.isEmpty(targetSSID)) continue;

                    for (ScanResult results : scanResults) {
                        if (results.SSID.equals(targetSSID)) {
                            // 子线程中处理 ping 检测
                            executor.execute(() -> {
                                try {
                                    String subnet = getLocalSubnet(); // 例如返回 192.168.1
                                    // 扫描指定的IP网段
                                    scanIP(subnet, targetMachineName, targetMode);
                                } catch (Exception e) {
                                    Log.e(TAG, "Ping 扫描失败", e);
                                }
                            });
                            break; // 找到匹配的 SSID 就跳出当前 config 的扫描
                        }
                    }
                }
            }


            if (!wifiNetworkList.isEmpty()) {
                runOnUiThread(() -> showDeviceDialog(wifiNetworkList));
            }

        }
    };

    /**
     * 获取本地子网IP段
     *
     * @return
     */
    private String getLocalSubnet() {
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        return (ip & 0xff) + "." + ((ip >> 8) & 0xff) + "." + ((ip >> 16) & 0xff);
    }

    /**
     * 扫描指定的网段
     *
     * @param ip 指定的网段
     */
    private void scanIP(String ip, String machineName, String mode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanNetworkInParallel(ip, machineName, mode);
            }
        }).start();
    }

    /**
     * 创建线程池并分段扫描
     */
    public void scanNetworkInParallel(String subnet, String machineName,String mode) {
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

            executor.submit(() -> scanNetwork(subnet, segmentStart, segmentEnd, machineName, mode)); // 提交扫描任务
        }

        // 关闭线程池
        executor.shutdown();
    }

    /**
     * 扫描指定子网范围的IP
     */
    public void scanNetwork(String subnet, int start, int end, String machineName, String mode) {
        for (int i = start; i <= end; i++) {
            String host = subnet + "." + i;
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address.isReachable(500)) { // 超时设置为 500ms
                    Log.d(TAG, "Device Found: " + host + " - " + address.getHostName());
                    if (address.getHostName().startsWith("esp") ||
                            address.getHostName().startsWith("grbl") ||
                            address.getHostName().startsWith("mks")) {

                        // TODO 添加MAC地址判断
                        WifiNetwork newNetwork = new WifiNetwork(machineName, host, mode);

                        // 添加设备数据 + 通知 UI 刷新（主线程执行）
                        new Handler(Looper.getMainLooper()).post(() -> {
                            wifiNetworkList.add(newNetwork);
//                            deviceAdapter.notifyDataSetChanged();  // 刷新 RecyclerView
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showDeviceDialog(List<WifiNetwork> deviceList) {
        // 创建 Dialog
        Dialog dialog = new Dialog(this, R.style.CustomDialog); // 使用自定义样式
        dialog.setContentView(R.layout.fragment_device_carousel_bottom_sheet);

        // 获取弹窗布局中的根容器
        ViewGroup container = dialog.findViewById(android.R.id.content);  // 根容器

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }

        // ViewPager
        ViewPager2 deviceViewPager = dialog.findViewById(R.id.deviceViewPager);
        // 指示器
        DotsIndicator dotsIndicator = dialog.findViewById(R.id.dots_indicator);
        DevicePagerAdapter adapter = new DevicePagerAdapter(this, deviceList, new DevicePagerAdapter.OnDevicePagerClickLitener() {
            @Override
            public void onIgnore() {
                dialog.dismiss();
            }

            @Override
            public void onConnectClick(String connectType, String machineName, String wifiName, String ipAddress) {
                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent(connectType, machineName, wifiName, ipAddress));
//                new Thread(() -> {
//
//                }).start();

            }
        });
        deviceViewPager.setAdapter(adapter);
        dotsIndicator.setViewPager2(deviceViewPager);
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.95), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
    }
}
