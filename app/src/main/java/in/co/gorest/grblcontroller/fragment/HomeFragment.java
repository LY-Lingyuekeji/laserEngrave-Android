package in.co.gorest.grblcontroller.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
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
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xuexiang.xui.widget.guidview.FocusShape;
import com.xuexiang.xui.widget.guidview.GuideCaseView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.AddDeviceActivity;
import in.co.gorest.grblcontroller.activity.BarCodeActivity;
import in.co.gorest.grblcontroller.activity.BeginEngraveActivity;
import in.co.gorest.grblcontroller.activity.BluetoothConnectionActivity;
import in.co.gorest.grblcontroller.activity.DrawBoardActivity;
import in.co.gorest.grblcontroller.activity.EngraveActivity;
import in.co.gorest.grblcontroller.activity.FileActivity;
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
import in.co.gorest.grblcontroller.model.WifiNetwork;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.RadarView;

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
    // 机器状态信息
    private LinearLayout llMachineStatus;
    // 机器状态标识
    private TextView tvMachineStatusTips;
    // 机器状态
    private TextView tvMachineStatus;
    // 激光模组
    private TextView tvLaserModule;
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
        // 注销EventBus
        EventBus.getDefault().unregister(this);
        // 注销广播接收器，避免内存泄漏
        requireContext().unregisterReceiver(wifiScanReceiver);
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
        // 机器状态信息
        llMachineStatus = view.findViewById(R.id.ll_machine_status);
        // 机器状态标识
        tvMachineStatusTips = view.findViewById(R.id.tv_machine_status_tips);
        // 机器状态
        tvMachineStatus = view.findViewById(R.id.tv_machine_status);
        // 激光模组
        tvLaserModule = view.findViewById(R.id.tv_laser_module);
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
        wifiManager = (WifiManager) requireContext().getSystemService(Context.WIFI_SERVICE);
        // 检查 Wi-Fi 是否已启用，如果没有启用，则启用 Wi-Fi
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);  // 启用 Wi-Fi
        }

        // 设置 RecyclerView
        recyclerViewDevice.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        deviceAdapter = new DeviceAdapter(requireContext(), wifiNetworkList);
        recyclerViewDevice.setAdapter(deviceAdapter);

        // 注册广播接收器，接收 Wi-Fi 扫描结果
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        requireActivity().registerReceiver(wifiScanReceiver, filter);

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
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 机器状态
        llMachineStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatus.getText().equals("雕刻中")) {
                    Intent intent = new Intent(getActivity(), EngraveActivity.class);
                    String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                    String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } else if (tvMachineStatus.getText().equals("暂停")){
                    // 终止雕刻
                    NettyClient.getInstance(new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            return false;
                        }
                    })).sendMsgToServer(("\u0018" + "\r\n").getBytes(StandardCharsets.UTF_8), null);
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
                startActivity(new Intent(getActivity(), BeginEngraveActivity.class));
            }
        });

        // 控制中心(简易模式)
        rlControlSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectType != null) {
                    Log.d(TAG, "connectType=" + connectType);
                    if (connectType.equals("AP")) {
                        startActivity(new Intent(getActivity(), TelnetConnectionActivity.class));
                    } else {
                        startActivity(new Intent(getActivity(), BluetoothConnectionActivity.class));
                    }
                } else {
                    BaseDialog.showCustomDialog(getActivity(),
                            "温馨提示", "请先连接设备！",
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
                    if (connectType.equals("AP")) {
                        startActivity(new Intent(getActivity(), TelnetConnectionActivity.class));
                    } else {
                        startActivity(new Intent(getActivity(), BluetoothConnectionActivity.class));
                    }
                } else {
                    BaseDialog.showCustomDialog(getActivity(),
                            "温馨提示", "请先连接设备！",
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
                startActivity(new Intent(getActivity(), MaterialActivity.class));
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
                startActivity(new Intent(getActivity(), DrawBoardActivity.class));
            }
        });

        // 文字
        llText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TextCreateActivity.class));
            }
        });

        // 条形码
        llBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BarCodeActivity.class));
            }
        });

        // 二维码
        llQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), QrCodeActivity.class));
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
            // 标志位，判断是否找到包含 "MSK" 的 Wi-Fi 网络
            boolean foundMSK = false;

            // 清空旧的数据
            wifiNetworkList.clear();

            // 遍历扫描结果，检查每个网络的 SSID 是否包含 "MSK"
            for (ScanResult result : scanResults) {
                if (result.SSID.contains("MKS")) {  // 如果 SSID 包含 "MSK"
                    // 构造 WifiNetwork 对象，并添加到列表
                    String ipAddress = "192.168.4.1";  // 这里假设 IP 地址为默认值
                    wifiNetworkList.add(new WifiNetwork(result.SSID, ipAddress));
                    foundMSK = true;  // 找到包含 "MSK" 的 Wi-Fi
                }
            }

            // 如果没有找到包含 "MSK" 的 Wi-Fi 网络，隐藏控件
            if (!foundMSK) {
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
     * Wi-Fi 名字
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectEvent(DeviceConnectEvent event) {
        if (!event.getType().isEmpty() && !event.getName().isEmpty() && !event.getAddress().isEmpty()) {
            Log.d(TAG, "Type=" + event.getType() + "----Name=" + event.getName() + "----Address=" + event.getAddress());
            connectType = event.getType();
            if (connectType.equals("AP")) {
                // 连接Telnet
                NettyClient.getInstance().connect(event.getAddress(), 8080);

                // 隐藏空白展示界面
                llEmptyContent.setVisibility(View.GONE);
                // 隐藏设备搜索界面
                llRadarContent.setVisibility(View.GONE);
                // 隐藏设备列表界面
                rlDeviceList.setVisibility(View.GONE);
                // 显示设备信息界面
                llDeviceInfo.setVisibility(View.VISIBLE);

                // 设置信息
                tvMachineName.setText(event.getName());
                String laserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
                tvLaserModule.setText(laserModule);

                // 保存连接方式
                sharedPref.edit().putString(getString(R.string.preference_connect_type), event.getType()).apply();

//                if (event.getName().contains("MKS")) {
//                    // 获取自动连接状态
//                    boolean isAutoConnect = sharedPref.getBoolean(getString(R.string.preference_auto_connect), false);
//                    if (!isAutoConnect) {
//                        BaseDialog.showCustomDialog(getActivity(),
//                                "温馨提示", "是否开启自动连接设备？\r\n\r\n开启后下次打开App可默认连接此设备",
//                                "开启", "取消",
//                                v -> {
//                                    // 自动连接
//                                    sharedPref.edit().putBoolean(getString(R.string.preference_auto_connect), true).apply();
//                                    // SSID
//                                    Log.d(TAG, "SSID=" + event.getName());
//                                    sharedPref.edit().putString(getString(R.string.preference_wifi_ssid), event.getName()).apply();
//                                },
//                                v -> {
//                                    Log.d(TAG, "用户取消开启");
//                                });
//                    }
//                }

            } else if (connectType.equals("disconnect")) {
                // 显示空白展示界面
                llEmptyContent.setVisibility(View.VISIBLE);
                // 隐藏设备搜索界面
                llRadarContent.setVisibility(View.GONE);
                // 隐藏设备列表界面
                rlDeviceList.setVisibility(View.GONE);
                // 隐藏设备信息界面
                llDeviceInfo.setVisibility(View.GONE);
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
        if (!event.getMessage().isEmpty() && event.getMessage().startsWith("<")) {
            Log.d(TAG, "message=" + event.getMessage().toString());
            String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
            Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);
            if (parts[0].equals(Constants.MACHINE_STATUS_IDLE)) {
                tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                tvMachineStatus.setText("已连接");
            } else if (parts[0].equals(Constants.MACHINE_STATUS_RUN)) {
                tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                tvMachineStatus.setText("雕刻中");
            } else if (parts[0].contains(Constants.MACHINE_STATUS_HOLD)) {
                tvMachineStatusTips.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                tvMachineStatus.setText("暂停");
            }

        }
    }

}