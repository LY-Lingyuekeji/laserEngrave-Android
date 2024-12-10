package in.co.gorest.grblcontroller.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.BluetoothDeviceAdapter;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class BtConnetModelFragment extends Fragment implements BluetoothDeviceAdapter.OnItemClickListener{
    // 用于日志记录的标签
    private final static String TAG = BtConnetModelFragment.class.getSimpleName();
    // 请求位置权限的请求码
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    // RecyclerView
    private RecyclerView recyclerView;
    // 本地设备的蓝牙适配器
    private BluetoothAdapter bluetoothAdapter;
    // 蓝牙列表适配器
    private BluetoothDeviceAdapter mAdapter;
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;

    public BtConnetModelFragment() {}

    public static BtConnetModelFragment newInstance() { return  new BtConnetModelFragment();}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bt_connect_model, container, false);
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
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    /**
     * 初始化数据
     */
    @SuppressLint("MissingPermission")
    private void initData() {

        // 检查并请求蓝牙权限（运行时）
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // 设置 BluetoothAdapter 并开始扫描蓝牙设备
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.startDiscovery();
        }

        // 注册一个 BroadcastReceiver 以接收蓝牙设备发现事件
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        // 创建 RecyclerView 和适配器来显示扫描到的蓝牙设备
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        mAdapter = new BluetoothDeviceAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {

    }

    /**
     * 定义 BroadcastReceiver
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().contains("btgrblesp")) {
                    mAdapter.addDevice(device);
                }
            }
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(BluetoothDevice device) {
        // type
        sharedPref.edit().putString(getString(R.string.preference_connect_type), "bt").apply();
        // deviceName
        sharedPref.edit().putString(getString(R.string.preference_device_name), device.getName()).apply();
        // deviceAddress
        sharedPref.edit().putString(getString(R.string.preference_device_address), device.getAddress()).apply();

        // 关闭页面
        getActivity().finish();
    }

}