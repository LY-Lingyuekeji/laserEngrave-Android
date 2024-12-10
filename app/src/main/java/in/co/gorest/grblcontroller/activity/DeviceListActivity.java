package in.co.gorest.grblcontroller.activity;

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
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.BluetoothDeviceAdapter;
import in.co.gorest.grblcontroller.events.UiToastEvent;


public class DeviceListActivity extends Activity implements BluetoothDeviceAdapter.OnItemClickListener {

    // 用于日志记录的标签
    private final static String TAG = DeviceListActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 绑定的设备
    private RecyclerView pairedRecyclerView;
    // 可用的设备
    private RecyclerView nearbyRecyclerView;

    // 用于在 Intent 中传递或接收蓝牙设备的地址
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    // 权限请求码
    private static final int REQUEST_ENABLE_BT = 1;
    // 本地设备的蓝牙适配器
    private BluetoothAdapter mBtAdapter;
    // 绑定的设备
    private BluetoothDeviceAdapter mPairedAdapter;
    // 可用的设备
    private BluetoothDeviceAdapter mNearbyAdapter;

    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED);

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
        // 绑定的设备
        pairedRecyclerView = findViewById(R.id.recycler_view_paired);
        // 可用的设备
        nearbyRecyclerView = findViewById(R.id.recycler_view_nearby);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 绑定的设备
        pairedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPairedAdapter = new BluetoothDeviceAdapter(new ArrayList<>(), this);
        pairedRecyclerView.setAdapter(mPairedAdapter);
        // 可用的设备
        nearbyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNearbyAdapter = new BluetoothDeviceAdapter(new ArrayList<>(), this);
        nearbyRecyclerView.setAdapter(mNearbyAdapter);
        // 初始化BluetoothAdapter对象
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // 设备不支持蓝牙
        if (mBtAdapter == null) {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_bluetooth_adapter_error), true, true));
            finish();
        }
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_BT);
        } else {
            // 配对列表
            displayPairedDevices();
            // 可用列表
            startDiscovery();
        }
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 已配对列表
     */
    private void displayPairedDevices() {
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
        mPairedAdapter.updateDevices(deviceList);
    }

    /**
     * 可用蓝牙设备
     */
    @SuppressLint("MissingPermission")
    private void startDiscovery() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter.startDiscovery();
    }

    /**
     * 搜索蓝牙广播
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && device.getName() != null) {
                    mNearbyAdapter.addDevice(device);
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(BluetoothDevice device) {
        // 处理点击事件
        Log.d(TAG, "device: Name=" + device.getName() + "-----address=" + device.getAddress() + "-----type=" + device.getType());
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * 权限请求结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayPairedDevices();
                startDiscovery();
            } else {
                Toast.makeText(getApplicationContext(), "请手动开启权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }
}