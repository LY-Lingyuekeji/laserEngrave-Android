
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.events.WifiNameEvent;
import in.co.gorest.grblcontroller.fragment.WifiChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.util.NettyClient;

public class STAModelActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = STAModelActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 请求位置权限的请求码
    private static final int PERMISSION_REQUEST_CODE = 100;
    // 用于管理Wi-Fi状态的WifiManager
    private WifiManager wifiManager;
    // 用于监听网络状态变化的广播接收器
    private BroadcastReceiver networkReceiver;
    // Wi-Fi名称（SSID）
    private EditText etSsid;
    // 选择网络
    private TextView tvChooseWifi;
    // 5GHz Wi-Fi提示
    private TextView tvIs5Gtips;
    // Wi-Fi密码
    private EditText etPassword;
    // 密码可见/不可见
    private ImageView ivPasswordVisibility;
    // 用于跟踪密码的显示状态
    private boolean isPasswordVisible = false;
    // 密码提示
    private TextView tvPasswordTips;
    // 下一步
    private TextView tvNext;
    // Mac地址
    private String macAddress;

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
        DataBindingUtil.setContentView(this, R.layout.activity_sta_model);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();

        // 注册EventBus
        EventBus.getDefault().register(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 在Fragment销毁时注销广播接收器，防止内存泄漏
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // Wi-Fi名称（SSID）
        etSsid = findViewById(R.id.et_ssid);
        // 选择网络
        tvChooseWifi = findViewById(R.id.tv_choose_wifi);
        // 5GHz Wi-Fi提示
        tvIs5Gtips = findViewById(R.id.tv_is5G_tips);
        // Wi-Fi密码
        etPassword = findViewById(R.id.et_password);
        // 密码可见/不可见
        ivPasswordVisibility = findViewById(R.id.iv_password_visibility);
        // 密码提示
        tvPasswordTips = findViewById(R.id.tv_password_tips);
        // 下一步
        tvNext = findViewById(R.id.tv_next);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取系统的WifiManager实例
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // 检查权限并初始化WiFi信息
        checkPermissionsAndSetup();
        // 注册网络状态变化的广播接收器
        registerNetworkChangeReceiver();
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

        // Wi-Fi名称（SSID）
        etSsid.addTextChangedListener(new TextWatcher() {
            // 文本变化前的操作
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            // 文本变化时的操作
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "etSsid.string=" + s.toString());
                if (s.toString().contains("5G")) {
                    tvIs5Gtips.setText("这可能是一个5GHz Wi-Fi，请选择2.4GHz Wi-Fi");
                } else {
                    tvIs5Gtips.setText("");
                }
            }

            // 文本变化后的操作
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 选择网络
        tvChooseWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示选择WiFi弹窗
                WifiChooseBottomSheetFragment wifiChooseBottomSheetFragment = new WifiChooseBottomSheetFragment();
                wifiChooseBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 密码可见/不可见
        ivPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // 如果密码当前是可见的，切换为不可见
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPasswordVisibility.setImageResource(R.drawable.ic_password_visibility_off);  // 设置图标为隐藏状态
                } else {
                    // 如果密码当前是不可见的，切换为可见
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPasswordVisibility.setImageResource(R.drawable.ic_password_visibility_on);  // 设置图标为显示状态
                }

                // 切换状态
                isPasswordVisible = !isPasswordVisible;
                // 将光标移动到文本的末尾
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // 下一步
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPassword.getText().length() < 8) {
                    tvPasswordTips.setTextColor(Color.parseColor("#ff1135"));
                } else {
                    tvPasswordTips.setTextColor(Color.parseColor("#000000"));
                    // 连接WIFI
                    if (!etSsid.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty()) {
                        // TODO 第一步 获取当前模式
                        NettyClient.getInstance(new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(@NonNull Message msg) {
                                Log.d(TAG, "msg = " + msg.obj.toString());
                                if (msg.obj.toString().contains("MAC=")) {
                                    Log.d(TAG, "-------------------------------------");
                                    // 正则表达式匹配MAC地址
                                    Pattern pattern = Pattern.compile("MAC=((?:[0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2})");
                                    Matcher matcher = pattern.matcher(msg.obj.toString());
                                    if (matcher.find()) {
                                        macAddress = matcher.group(0).substring(4); // 移除"MAC="前缀
                                        // 去掉连接符
                                        macAddress = macAddress.replace("-", "");
                                        Log.d(TAG, "macAddress=" + macAddress);


                                        // TODO 第二步 配置STA模式
                                        // 发送命令切换成STA模式
                                        NettyClient.getInstance(null).sendMsgToServer("$50=2\r\n".getBytes(StandardCharsets.UTF_8), null);
                                        // 发送命令配置账号密码
                                        NettyClient.getInstance(null).sendMsgToServer(("$53=" + etSsid.getText().toString() + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                                        NettyClient.getInstance(null).sendMsgToServer(("$54=" + etPassword.getText().toString() + "\r\n").getBytes(StandardCharsets.UTF_8), null);

                                        // TODO 第三步 重启设备
                                        // 发送命令重启设备
                                        NettyClient.getInstance(null).sendMsgToServer("$System/Control=RESTART\r\n".getBytes(StandardCharsets.UTF_8), null);

                                        // TODO 第三步 页面跳转，传递SSID和PASSWORD进行匹配和连接
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(4000);
                                                    Intent intent = new Intent(STAModelActivity.this, STAConnectStepActivity.class);
                                                    intent.putExtra("ssid", etSsid.getText().toString());
                                                    intent.putExtra("password", etPassword.getText().toString());
                                                    intent.putExtra("macAddress", macAddress);
                                                    startActivity(intent);
                                                    finish();
                                                } catch (InterruptedException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }).start();

                                    } else {
                                        Log.d(TAG, "No MAC address found.");
                                        Toast.makeText(STAModelActivity.this, "No MAC address found.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                return false;
                            }
                        })).sendMsgToServer("$I\r\n".getBytes(StandardCharsets.UTF_8), null);


                    }
                }
            }
        });
    }

    /**
     * 检查权限并设置WiFi信息。
     * 如果没有位置权限，则请求权限；如果已经获得权限，更新WiFi信息。
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkPermissionsAndSetup() {
        // 检查是否已经获得ACCESS_FINE_LOCATION权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，向用户请求位置权限
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            // 如果已经有权限，直接获取并更新WiFi信息
            updateWifiInfo();
        }
    }

    /**
     * 获取当前WiFi的SSID并更新EditText。
     * 如果WiFi已连接，则获取SSID并显示在EditText中；如果WiFi未连接或权限不足，则提示相应信息。
     * <p>
     * 2.4 GHz WiFi 的频率范围大约在 2400~2500 MHz。
     * 5 GHz WiFi 的频率范围大约在 4900~5900 MHz。
     * 通过判断 getFrequency() 的返回值，可以判断当前 WiFi 是否为 5G。
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateWifiInfo() {
        // 获取当前WiFi连接信息
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            // 获取SSID
            String ssid = wifiInfo.getSSID();

            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);  // 去掉首尾的双引号
            }

            // 判断是否是5G网络
            int frequency = wifiInfo.getFrequency();
            boolean is5G = frequency >= 4900 && frequency <= 5900;

            // 处理Android 10 (API 29) 及以上版本中SSID可能返回"<unknown ssid>"的问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && "<unknown ssid>".equals(ssid)) {
                Log.d(TAG, "SSID not available");
                EventBus.getDefault().post(new UiToastEvent("SSID not available", true, true));
            } else {
                // 显示当前连接的SSID
                etSsid.setText(ssid);
                if (is5G) {
                    tvIs5Gtips.setText("这可能是一个5GHz Wi-Fi，请选择2.4GHz Wi-Fi");
                } else {
                    tvIs5Gtips.setText("");
                }
            }
        }
    }

    /**
     * 处理权限请求的回调方法。
     * 当用户同意或拒绝权限请求时，该方法会被调用。
     *
     * @param requestCode  请求码，判断是哪一次权限请求
     * @param permissions  请求的权限列表
     * @param grantResults 用户对权限请求的处理结果
     */
    @SuppressLint("MissingSuperCall")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 如果用户授予了权限，更新WiFi信息
            updateWifiInfo();
        } else {
            // 如果用户拒绝了权限请求，显示权限被拒绝的信息
            Log.d(TAG, "Permission Denied");
            EventBus.getDefault().post(new UiToastEvent("Permission Denied", true, true));
        }
    }


    /**
     * 注册一个广播接收器，用于监听网络状态的变化。
     * 当WiFi连接状态发生变化时，更新显示的SSID。
     */
    private void registerNetworkChangeReceiver() {
        // 创建广播接收器，监听网络变化
        networkReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // 检查是否是网络状态变化的广播
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    // 当网络状态变化时，更新WiFi信息
                    updateWifiInfo();
                }
            }
        };

        // 创建IntentFilter，指定接收CONNECTIVITY_ACTION广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        // 注册广播接收器，开始监听网络状态变化
        registerReceiver(networkReceiver, filter);
    }


    /**
     * Wi-Fi 名字
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiNameEvent(WifiNameEvent event) {
        if (!event.getMessage().isEmpty()) {
            Log.d(TAG, "Wi-Fi SSID" + event.getMessage().toString());
            // 显示当前连接的SSID
            etSsid.setText(event.getMessage().toString());
        }
    }

}