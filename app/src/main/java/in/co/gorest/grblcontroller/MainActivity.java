package in.co.gorest.grblcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.hailong.appupdate.AppUpdateManager;
import com.yalantis.ucrop.UCrop;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import in.co.gorest.grblcontroller.activity.EditActivity;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.NewVersionEvent;
import in.co.gorest.grblcontroller.fragment.HomeFragment;
import in.co.gorest.grblcontroller.fragment.SettingFragment;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.NettyClient;
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
     * wifiManager
     */
    private WifiManager wifiManager = null;

    /**
     * 是否设置电池优化
     */
    private boolean isShowCheckPowerDialog = false;

    /**
     * 是否显示雕刻弹窗
     */
    private boolean isShowEngravingDialog = false;

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


    @Override
    protected void onResume() {
        super.onResume();
        boolean isConnect = NettyClient.getInstance(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                return false;
            }
        })).getConnectStatus();
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

        // 自动连接
//        boolean isAutoConnect = sharedPref.getBoolean(getString(R.string.preference_auto_connect), false);
//        if (isAutoConnect) {
//            String ssidStr = sharedPref.getString(getString(R.string.preference_wifi_ssid), "");
//            if (ssidStr != null || !ssidStr.isEmpty()) {
//                Log.d(TAG, "SSID=" + ssidStr);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    connectToWifiForAndroidQ(this, ssidStr, "12345678");
//                } else {
//                    connectToWifi(this, ssidStr, "12345678");
//                }
//            }
//        }

        // 获取服务器版本信息
        fetchVersionInfo();
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


//    /**
//     * ServiceMessageEvent
//     *
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onServiceMessageEvent(ServiceMessageEvent event) {
//        if (!event.getMessage().isEmpty() && event.getMessage().startsWith("<")) {
//            Log.d(TAG, "message=" + event.getMessage().toString());
//            String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
//            Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);
//            if (parts[0].equals(Constants.MACHINE_STATUS_RUN)) {
//                if (!isShowEngravingDialog) {
//                    BaseDialog.showCustomDialog(this, "温馨提示",
//                            "检测到设备正在雕刻是否跳转到雕刻界面？",
//                            "跳转", "取消",
//                            v -> {
//                                Intent intent = new Intent(this, EngraveActivity.class);
//                                String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
//                                String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
//                                intent.putExtra("imagePath", imagePath);
//                                intent.putExtra("filePath", filePath);
//                                startActivity(intent);
//                            },
//                            v -> {
//                                Log.d(TAG, "用户选择取消");
//                            });
//                }
//                isShowEngravingDialog = true;
//            }
//        }
//    }

    /**
     * 输出裁剪的图片文件路径
     * @return 图片文件路径
     */
    private Uri getImageOutputUri() {
        File file = new File(getExternalCacheDir(), "cropped_image.jpg"); // 指定输出文件路径
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
    }

}
