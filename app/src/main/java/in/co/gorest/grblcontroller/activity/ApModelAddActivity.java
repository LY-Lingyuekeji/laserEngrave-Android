
package in.co.gorest.grblcontroller.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class ApModelAddActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = ApModelAddActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
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

        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));

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
            String ssid = wifiInfo.getSSID().replaceAll("^\"|\"$", ""); // 移除前后引号;
            Log.d(TAG, "ssid" + ssid);

            // 检查SSID是否包含 "Laser"
            if (ssid != null && ssid.contains("Laser") || ssid != null && ssid.contains("CNC")) {
                Log.d(TAG, "Connected to Laser network");
                // TODO 自定义弹窗
                showDeviceDialog(ssid);
            } else {
                // 创建自定义弹窗对象
                BaseAlertDialog baseAlertDialog = new BaseAlertDialog(ApModelAddActivity.this);

                // 显示弹窗并传入标题、内容以及确认按钮的点击事件
                baseAlertDialog.show("温馨提示", "当前并未连接至\"Laser_XXX\"的热点，请检查后重试！", new View.OnClickListener() {
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


    // 显示自定义 Dialog
    private void showDeviceDialog(String ssid) {
        // 创建 Dialog
        Dialog dialog = new Dialog(this, R.style.CustomDialog); // 使用自定义样式
        dialog.setContentView(R.layout.dialog_device);

        // 获取弹窗布局中的根容器
        ViewGroup container = dialog.findViewById(android.R.id.content);  // 根容器

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 初始化激光模组数据
        List<String> laserModuleDataList = new ArrayList<>();
        laserModuleDataList.add("请选择");
        laserModuleDataList.add("LdT-3W");
        laserModuleDataList.add("LdT4-10W");
        laserModuleDataList.add("LdT4-20W");


        // 初始化主轴电机数据
        List<String> cncModuleDataList = new ArrayList<>();
        cncModuleDataList.add("请选择");
        cncModuleDataList.add("Cd-100W");
        cncModuleDataList.add("Cd-150W");
        cncModuleDataList.add("Cd-500W");


        // 设置 Dialog 属性
        TextView tvMachineName = dialog.findViewById(R.id.tv_machine_name);
        TextView tvMachineStatus = dialog.findViewById(R.id.tv_machine_status);
        ImageView ivMachineImage = dialog.findViewById(R.id.iv_machine_image);
        TextView tvMachineSize = dialog.findViewById(R.id.tv_machine_size);
        TextView tvMachineFirmware = dialog.findViewById(R.id.tv_machine_firmware);
        ImageView ivMachineModuleIcon = dialog.findViewById(R.id.iv_module_icon);
        TextView tvLaserModule = dialog.findViewById(R.id.tv_laser_module);
        TextView tvMachineSdCard = dialog.findViewById(R.id.tv_machine_sd);
        TextView tvComponentSize = dialog.findViewById(R.id.tv_component_size);
        TextView tvMachineModuleText = dialog.findViewById(R.id.tv_module_text);
        Spinner spinnerLaserModule  = dialog.findViewById(R.id.spinner_laser_module);
        TextView tvConfirm = dialog.findViewById(R.id.tv_confirm);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);

        // 设置内容
        tvMachineName.setText(ssid);
        // 设置连接状态
        tvMachineStatus.setText("可连接");
        // 设置图片
        if (ssid.contains("Laser")) {
            // 设置模组图标为激光
            Glide.with(this).load(R.drawable.icon_laser).into(ivMachineModuleIcon);
            // 设置机器图片
            if (ssid.contains("T2020")) {
                // 设置激光雕刻机 T2020图片
                Glide.with(this).load(R.mipmap.ic_laser_t2020).into(ivMachineImage);
                // 设置激光雕刻机 T2020行程
                tvMachineSize.setText("200x200(mm²)");
                tvComponentSize.setText("200x200(mm²)");
                // 保存激光雕刻机 T2020行程
                sharedPref.edit().putInt(getString(R.string.preference_machine_width), 200).apply();
                sharedPref.edit().putInt(getString(R.string.preference_machine_height), 200).apply();

            } else {
                // 设置激光雕刻机 T4图片
                Glide.with(this).load(R.mipmap.ic_laser_t4).into(ivMachineImage);
                // 设置激光雕刻机 T4行程
                tvMachineSize.setText("300x300(mm²)");
                tvComponentSize.setText("300x300(mm²)");
                // 保存激光雕刻机 T4行程
                sharedPref.edit().putInt(getString(R.string.preference_machine_width), 300).apply();
                sharedPref.edit().putInt(getString(R.string.preference_machine_height), 300).apply();
            }
            // 设置模组名称为激光模组
            tvMachineModuleText.setText("激光模组");

            // 创建 ArrayAdapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, laserModuleDataList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // 设置适配器
            spinnerLaserModule.setAdapter(adapter);
        } else {
            // 设置模组图标为CNC
            Glide.with(this).load(R.drawable.icon_cnc).into(ivMachineModuleIcon);
            // 设置机器图片
            if (ssid.contains("3018MAX")) {
                // 设置CNC雕刻机 3018MAX图片
                Glide.with(this).load(R.mipmap.ic_cnc_3018max).into(ivMachineImage);
                // 设置CNC雕刻机 3018MAX行程
                tvMachineSize.setText("300x180x45(mm²)");
                tvComponentSize.setText("300x180x45(mm²)");
                // 保存CNC雕刻机 3018MAX行程
                sharedPref.edit().putInt(getString(R.string.preference_machine_width_cnc), 300).apply();
                sharedPref.edit().putInt(getString(R.string.preference_machine_height_cnc), 180).apply();
            } else if (ssid.contains("3018PRO")){
                // 设置CNC雕刻机 3018PRO图片
                Glide.with(this).load(R.mipmap.ic_cnc_3018pro).into(ivMachineImage);
                // 设置CNC雕刻机 3018PRO行程
                tvMachineSize.setText("300x180x45(mm²)");
                tvComponentSize.setText("300x180x45(mm²)");
                // 保存CNC雕刻机 3018PRO行程
                sharedPref.edit().putInt(getString(R.string.preference_machine_width_cnc), 300).apply();
                sharedPref.edit().putInt(getString(R.string.preference_machine_height_cnc), 180).apply();
            } else {
                // 设置CNC雕刻机 3020PLUS图片
                Glide.with(this).load(R.mipmap.ic_cnc_3020plus).into(ivMachineImage);
                // 设置CNC雕刻机 3020PLUS行程
                tvMachineSize.setText("300x200x73(mm²)");
                tvComponentSize.setText("300x200x73(mm²)");
                // 保存CNC雕刻机 3020PLUS行程
                sharedPref.edit().putInt(getString(R.string.preference_machine_width_cnc), 300).apply();
                sharedPref.edit().putInt(getString(R.string.preference_machine_height_cnc), 200).apply();
            }
            // 设置模组名称为主轴电机
            tvMachineModuleText.setText("主轴电机");
            // 创建 ArrayAdapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cncModuleDataList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // 设置适配器
            spinnerLaserModule.setAdapter(adapter);
        }
        // 设置机器芯片
        tvMachineFirmware.setText("ESP_S3");


        // 设置 Spinner 的监听器
        spinnerLaserModule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 当选中一个项时，更新 tvLaserModule 的文本
                String selectedModule = (String) parentView.getItemAtPosition(position);
                if (spinnerLaserModule.getSelectedItemPosition() == 0) {
                    tvLaserModule.setText("未知");
                } else {
                    tvLaserModule.setText(selectedModule);
                }
                // 保存激光模组
                sharedPref.edit().putString(getString(R.string.preference_laser_module), selectedModule).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 没有选择任何项时的操作（可选）
            }
        });

        // 按钮点击事件
        tvConfirm.setOnClickListener(v -> {
            // TODO: 执行连接逻辑
            if (spinnerLaserModule.getSelectedItemPosition() == 0) {
                // 如果选择了 "请选择"，执行晃动效果
                shakeView(spinnerLaserModule);
                Toast.makeText(this, "请选择设置模组型号", Toast.LENGTH_SHORT).show();
                return;
            }
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                // 创建自定义弹窗对象
                BaseAlertDialog baseAlertDialog = new BaseAlertDialog(this);

                // 显示弹窗并传入标题、内容以及确认按钮的点击事件
                baseAlertDialog.show("温馨提示", "当前检测到Wi-Fi开关暂未打开，请手动打开Wi-Fi后重试", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 点击确认按钮后执行的操作
                        Log.d(TAG, "用户点击了确认按钮");
                    }
                });
            } else {

                // 获取当前 Wi-Fi 网络的 SSID 和密码
                Log.d(TAG, "ssid=" + ssid);
                String password = "12345678"; //
                // 连接到 Wi-Fi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    connectToWifiForAndroidQ(this, ssid, password);
                } else {
                    connectToWifi(this, ssid, password);
                }

                dialog.dismiss();
            }




        });
        tvCancel.setOnClickListener(v -> dialog.dismiss());

        // 显示 Dialog
        dialog.show();

    }


    /**
     * 抖动动画
     * @param view 抖动的视图
     */
    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 30f, -30f, 30f, -30f, 0f);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }


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
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = Formatter.formatIpAddress(ipAddress);
                Log.d(TAG, "Connected Wi-Fi IP Address: " + ip);
                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent("AP", ssid, ip.substring(0, ip.lastIndexOf('.') + 1) + "1" ));

                finish();
            }

            @Override
            public void onUnavailable() {
                // Connection failed
                Toast.makeText(context, "连接失败，请重新连接！", Toast.LENGTH_SHORT).show();

            }
        });
    }


    /**
     * 通过SSID和密码连接WIFI
     *
     * @param context  上下文
     * @param ssid     SSID
     * @param password 密码A
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
                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent("AP", ssid, ip));

                finish();
            }

            @Override
            public void onLost(Network network) {
                // Network is lost
                super.onLost(network);
                connectivityManager.unregisterNetworkCallback(this);

                // Connection failed
                Toast.makeText(context, "连接失败，请重新连接！", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
