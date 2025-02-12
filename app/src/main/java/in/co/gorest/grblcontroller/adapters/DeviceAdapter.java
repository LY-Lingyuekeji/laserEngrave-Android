package in.co.gorest.grblcontroller.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.ApModelAddActivity;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.WifiNetwork;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private static final String TAG = DeviceAdapter.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    private Context context;
    private List<WifiNetwork> wifiNetworkList;

    // 是否显示连接设备流程引导
    private boolean isShowConnectDeviceQueue;

    // 构造函数
    public DeviceAdapter(Context context, List<WifiNetwork> wifiNetworkList) {
        this.context = context;
        this.wifiNetworkList = wifiNetworkList;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 加载每个项的布局
        View view = LayoutInflater.from(context).inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), context.getString(R.string.shared_preference_key));

        WifiNetwork wifiNetwork = wifiNetworkList.get(position);
        // 设置 SSID 和 IP 地址
        holder.tvSsid.setText(wifiNetwork.getSsid());
        holder.tvIpAddress.setText(wifiNetwork.getIpAddress());

        // 设置按钮的点击事件（例如连接 Wi-Fi）
        holder.llDeviceItem.setOnClickListener(v -> {
            // TODO 自定义弹窗
            showDeviceDialog(wifiNetwork);
        });
    }

    @Override
    public int getItemCount() {
        return wifiNetworkList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llDeviceItem;
        TextView tvSsid, tvIpAddress;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            llDeviceItem = itemView.findViewById(R.id.ll_device_item);
            tvSsid = itemView.findViewById(R.id.tv_ssid);
            tvIpAddress = itemView.findViewById(R.id.tv_ipaddress);

        }
    }


    // 显示自定义 Dialog
    private void showDeviceDialog(WifiNetwork wifiNetwork) {
        // 创建 Dialog
        Dialog dialog = new Dialog(context, R.style.CustomDialog); // 使用自定义样式
        dialog.setContentView(R.layout.dialog_device);

        // 获取弹窗布局中的根容器
        ViewGroup container = dialog.findViewById(android.R.id.content);  // 根容器

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 设置 Dialog 属性
        TextView tvMachineName = dialog.findViewById(R.id.tv_machine_name);
        TextView tvMachineStatus = dialog.findViewById(R.id.tv_machine_status);
        ImageView ivMachineImage = dialog.findViewById(R.id.iv_machine_image);
        TextView tvMachineSize = dialog.findViewById(R.id.tv_machine_size);
        TextView tvMachineFirmware = dialog.findViewById(R.id.tv_machine_firmware);
        TextView tvLaserModule = dialog.findViewById(R.id.tv_laser_module);
        TextView tvMachineSdCard = dialog.findViewById(R.id.tv_machine_sd);
        TextView tvComponentSize = dialog.findViewById(R.id.tv_component_size);
        Spinner spinnerLaserModule  = dialog.findViewById(R.id.spinner_laser_module);
        TextView tvConfirm = dialog.findViewById(R.id.tv_confirm);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);

        // 设置内容
        tvMachineName.setText(wifiNetwork.getSsid());
        tvMachineStatus.setText("可连接");
        tvMachineSize.setText("300x300(mm²)");
        tvMachineFirmware.setText("ESP_S3");
        tvComponentSize.setText("300x300(mm²)");


        // 设置 Spinner 的监听器
        spinnerLaserModule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 当选中一个项时，更新 tvLaserModule 的文本
                String selectedLaserModule = (String) parentView.getItemAtPosition(position);
                if (spinnerLaserModule.getSelectedItemPosition() == 0) {
                    tvLaserModule.setText("未知");
                } else {
                    tvLaserModule.setText(selectedLaserModule);
                }
                // 保存激光模组
                sharedPref.edit().putString(context.getString(R.string.preference_laser_module), selectedLaserModule).apply();
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
                Toast.makeText(context, "请选择激光型号", Toast.LENGTH_SHORT).show();
                return;
            }
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                // 创建自定义弹窗对象
                BaseAlertDialog baseAlertDialog = new BaseAlertDialog(context);

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
                String ssid = wifiNetwork.getSsid();
                String password = "12345678"; //
                // 连接到 Wi-Fi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    connectToWifiForAndroidQ(context, ssid, password);
                } else {
                    connectToWifi(context, ssid, password);
                }

                dialog.dismiss();
            }




        });
        tvCancel.setOnClickListener(v -> dialog.dismiss());

        // 显示 Dialog
        dialog.show();

    }


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

    /**
     * 将context转换为Activity
     */
    public Activity getActivity() {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;  // 如果context不是Activity，返回null
    }

}
