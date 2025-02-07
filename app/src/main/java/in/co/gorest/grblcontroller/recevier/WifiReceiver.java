package in.co.gorest.grblcontroller.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.MainActivity;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.MaterialActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class WifiReceiver extends BroadcastReceiver {

    // TAG
    private final String TAG = WifiReceiver.class.getSimpleName();
    /**
     * 用于管理和访问增强的共享偏好设置实例。
     * 通过 {@link EnhancedSharedPreferences} 提供更强大的共享偏好设置功能。
     */
    protected EnhancedSharedPreferences sharedPref;
    // 获取 WifiManager 实例
    private WifiManager wifiManager;
    // 用于存储上次连接的 WiFi SSID
    private static String lastSSID = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), "in.co.gorest.grblcontroller.f635f2e523d7c64f5b4ad9179b689433");

        // 获取 WifiManager 实例
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // 获取当前 WiFi 连接的信息
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSSID = wifiInfo.getSSID();

        // 获取 WiFi 状态变化的 action
        String action = intent.getAction();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            // WiFi 状态变化（启用/禁用）
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                // WiFi 已开启
            } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                // WiFi 已关闭
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            // 网络连接状态变化
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                // WiFi 已连接
                if (currentSSID.contains("MKS")) {
                    int ipAddress = wifiInfo.getIpAddress();
                    String ip = Formatter.formatIpAddress(ipAddress);
                    Log.d(TAG, "Connected Wi-Fi IP Address: " + ip);
                    // 连接Telnet
                    EventBus.getDefault().post(new DeviceConnectEvent("Telnet", currentSSID, ip));
                }

                // 保存当前连接的 SSID 作为下一次对比的基准
                lastSSID = currentSSID;
            } else {
                // WiFi 断开连接
                if (lastSSID.contains("MKS") && !currentSSID.equals(lastSSID)) {
                   Log.d(TAG, "断开连接");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        connectToWifiForAndroidQ(context, lastSSID, "12345678");
                    } else {
                        connectToWifi(context, lastSSID, "12345678");
                    }
                }
            }
        }
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
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = Formatter.formatIpAddress(ipAddress);
                Log.d(TAG, "Connected Wi-Fi IP Address: " + ip);
                if (ssid.contains("MKS")) {
                    // 连接Telnet
                    EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, ip));
                } else {
                    String host = sharedPref.getString("preference_sta_host", "");
                    if (!TextUtils.isEmpty(host)) {
                        // 连接Telnet
                        EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, host));
                    }
                }
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
                if (ssid.contains("MKS")) {
                    // 连接Telnet
                    EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, ip));
                } else {
                    String host = sharedPref.getString("preference_sta_host", "");
                    if (!TextUtils.isEmpty(host)) {
                        // 连接Telnet
                        EventBus.getDefault().post(new DeviceConnectEvent("Telnet", ssid, host));
                    }
                }

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
}
