package in.co.gorest.grblcontroller.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import es.dmoral.toasty.Toasty;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.databinding.ActivityBaseBinding;
import in.co.gorest.grblcontroller.events.ConsoleMessageEvent;
import in.co.gorest.grblcontroller.events.GrblAlarmEvent;
import in.co.gorest.grblcontroller.events.GrblErrorEvent;
import in.co.gorest.grblcontroller.events.StreamingCompleteEvent;
import in.co.gorest.grblcontroller.events.StreamingStartedEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.helpers.NotificationHelper;
import in.co.gorest.grblcontroller.listeners.ConsoleLoggerListener;
import in.co.gorest.grblcontroller.listeners.FileSenderListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.service.FileStreamerIntentService;
import in.co.gorest.grblcontroller.service.GrblBluetoothSerialService;
import in.co.gorest.grblcontroller.service.GrblTelnetSerialService;
import in.co.gorest.grblcontroller.ui.BaseFragment;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class BaseActivity extends AppCompatActivity implements BaseFragment.OnFragmentInteractionListener {

    /**
     * 用于管理和访问增强的共享偏好设置实例。
     * 通过 {@link EnhancedSharedPreferences} 提供更强大的共享偏好设置功能。
     */
    protected EnhancedSharedPreferences sharedPref;
    /**
     * 用于记录控制台日志的监听器。
     * 实现 {@link ConsoleLoggerListener} 接口以接收和处理日志信息。
     */
    protected ConsoleLoggerListener consoleLogger = null;
    /**
     * 用于监听和管理机器状态的监听器。
     * 实现 {@link MachineStatusListener} 接口以处理机器状态的变化。
     */
    protected MachineStatusListener machineStatus = null;
    /**
     * 设备的蓝牙串行通信的服务实例
     */
    protected GrblBluetoothSerialService grblBluetoothSerialService = null;
    /**
     * 设备的 Telnet 串行通信的服务实例
     */
    protected GrblTelnetSerialService grblTelnetSerialService = null;
    /**
     * 保存上一次显示的 Toast 对象，方便在新 Toast 显示时取消之前的 Toast
     */
    private Toast lastToast;
    /**
     * 是否正在运行标识
     */
    public static boolean isAppRunning;
    /**
     * 注册权限请求结果的回调
     */
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    /**
     * 启用矢量图支持，确保在应用中可以正确显示矢量图形
     */
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 绑定视图
        ActivityBaseBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_base);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        // 应用初始化
        applicationSetup();
        // 绑定机器状态
        binding.setMachineStatus(machineStatus);
        // 初始化 Iconify 以支持 FontAwesome 图标
        Iconify.with(new FontAwesomeModule());
        // 申请权限
        requestPermissions();
        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化点击事件
        initListeners();
    }

    /**
     * 初始化界面
     */
    private void initView() {}

    /**
     * 初始化数据
     */
    private void initData() {}

    /**
     * 初始化点击事件
     */
    private void initListeners() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FileStreamerIntentService.class));
        ConsoleLoggerListener.resetClass();
        FileSenderListener.resetClass();
        MachineStatusListener.resetClass();
        isAppRunning = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lastToast = null;
    }

    /**
     * 应用初始化
     */
    protected void applicationSetup() {
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.createChannels();

        consoleLogger = ConsoleLoggerListener.getInstance();
        machineStatus = MachineStatusListener.getInstance();
        machineStatus.setJogging(sharedPref.getDouble(getString(R.string.preference_jogging_step_size), 1.00),
                sharedPref.getDouble(getString(R.string.preference_jogging_step_size_z), 0.1),
                sharedPref.getDouble(getString(R.string.preference_jogging_feed_rate), 2400.0),
                sharedPref.getBoolean(getString(R.string.preference_jogging_in_inches), false));
        machineStatus.setVerboseOutput(sharedPref.getBoolean(getString(R.string.preference_console_verbose_mode), false));
        machineStatus.setIgnoreError20(sharedPref.getBoolean(getString(R.string.preference_ignore_error_20), false));
        machineStatus.setUsbBaudRate(Integer.parseInt(sharedPref.getString(getString(R.string.usb_serial_baud_rate), Constants.USB_BAUD_RATE)));
        machineStatus.setSingleStepMode(sharedPref.getBoolean(getString(R.string.preference_single_step_mode), false));
        machineStatus.setCustomStartUpString(sharedPref.getString(getString(R.string.preference_start_up_string), ""));
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
        }


        // 申请所有必要的权限
        if (!requiredPermissions.isEmpty()) {
            requestPermissionsLauncher.launch(requiredPermissions.toArray(new String[0]));
        }
    }

    private void sendCommandIfIdle(String command) {
        if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)) {
            onGcodeCommandReceived(command);
        } else {
            showToastMessage(getString(R.string.text_machine_not_idle), true, true);
        }
    }

    /**
     * 显示一个 Toast 消息
     *
     * @param message 消息内容
     */
    protected void showToastMessage(String message) {
        this.showToastMessage(message, false, false);
    }

    /**
     * 显示一个 Toast 消息，根据 isWarning 参数判断是否显示警告类型的 Toast
     *
     * @param message   要显示的消息文本
     * @param longToast 是否显示较长时间的 Toast
     * @param isWarning 是否显示警告类型的 Toast
     */
    @SuppressLint("ShowToast")
    protected void showToastMessage(String message, Boolean longToast, Boolean isWarning) {
        // 如果 isWarning 为 true，则显示警告类型的 Toast，否则显示成功类型的 Toast
        if (isWarning) {
            lastToast = Toasty.warning(this, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT, true);
        } else {
            lastToast = Toasty.success(this, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        }

        // 设置 Toast 的显示位置并显示
        lastToast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 120);
        lastToast.show();
    }

    /**
     * 检查当前设备是否为平板电脑
     *
     * @param context 应用上下文
     * @return 如果设备是平板电脑，返回 true，否则返回 false
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onGcodeCommandReceived(String command) {

    }

    @Override
    public void onGrblRealTimeCommandReceived(byte command) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGrblAlarmEvent(GrblAlarmEvent event) {
        consoleLogger.offerMessage(event.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGrblErrorEvent(GrblErrorEvent event) {
        consoleLogger.offerMessage(event.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConsoleMessageEvent(ConsoleMessageEvent event) {
        consoleLogger.offerMessage(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUiToastEvent(UiToastEvent event) {
        showToastMessage(event.getMessage(), event.getLongToast(), event.getIsWarning());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnStreamingCompleteEvent(StreamingCompleteEvent event) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnStreamingStartEvent(StreamingStartedEvent event) {
        if (sharedPref.getBoolean(getString(R.string.preference_keep_screen_on), false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 使用音量键作为控制接口，根据机器的当前状态来暂停或继续机器的操作
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            if (machineStatus.getState().equals(Constants.MACHINE_STATUS_RUN)) {
                onGrblRealTimeCommandReceived(GrblUtils.GRBL_PAUSE_COMMAND);
                return true;
            }

            if (machineStatus.getState().equals(Constants.MACHINE_STATUS_HOLD)) {
                onGrblRealTimeCommandReceived(GrblUtils.GRBL_RESUME_COMMAND);
                return true;
            }
        }

        return false;
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
//        Toast.makeText(this, permission + " 权限已授予", Toast.LENGTH_SHORT).show();
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
}