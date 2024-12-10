package in.co.gorest.grblcontroller.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.MainActivity;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.BarCodeActivity;
import in.co.gorest.grblcontroller.activity.BeginEngraveActivity;
import in.co.gorest.grblcontroller.activity.BluetoothConnectionActivity;
import in.co.gorest.grblcontroller.activity.ConnectActivity;
import in.co.gorest.grblcontroller.activity.EditActivity;
import in.co.gorest.grblcontroller.activity.EngraveActivity;
import in.co.gorest.grblcontroller.activity.FileActivity;
import in.co.gorest.grblcontroller.activity.MaterialActivity;
import in.co.gorest.grblcontroller.activity.QrCodeActivity;
import in.co.gorest.grblcontroller.activity.TelnetConnectionActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.ModelChangeEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.events.TelnetConnectEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.ui.FileSenderTabFragment;
import in.co.gorest.grblcontroller.util.FileUploader;
import in.co.gorest.grblcontroller.util.GrblUtils;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.NettyClient;

public class HomeFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = HomeFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // 机器状态
    private LinearLayout llMachineStatus;
    private TextView tvMachineStatusTips;
    private TextView tvMachineStatus;
    // 设备图片
    private ImageView ivMachine;
    // 设备地址
    private TextView tvAddress;
    // 添加设备
    private TextView tvAddDevice;
    // 简易模式
    private LinearLayout llHomeSimple;
    // 雕刻
    private LinearLayout llEngraveSimple;
    // 控制中心
    private LinearLayout llControlSimple;
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


    // 连接方式
    private String connectType = null;
    // WifiManager
    private WifiManager wifiManager = null;


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
        // 机器状态
        llMachineStatus = view.findViewById(R.id.ll_machine_status);
        // 机器状态图标
        tvMachineStatusTips = view.findViewById(R.id.tv_machine_status_tips);
        // 机器状态文字
        tvMachineStatus = view.findViewById(R.id.tv_machine_status);
        // 设备图片
        ivMachine = view.findViewById(R.id.iv_machine);
        // 设备地址
        tvAddress = view.findViewById(R.id.tv_address);
        // 添加设备
        tvAddDevice = view.findViewById(R.id.tv_add_device);
        // 简易模式
        llHomeSimple = view.findViewById(R.id.ll_home_simple);
        // 雕刻
        llEngraveSimple = view.findViewById(R.id.ll_engrave_simple);
        // 控制中心
        llControlSimple = view.findViewById(R.id.ll_control_simple);
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
    @SuppressLint("MissingPermission")
    private void initData() {
        // 蓝牙服务

        // 模式切换
        String operationModel = sharedPref.getString(getString(R.string.preference_operation_mode), "simple");
        if ("simple".equals(operationModel)) {
            llHomeSimple.setVisibility(View.VISIBLE);
            llHomePro.setVisibility(View.GONE);
        } else {
            llHomeSimple.setVisibility(View.GONE);
            llHomePro.setVisibility(View.VISIBLE);
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
                } else {
                    Log.d(TAG, "无效点击");
                }
            }
        });

        // 添加设备
        tvAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ConnectActivity.class));
            }
        });

        // 雕刻
        llEngraveSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BeginEngraveActivity.class));
            }
        });

        // 控制中心(简易模式)
        llControlSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectType != null) {
                    Log.d(TAG, "connectType=" + connectType);
                    if (connectType.equals("Telnet")) {
                        startActivity(new Intent(getActivity(), TelnetConnectionActivity.class));
                    } else {
                        startActivity(new Intent(getActivity(), BluetoothConnectionActivity.class));
                    }
                } else {
                    BaseDialog.showCustomDialog(getActivity(),
                            "温馨提示", "请先连接设备！",
                            "确定", "取消",
                            v1 -> {
                                startActivity(new Intent(getActivity(), ConnectActivity.class));
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
                    if (connectType.equals("Telnet")) {
                        startActivity(new Intent(getActivity(), TelnetConnectionActivity.class));
                    } else {
                        startActivity(new Intent(getActivity(), BluetoothConnectionActivity.class));
                    }
                } else {
                    BaseDialog.showCustomDialog(getActivity(),
                            "温馨提示", "请先连接设备！",
                            "确定", "取消",
                            v1 -> {
                                startActivity(new Intent(getActivity(), ConnectActivity.class));
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
                ImgUtil.openAlbum(getActivity());
            }
        });

        // 相机
        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImgUtil.openCamera(getActivity());
            }
        });

        // 画图
        llCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 文字
        llText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
     * Wi-Fi 名字
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectEvent(DeviceConnectEvent event) {
        if (!event.getType().isEmpty() && !event.getName().isEmpty() && !event.getAddress().isEmpty()) {
            Log.d(TAG, "Type=" + event.getType() + "----Name=" + event.getName() + "----Address=" + event.getAddress());
            connectType = event.getType();
            if (connectType.equals("Telnet")) {
                // 连接Telnet
                NettyClient.getInstance().connect(event.getAddress(), 8080);

                // 设置设备图片
                Glide.with(getContext()).load(R.mipmap.ic_machine).into(ivMachine);
                // 设置设备名称和地址
                tvAddress.setText(event.getName() + "   " + event.getAddress());
                // 设置按钮提示
                tvAddDevice.setText("添加其他设备");
                // 设置机器状态
                llMachineStatus.setVisibility(View.VISIBLE);
                // 保存连接方式
                sharedPref.edit().putString(getString(R.string.preference_connect_type), event.getType()).apply();
                if (event.getName().contains("MKS")) {
                    // 获取自动连接状态
                    boolean isAutoConnect = sharedPref.getBoolean(getString(R.string.preference_auto_connect), false);
                    if (!isAutoConnect) {
                        BaseDialog.showCustomDialog(getActivity(),
                                "温馨提示", "是否开启自动连接设备？\r\n\r\n开启后下次打开App可默认连接此设备",
                                "开启", "取消",
                                v -> {
                                    // 自动连接
                                    sharedPref.edit().putBoolean(getString(R.string.preference_auto_connect), true).apply();
                                    // SSID
                                    Log.d(TAG, "SSID=" + event.getName());
                                    sharedPref.edit().putString(getString(R.string.preference_wifi_ssid), event.getName()).apply();
                                },
                                v -> {
                                    Log.d(TAG, "用户取消开启");
                                });
                    }
                }

            }
        }
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