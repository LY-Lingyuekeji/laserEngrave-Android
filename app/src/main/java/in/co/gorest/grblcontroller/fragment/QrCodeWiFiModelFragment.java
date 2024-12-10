package in.co.gorest.grblcontroller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.EditActivity;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class QrCodeWiFiModelFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = QrCodeWiFiModelFragment.class.getSimpleName();
    // 用于管理Wi-Fi状态的WifiManager
    private WifiManager wifiManager;
    // 用于监听网络状态变化的广播接收器
    private BroadcastReceiver networkReceiver;
    // 二维码
    private ImageView ivQrCode;
    // WPA模式
    private TextView tvWpaModel;
    // WEP模式
    private TextView tvWepModel;
    // 无密码模式
    private TextView tvNullModel;
    // ssid输入框
    private EditText etSsid;
    // 密码
    private LinearLayout llPassword;
    // 密码输入框
    private EditText etPassword;
    // 下一步
    private TextView tvNext;
    // 模式
    private String modelStr = "WPA/WPA2";

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 在Fragment销毁时注销广播接收器，防止内存泄漏
        if (networkReceiver != null) {
            requireContext().unregisterReceiver(networkReceiver);
        }
    }

    public QrCodeWiFiModelFragment() {
    }

    public static QrCodeWiFiModelFragment newInstance() {
        return new QrCodeWiFiModelFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qrcode_wifi_model, container, false);
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
        // 二维码
        ivQrCode = view.findViewById(R.id.iv_qr_code);
        // WPA模式
        tvWpaModel = view.findViewById(R.id.tv_wpa_model);
        // WEP模式
        tvWepModel = view.findViewById(R.id.tv_wep_model);
        // 无密码模式
        tvNullModel = view.findViewById(R.id.tv_null_model);
        // ssid输入框
        etSsid = view.findViewById(R.id.et_ssid);
        // 密码
        llPassword = view.findViewById(R.id.ll_password);
        // 密码输入框
        etPassword = view.findViewById(R.id.et_password);
        // 下一步
        tvNext = view.findViewById(R.id.tv_next);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置二维码
        setBarcodeToImageView(ivQrCode, "WIFI:T:" + modelStr + ";S:" + etSsid.getText().toString() + ";P:" + etPassword.getText().toString() + ";");
        // 获取系统的WifiManager实例
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 检查权限并初始化WiFi信息
        checkPermissionsAndSetup();
        // 注册网络状态变化的广播接收器
        registerNetworkChangeReceiver();

        tvWpaModel.setSelected(true);
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // WPA模式
        tvWpaModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvWpaModel.setSelected(true);
                tvWepModel.setSelected(false);
                tvNullModel.setSelected(false);
                llPassword.setVisibility(View.VISIBLE);
                modelStr = "WPA/WPA2";
                setBarcodeToImageView(ivQrCode, "WIFI:T:" + modelStr + ";S:" + etSsid.getText().toString() + ";P:" + etPassword.getText().toString() + ";");
            }
        });

        // WEP模式
        tvWepModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvWpaModel.setSelected(false);
                tvWepModel.setSelected(true);
                tvNullModel.setSelected(false);
                llPassword.setVisibility(View.VISIBLE);
                modelStr = "WEP";
                setBarcodeToImageView(ivQrCode, "WIFI:T:" + modelStr + ";S:" + etSsid.getText().toString() + ";P:" + etPassword.getText().toString() + ";");
            }
        });

        // 无密码模式
        tvNullModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvWpaModel.setSelected(false);
                tvWepModel.setSelected(false);
                tvNullModel.setSelected(true);
                llPassword.setVisibility(View.GONE);
                modelStr = "nopass";
                setBarcodeToImageView(ivQrCode, "WIFI:T:" + modelStr + ";S:" + etSsid.getText().toString() + ";P:;");
            }
        });

        // SSID
        etSsid.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 更新UI的操作
                        if (modelStr.equals("nopass")) {
                            setBarcodeToImageView(ivQrCode,  "WIFI:T:" + modelStr + ";S:" + s.toString() + ";P:;");
                        } else {
                            setBarcodeToImageView(ivQrCode, "WIFI:T:" + modelStr + ";S:" + s.toString() + ";P:" + etPassword.getText().toString() + ";");
                        }
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 密码
        etPassword.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 更新UI的操作
                        if (modelStr.equals("nopass")) {
                            setBarcodeToImageView(ivQrCode,  "WIFI:T:" + modelStr + ";S:" + etSsid.getText().toString() + ";P:;");
                        } else {
                            setBarcodeToImageView(ivQrCode, "WIFI:T:" + modelStr + ";S:" + etSsid.getText().toString() + ";P:" + s.toString() + ";");
                        }
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });


        // 下一步
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个与LinearLayout大小相同的Bitmap对象
                Bitmap bitmap = Bitmap.createBitmap(ivQrCode.getWidth(), ivQrCode.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                ivQrCode.draw(canvas);
                File barcodeBitmap = ImgUtil.saveBitmap("qrcode" + System.currentTimeMillis() + ".png", bitmap);
                Uri imageUris = Uri.fromFile(barcodeBitmap);
                Intent intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                intent.putExtra("businessType", 1);
                getActivity().startActivity(intent);
                getActivity().finish();
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
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
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

            // 处理Android 10 (API 29) 及以上版本中SSID可能返回"<unknown ssid>"的问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && "<unknown ssid>".equals(ssid)) {
                Log.d(TAG, "SSID not available");
                EventBus.getDefault().post(new UiToastEvent("SSID not available", true, true));
            } else {
                // 显示当前连接的SSID
                etSsid.setText(ssid);
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
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
     * 设置条形码到ImageView
     *
     * @param imageView 指定的ImageView
     * @param text      文字
     */
    public static void setBarcodeToImageView(ImageView imageView, String text) {
        int width = imageView.getWidth(); // 获取ImageView的宽度
        int height = imageView.getHeight(); // 获取ImageView的高度
        if (width <= 0 || height <= 0) {
            width = 200;
            height = 200;
        }

        try {
            Log.d(TAG, "w=" + width + "--h=" + height);
            generateBarcode(imageView, text, width, height);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为条形码
     *
     * @param imageView imageView
     * @param text      内容
     * @param width     宽度
     * @param height    高度
     */
    private static void generateBarcode(ImageView imageView, String text, int width, int height) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        Bitmap bitmap = Bitmap.createBitmap(bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        imageView.setImageBitmap(bitmap);
    }
}