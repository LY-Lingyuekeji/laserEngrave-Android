package in.co.gorest.grblcontroller.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.xuexiang.xui.widget.guidview.FocusShape;
import com.xuexiang.xui.widget.guidview.GuideCaseView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.AddDeviceActivity;
import in.co.gorest.grblcontroller.activity.BarCodeActivity;
import in.co.gorest.grblcontroller.activity.BeginEngraveActivity;
import in.co.gorest.grblcontroller.activity.BluetoothConnectionActivity;
import in.co.gorest.grblcontroller.activity.DeviceConnectRecordActivity;
import in.co.gorest.grblcontroller.activity.DrawBoardActivity;
import in.co.gorest.grblcontroller.activity.EngraveActivity;
import in.co.gorest.grblcontroller.activity.FileActivity;
import in.co.gorest.grblcontroller.activity.MachineDetailActivity;
import in.co.gorest.grblcontroller.activity.MaterialActivity;
import in.co.gorest.grblcontroller.activity.QrCodeActivity;
import in.co.gorest.grblcontroller.activity.TelnetConnectionActivity;
import in.co.gorest.grblcontroller.activity.TextCreateActivity;
import in.co.gorest.grblcontroller.adapters.DeviceAdapter;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.ModelChangeEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.DeviceConnectRecord;
import in.co.gorest.grblcontroller.model.StaModelConfig;
import in.co.gorest.grblcontroller.model.WifiNetwork;
import in.co.gorest.grblcontroller.util.FileUtil;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.RadarView;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class HomeFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = HomeFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // WifiManager 用来管理 Wi-Fi 连接和扫描
    private WifiManager wifiManager;
    // Wi-Fi 网络的信息
    private List<WifiNetwork> wifiNetworkList = new ArrayList<>();
    // 机器适配器
    private DeviceAdapter deviceAdapter;
    // 空白提示界面
    private LinearLayout llEmptyContent;
    // 搜索设备
    private TextView tvScanDevice;
    // 去添加
    private TextView tvAddDevice;
    // 设备搜索界面
    private LinearLayout llRadarContent;
    // 设备搜索（雷达动画）
    private RadarView radarView;
    // 设备列表界面
    private RelativeLayout rlDeviceList;
    // 添加设备图标按钮
    private ImageView ivAddDevice;
    // 设备列表
    private RecyclerView recyclerViewDevice;
    // 重新搜索
    private LinearLayout llRetryScan;
    // 机器信息页面
    private LinearLayout llDeviceInfo;
    // 机器名称
    private TextView tvMachineName;
    // 连接记录
    private LinearLayout llMachineConnectRecord;
    // 机器状态信息
    private LinearLayout llMachineStatus;
    // 机器状态标识
    private TextView tvMachineStatusTips;
    // 机器状态
    private TextView tvMachineStatus;
    // 机器图片
    private ImageView ivMachineImage;
    // 机器行程
    private TextView tvMachineSize;
    // 机器固件版本
    private TextView tvMachineVersion;
    // 模组图标
    private ImageView ivModuleIcon;
    // 激光模组
    private TextView tvLaserModule;
    // 机器SD卡容量
    private TextView tvMachineSD;
    // 简易模式
    private LinearLayout llHomeSimple;
    // 雕刻
    private RelativeLayout rlEngraveSimple;
    // 控制中心
    private RelativeLayout rlControlSimple;
    // 专业模式
    private LinearLayout llHomePro;
    // 控制中心
    private LinearLayout llControl;
    // 素材库
    private LinearLayout llMaterial;
    // 文件
    private LinearLayout llFile;
    // 相册
    private LinearLayout llPhoto;
    // 相机
    private LinearLayout llCamera;
    // 画图
    private LinearLayout llCreate;
    // 文字
    private LinearLayout llText;
    // 条形码
    private LinearLayout llBarcode;
    // 二维码
    private LinearLayout llQrcode;

    // 是否显示搜索设备引导
    private boolean isShowScanDeviceGuide;
    // 是否显示连接设备引导
    private boolean isShowConnectDeviceGuide;
    // 连接方式
    private String connectType = null;

    // 是否手动搜索
    private boolean isManualScan = false; // 默认是 false（表示自动扫描）
    // 标志位，判断是否找到符合 的 Wi-Fi 网络
    boolean foundLaserOrCNC = false;

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

    // WS
    private WebSocketManager webSocketManager;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        // 注册EventBus
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播接收器，避免内存泄漏
        requireContext().unregisterReceiver(wifiScanReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 激光模组
        String machineLaserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
        // 设置激光模组
        if (TextUtils.isEmpty(machineLaserModule)) {
            tvLaserModule.setText("未知");
        } else {
            tvLaserModule.setText(machineLaserModule);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化界面
        initView(view);
        // 初始化数据
        initData();
        // 初始化事件监听
        setupListeners();
    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // 空白提示界面
        llEmptyContent = view.findViewById(R.id.ll_empty_content);
        // 搜索设备
        tvScanDevice = view.findViewById(R.id.tv_scan_device);
        // 去添加
        tvAddDevice = view.findViewById(R.id.tv_add_device);
        // 设备搜索界面
        llRadarContent = view.findViewById(R.id.ll_radar_content);
        // 设备搜索（雷达动画）
        radarView = view.findViewById(R.id.radar);
        // 设备列表界面
        rlDeviceList = view.findViewById(R.id.rl_device_list);
        // 添加设备图标按钮
        ivAddDevice = view.findViewById(R.id.iv_add_device);
        // 设备列表
        recyclerViewDevice = view.findViewById(R.id.recycler_view_device);
        // 重新搜索
        llRetryScan = view.findViewById(R.id.ll_retry_scan);
        // 机器信息界面
        llDeviceInfo = view.findViewById(R.id.ll_device_info);
        // 机器名称
        tvMachineName = view.findViewById(R.id.tv_machine_name);
        // 连接记录
        llMachineConnectRecord = view.findViewById(R.id.ll_machine_connect_record);
        // 机器状态信息
        llMachineStatus = view.findViewById(R.id.ll_machine_status);
        // 机器状态标识
        tvMachineStatusTips = view.findViewById(R.id.tv_machine_status_tips);
        // 机器状态
        tvMachineStatus = view.findViewById(R.id.tv_machine_status);
        // 机器图片
        ivMachineImage = view.findViewById(R.id.iv_machine_image);
        // 机器行程
        tvMachineSize = view.findViewById(R.id.tv_machine_size);
        // 机器固件版本
        tvMachineVersion = view.findViewById(R.id.tv_machine_version);
        // 模组图标
        ivModuleIcon = view.findViewById(R.id.iv_module_icon);
        // 激光模组
        tvLaserModule = view.findViewById(R.id.tv_laser_module);
        // 机器SD卡容量
        tvMachineSD = view.findViewById(R.id.tv_machine_sd);
        // 简易模式
        llHomeSimple = view.findViewById(R.id.ll_home_simple);
        // 雕刻
        rlEngraveSimple = view.findViewById(R.id.rl_engrave_simple);
        // 控制中心
        rlControlSimple = view.findViewById(R.id.rl_control_simple);
        // 专业模式
        llHomePro = view.findViewById(R.id.ll_home_pro);
        // 控制中心
        llControl = view.findViewById(R.id.ll_control);
        // 素材库
        llMaterial = view.findViewById(R.id.ll_material);
        // 文件
        llFile = view.findViewById(R.id.ll_file);
        // 相册
        llPhoto = view.findViewById(R.id.ll_photo);
        // 拍照
        llCamera = view.findViewById(R.id.ll_camera);
        // 画图
        llCreate = view.findViewById(R.id.ll_create);
        // 文字
        llText = view.findViewById(R.id.ll_text);
        // 条形码
        llBarcode = view.findViewById(R.id.ll_barcode);
        // 二维码
        llQrcode = view.findViewById(R.id.ll_qrcode);
    }

    /**
     * 初始化数据
     */
    @SuppressLint({"MissingPermission", "WrongConstant"})
    private void initData() {
        // 获取系统的 WifiManager 实例
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        Log.d(TAG, "wifi是否可用：" + wifiManager.isWifiEnabled());
        // 检查 Wi-Fi 是否已启用，如果没有启用，则启用 Wi-Fi
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);  // 启用 Wi-Fi
        }

        // 设置 RecyclerView
        recyclerViewDevice.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        deviceAdapter = new DeviceAdapter(requireContext(), wifiNetworkList);
        recyclerViewDevice.setAdapter(deviceAdapter);

        // 模式切换
        String operationModel = sharedPref.getString(getString(R.string.preference_operation_mode), "simple");
        if ("simple".equals(operationModel)) {
            llHomeSimple.setVisibility(View.VISIBLE);
            llHomePro.setVisibility(View.GONE);
        } else {
            llHomeSimple.setVisibility(View.GONE);
            llHomePro.setVisibility(View.VISIBLE);
        }

        // 获取保存的布尔值（是否显示搜索设备引导）
        isShowScanDeviceGuide = sharedPref.getBoolean(getString(R.string.preference_scandevice_guide_isshow), false);
        // 搜索设备引导
        if (!isShowScanDeviceGuide) {
            showScanDeviceGuide();
        }

        // 获取保存的危险警报震动提醒实例值
        isOpenVibrateAlert = sharedPref.getBoolean(getString(R.string.preference_vibrate_alert), true);
        // 获取保存的危险警报震动提醒时长实例值
        vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);

    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {

        // 机器名称
        tvMachineName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 跳转机器详情页面
                Intent intent = new Intent(getActivity(), MachineDetailActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                intent.putExtra("sdDetails", tvMachineSD.getText().toString());
                startActivity(intent);
            }
        });

        // 连接记录
        llMachineConnectRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DeviceConnectRecordActivity.class));
            }
        });

        // 机器状态
        llMachineStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatus.getText().equals("工作中")) {
                    Intent intent = new Intent(getActivity(), EngraveActivity.class);
                    String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                    String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } else if (tvMachineStatus.getText().equals("暂停")) {
                    // 终止雕刻
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("\u0018");
                } else if (tvMachineStatus.getText().equals("警告")) {
                    // 解除警告
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("$X");
                } else {
                    Log.d(TAG, "无效点击");
                }
            }
        });

        // 搜索设备
        tvScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 扫描设备
                scanDevice();
            }
        });

        // 添加设备
        tvAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddDeviceActivity.class));
            }
        });

        // 添加设备图标按钮
        ivAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddDeviceActivity.class));
            }
        });

        // 重新搜索
        llRetryScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏空白展示界面
                llEmptyContent.setVisibility(View.GONE);
                // 显示设备搜索界面
                llRadarContent.setVisibility(View.VISIBLE);
                // 隐藏设备列表界面
                rlDeviceList.setVisibility(View.GONE);
                // 隐藏设备信息界面
                llDeviceInfo.setVisibility(View.GONE);
                // 启动搜索动画
                radarView.start();

                // 先注销之前的接收器，防止重复注册
                requireContext().unregisterReceiver(wifiScanReceiver);

                // 注册接收器
                IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                requireActivity().registerReceiver(wifiScanReceiver, filter);

                // 开始搜索WIFI
                startWifiScan();
            }
        });

        // 雕刻
        rlEngraveSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BeginEngraveActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
            }
        });

        // 控制中心(简易模式)
        rlControlSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectType != null) {
                    Log.d(TAG, "connectType=" + connectType);
                    if (connectType.equals("AP") || connectType.equals("STA")) {
                        Log.d(TAG, "machineName=" + tvMachineName.getText().toString());
                        Intent intent = new Intent(getActivity(), TelnetConnectionActivity.class);
                        intent.putExtra("machineName", tvMachineName.getText().toString());
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(getActivity(), BluetoothConnectionActivity.class));
                    }
                } else {
                    BaseDialog.showCustomDialog(getActivity(),
                            "温馨提示", "检测到您还未连接设备，无法进行控制！\r\n\r\n是否连接设备？",
                            "确定", "取消",
                            v1 -> {
                                scanDevice();
                            },
                            v1 -> {

                            });
                }
            }
        });

        // 控制中心(专业模式)
        llControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectType != null) {
                    Log.d(TAG, "connectType=" + connectType);
                    if (connectType.equals("AP") || connectType.equals("STA")) {
                        Log.d(TAG, "machineName=" + tvMachineName.getText().toString());
                        Intent intent = new Intent(getActivity(), TelnetConnectionActivity.class);
                        intent.putExtra("machineName", tvMachineName.getText().toString());
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(getActivity(), BluetoothConnectionActivity.class));
                    }
                } else {
                    BaseDialog.showCustomDialog(getActivity(),
                            "温馨提示", "检测到您还未连接设备，无法进行控制！\r\n\r\n是否连接设备？",
                            "确定", "取消",
                            v1 -> {
                                scanDevice();
                            },
                            v1 -> {

                            });
                }
            }
        });

        // 素材库
        llMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MaterialActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
            }
        });

        // 文件
        llFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FileActivity.class));
            }
        });

        // 相册
        llPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImgUtil.openAlbum(requireActivity());
            }
        });

        // 相机
        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImgUtil.openCamera(requireActivity());
            }
        });

        // 画图
        llCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DrawBoardActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
            }
        });

        // 文字
        llText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TextCreateActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
            }
        });

        // 条形码
        llBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BarCodeActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
            }
        });

        // 二维码
        llQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
            }
        });
    }

    /**
     * 扫描设备
     */
    private void scanDevice() {
        // 检查必要的权限
        if (checkPermissions()) {
            // 权限已授权，执行 Wi-Fi 扫描
            // 注册接收器
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            requireActivity().registerReceiver(wifiScanReceiver, filter);

            // 隐藏空白展示界面
            llEmptyContent.setVisibility(View.GONE);
            // 显示设备搜索界面
            llRadarContent.setVisibility(View.VISIBLE);
            // 隐藏设备列表界面
            rlDeviceList.setVisibility(View.GONE);
            // 隐藏设备信息界面
            llDeviceInfo.setVisibility(View.GONE);
            // 启动搜索动画
            radarView.start();
            // 开始搜索WIFI
            startWifiScan();
            // 用户手动点击扫描
            isManualScan = true;
        } else {
            // 权限未授权，向用户请求权限
            requestPermissions();
        }
    }

    /**
     * WiFi扫描
     */
    private void startWifiScan() {
        boolean success = wifiManager.startScan();  // 启动 Wi-Fi 扫描
        Log.d(TAG, "WiFi scan started: " + success); // 添加日志查看扫描是否成功启动

        if (!success) {
            // 隐藏空白展示界面
            llEmptyContent.setVisibility(View.VISIBLE);
            // 显示设备搜索界面
            llRadarContent.setVisibility(View.GONE);
            // 隐藏设备列表界面
            rlDeviceList.setVisibility(View.GONE);
            // 隐藏设备信息界面
            llDeviceInfo.setVisibility(View.GONE);
            // 停止搜索动画
            radarView.stop();
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

                // 如果当前已连接的 Wi-Fi 是激光雕刻机 (Laser 或 CNC)，则不处理扫描结果
                if (!isManualScan && (currentSSID.startsWith("Laser") || currentSSID.startsWith("CNC"))) {
                    Log.d(TAG, "设备已连接，不再更新 Wi-Fi 列表");
                    return;
                }
            }

            // 不管是自动还是手动扫描，都要重置手动搜索标识isManualScan
            isManualScan = false;
            // 清空旧的数据
            wifiNetworkList.clear();

            // 遍历扫描结果，检查每个网络的 SSID 是否包含 "MSK"
            for (ScanResult result : scanResults) {
                // 搜索AP模式的机器
                if (result.SSID.startsWith("Laser") || result.SSID.startsWith("CNC")) {  // 如果 SSID 包含 "Laser" 或 "CNC"
                    // 构造 WifiNetwork 对象，并添加到列表
                    String ipAddress = "192.168.4.1";  // 这里假设 IP 地址为默认值
                    wifiNetworkList.add(new WifiNetwork(result.SSID, ipAddress, "AP"));
                    foundLaserOrCNC = true;  // 找到包含 "MSK" 的 Wi-Fi
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
                        scanIP(subnet, "未知类型", "STA");
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

            // 如果没有找到包含 "MSK" 的 Wi-Fi 网络，隐藏控件
            if (!foundLaserOrCNC) {
                // 显示空白展示界面
                llEmptyContent.setVisibility(View.VISIBLE);
                // 隐藏设备搜索界面
                llRadarContent.setVisibility(View.GONE);
                // 隐藏设备列表界面
                rlDeviceList.setVisibility(View.GONE);
                // 隐藏设备信息界面
                llDeviceInfo.setVisibility(View.GONE);
                // 停止搜索动画
                radarView.stop();
            } else {
                // 隐藏空白展示界面
                llEmptyContent.setVisibility(View.GONE);
                // 隐藏设备搜索界面
                llRadarContent.setVisibility(View.GONE);
                // 显示设备列表界面
                rlDeviceList.setVisibility(View.VISIBLE);
                // 隐藏设备信息界面
                llDeviceInfo.setVisibility(View.GONE);
                // 停止搜索动画
                radarView.stop();


                // 获取保存的布尔值（是否显示连接设备引导）
                isShowConnectDeviceGuide = sharedPref.getBoolean(getString(R.string.preference_connectdevice_guide_isshow), false);
                // 搜索设备引导
                if (!isShowConnectDeviceGuide) {
                    showConnectDeviceGuide();
                }
            }
        }
    };


    /**
     * 获取本地子网IP段
     *
     * @return
     */
    private String getLocalSubnet() {
        WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
    public void scanNetworkInParallel(String subnet, String machineName, String mode) {
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
                            foundLaserOrCNC = true;

                            // 更新 UI 显示
                            llEmptyContent.setVisibility(View.GONE);
                            llRadarContent.setVisibility(View.GONE);
                            rlDeviceList.setVisibility(View.VISIBLE);
                            llDeviceInfo.setVisibility(View.GONE);
                            radarView.stop();
                            deviceAdapter.notifyDataSetChanged();  // 刷新 RecyclerView


                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Wi-Fi 名字
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectEvent(DeviceConnectEvent event) {
        if (!event.getConnectType().isEmpty() && !event.getMachineName().isEmpty() && !event.getWifiName().isEmpty() && !event.getIpAddress().isEmpty()) {
            Log.d(TAG, "connectType=" + event.getConnectType() + "----machineName=" + event.getMachineName()
                    + "----wifiName=" + event.getWifiName() + "----ipAddress=" + event.getIpAddress());
            connectType = event.getConnectType();
            if (connectType.equals("disconnect")) {
                // 显示空白展示界面
                llEmptyContent.setVisibility(View.VISIBLE);
                // 隐藏设备搜索界面
                llRadarContent.setVisibility(View.GONE);
                // 隐藏设备列表界面
                rlDeviceList.setVisibility(View.GONE);
                // 隐藏设备信息界面
                llDeviceInfo.setVisibility(View.GONE);
            } else {
                webSocketManager = WebSocketManager.getInstance();
                webSocketManager.connect(event.getIpAddress());


                // 定义指令列表
                List<String> queryCommands = new ArrayList<>();
                queryCommands.add("$I");  // 版本
                queryCommands.add("$SD/List");  // SD卡信息

                // 使用 Handler 分批延迟发送
                Handler queryHandler = new Handler(Looper.getMainLooper());
                final int[] index = {0};  // 指令索引

                Runnable sendNextQueryCommand = new Runnable() {
                    @Override
                    public void run() {
                        if (index[0] < queryCommands.size()) {
                            sendCommand(queryCommands.get(index[0]));
                            index[0]++;
                            queryHandler.postDelayed(this, 500);  // 每条指令间隔 500ms
                        }
                    }
                };
                // 启动查询指令发送流程
                queryHandler.postDelayed(sendNextQueryCommand, 1000);

//                NettyClient.getInstance().connect(event.getIpAddress(), 8080);
                // 隐藏空白展示界面
                llEmptyContent.setVisibility(View.GONE);
                // 隐藏设备搜索界面
                llRadarContent.setVisibility(View.GONE);
                // 隐藏设备列表界面
                rlDeviceList.setVisibility(View.GONE);
                // 显示设备信息界面
                llDeviceInfo.setVisibility(View.VISIBLE);

                // 设置信息
                tvMachineName.setText(event.getMachineName());
                // 设置机器图片与行程
                if (event.getMachineName().contains("Laser")) {
                    // 设置激光雕刻机器图片
                    if (event.getMachineName().contains("T2020")) {
                        // 设置激光雕刻机 T2020图片
                        Glide.with(requireContext()).load(R.mipmap.ic_laser_t2020).into(ivMachineImage);
                        // 设置激光雕刻机 T2020行程
                        tvMachineSize.setText("200x200(mm²)");
                    } else {
                        // 设置激光雕刻机 T4图片
                        Glide.with(requireContext()).load(R.mipmap.ic_laser_t4).into(ivMachineImage);
                        // 设置激光雕刻机 T4行程
                        tvMachineSize.setText("300x300(mm²)");
                    }
                    // 设置模组图标
                    Glide.with(requireContext()).load(R.drawable.icon_laser).into(ivModuleIcon);
                } else {
                    // 设置CNC雕刻机机器图片
                    if (event.getMachineName().contains("3018MAX")) {
                        // 设置CNC雕刻机 3018MAX图片
                        Glide.with(requireContext()).load(R.mipmap.ic_cnc_3018max).into(ivMachineImage);
                        // 设置CNC雕刻机 3018PRO行程
                        tvMachineSize.setText("300x180x45(mm²)");
                    } else if (event.getMachineName().contains("3018PRO")) {
                        // 设置CNC雕刻机 3018PRO图片
                        Glide.with(requireContext()).load(R.mipmap.ic_cnc_3018pro).into(ivMachineImage);
                        // 设置CNC雕刻机 3018PRO行程
                        tvMachineSize.setText("300x180x45(mm²)");
                    } else {
                        // 设置CNC雕刻机 3020PLUS图片
                        Glide.with(requireContext()).load(R.mipmap.ic_cnc_3020plus).into(ivMachineImage);
                        // 设置CNC雕刻机 3020PLUS行程
                        tvMachineSize.setText("300x200x73(mm²)");
                    }
                    // 设置模组图标
                    Glide.with(requireContext()).load(R.drawable.icon_cnc).into(ivModuleIcon);
                }
                // 设置激光模组
                String laserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
                tvLaserModule.setText(laserModule);
                // 保存连接方式
                sharedPref.edit().putString(getString(R.string.preference_connect_type), event.getConnectType()).apply();
                // 保存IP地址
                sharedPref.edit().putString(getString(R.string.preference_sta_type_ipaddress), event.getIpAddress()).apply();

                // 创建记录
                DeviceConnectRecord record = new DeviceConnectRecord();
                record.setMachineName(event.getMachineName());
                record.setMode(event.getConnectType());
                record.setSsid(event.getWifiName());
                record.setLaserModule(tvLaserModule.getText().toString()); // 从界面获取激光模组
                record.setSize(tvMachineSize.getText().toString()); // 从界面获取尺寸
                record.setIpAddress(event.getIpAddress());
                record.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                // 创建路径
                File directory = new File(GrblController.getInstance().getExternalFilesDir(null) + "/connect");
                if (!directory.exists()) directory.mkdirs();
                File file = new File(directory, "device_record.json");

                // 读取已有记录
                List<DeviceConnectRecord> list = FileUtil.readConnectRecordList(file);

                // 查找并更新已有记录（按名称唯一判断）
                boolean updated = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getMachineName().equals(record.getMachineName())) {
                        list.set(i, record); // 替换为最新
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    list.add(record); // 不存在则新增
                }

                // 保存
                FileUtil.saveConnectRecordList(list, file);


            }
        }
    }

    /**
     * 权限检测
     */
    private boolean checkPermissions() {
        // 检查 Wi-Fi 和位置权限
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * 请求权限
     */
    private void requestPermissions() {
        // 请求必要的权限
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                1001); // 请求码，可以自定义
    }

    /**
     * 权限请求结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
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
                    startWifiScan();
                } else {
                    // 权限未授予，向用户显示提示
                    Log.d(TAG, "权限未授予");
                    Toast.makeText(getActivity(), "需要 Wi-Fi 和位置权限才能扫描设备", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 搜索设备引导
     */
    private void showScanDeviceGuide() {
        new GuideCaseView.Builder(getActivity())
                .focusOn(tvScanDevice)
                .title("点击\"搜索设备\"开始搜索附近的设备")
                .titleSize(20, 1)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(90)
                .build()
                .show();
        // 保存布尔值（是否显示搜索设备引导）
        sharedPref.edit().putBoolean(getString(R.string.preference_scandevice_guide_isshow), true).apply();
    }

    /**
     * 连接设备引导
     */
    private void showConnectDeviceGuide() {
        new GuideCaseView.Builder(getActivity())
                .focusOn(rlDeviceList)
                .title("请选择自己的设备进行连接")
                .titleSize(20, 1)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(220)
                .build()
                .show();
        // 保存布尔值（是否显示搜索设备引导）
        sharedPref.edit().putBoolean(getString(R.string.preference_connectdevice_guide_isshow), true).apply();
    }

    /**
     * 发送命令
     *
     * @param command
     */
    private void sendCommand(String command) {
        WebSocketManager webSocketManager = WebSocketManager.getInstance();
        webSocketManager.send(command);
    }

    /**
     * 模式切换
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnModelChangeEvent(ModelChangeEvent event) {
        String model = event.getMessage();
        Log.d(TAG, "model=" + model);
        if (!model.isEmpty()) {
            if ("simple".equals(model)) {
                llHomeSimple.setVisibility(View.VISIBLE);
                llHomePro.setVisibility(View.GONE);
            } else {
                llHomeSimple.setVisibility(View.GONE);
                llHomePro.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Log.d(TAG, "message=" + event.getMessage().toString());
            Activity topActivity = GrblController.getInstance().getTopActivity();
            if (event.getMessage().startsWith("<")) {
                String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
                if (parts[0].equals(Constants.MACHINE_STATUS_IDLE)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineStatus.setText("已连接");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_RUN)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineStatus.setText("工作中");
                } else if (parts[0].contains(Constants.MACHINE_STATUS_HOLD)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                    tvMachineStatus.setText("暂停");
                } else if (parts[0].contains(Constants.MACHINE_STATUS_ALARM)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                    tvMachineStatus.setText("警告");
                }
            } else {
                if (topActivity != requireActivity()) {
                    Log.d(TAG, "当前 Activity 不是顶层，不弹窗");
                    return; // 不是当前页面，直接 return
                }

                if (event.getMessage().contains("MSG:Safe door err") && tvMachineStatus.getText().equals("工作中")) { // 开门警告弹窗打开
                    // TODO 开门警告弹窗
                    showDialogDoorWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(requireContext(), vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe door reset") && tvMachineStatus.getText().equals("暂停")) { // 开门警告弹窗关闭
                    // 隐藏开门警告弹窗
                    dialogDoorWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Flame err") && tvMachineStatus.getText().equals("工作中")) { // 火焰警告弹窗打开
                    // TODO 火焰警告弹窗
                    showDialogFireWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(requireContext(), vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Flame reset") && tvMachineStatus.getText().equals("暂停")) { // 火焰警告弹窗关闭
                    // 隐藏火焰警告弹窗
                    dialogFireWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Tilt sensor") && tvMachineStatus.getText().equals("工作中")) { // 倾斜警告弹窗打开
                    // TODO 倾斜警告弹窗
                    showDialogProbeWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(requireContext(), vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Probe reset") && tvMachineStatus.getText().equals("暂停")) { // 倾斜警告弹窗关闭
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
                                tvMachineVersion.setText(result);
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
                            tvMachineSD.setText(free + " / " + total);

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
                                BaseDialog.showCustomDialog(getContext(),
                                        "存储空间不足",
                                        "检测到SD卡剩余空间小于100MB\r\n\r\n避免出现异常情况，请清理后再操作。",
                                        "清理", "取消",
                                        v -> {
                                            // TODO 跳转文件页面
                                            startActivity(new Intent(getActivity(), FileActivity.class));
                                        },
                                        v -> {
                                            Log.d(TAG, "用户点击取消");
                                        });
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 开门风险提示弹窗
     */
    private void showDialogDoorWarning() {
        dialogDoorWarning = new Dialog(requireContext(), R.style.CustomDialog);
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
        dialogFireWarning = new Dialog(requireContext(), R.style.CustomDialog);
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
        dialogProbeWarning = new Dialog(requireContext(), R.style.CustomDialog);
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
     *
     * @param context      上下文
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