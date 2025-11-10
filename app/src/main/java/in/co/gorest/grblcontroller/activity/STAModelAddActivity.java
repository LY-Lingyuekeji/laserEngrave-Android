
package in.co.gorest.grblcontroller.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.StaModelConfigAdapter;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.StaModelConfig;
import in.co.gorest.grblcontroller.util.FileUtil;

public class STAModelAddActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = STAModelAddActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // 获取 WifiManager 实例
    private WifiManager wifiManager;
    // 返回
    private ImageView ivBack;
    // 添加配置
    private TextView tvStaModelAddConfigure;
    // 我的配置
    private TextView tvStaModelAddMyConfiguration;
    // 配置列表
    private RecyclerView rvStaModelConfigurationList;
    // IP地址号段一
    private TextView tvStaModelAddConnectHostOne;
    // IP地址号段二
    private TextView tvStaModelAddConnectHostTwo;
    // IP地址号段三
    private TextView tvStaModelAddConnectHostThree;
    // IP地址号段四
    private EditText etStaModelAddConnectHost;
    // 连接
    private TextView tvStaModelAddConnect;

    // 配置列表适配器
    private StaModelConfigAdapter adapter;
    // 配置列表数据源
    private List<StaModelConfig> configList = new ArrayList<>();
    // 配置的机器名称
    private String machineName;
    // 配置的Wi-Fi名称
    private String wifiName;

    // 连接目标机器名称
    private String deviceName;


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
        DataBindingUtil.setContentView(this, R.layout.activity_sta_model_add);

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
        // 添加配置
        tvStaModelAddConfigure = findViewById(R.id.tv_sta_model_add_configure);
        // 我的配置
        tvStaModelAddMyConfiguration = findViewById(R.id.tv_sta_model_add_my_configuration);
        // 配置列表
        rvStaModelConfigurationList = findViewById(R.id.rv_sta_model_configuration_list);
        // IP地址号段一
        tvStaModelAddConnectHostOne = findViewById(R.id.tv_sta_model_add_connect_host_one);
        // IP地址号段二
        tvStaModelAddConnectHostTwo = findViewById(R.id.tv_sta_model_add_connect_host_two);
        // IP地址号段三
        tvStaModelAddConnectHostThree = findViewById(R.id.tv_sta_model_add_connect_host_three);
        // IP地址号段四
        etStaModelAddConnectHost = findViewById(R.id.et_sta_model_add_connect_host);
        // 连接
        tvStaModelAddConnect = findViewById(R.id.tv_sta_model_add_connect);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 连接类型
        deviceName = getIntent().getStringExtra("deviceName");
        Log.d(TAG, "deviceName" + deviceName);
        // 获取WifiManager实例
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // 设置LayoutManager
        rvStaModelConfigurationList.setLayoutManager(new LinearLayoutManager(this));
        // 加载配置记录
        loadConfigs();
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

        // 添加配置
        tvStaModelAddConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(STAModelAddActivity.this, STAModelConfigurationActivity.class));
                finish();
            }
        });

        // 连接
        tvStaModelAddConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(tvStaModelAddConnectHostOne.getText().toString())
                        && !TextUtils.isEmpty(tvStaModelAddConnectHostTwo.getText().toString())
                        && !TextUtils.isEmpty(tvStaModelAddConnectHostThree.getText().toString())) {
                    if (!TextUtils.isEmpty(etStaModelAddConnectHost.getText().toString())) {
                        showDeviceDialog(machineName);
                    } else {
                        Toast.makeText(STAModelAddActivity.this, "请自行输入主机地址后进行连接！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(STAModelAddActivity.this, "请选择配置项目进行获取，若配置项为空，请先添加配置！", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    /**
     * 加载配置记录
     */
    private void loadConfigs() {
        File file = new File(GrblController.getInstance().getExternalFilesDir(null) + "/config/", "sta_model_config.json");
        if (!file.exists()) {
            Log.d(TAG, "文件不存在");
            Intent intent = new Intent(this, STAModelConfigurationActivity.class);
            intent.putExtra("deviceName", deviceName);
            startActivity(intent);
            finish();
            return;
        }

        try {
            String json = FileUtil.readFile(file);
            Log.d(TAG, "读取到的文件内容: " + json); // 输出文件内容进行调试
            configList = new Gson().fromJson(json, new TypeToken<List<StaModelConfig>>() {
            }.getType());
            // 创建适配器，将设备数据传入适配器
            adapter = new StaModelConfigAdapter(this, configList);
            // adapter点击事件
            adapter.setOnItemClickListener(new StaModelConfigAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(StaModelConfig config) {
                    // 机器名称
                    machineName = config.getMachineName();
                    // Wi-Fi名称
                    wifiName = config.getConfigSSID();

                    // 检查Wi-Fi
                    checkWifiConnection();
                }
            });
            // 设置我的配置
            tvStaModelAddMyConfiguration.setText("我的配置（" + configList.size() + "）");
            // 设置Adpter
            rvStaModelConfigurationList.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (ssid != null) {
                if (ssid.equals(wifiName)) {
                    int ipAddress = wifiInfo.getIpAddress();
                    String ip = Formatter.formatIpAddress(ipAddress);
                    Log.d(TAG, "Connected Wi-Fi IP Address: " + ip);
                    String[] parts = ip.split("\\."); // 注意：点号是正则表达式的特殊字符，要用 \\.
                    // 第一段
                    tvStaModelAddConnectHostOne.setText(parts[0]);
                    // 第二段
                    tvStaModelAddConnectHostTwo.setText(parts[1]);
                    // 第三段
                    tvStaModelAddConnectHostThree.setText(parts[2]);
                } else {
                    // 创建自定义弹窗对象
                    BaseAlertDialog baseAlertDialog = new BaseAlertDialog(STAModelAddActivity.this);

                    // 显示弹窗并传入标题、内容以及确认按钮的点击事件
                    baseAlertDialog.show("温馨提示", "当前并未连接至\"" + wifiName + "\"的热点，请连接后重试！", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 点击确认按钮后执行的操作
                            Log.d(TAG, "用户点击了确认按钮");
                            // 跳转到Wi-Fi设置进行连接
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivityForResult(intent, 1002); // 使用自定义的请求码
                        }
                    });
                }
            } else {
                // 创建自定义弹窗对象
                BaseAlertDialog baseAlertDialog = new BaseAlertDialog(STAModelAddActivity.this);

                // 显示弹窗并传入标题、内容以及确认按钮的点击事件
                baseAlertDialog.show("温馨提示", "当前并未连接至\"" + wifiName + "\"的热点，请检查后重试！", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 点击确认按钮后执行的操作
                        Log.d(TAG, "用户点击了确认按钮");
                        // 跳转到Wi-Fi设置进行连接
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivityForResult(intent, 1002); // 使用自定义的请求码
                    }
                });
            }
        }
    }

    // 显示自定义 Dialog
    private void showDeviceDialog(String machineName) {
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
        laserModuleDataList.add("LdT4-1064nm-2W");


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
        Spinner spinnerLaserModule = dialog.findViewById(R.id.spinner_laser_module);
        TextView tvConfirm = dialog.findViewById(R.id.tv_confirm);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);

        // 设置内容
        tvMachineName.setText(machineName);
        // 设置连接状态
        tvMachineStatus.setText("可连接");
        // 设置图片
        if (machineName.contains("Laser")) {
            // 设置模组图标为激光
            Glide.with(this).load(R.drawable.icon_laser).into(ivMachineModuleIcon);
            // 设置机器图片
            if (machineName.contains("T2020")) {
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
            if (machineName.contains("3018MAX")) {
                // 设置CNC雕刻机 3018MAX图片
                Glide.with(this).load(R.mipmap.ic_cnc_3018max).into(ivMachineImage);
                // 设置CNC雕刻机 3018MAX行程
                tvMachineSize.setText("300x180x45(mm²)");
                tvComponentSize.setText("300x180x45(mm²)");
                // 保存CNC雕刻机 3018MAX行程
                sharedPref.edit().putInt(getString(R.string.preference_machine_width_cnc), 300).apply();
                sharedPref.edit().putInt(getString(R.string.preference_machine_height_cnc), 180).apply();
            } else if (machineName.contains("3018PRO")) {
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


                String ipAddress = tvStaModelAddConnectHostOne.getText().toString() + "."
                        + tvStaModelAddConnectHostTwo.getText().toString() + "."
                        + tvStaModelAddConnectHostThree.getText().toString() + "."
                        + etStaModelAddConnectHost.getText().toString();
                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent("STA", machineName, wifiName, ipAddress));
                // 关闭当前页面
                finish();
                // 隐藏弹窗
                dialog.dismiss();
            }


        });
        tvCancel.setOnClickListener(v -> dialog.dismiss());

        // 显示 Dialog
        dialog.show();
    }

    /**
     * 抖动动画
     *
     * @param view 抖动的视图
     */
    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 30f, -30f, 30f, -30f, 0f);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 判断是否从 Wi-Fi 设置页面返回
        if (requestCode == 1002) { // 你可以自定义一个请求码
            // Wi-Fi 设置页面返回后进行检查
            checkWifiConnection();
        }
    }

}