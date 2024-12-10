package in.co.gorest.grblcontroller.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nbsp.materialfilepicker.MaterialFilePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.InetAddress;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.events.WifiNameEvent;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class LocalFileFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = LocalFileFragment.class.getSimpleName();


    // 选择文件
    private TextView tvChooseFile;


    public LocalFileFragment() {
    }

    public static LocalFileFragment newInstance() {
        return new LocalFileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_file, container, false);
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
        // 选择文件
        tvChooseFile = view.findViewById(R.id.tv_choose_file);
    }

    /**
     * 初始化数据
     */
    private void initData() {

    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 选择文件
        tvChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(getActivity())
                        .withCloseMenu(true)
                        .withRequestCode(Constants.FILE_PICKER_REQUEST_CODE)
                        .withHiddenFiles(false)
                        .withFilter(Pattern.compile(Constants.SUPPORTED_FILE_TYPES_STRING, Pattern.CASE_INSENSITIVE))
                        .withTitle(GrblUtils.implode(" | ", Constants.SUPPORTED_FILE_TYPES))
                        .withPath("/storage/")
                        .start();
            }
        });
    }
}