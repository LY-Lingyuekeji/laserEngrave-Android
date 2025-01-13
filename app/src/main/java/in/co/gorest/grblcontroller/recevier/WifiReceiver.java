package in.co.gorest.grblcontroller.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取 WifiManager 实例
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // 获取当前 WiFi 连接的状态
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int networkId = wifiInfo.getNetworkId();

        // 获取 WiFi 状态变化广播的 action
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
                String connectedSSID = wifiInfo.getSSID();
                // 你可以根据连接的 SSID 执行某些动作
            } else {
                // WiFi 断开连接
            }
        }
    }
}
