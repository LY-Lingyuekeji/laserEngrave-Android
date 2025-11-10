
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.Arrays;
import java.util.List;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class MachineDetailActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = MachineDetailActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 机器名称
    private TextView tvMachineDetailName;
    // 机器状态
    private TextView tvMachineDetailStatus;
    // 机器图片
    private ImageView ivMachineDetailImage;
    // 行程
    private TextView tvMachineDetailSize;
    // 芯片固件
    private TextView tvMachineDetailFirmware;
    // 模组图片
    private ImageView ivMachineDetailModuleIcon;
    // 激光模组
    private TextView tvMachineDetailLaserModule;
    // SD卡
    private TextView tvMachineDetailSd;
    // 加工类型
    private LinearLayout llMachineDetailWorkType;
    // 加工类型 RadioGroup
    private RadioGroup rgMachineDetailWorkType;
    // XYZ常规雕刻
    private RadioButton rbMachineDetailXyz;
    // YRR转轴雕刻(三档旋转轴)
    private RadioButton rbMachineDetailYyrThreelevel;
    // YRR转轴雕刻(无极旋转轴)
    private RadioButton rbMachineDetailYyrNonpolar;
    // Y轴卡盘雕刻
    private RadioButton rbMachineDetailYrc;
    // 模组切换
    private TextView tvMachineDetailModuleToggle;
    // 模组切换 spinner
    private Spinner spinnerMachineDetailLaserModule;
    // 一键抬升Z轴
    private TextView tvMachineDetailZup;
    // 火焰传感器 RelativeLayout
    private RelativeLayout rlMachineDetailFireModule;
    // 火焰传感器 Switch
    private Switch switchMachineDetailFireModule;
    // 门动传感器 RelativeLayout
    private RelativeLayout rlMachineDetailDoorModule;
    // 门动传感器 Switch
    private Switch switchMachineDetailDoorModule;
    // 倾斜检测传感器 RelativeLayout
    private RelativeLayout rlMachineDetailSlantModule;
    // 倾斜检测传感器 Switch
    private Switch switchMachineDetailSlantModule;
    // 红十字激光 RelativeLayout
    private RelativeLayout rlMachineDetailRtlaserModule;
    // 红十字激光 Switch
    private Switch switchMachineDetailRtlaserModule;
    // 照明灯 RelativeLayout
    private RelativeLayout rlMachineDetailLedModule;
    // 照明灯 Switch
    private Switch switchMachineDetailLedModule;
    // 气泵功能 RelativeLayout
    private RelativeLayout rlMachineDetailAirModule;
    // 气泵功能 Switch
    private Switch switchMachineDetailAirModule;
    // 外接屏幕功能 RelativeLayout
    private RelativeLayout rlMachineDetailExternalScreen;
    // 外接屏幕功能 Switch
    private Switch switchMachineDetailExternalScreen;
    // 锁定XYZ轴电机 RelativeLayout
    private RelativeLayout rlMachineDetailLockMotor;
    // 锁定XYZ轴电机 Switch
    private Switch switchMachineDetailLockMotor;

    // 断开连接
    private TextView tvMachineDetailDisconnect;
    // 重置设备
    private TextView tvMachineDetailReset;

    // Spinner Laser数据源
    List<String> laserOptions = Arrays.asList("LdT-3W", "LdT4-10W", "LdT4-20W", "LdT4-1064nm-2W");
    // Spinner CNC数据源
    List<String> cncOptions = Arrays.asList("Cd-100W", "Cd-150W", "Cd-500W");
    // 是否选中
    private boolean isChecked = false;

    // 需要查询的命令
    private final String[] commands = {"$I", "$45", "$103", "$43", "$48","$42", "$40", "$39", "$47", "$1"};

    // 门警告弹窗
    private Dialog dialogDoorWarning;
    // 火焰警告弹窗
    private Dialog dialogFireWarning;
    // 倾斜警告弹窗
    private Dialog dialogProbeWarning;
    // 是否震动提醒
    private boolean isOpenVibrateAlert;
    // 震动提醒持续时长
    private int vibrateAlertTime;

    private ConnectivityManager.NetworkCallback networkCallback;

    // 全局变量
    private String yrrValue = null;
    private String val103 = null;

    // WebSocket
    private WebSocketManager webSocketManager;


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
        DataBindingUtil.setContentView(this, R.layout.activity_machine_detail);

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

        // 注册EventBus
        EventBus.getDefault().register(this);

        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 机器名称
        tvMachineDetailName = findViewById(R.id.tv_machine_detail_name);
        // 机器状态
        tvMachineDetailStatus = findViewById(R.id.tv_machine_detail_status);
        // 机器图片
        ivMachineDetailImage = findViewById(R.id.iv_machine_detail_image);
        // 行程
        tvMachineDetailSize = findViewById(R.id.tv_machine_detail_size);
        // 芯片固件
        tvMachineDetailFirmware = findViewById(R.id.tv_machine_detail_firmware);
        // 模组图片
        ivMachineDetailModuleIcon = findViewById(R.id.iv_machine_detail_module_icon);
        // 激光模组
        tvMachineDetailLaserModule = findViewById(R.id.tv_machine_detail_laser_module);
        // SD卡
        tvMachineDetailSd = findViewById(R.id.tv_machine_detail_sd);
        // 加工类型
        llMachineDetailWorkType = findViewById(R.id.ll_machine_detail_work_type);
        // 加工类型 RadioGroup
        rgMachineDetailWorkType = findViewById(R.id.rg_machine_detail_work_type);
        // XYZ常规雕刻
        rbMachineDetailXyz = findViewById(R.id.rb_machine_detail_xyz);
        // YRR转轴雕刻(三档旋转轴)
        rbMachineDetailYyrThreelevel = findViewById(R.id.rb_machine_detail_yyr_threelevel);
        // YRR转轴雕刻(无极旋转轴)
        rbMachineDetailYyrNonpolar = findViewById(R.id.rb_machine_detail_yyr_non_polar);
        // Y轴卡盘雕刻
        rbMachineDetailYrc = findViewById(R.id.rb_machine_detail_yrc);
        // 模组切换
        tvMachineDetailModuleToggle = findViewById(R.id.tv_machine_detail_module_toggle);
        // spinner
        spinnerMachineDetailLaserModule = findViewById(R.id.spinner_machine_detail_laser_module);
        // 一键抬升Z轴
        tvMachineDetailZup = findViewById(R.id.tv_machine_detail_zup);
        // 火焰传感器 RelativeLayout
        rlMachineDetailFireModule = findViewById(R.id.rl_machine_detail_fire_module);
        // 火焰传感器 Switch
        switchMachineDetailFireModule = findViewById(R.id.switch_machine_detail_fire_module);
        // 门动传感器 RelativeLayout
        rlMachineDetailDoorModule = findViewById(R.id.rl_machine_detail_door_module);
        // 门动传感器 Switch
        switchMachineDetailDoorModule = findViewById(R.id.switch_machine_detail_door_module);
        // 倾斜检测传感器 RelativeLayout
        rlMachineDetailSlantModule = findViewById(R.id.rl_machine_detail_slant_module);
        // 倾斜检测传感器 Switch
        switchMachineDetailSlantModule = findViewById(R.id.switch_machine_detail_slant_module);
        // 红十字激光 RelativeLayout
        rlMachineDetailRtlaserModule = findViewById(R.id.rl_machine_detail_rtlaser_module);
        // 红十字激光 switch
        switchMachineDetailRtlaserModule = findViewById(R.id.switch_machine_detail_rtlaser_module);
        // 红十字激光 RelativeLayout
        rlMachineDetailLedModule = findViewById(R.id.rl_machine_detail_led_module);
        // 红十字激光 switch
        switchMachineDetailLedModule = findViewById(R.id.switch_machine_detail_led_module);
        // 气泵功能 RelativeLayout
        rlMachineDetailAirModule = findViewById(R.id.rl_machine_detail_air_module);
        // 气泵功能 switch
        switchMachineDetailAirModule = findViewById(R.id.switch_machine_detail_air_module);
        // 外接屏幕功能 RelativeLayout
        rlMachineDetailExternalScreen = findViewById(R.id.rl_machine_detail_external_screen);
        // 外接屏幕功能 switch
        switchMachineDetailExternalScreen = findViewById(R.id.switch_machine_detail_external_screen);
        // 锁定XYZ轴电机 RelativeLayout
        rlMachineDetailLockMotor = findViewById(R.id.rl_machine_detail_lock_motor);
        // 锁定XYZ轴电机 switch
        switchMachineDetailLockMotor = findViewById(R.id.switch_machine_detail_lock_motor);
        // 断开连接
        tvMachineDetailDisconnect = findViewById(R.id.tv_machine_detail_disconnect);
        // 重置设备
        tvMachineDetailReset = findViewById(R.id.tv_machine_detail_reset);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取机器名称
        String machineName = getIntent().getStringExtra("machineName");
        String sdDetails = getIntent().getStringExtra("sdDetails");
        // 设置机器名称
        if (machineName.isEmpty() || "".equals(machineName)) {
            Log.d(TAG, "未能获取设备名称");
            Toast.makeText(MachineDetailActivity.this, "未能获取设备名称", Toast.LENGTH_SHORT).show();
            tvMachineDetailName.setText("UnKnown");
        } else {
            tvMachineDetailName.setText(machineName);
            // 设置机器图片
            if (machineName.contains("Laser")) {
                // 设置激光雕刻机器图片
                if (machineName.contains("T2020")) {
                    // 设置激光雕刻机 T2020图片
                    Glide.with(this).load(R.mipmap.ic_laser_t2020).into(ivMachineDetailImage);
                    // 设置激光雕刻机 T2020行程
                    tvMachineDetailSize.setText("200x200(mm²)");
                } else {
                    // 设置激光雕刻机 T4图片
                    Glide.with(this).load(R.mipmap.ic_laser_t4).into(ivMachineDetailImage);
                    // 设置激光雕刻机 T4行程
                    tvMachineDetailSize.setText("300x300(mm²)");
                }
            } else {
                // 设置CNC雕刻机机器图片
                if (machineName.contains("3018MAX")) {
                    // 设置CNC雕刻机 3018MAX图片
                    Glide.with(this).load(R.mipmap.ic_cnc_3018max).into(ivMachineDetailImage);
                    // 设置CNC雕刻机 3018PRO行程
                    tvMachineDetailSize.setText("300x180x45(mm²)");
                } else if (machineName.contains("3018PRO")) {
                    // 设置CNC雕刻机 3018PRO图片
                    Glide.with(this).load(R.mipmap.ic_cnc_3018pro).into(ivMachineDetailImage);
                    // 设置CNC雕刻机 3018PRO行程
                    tvMachineDetailSize.setText("300x180x45(mm²)");
                } else {
                    // 设置CNC雕刻机 3020PLUS图片
                    Glide.with(this).load(R.mipmap.ic_cnc_3020plus).into(ivMachineDetailImage);
                    // 设置CNC雕刻机 3020PLUS行程
                    tvMachineDetailSize.setText("300x200x73(mm²)");
                }

                // 隐藏加工类型功能
                llMachineDetailWorkType.setVisibility(View.GONE);
                // 隐藏火焰传感器功能
                rlMachineDetailFireModule.setVisibility(View.GONE);
                // 隐藏门动传感器功能
                rlMachineDetailDoorModule.setVisibility(View.GONE);
                // 隐藏倾斜检测传感器功能
                rlMachineDetailSlantModule.setVisibility(View.GONE);
                // 隐藏红十字激光功能
                rlMachineDetailRtlaserModule.setVisibility(View.GONE);
                // 隐藏照明灯功能
                rlMachineDetailLedModule.setVisibility(View.GONE);
                // 隐藏气泵功能
                rlMachineDetailAirModule.setVisibility(View.GONE);
                // 隐藏外接屏幕功能
                rlMachineDetailExternalScreen.setVisibility(View.GONE);
                // 隐藏锁定XYZ电机功能
                rlMachineDetailLockMotor.setVisibility(View.GONE);
            }
        }

        if (sdDetails.isEmpty() || "".equals(sdDetails)) {
            Log.d(TAG, "未能获取设备名称");
            Toast.makeText(MachineDetailActivity.this, "未能获取设备名称", Toast.LENGTH_SHORT).show();
            tvMachineDetailName.setText("UnKnown");
        } else {
            tvMachineDetailSd.setText(sdDetails);
        }


        // 连接状态
        webSocketManager = WebSocketManager.getInstance();
        boolean isConnected = webSocketManager.isConnected();
        Log.d(TAG, "机器连接状态：" + isConnected);
        if (isConnected) {
            // 设置机器状态
            tvMachineDetailStatus.setText("已连接");
            tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
            // 发送命令查询状态
            for (String cmd : commands) {
                sendCommand(cmd);
            }
            Handler handler = new Handler(Looper.getMainLooper());
            for (int i = 0; i < commands.length; i++) {
                final String cmd = commands[i];
                handler.postDelayed(() -> {
                    sendCommand(cmd);
                }, i * 1000);
            }
        } else {
            // 设置机器状态
            tvMachineDetailStatus.setText("未连接");
            tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
            // Toast
            Toast.makeText(this, "未连接到设备或连接设备异常，请重试！", Toast.LENGTH_SHORT).show();
        }

        // 根据机器类型设置模组
        if (machineName.contains("Laser")) {
            // 模组图片
            Glide.with(this).load(R.drawable.icon_laser).into(ivMachineDetailModuleIcon);
            // 模组切换提示
            tvMachineDetailModuleToggle.setText("激光模组切换");

            // 创建适配器并设置给 激光模组Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, laserOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMachineDetailLaserModule.setAdapter(adapter);
        } else {
            // 模组图片
            Glide.with(this).load(R.drawable.icon_cnc).into(ivMachineDetailModuleIcon);
            // 模组切换提示
            tvMachineDetailModuleToggle.setText("主轴电机切换");

            // 创建适配器并设置给 主轴电机Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cncOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMachineDetailLaserModule.setAdapter(adapter);
        }


        // 模组
        if (machineName.contains("Laser")) {
            String machineLaserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
            // 设置激光模组
            if (TextUtils.isEmpty(machineLaserModule)) {
                tvMachineDetailLaserModule.setText("未知");
            } else {
                tvMachineDetailLaserModule.setText(machineLaserModule);
            }

            // 设置激光模组Spinner 选中项
            int position = laserOptions.indexOf(machineLaserModule);
            // 如果找到了该值，设置 Spinner 的选中项
            if (position != -1) {
                spinnerMachineDetailLaserModule.setSelection(position);
            } else {
                // 如果没有找到该值，可以设置一个默认选项，例如 position = 0
                spinnerMachineDetailLaserModule.setSelection(0);
            }
        } else {
            String machineCNCModule = sharedPref.getString(getString(R.string.preference_laser_module), "Cd-100W");
            // 设置激光模组
            if (TextUtils.isEmpty(machineCNCModule)) {
                tvMachineDetailLaserModule.setText("未知");
            } else {
                tvMachineDetailLaserModule.setText(machineCNCModule);
            }

            // 设置激光模组Spinner 选中项
            int position = cncOptions.indexOf(machineCNCModule);
            // 如果找到了该值，设置 Spinner 的选中项
            if (position != -1) {
                spinnerMachineDetailLaserModule.setSelection(position);
            } else {
                // 如果没有找到该值，可以设置一个默认选项，例如 position = 0
                spinnerMachineDetailLaserModule.setSelection(0);
            }
        }

        // 获取保存的气泵开关实例值
        boolean isOpenAir = sharedPref.getBoolean(getString(R.string.preference_air_module), false);
        switchMachineDetailAirModule.setChecked(isOpenAir);

        // 获取保存的危险警报震动提醒实例值
        isOpenVibrateAlert = sharedPref.getBoolean(getString(R.string.preference_vibrate_alert), true);
        // 获取保存的危险警报震动提醒时长实例值
        vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);
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

        // 加工类型
        rgMachineDetailWorkType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_machine_detail_xyz:
                        sendCommand("45=0");
                        sendCommand("$103=80");
                        break;
                    case R.id.rb_machine_detail_yyr_threelevel:
                        sendCommand("$45=1");
                        sendCommand("$103=80");
                        break;
                    case R.id.rb_machine_detail_yyr_non_polar:
                        sendCommand("$45=1");
                        sendCommand("$103=50.931");
                        break;
                    case R.id.rb_machine_detail_yrc:
                        sendCommand("$45=1");
                        break;
                }
            }
        });


        // 激光模组
        spinnerMachineDetailLaserModule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 当选中一个项时，更新 tvLaserModule 的文本
                String selectedLaserModule = (String) parentView.getItemAtPosition(position);
                tvMachineDetailLaserModule.setText(selectedLaserModule);
                // 保存激光模组
                sharedPref.edit().putString(getString(R.string.preference_laser_module), selectedLaserModule).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 没有选择任何项时的操作（可选）
            }
        });

        // 一键抬升Z轴
        tvMachineDetailZup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseDialog.showCustomDialog(MachineDetailActivity.this,
                        "温馨提示", "即将为您一键抬升Z轴至最大高度，以便您更换激光或拆卸雕刻地板！\r\n\r\n请问是否继续？",
                        "确定", "取消",
                        v1 -> {
                            sendCommand("$J=G90 Z50 F700");
                        },
                        v1 -> {
                            Log.d(TAG, "用户点击取消");
                        });
            }
        });

        // 火焰传感器
        switchMachineDetailFireModule.setOnCheckedChangeListener(fireModuleListener);

        // 门动传感器
        switchMachineDetailDoorModule.setOnCheckedChangeListener(doorModuleListener);

        // 倾斜检测传感器
        switchMachineDetailSlantModule.setOnCheckedChangeListener(slantModuleListener);

        // 红十字激光
        switchMachineDetailRtlaserModule.setOnCheckedChangeListener(laserRtListener);

        // 照明灯
        switchMachineDetailLedModule.setOnCheckedChangeListener(ledModuleListener);

        // 外接屏幕
        switchMachineDetailExternalScreen.setOnCheckedChangeListener(externalScreenListener);

        // 锁定XYZ电机
        switchMachineDetailLockMotor.setOnCheckedChangeListener(lockMotorListener);

        // 气泵功能
        switchMachineDetailAirModule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "isOpenAir=" + isChecked);
                if (isChecked) {
                    sendCommand("M8");
                    sharedPref.edit().putBoolean(getString(R.string.preference_air_module), true).apply();
                } else {
                    sendCommand("M9");
                    sharedPref.edit().putBoolean(getString(R.string.preference_air_module), false).apply();

                }
            }
        });

        // 断开连接
        tvMachineDetailDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 断开
                webSocketManager.disconnect();
                // 断开 Wi-Fi
                disconnectWifi();
                // 发送EventBus事件
                EventBus.getDefault().post(new DeviceConnectEvent("disconnect", "null", "null", "null"));
                // 关闭页面
                finish();
            }
        });

        // 重置设备
        tvMachineDetailReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示风险提示弹窗
                showDialogRiskWarning();
            }
        });
    }


    // 断开 Wi-Fi，根据 Android 版本选择合适的方法
    private void disconnectWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29 及以上)
            disconnectWifiAndroid10Above();
        } else {
            // Android 9 及以下
            forgetWifiNetwork();
        }
    }

    // Android 10 及以上，使用 ConnectivityManager 断开 Wi-Fi
    private void disconnectWifiAndroid10Above() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                networkCallback = null;
            }

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(null); // 解绑网络
                    connectivityManager.unregisterNetworkCallback(this); // 取消 Wi-Fi 连接
                    Log.d(TAG, "Wi-Fi 连接已断开");
                }
            };

            connectivityManager.requestNetwork(networkRequest, networkCallback);
        }
    }

    // Android 9 及以下 (可用 removeNetwork)
    @SuppressLint("MissingPermission")
    private void forgetWifiNetwork() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int netId = wifiInfo.getNetworkId();
            wifiManager.disableNetwork(netId);
            wifiManager.removeNetwork(netId);
            wifiManager.saveConfiguration();
        }
    }


    /**
     * 重置设备风险提示弹窗
     */
    private void showDialogRiskWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_risk_warning);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 设置可取消（点击空白处取消）
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

        // 选择框
        ImageView ivRiskWarning = dialog.findViewById(R.id.iv_risk_warning);
        // 取消
        TextView tvDialogRiskWarningCancel = dialog.findViewById(R.id.tv_dialog_risk_warning_cancel);
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_risk_warning_confirm);

        // 初始倒计时 5 秒
        int[] countdown = {5};
        // 定义倒计时逻辑
        final Handler handler = new Handler();
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (countdown[0] > 0) {
                    // 更新确认按钮的文本
                    tvDialogRiskWarningConfirm.setText("确定 (" + countdown[0] + ")");
                    countdown[0]--;
                    handler.postDelayed(this, 1000);  // 每秒更新一次
                } else {
                    tvDialogRiskWarningConfirm.setText("确定");
                }
            }
        };
        // 启动倒计时
        handler.post(countdownRunnable);

        // 选择框
        ivRiskWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 反转勾选状态
                isChecked = !isChecked;
                if (isChecked) {
                    Glide.with(getApplicationContext()).load(R.drawable.ic_checkbox_select).into(ivRiskWarning);
                    tvDialogRiskWarningConfirm.setBackgroundResource(R.drawable.bg_green_1e853a_r30);
                } else {
                    Glide.with(getApplicationContext()).load(R.drawable.ic_checkbox_unselect).into(ivRiskWarning);
                    tvDialogRiskWarningConfirm.setBackgroundResource(R.drawable.bg_gray_999999_r30);
                }
            }
        });


        // 取消
        tvDialogRiskWarningCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChecked) {
                    // 重置设备
                    sendCommand("$RST=*");
                    // 隐藏弹窗
                    dialog.dismiss();
                } else {
                    // 如果没勾选，显示提示
                    Toast.makeText(getApplicationContext(), "请确认您已了解相关风险，并进行勾选。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (getApplicationContext().getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // 显示 Dialog
        dialog.show();
    }


    /**
     * 发送命令
     *
     * @param command
     */
    private void sendCommand(String command) {
        Log.d(TAG, "Sending: " + command);
        webSocketManager.send(command);
    }

    /**
     * 解析结果
     *
     * @param response 回复值
     */
    private void processResponse(String response) {
        if (response == null || response.isEmpty()) {
            return;
        }
        Log.d(TAG, "Processing response:\n" + response);


        // 处理整体错误信息（如 response = "error:3"）
        if (response.startsWith("error:")) {
            Toast.makeText(MachineDetailActivity.this, "设备返回错误：" + response + "，请联系客服处理", Toast.LENGTH_SHORT).show();

        }

        // 拆分多行数据
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim(); // 去掉前后空格

            // 只处理 $ 开头的参数
            if (line.startsWith("$")) {
                // 解析格式 "$XX=YY"
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim(); // 变量名，如 "$43"
                    String value = parts[1].trim(); // 对应的值

                    // 根据不同的传感器类型调用更新方法
                    switch (key) {
                        case "$45":
                            yrrValue = value;
                            break;
                        case "$103":
                            val103 = value;
                            break;
                        case "$43": // 火焰传感器
                            updateFireSensor(value);
                            break;
                        case "$48": // 门动传感器
                            updateDoorSensor(value);
                            break;
                        case "$42": // 倾斜传感器
                            updateSlantSensor(value);
                            break;
                        case "$40": // 红十字激光传感器
                            updateLaserRt(value);
                            break;
                        case "$39": // 照明灯
                            updateLedSensor(value);
                            break;
                        case "$47": // 外接屏幕功能传感器
                            updateExternalScreen(value);
                            break;
                        case "$1": // 锁定XYZ电机功能
                            updateLockMotor(value);
                            break;
                        default:
                            break; // 忽略其他数据
                    }

                    // 检查是否已获取到两个关键值
                    if (yrrValue != null && val103 != null) {
                        handleYRRAnd103(yrrValue, val103);
                        yrrValue = null;
                        val103 = null;
                    }
                }
            }
        }
    }

    // 这里要查询$YRR和$103 $YRR=0 && $103=80---XY轴常规   $YRR=1 && $103=80---三档旋转轴  $YRR=1 && $103=50.931---无极旋转轴  $YRR=1 卡盘转轴
    private void handleYRRAnd103(String yrr, String v103) {
        Log.d(TAG, "处理 $45=" + yrr + ", $103=" + v103);
        if ("1".equals(yrr)) {
            if ("80.000".equals(v103)) {
                Log.d(TAG, "$45=1 且 $103=80，设置选中三档旋转轴");
                rbMachineDetailYyrThreelevel.setChecked(true);
            } else if ("50.931".equals(v103)){
                Log.d(TAG, "$45=1 且 $103=50.931，设置选中无极旋转轴");
                rbMachineDetailYyrNonpolar.setChecked(true);
            }
        } else {
            Log.d(TAG, "$45=0");
            if ("80.000".equals(val103)) {
                rbMachineDetailXyz.setChecked(true);
            } else {

            }

        }
    }

    /**
     * 更新火焰传感器
     *
     * @param value 接受的值
     */
    private void updateFireSensor(String value) {
        boolean isOpenFireModule = !value.equals("0");
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailFireModule.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailFireModule.setChecked(isOpenFireModule);

        // 3. 重新绑定监听器
        switchMachineDetailFireModule.setOnCheckedChangeListener(fireModuleListener);
        Log.d(TAG, "火焰传感器状态已更新: " + isOpenFireModule);
    }

    /**
     * 更新门动传感器
     *
     * @param value 接受的值
     */
    private void updateDoorSensor(String value) {
        boolean isOpenDoorModule = !value.equals("0");
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailDoorModule.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailDoorModule.setChecked(isOpenDoorModule);

        // 3. 重新绑定监听器
        switchMachineDetailDoorModule.setOnCheckedChangeListener(doorModuleListener);
        Log.d(TAG, "门动传感器状态已更新: " + isOpenDoorModule);
    }

    /**
     * 更新倾斜传感器
     *
     * @param value 接收的值
     */
    private void updateSlantSensor(String value) {
        boolean isOpenSlantModule = !value.equals("0");
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailSlantModule.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailSlantModule.setChecked(isOpenSlantModule);

        // 3. 重新绑定监听器
        switchMachineDetailSlantModule.setOnCheckedChangeListener(slantModuleListener);
        Log.d(TAG, "倾斜传感器状态已更新: " + isOpenSlantModule);
    }

    /**
     * 更新红十字激光传感器
     *
     * @param value 接收的值
     */
    private void updateLaserRt(String value) {
        boolean isOpenLaserRt = !value.equals("0");
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailRtlaserModule.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailRtlaserModule.setChecked(isOpenLaserRt);

        // 3. 重新绑定监听器
        switchMachineDetailRtlaserModule.setOnCheckedChangeListener(laserRtListener);
        Log.d(TAG, "红十字激光传感器状态已更新: " + isOpenLaserRt);
    }

    /**
     * 更新照明灯传感器
     *
     * @param value 接收的值
     */
    private void updateLedSensor(String value) {
        boolean isOpenLedModule = !value.equals("0");
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailLedModule.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailLedModule.setChecked(isOpenLedModule);

        // 3. 重新绑定监听器
        switchMachineDetailLedModule.setOnCheckedChangeListener(ledModuleListener);
        Log.d(TAG, "照明灯传感器状态已更新: " + isOpenLedModule);
    }

    /**
     * 更新外接屏幕功能传感器
     *
     * @param value 接收的值
     */
    private void updateExternalScreen(String value) {
        boolean isOpenExternalScreen = !value.equals("0");
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailExternalScreen.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailExternalScreen.setChecked(isOpenExternalScreen);

        // 3. 重新绑定监听器
        switchMachineDetailExternalScreen.setOnCheckedChangeListener(externalScreenListener);
        Log.d(TAG, "外接屏幕功能传感器状态已更新: " + isOpenExternalScreen);
    }

    /**
     * 更新锁定XYZ轴电机功能
     *
     * @param value 接收的值
     */
    private void updateLockMotor(String value) {
        boolean isOpenLockMotor = "255".equals(value);
        // 1. 先移除监听器，防止触发回调
        switchMachineDetailLockMotor.setOnCheckedChangeListener(null);

        // 2. 更新 Switch 选中状态
        switchMachineDetailLockMotor.setChecked(isOpenLockMotor);

        // 3. 重新绑定监听器
        switchMachineDetailLockMotor.setOnCheckedChangeListener(lockMotorListener);
        Log.d(TAG, "锁定XYZ轴电机功能状态已更新: " + isOpenLockMotor);
    }



    /**
     * 火焰传感器状态监听
     */
    private final CompoundButton.OnCheckedChangeListener fireModuleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenFireModule=" + isChecked);
            if (isChecked) {
                sendCommand("$43=1");
            } else {
                sendCommand("$43=0");
            }
        }
    };

    /**
     * 火焰传感器状态监听
     */
    private final CompoundButton.OnCheckedChangeListener doorModuleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenDoorModule=" + isChecked);
            if (isChecked) {
                sendCommand("$48=1");
            } else {
                sendCommand("$48=0");
            }
        }
    };

    /**
     * 火焰传感器状态监听
     */
    private final CompoundButton.OnCheckedChangeListener slantModuleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenSlantModule=" + isChecked);
            if (isChecked) {
                sendCommand("$42=1");
            } else {
                sendCommand("$42=0");
            }
        }
    };

    /**
     * 红十字激光传感器状态监听
     */
    private final CompoundButton.OnCheckedChangeListener laserRtListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenLaserRt=" + isChecked);
            if (isChecked) {
                sendCommand("$40=1");
            } else {
                sendCommand("$40=0");
            }
        }
    };

    /**
     * 照明灯传感器状态监听
     */
    private final CompoundButton.OnCheckedChangeListener ledModuleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenLedModule=" + isChecked);
            if (isChecked) {
                sendCommand("$39=1");
            } else {
                sendCommand("$39=0");
            }
        }
    };

    /**
     * 外接屏幕功能传感器状态监听
     */
    private final CompoundButton.OnCheckedChangeListener externalScreenListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenExternalScreen=" + isChecked);
            if (isChecked) {
                sendCommand("$47=1");
            } else {
                sendCommand("$47=0");
            }
        }
    };

    /**
     * 锁定XYZ轴电机功能状态监听
     */
    private final CompoundButton.OnCheckedChangeListener lockMotorListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "isOpenLockMotor=" + isChecked);
            if (isChecked) {
                sendCommand("$1=255");
            } else {
                sendCommand("$1=250");
            }
        }
    };


    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Activity topActivity = GrblController.getInstance().getTopActivity();
            if (event.getMessage().startsWith("<")) {
                Log.d(TAG, "message=" + event.getMessage().toString());
                String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
                Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);

                if (parts[0].equals(Constants.MACHINE_STATUS_IDLE)) {
                    tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineDetailStatus.setText("已连接");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_RUN)) {
                    tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineDetailStatus.setText("工作中");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_JOG)) {
                    tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineDetailStatus.setText("运动中");
                } else if (parts[0].contains(Constants.MACHINE_STATUS_HOLD)) {
                    tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                    tvMachineDetailStatus.setText("暂停");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_ALARM)) {
                    tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                    tvMachineDetailStatus.setText("警告");
                }

                processResponse(event.getMessage());
            } else {

                if (topActivity != this) {
                    Log.d(TAG, "当前 Activity 不是顶层，不弹窗");
                    return; // 不是当前页面，直接 return
                }

                if (event.getMessage().contains("MSG:Safe door err") && tvMachineDetailStatus.getText().equals("工作中")) { // 开门警告弹窗打开
                    // TODO 开门警告弹窗
                    showDialogDoorWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe door reset") && tvMachineDetailStatus.getText().equals("暂停")) { // 开门警告弹窗关闭
                    // 隐藏开门警告弹窗
                    dialogDoorWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Flame err") && tvMachineDetailStatus.getText().equals("工作中")) { // 火焰警告弹窗打开
                    // TODO 火焰警告弹窗
                    showDialogFireWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Flame reset") && tvMachineDetailStatus.getText().equals("暂停")) { // 火焰警告弹窗关闭
                    // 隐藏火焰警告弹窗
                    dialogFireWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Tilt sensor") && tvMachineDetailStatus.getText().equals("工作中")) { // 倾斜警告弹窗打开
                    // TODO 倾斜警告弹窗
                    showDialogProbeWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Probe reset") && tvMachineDetailStatus.getText().equals("暂停")) { // 倾斜警告弹窗关闭
                    // 隐藏倾斜警告弹窗
                    dialogProbeWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Using machine")) {
                    for (String line : event.getMessage().split("\n")) {
                        if (line.contains("[MSG:Using machine:")) {
                            int lastColon = line.lastIndexOf(':');
                            int endBracket = line.lastIndexOf(']');
                            if (lastColon != -1 && endBracket != -1 && lastColon < endBracket) {
                                String result = line.substring(lastColon + 1, endBracket);
                                Log.d(TAG, "版本: " + result); // 输出: 版本
                                // 设置固件版本
                                tvMachineDetailFirmware.setText(result);
                            }
                        }
                    }
                } else if (event.getMessage().contains("[SD Free:")) {
                    for (String line : event.getMessage().split("\n")) {
                        if (line.startsWith("[SD Free:")) {
                            String free = null, total = null;

                            // 提取 SD Free
                            int freeStart = line.indexOf("SD Free:") + "SD Free:".length();
                            int freeEnd = line.indexOf("Used:", freeStart);
                            free = line.substring(freeStart, freeEnd).trim();

                            // 提取 Total
                            int totalStart = line.indexOf("Total:") + "Total:".length();
                            int totalEnd = line.indexOf("]", totalStart);
                            total = line.substring(totalStart, totalEnd);

                            Log.d(TAG, "SD Free: " + free);
                            Log.d(TAG, "Total: " + total);
                            // 设置SD卡容量
                            tvMachineDetailSd.setText(free + " / " + total);

                            // Free容量转换为 MB
                            double freeMB = 0;
                            if (free != null) {
                                if (free.endsWith("GB")) {
                                    double gb = Double.parseDouble(free.replace("GB", "").trim());
                                    freeMB = gb * 1024;
                                } else if (free.endsWith("MB")) {
                                    freeMB = Double.parseDouble(free.replace("MB", "").trim());
                                }
                            }

                            // 弹窗提示
                            if (freeMB < 100) {
                                BaseDialog.showCustomDialog(MachineDetailActivity.this,
                                        "存储空间不足",
                                        "检测到SD卡剩余空间小于100MB\r\n\r\n避免出现异常情况，请清理后再操作。",
                                        "清理", "取消",
                                        v -> {
                                            // TODO 跳转文件页面
                                            startActivity(new Intent(this, FileActivity.class));
                                        },
                                        v -> {
                                            Log.d(TAG, "用户点击取消");
                                        });
                            }
                        }
                    }
                } else {
                    processResponse(event.getMessage());
                }
            }

        }
    }

    /**
     * 开门风险提示弹窗
     */
    private void showDialogDoorWarning() {
        dialogDoorWarning = new Dialog(this, R.style.CustomDialog);
        dialogDoorWarning.setContentView(R.layout.dialog_door_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialogDoorWarning.getWindow() != null) {
            dialogDoorWarning.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialogDoorWarning.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialogDoorWarning.isShowing()) {
                    dialogDoorWarning.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialogDoorWarning.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialogDoorWarning.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialogDoorWarning.show();
    }

    /**
     * 火焰风险提示弹窗
     */
    private void showDialogFireWarning() {
        dialogFireWarning = new Dialog(this, R.style.CustomDialog);
        dialogFireWarning.setContentView(R.layout.dialog_fire_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialogFireWarning.getWindow() != null) {
            dialogFireWarning.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialogFireWarning.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialogFireWarning.isShowing()) {
                    dialogFireWarning.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialogFireWarning.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialogFireWarning.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialogFireWarning.show();
    }

    /**
     * 倾斜风险提示弹窗
     */
    private void showDialogProbeWarning() {
        dialogProbeWarning = new Dialog(this, R.style.CustomDialog);
        dialogProbeWarning.setContentView(R.layout.dialog_probe_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialogProbeWarning.getWindow() != null) {
            dialogProbeWarning.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialogProbeWarning.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialogProbeWarning.isShowing()) {
                    dialogProbeWarning.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialogProbeWarning.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialogProbeWarning.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialogProbeWarning.show();
    }

    /**
     * 震动提醒
     * @param context 上下文
     * @param milliseconds 震动时长
     */
    public void vibratePhone(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }
}
