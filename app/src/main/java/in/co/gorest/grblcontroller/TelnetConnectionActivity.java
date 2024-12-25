package in.co.gorest.grblcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.core.app.ActivityCompat;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.lang.ref.WeakReference;
import java.util.Objects;
import in.co.gorest.grblcontroller.activity.DeviceListActivity;
import in.co.gorest.grblcontroller.activity.WifiListActivity;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.events.APModelUploadEvent;
import in.co.gorest.grblcontroller.events.AfterUploadFileEvent;
import in.co.gorest.grblcontroller.events.BluetoothDisconnectEvent;
import in.co.gorest.grblcontroller.events.FilePathEvent;
import in.co.gorest.grblcontroller.events.GrblSettingMessageEvent;
import in.co.gorest.grblcontroller.events.JogCommandEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.service.GrblBluetoothSerialService;
import in.co.gorest.grblcontroller.service.GrblTelnetSerialService;
import in.co.gorest.grblcontroller.util.FileUploader;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class TelnetConnectionActivity extends BaseActivity {

    private GrblServiceMessageHandler grblServiceMessageHandler;
    private BluetoothAdapter bluetoothAdapter = null;
    private WifiManager wifiManager = null;
    private boolean mBound = false;
    private boolean wifiBound = false;
    private String mConnectedDeviceName = null;

    private String connectType;

    private static final String TAG = TelnetConnectionActivity.class.getSimpleName();


    private Handler handler = new Handler();
    private boolean isLongPress = false;
    private int pressedKeyCode = -1; // 用于存储按下的keyCode


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectType = sharedPref.getString(getString(R.string.connect_type), "AP");



        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);



        // AP模式
        if (wifiManager == null) {
            showToastMessage(getString(R.string.text_no_wifi_manager));
            restartInUsbMode();
        } else {
            Log.d(TAG, "GrblTelnetSerialService create");
            Intent intent = new Intent(getApplicationContext(), GrblTelnetSerialService.class);
            bindService(intent, wifiServiceConnection, Context.BIND_AUTO_CREATE);
        }

        grblServiceMessageHandler = new TelnetConnectionActivity.GrblServiceMessageHandler(this);


        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();

            // AP模式
            if (!wifiManager.isWifiEnabled()) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            // 检查蓝牙权限
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                                EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_no_wifi_permission), true, true));
                                restartInUsbMode();
                            }
                            // 尝试启用Wi-Fi
                            wifiManager.setWifiEnabled(true);
                        } catch (RuntimeException e) {
                            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_no_wifi_permission), true, true));
                            restartInUsbMode();
                        }
                    }
                };
                thread.start();
            }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onGcodeCommandReceived("$10=1");

        // Wi-Fi
        if (wifiBound) {
            grblTelnetSerialService.setMessageHandler(null);
            unbindService(wifiServiceConnection);
            wifiBound = false;
            stopService(new Intent(this, GrblTelnetSerialService.class));
        }

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (grblTelnetSerialService != null)
            onBluetoothStateChange(grblTelnetSerialService.getState());
    }

    /**
     * OTG模式
     */
    private void restartInUsbMode() {
        sharedPref.edit().putString(getString(R.string.text_default_connection), Constants.SERIAL_CONNECTION_TYPE_USB_OTG).apply();
        startActivity(new Intent(this, UsbConnectionActivity.class));
        finish();
    }


    /**
     * Telnet连接 -- AP模式
     *
     * @param ip   ip地址
     * @param port 端口号
     */
    private void connectToTelnet(String ip, int port) {
        Intent intent = new Intent(getApplicationContext(), GrblTelnetSerialService.class);
        intent.putExtra(GrblTelnetSerialService.KEY_IP_ADDRESS, ip);
        intent.putExtra(GrblTelnetSerialService.KEY_PORT, port);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            getApplicationContext().startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * 初始化菜单项目
     *
     * @param menu The options menu in which you place your items.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem actionConnect = menu.findItem(R.id.action_connect);
        if (grblBluetoothSerialService != null) {
            if (grblBluetoothSerialService.getState() == GrblBluetoothSerialService.STATE_CONNECTED) {
                actionConnect.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_bluetooth).colorRes(R.color.colorWhite).sizeDp(24));
                actionConnect.setTitle(R.string.text_disconnect);
            } else {
                actionConnect.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_bluetooth_b).colorRes(R.color.colorWhite).sizeDp(24));
                actionConnect.setTitle(R.string.text_connect);
            }
        } else {
            actionConnect.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_bluetooth_b).colorRes(R.color.colorWhite).sizeDp(24));
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单选项
     *
     * @param item The menu item that was selected.
     * @return
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // 蓝牙模式
            case R.id.action_connect:
                if (machineStatus.getState().equals(Constants.MACHINE_STATUS_RUN) || machineStatus.getState().equals(Constants.MACHINE_STATUS_HOLD)) {
                    EventBus.getDefault().post(new UiToastEvent("Machine is Running or Hold, please try after machinestatus IDLE", true, true));
                } else {
                    // 设置为蓝牙模式
                    sharedPref.edit().putString(getString(R.string.connect_type), "BT").apply();
                    connectType = sharedPref.getString(getString(R.string.connect_type), "AP");
                    Log.d(TAG, "connectType=" + connectType);
                    // 蓝牙可用
                    if (bluetoothAdapter.isEnabled()) {
                        if (grblBluetoothSerialService != null) {
                            if (grblBluetoothSerialService.getState() == GrblBluetoothSerialService.STATE_CONNECTED) {
                                new AlertDialog.Builder(this)
                                        .setTitle(R.string.text_disconnect)
                                        .setMessage(getString(R.string.text_disconnect_confirm))
                                        .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
                                            onGcodeCommandReceived("$10=1");
                                            if (grblBluetoothSerialService != null) {
                                                grblBluetoothSerialService.disconnectService();
                                                if (mBound) {
                                                    unbindService(serviceConnection);
                                                    mBound = false;
                                                    Log.d(TAG, "mBound=" + mBound);
                                                }
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.text_cancel), null)
                                        .show();

                            } else {
                                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                                startActivityForResult(serverIntent, Constants.CONNECT_DEVICE_INSECURE);
                            }
                        } else {
                            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_bt_service_not_running), true, true));
                        }
                    } else {
                        showToastMessage(getString(R.string.text_bt_not_enabled));
                    }
                }
                return true;
            // AP模式
            case R.id.action_grbl_wifi:
                if (machineStatus.getState().equals(Constants.MACHINE_STATUS_RUN) || machineStatus.getState().equals(Constants.MACHINE_STATUS_HOLD)) {
                    EventBus.getDefault().post(new UiToastEvent("Machine is Running or Hold, please try after machinestatus IDLE", true, true));
                } else {
                    sharedPref.edit().putString(getString(R.string.connect_type), "AP").apply();
                    connectType = sharedPref.getString(getString(R.string.connect_type), "AP");
                    Log.d(TAG, "connectType=" + connectType);
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("测试阶段，请确保您已手动连接到WIFI\n名称：GRBL_ESP\n若未连接可能出现意外情况，请先手动连接")
                            .setPositiveButton("OK", (dialog, which) -> {
                                handleWifiConnection();
                                dialog.dismiss();
                            }).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Wi-Fi连接
     */
    private void handleWifiConnection() {
        if (grblTelnetSerialService != null && grblTelnetSerialService.getState() == GrblTelnetSerialService.STATE_CONNECTED) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.text_disconnect)
                    .setMessage(getString(R.string.text_disconnect_confirm))
                    .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
                        if (grblTelnetSerialService != null) {
                            grblTelnetSerialService.disconnectService();
                            if (wifiBound) {
                                unbindService(wifiServiceConnection);
                                wifiBound = false;
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.text_cancel), null)
                    .show();
        } else {
            Intent serverIntent = new Intent(getApplicationContext(), WifiListActivity.class);
            startActivityForResult(serverIntent, Constants.CONNECT_WIFI);
        }
    }

    /**
     * 蓝牙服务 -- 蓝牙模式
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GrblBluetoothSerialService.GrblSerialServiceBinder binder = (GrblBluetoothSerialService.GrblSerialServiceBinder) service;
            grblBluetoothSerialService = binder.getService();
            mBound = true;
            grblBluetoothSerialService.setMessageHandler(grblServiceMessageHandler);
            grblBluetoothSerialService.setStatusUpdatePoolInterval(Long.parseLong(sharedPref.getString(getString(R.string.preference_update_pool_interval), String.valueOf(Constants.GRBL_STATUS_UPDATE_INTERVAL))));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Intent intent = new Intent(getApplicationContext(), GrblBluetoothSerialService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    /**
     * Telnet服务 -- AP模式
     */
    private final ServiceConnection wifiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GrblTelnetSerialService.GrblTelnetSerialServiceBinder binder = (GrblTelnetSerialService.GrblTelnetSerialServiceBinder) service;
            grblTelnetSerialService = binder.getService();
            wifiBound = true;
            grblTelnetSerialService.setMessageHandler(grblServiceMessageHandler);
            grblTelnetSerialService.setStatusUpdatePoolInterval(Long.parseLong(sharedPref.getString(getString(R.string.preference_update_pool_interval), String.valueOf(Constants.GRBL_STATUS_UPDATE_INTERVAL))));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            wifiBound = false;
            Intent intent = new Intent(getApplicationContext(), GrblTelnetSerialService.class);
            bindService(intent, wifiServiceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    /**
     * 从蓝牙服务/Telnet服务发送到主线程得消息
     */
    private static class GrblServiceMessageHandler extends Handler {

        private final WeakReference<TelnetConnectionActivity> mActivity;

        GrblServiceMessageHandler(TelnetConnectionActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    mActivity.get().onBluetoothStateChange(msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mActivity.get().mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mActivity.get().showToastMessage(mActivity.get().getString(R.string.text_connected_to) + " " + mActivity.get().mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    mActivity.get().showToastMessage(msg.getData().getString(Constants.TOAST));
                    break;
            }
        }
    }

    /**
     * 更新连接状态
     *
     * @param currentState
     */
    private void onBluetoothStateChange(int currentState) {
        Log.d(TAG, "State=" + currentState);
        switch (currentState) {
            case GrblBluetoothSerialService.STATE_CONNECTED:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setSubtitle((mConnectedDeviceName != null) ? mConnectedDeviceName : getString(R.string.text_connected));
                invalidateOptionsMenu();
                break;
            case GrblBluetoothSerialService.STATE_CONNECTING:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setSubtitle(getString(R.string.text_connecting));
                break;
            case GrblBluetoothSerialService.STATE_LISTEN:
                break;
            case GrblBluetoothSerialService.STATE_NONE:
                EventBus.getDefault().post(new BluetoothDisconnectEvent(getString(R.string.text_connection_lost)));
                MachineStatusListener.getInstance().setState(Constants.MACHINE_STATUS_NOT_CONNECTED);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setSubtitle(getString(R.string.text_not_connected));
                invalidateOptionsMenu();
                break;
        }
    }

    /**
     * 控制命令
     *
     * @param command
     */
    @Override
    public void onGcodeCommandReceived(String command) {
        if ("BT".equals(connectType)) {
            if (grblBluetoothSerialService != null)
                grblBluetoothSerialService.serialWriteString(command);
        } else if ("AP".equals(connectType)) {
            if (grblTelnetSerialService != null)
                grblTelnetSerialService.serialWriteString(command);
        }

    }

    /**
     * 实时控制命令
     *
     * @param command
     */
    public void onGrblRealTimeCommandReceived(byte command) {
        if ("BT".equals(connectType)) {
            if (grblBluetoothSerialService != null)
                grblBluetoothSerialService.serialWriteByte(command);
        } else if ("AP".equals(connectType)) {
            if (grblTelnetSerialService != null)
                grblTelnetSerialService.serialWriteByte(command);
        }
    }

    /**
     * 移动
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJogCommandEvent(JogCommandEvent event) {
        if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_JOG)) {
            if (machineStatus.getPlannerBuffer() > 5) onGcodeCommandReceived(event.getCommand());
        }
    }

    /**
     * 设置
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGrblSettingMessageEvent(GrblSettingMessageEvent event) {
        if (event.getSetting().equals("$10") && !event.getValue().equals("2")) {
            onGcodeCommandReceived("$10=2");
        }

        if (event.getSetting().equals("$110") || event.getSetting().equals("$111") || event.getSetting().equals("$112")) {
            double maxFeedRate = Double.parseDouble(event.getValue());
            if (maxFeedRate > sharedPref.getDouble(getString(R.string.preference_jogging_max_feed_rate), machineStatus.getJogging().feed)) {
                sharedPref.edit().putDouble(getString(R.string.preference_jogging_max_feed_rate), maxFeedRate).apply();
            }
        }

        if (event.getSetting().equals("$32")) {
            machineStatus.setLaserModeEnabled(event.getValue().equals("1"));
        }

    }

    /**
     * AP模式文件上传
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAPModelUploadEvent(APModelUploadEvent event) {
        Log.d(TAG, event.getMessage().toString());
//        // 上传文件断开连接
        if (grblTelnetSerialService != null) {
            grblTelnetSerialService.disconnectService();
            if (wifiBound) {
                unbindService(wifiServiceConnection);
                wifiBound = false;
            }
        }

        // 开始上传文件
        String filePath = event.getMessage().toString();
        String url = "http://192.168.0.1/upload";
        FileUploader uploader = new FileUploader();
        uploader.uploadFile(filePath, url, this);
    }

    /**
     * 文件上传后
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadFileEvent(AfterUploadFileEvent event) {
        Log.d(TAG, event.getMessage().toString());
        if (!event.getMessage().isEmpty()) {
            // 重新连接Telnet
            if (grblTelnetSerialService != null) {
                connectToTelnet("192.168.0.1", 23);
            } else {
                if (wifiManager == null) {
                    showToastMessage(getString(R.string.text_no_wifi_manager));
                    restartInUsbMode();
                } else {
                    Log.d(TAG, "GrblTelnetSerialService create");
                    Intent intent = new Intent(getApplicationContext(), GrblTelnetSerialService.class);
                    bindService(intent, wifiServiceConnection, Context.BIND_AUTO_CREATE);

                    connectToTelnet("192.168.0.1", 23);
                }
            }
        }
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // AP模式
            case Constants.CONNECT_WIFI:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String ip = Objects.requireNonNull(data.getExtras()).getString(WifiListActivity.EXTRA_WIFI_IP);
                        int port = Objects.requireNonNull(data.getExtras()).getInt(WifiListActivity.EXTRA_WIFI_PORT, 23);

                        if (grblTelnetSerialService != null) {
                            connectToTelnet(ip, port);
                        } else {
                            EventBus.getDefault().post(new UiToastEvent("GrblTelnetSerialService is null", true, true));
                            Log.d(TAG, "grblTelnetSerialService is null");
                        }
                    } catch (NullPointerException ignored) {
                    }
                }
                break;
            // 图片选择
            case ImgUtil.CHOOSE_PHOTO:
                if (data != null) {
                    Uri uri = data.getData();
                    Log.d("BluetoothConnectionActivity", "uri=" + uri);
                    EventBus.getDefault().post(new FilePathEvent(ImgUtil.CHOOSE_PHOTO, uri.toString()));
                }
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(TAG, "onKeyDown=" + pressedKeyCode);

        if (event.getRepeatCount() == 0) { // 确保只在第一次按下时处理
            pressedKeyCode = keyCode; // 存储按下的keyCode
            handler.postDelayed(longPressRunnable, 500); // 启动500毫秒的计时器
        }
        return true; // 确保事件不会继续传递
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.d(TAG, "onKeyUp=" + pressedKeyCode);

        handler.removeCallbacks(longPressRunnable); // 按键抬起时取消长按计时器

        if (isLongPress) {
            // 停止动作
            onGcodeCommandReceived("!");
        } else {
            Log.d(TAG, "keyCode=" + pressedKeyCode);
            switch (pressedKeyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    sendJogCommand("$J=%s G91 Y%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    sendJogCommand("$J=%s G91 Y-%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    sendJogCommand("$J=%s G91 X-%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    sendJogCommand("$J=%s G91 X%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP_LEFT:
                    sendJogCommand("$J=%s G91 X-%s Y%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
                    sendJogCommand("$J=%s G91 X%s Y%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
                    sendJogCommand("$J=%s G91 X-%s Y-%s F%s");
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
                    sendJogCommand("$J=%s G91 X%s Y-%s F%s");
                    return true;
            }
        }

        isLongPress = false; // 重置长按标志
        pressedKeyCode = -1; // 重置keyCode
        return true;
    }

    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "pressedKeyCode=" + pressedKeyCode);
            isLongPress = true;
            switch (pressedKeyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    onGcodeCommandReceived("$J=G21 G91 Y999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    onGcodeCommandReceived("$J=G21 G91 Y-999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    onGcodeCommandReceived("$J=G21 G91 X-999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    onGcodeCommandReceived("$J=G21 G91 X999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_UP_LEFT:
                    sendJogCommand("$J=G21 G91 X-999.0 Y999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
                    sendJogCommand("$J=G21 G91 X999.0 Y999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
                    sendJogCommand("$J=G21 G91 X-999.0 Y-999.0 F2400.0");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
                    sendJogCommand("$J=G21 G91 X999.0 Y-999.0 F2400.0");
                    break;
            }
        }
    };

    private void sendJogCommand(String tag) {
        Log.d(TAG, "tag=" + tag);
        if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_JOG)) {

            String units = "G21";
            double jogFeed = machineStatus.getJogging().feed;

            if (machineStatus.getJogging().inches) {
                units = "G20";
                jogFeed = jogFeed / 25.4;
            }

            Double stepSize;
            if (tag.toUpperCase().contains("Z")) {
                stepSize = machineStatus.getJogging().stepZ;
            } else {
                stepSize = machineStatus.getJogging().stepXY;
            }

            String jog = String.format(tag, units, stepSize, jogFeed);
            EventBus.getDefault().post(new JogCommandEvent(jog));
        } else {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
        }
    }
}
