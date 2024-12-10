package in.co.gorest.grblcontroller.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.ConnectActivity;
import in.co.gorest.grblcontroller.events.AfterUploadFileEvent;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.events.WifiNameEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.service.GrblTelnetSerialService;

public class WiFiConnetModelFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = WiFiConnetModelFragment.class.getSimpleName();
    // Activity
    private Activity mActivity;
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

    public WiFiConnetModelFragment() {
    }

    public static WiFiConnetModelFragment newInstance() {
        return new WiFiConnetModelFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 在Fragment销毁时注销广播接收器，防止内存泄漏
        if (networkReceiver != null) {
            requireContext().unregisterReceiver(networkReceiver);
        }
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_connect_model, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化界面
        initView(view);
        // 初始化数据
        initData();
        // 初始化事件监听
        setupListeners();


        new Thread(new Runnable() {
            @Override
            public void run() {
                scanNetworkInParallel("192.168.31");
            }
        }).start();

    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // Wi-Fi名称（SSID）
        etSsid = view.findViewById(R.id.et_ssid);
        // 选择网络
        tvChooseWifi = view.findViewById(R.id.tv_choose_wifi);
        // 5GHz Wi-Fi提示
        tvIs5Gtips = view.findViewById(R.id.tv_is5G_tips);
        // Wi-Fi密码
        etPassword = view.findViewById(R.id.et_password);
        // 密码可见/不可见
        ivPasswordVisibility = view.findViewById(R.id.iv_password_visibility);
        // 密码提示
        tvPasswordTips = view.findViewById(R.id.tv_password_tips);
        // 下一步
        tvNext = view.findViewById(R.id.tv_next);
    }

    /**
     * 初始化数据
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    private void initData() {
        // 获取系统的WifiManager实例
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 检查权限并初始化WiFi信息
        checkPermissionsAndSetup();
        // 注册网络状态变化的广播接收器
        registerNetworkChangeReceiver();
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
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
                if (s.toString().contains("MKS")) {
                    etPassword.setText("12345678");
                }
            }
        });

        // 选择网络
        tvChooseWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示选择WiFi弹窗
                WifiChooseBottomSheetFragment wifiChooseBottomSheetFragment = new WifiChooseBottomSheetFragment();
                wifiChooseBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
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
                if (!etSsid.getText().toString().contains("MKS")) {
                    Toast.makeText(mActivity, "请选择名称为的 \"MKS_xxx\" Wi-Fi进行连接", Toast.LENGTH_SHORT).show();
                } else {
                    if (etPassword.getText().length() < 8) {
                        tvPasswordTips.setTextColor(Color.parseColor("#ff1135"));
                    } else {
                        tvPasswordTips.setTextColor(Color.parseColor("#000000"));
                        // 连接WIFI
                        if (!etSsid.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty()) {
                            Log.d(TAG, "SSID=" + etSsid.getText().toString() + "----- Password=" + etPassword.getText().toString());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                connectToWifiForAndroidQ(getContext(), etSsid.getText().toString(), etPassword.getText().toString());
                            } else {
                                connectToWifi(getContext(), etSsid.getText().toString(), etPassword.getText().toString());
                            }
                        }
                    }
                }

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String subnet = "192.168.31"; // 替换为实际网段
//                        scanNetwork(subnet);
//                    }
//                }).start();


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
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
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
        requireContext().registerReceiver(networkReceiver, filter);
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

                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, "192.168.4.1"));

                if (mActivity != null) {
                    // 关闭当前页面
                    mActivity.finish();
                }

            }

            @Override
            public void onUnavailable() {
                if (mActivity != null) {
                    // Connection failed
                    Toast.makeText(mActivity, "连接失败，请重新连接！", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    /**
     * 通过SSID和密码连接WIFI
     *
     * @param context  上下文
     * @param ssid     SSID
     * @param password 密码
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

                // 连接Telnet
                EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, "192.168.4.1"));

                if (mActivity != null) {
                    // 关闭当前页面
                    mActivity.finish();
                }

            }

            @Override
            public void onLost(Network network) {
                // Network is lost
                super.onLost(network);
                connectivityManager.unregisterNetworkCallback(this);

                if (mActivity != null) {
                    // Connection failed
                    Toast.makeText(mActivity, "连接失败，请重新连接！", Toast.LENGTH_SHORT).show();
                }
            }
        });
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


    // 扫描指定子网范围的IP
    public static void scanNetwork(String subnet, int start, int end) {
        for (int i = start; i <= end; i++) {
            String host = subnet + "." + i;
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address.isReachable(500)) { // 超时设置为 500ms
                    Log.d(TAG, "Device Found: " + host + " - " + address.getHostName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 创建线程池并分段扫描
    public static void scanNetworkInParallel(String subnet) {
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

            executor.submit(() -> scanNetwork(subnet, segmentStart, segmentEnd)); // 提交扫描任务
        }

        // 关闭线程池
        executor.shutdown();
    }

}