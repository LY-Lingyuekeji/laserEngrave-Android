package in.co.gorest.grblcontroller.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.ConnectActivity;
import in.co.gorest.grblcontroller.adapters.BluetoothDeviceAdapter;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.adapters.WifiChooseBottomSheetAdapter;
import in.co.gorest.grblcontroller.events.WifiNameEvent;

public class WifiChooseBottomSheetFragment extends BottomSheetDialogFragment implements WifiChooseBottomSheetAdapter.OnItemClickListener{
    // 可用Wi-Fi列表
    private RecyclerView rvEnableWifiList;
    // 去系统设置选择Wi-Fi
    private RelativeLayout rlSystemChoose;
    // 取消
    private TextView tvCancel;
    // 用于管理Wi-Fi状态的WifiManager
    private WifiManager wifiManager;
    // wifiList数据源
    private ArrayList<String> wifiList = new ArrayList<>();
    // 适配器
    private WifiChooseBottomSheetAdapter adapter;

    public WifiChooseBottomSheetFragment() {
    }


    public static WifiChooseBottomSheetFragment newInstance() {
        return new WifiChooseBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(wifiScanReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_choose_bottom_sheet, container, false);
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
        // 可用Wi-Fi列表
        rvEnableWifiList = view.findViewById(R.id.rv_enable_wifi_list);
        // 去系统设置选择Wi-Fi
        rlSystemChoose = view.findViewById(R.id.rl_system_choose);
        // 取消
        tvCancel = view.findViewById(R.id.tv_cancel);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        rvEnableWifiList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WifiChooseBottomSheetAdapter(wifiList,this);
        rvEnableWifiList.setAdapter(adapter);
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 去系统设置选择Wi-Fi
        rlSystemChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开系统 Wi-Fi 设置
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // 取消
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            @SuppressLint("MissingPermission") List<ScanResult> results = wifiManager.getScanResults();
            wifiList.clear();
            for (ScanResult scanResult : results) {
                if (scanResult.frequency > 2400 && scanResult.frequency < 2500 && !scanResult.SSID.isEmpty()) {
                    wifiList.add(scanResult.SSID);
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(String ssid) {
        // 传递ssid
        EventBus.getDefault().post(new WifiNameEvent(ssid));
        // 隐藏弹窗
        dismiss();
    }
}