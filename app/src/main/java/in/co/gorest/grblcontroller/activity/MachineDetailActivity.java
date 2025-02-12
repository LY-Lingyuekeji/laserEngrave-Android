
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.RemoteFileAdapter;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.fragment.HomeFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.NettyClient;

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
    // 行程
    private TextView tvMachineDetailSize;
    // 芯片固件
    private TextView tvMachineDetailFirmware;
    // 激光模组
    private TextView tvMachineDetailLaserModule;
    // SD卡
    private TextView tvMachineDetailSd;
    // spinner
    private Spinner spinnerMachineDetailLaserModule;
    // 断开连接
    private TextView tvMachineDetailDisconnect;
    // 重置设备
    private TextView tvMachineDetailReset;

    // Spinner 数据源
    List<String> options = Arrays.asList("LdT-3W", "LdT4-10W", "LdT4-20W");

    // 是否选中
    private boolean isChecked = false;


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
        // 机器名称
        tvMachineDetailName = findViewById(R.id.tv_machine_detail_name);
        // 机器状态
        tvMachineDetailStatus = findViewById(R.id.tv_machine_detail_status);
        // 行程
        tvMachineDetailSize = findViewById(R.id.tv_machine_detail_size);
        // 芯片固件
        tvMachineDetailFirmware = findViewById(R.id.tv_machine_detail_firmware);
        // 激光模组
        tvMachineDetailLaserModule = findViewById(R.id.tv_machine_detail_laser_module);
        // SD卡
        tvMachineDetailSd = findViewById(R.id.tv_machine_detail_sd);
        // spinner
        spinnerMachineDetailLaserModule = findViewById(R.id.spinner_machine_detail_laser_module);
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
        // 设置机器名称
        if (machineName.isEmpty() || "".equals(machineName)) {
            Log.d(TAG, "未能获取设备名称");
            Toast.makeText(MachineDetailActivity.this, "未能获取设备名称", Toast.LENGTH_SHORT).show();
            tvMachineDetailName.setText("UnKnown");
        } else {
            tvMachineDetailName.setText(machineName);
        }


        // 连接状态
        boolean isConnected = NettyClient.getInstance().getConnectStatus();
        Log.d(TAG, "机器连接状态：" + isConnected);
        if (isConnected) {
            tvMachineDetailStatus.setText("已连接");
        } else {
            tvMachineDetailStatus.setText("未连接");
            tvMachineDetailStatus.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
        }


        // 创建适配器并设置给 激光模组Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMachineDetailLaserModule.setAdapter(adapter);

        // 激光模组
        String machineLaserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
        // 设置激光模组
        if (TextUtils.isEmpty(machineLaserModule)) {
            tvMachineDetailLaserModule.setText("未知");
        } else {
            tvMachineDetailLaserModule.setText(machineLaserModule);
        }

        // 设置激光模组Spinner 选中项
        int position = options.indexOf(machineLaserModule);
        // 如果找到了该值，设置 Spinner 的选中项
        if (position != -1) {
            spinnerMachineDetailLaserModule.setSelection(position);
        } else {
            // 如果没有找到该值，可以设置一个默认选项，例如 position = 0
            spinnerMachineDetailLaserModule.setSelection(0);
        }

        // 获取SD卡信息
        if (isConnected) {
            NettyClient.getInstance(new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    Log.d(TAG, "message=" + msg.obj);
                    // 检查数据是否符合预期的格式
                    if (isValidSdCardData(msg.obj.toString())) {
                        // 解析并设置 SD 卡的空间信息
                        parseSdCardData(msg.obj.toString());
                    }
                    return false;
                }
            })).sendMsgToServer("$SD/List\r\n".getBytes(StandardCharsets.UTF_8), null);
        } else {
            Toast.makeText(this, "未能获取SD卡信息", Toast.LENGTH_SHORT).show();
        }

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

        // 断开连接
        tvMachineDetailDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 断开NettyClient
                NettyClient.getInstance().disconnect();
                // 发送EventBus事件
                EventBus.getDefault().post(new DeviceConnectEvent("disconnect", "null", "null"));
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
                    resetDevice();
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
     * 重置设备
     */
    private void resetDevice() {
        // TODO 执行设备重置操作
        Log.d(TAG, "设备已经重置。");
        String command = "$RST=*";
        boolean isConnected = NettyClient.getInstance().getConnectStatus();
        if (isConnected) {
            NettyClient.getInstance(new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    return false;
                }
            })).sendMsgToServer((command + "\r\n").getBytes(StandardCharsets.UTF_8), null);
        } else {
            Toast.makeText(this, "设备已断开，请重新连接", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 检查是否符合 SD 卡空间信息格式
     *
     * @param data 源数据
     * @return 布尔值
     */
    private boolean isValidSdCardData(String data) {
        String regex = "\\[SD Free:(\\d+\\.\\d+ \\w+) Used:(\\d+\\.\\d+ \\w+) Total:(\\d+\\.\\d+ \\w+)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        return matcher.find();  // 如果匹配到至少一个符合格式的项，返回 true
    }

    /**
     * 解析 SD 卡空间信息并显示到界面
     *
     * @param data 数据
     */
    private void parseSdCardData(String data) {
        String regex = "\\[SD Free:(\\d+\\.\\d+ \\w+) Used:(\\d+\\.\\d+ \\w+) Total:(\\d+\\.\\d+ \\w+)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            String free = matcher.group(1);
            String used = matcher.group(2);
            String total = matcher.group(3);

            // 设置数据到 TextView
            tvMachineDetailSd.setText("可用：" + free);
        }
    }
}
