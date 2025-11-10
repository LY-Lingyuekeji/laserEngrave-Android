
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.events.ScanResultMessageEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.fragment.QrCodeAudioModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeBusinessCardModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeCopyModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeTextModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeVideoModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeWiFiModelFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.QRCodeProvider;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class QrCodeActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private static final String TAG = QrCodeActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 保存
    private ImageView ivSave;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
    // 文本
    private TextView tvTextModel;
    // WiFi
    private TextView tvWifiModel;
    // 名片
    private TextView tvBusinessCardModel;
    // 复制
    private TextView tvCopyModel;
    // 音频
    private TextView tvAudioModel;
    // 视频
    private TextView tvVideoModel;
    // 分页
    private ViewPager2 viewPagerQrcode;
    //fragment数组
    private ArrayList<Fragment> fragments = new ArrayList<>();
    // PagerAdapter
    private ViewPagerAdapter adapter;
    // 门警告弹窗
    private Dialog dialogDoorWarning;
    // 火焰警告弹窗
    private Dialog dialogFireWarning;
    // 倾斜警告弹窗
    private Dialog dialogProbeWarning;
    // 是否震动提醒
    private boolean isOpenVibrateAlert;
    // 震动提醒持续时长
    private int vibrateAlertTime;



    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 绑定视图
        DataBindingUtil.setContentView(this, R.layout.activity_qrcode);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));

        // 注册EventBus
        EventBus.getDefault().register(this);

        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 保存
        ivSave = findViewById(R.id.iv_save);
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
        // 文本
        tvTextModel = findViewById(R.id.tv_text_model);
        // Wi-Fi
        tvWifiModel = findViewById(R.id.tv_wifi_model);
        // 名片
        tvBusinessCardModel = findViewById(R.id.tv_business_card_model);
        // 复制
        tvCopyModel = findViewById(R.id.tv_copy_model);
        // 音频
        tvAudioModel = findViewById(R.id.tv_audio_model);
        // 输入框
        tvVideoModel = findViewById(R.id.tv_video_model);
        // 分页
        viewPagerQrcode = findViewById(R.id.view_pager_qrcode);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 根据机器设置布局
        String machineName = getIntent().getStringExtra("machineName");
        if (!TextUtils.isEmpty(machineName)) {
            tvMachineName.setText(machineName);
        }

        // 添加数据源
        fragments.add(QrCodeTextModelFragment.newInstance(machineName));
        fragments.add(QrCodeWiFiModelFragment.newInstance(machineName));
        fragments.add(QrCodeBusinessCardModelFragment.newInstance(machineName));
        fragments.add(QrCodeCopyModelFragment.newInstance(machineName));
        fragments.add(QrCodeAudioModelFragment.newInstance(machineName));
        fragments.add(QrCodeVideoModelFragment.newInstance(machineName));

        adapter = new ViewPagerAdapter(this, fragments);

        viewPagerQrcode.setAdapter(adapter);
        viewPagerQrcode.setUserInputEnabled(false);
        viewPagerQrcode.setOffscreenPageLimit(3);
        viewPagerQrcode.setCurrentItem(0);

        tvTextModel.setSelected(true);


        // 获取保存的危险警报震动提醒实例值
        isOpenVibrateAlert = sharedPref.getBoolean(getString(R.string.preference_vibrate_alert), true);
        // 获取保存的危险警报震动提醒时长实例值
        vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        // 返回
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 保存
        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPagerQrcode.getCurrentItem());
                if (currentFragment != null) {
                    Log.e(TAG, "currentFragment=" + currentFragment.getClass().getSimpleName());
                    if (currentFragment != null && currentFragment instanceof QRCodeProvider) {
                        Bitmap qrCodeBitmap = ((QRCodeProvider) currentFragment).getQRCodeBitmap();
                        if (qrCodeBitmap != null) {
                            saveBitmapToGallery(currentFragment.requireContext(), qrCodeBitmap, "LY_Save_QRcode"); // 保存二维码图片
                        } else {
                            Toast.makeText(QrCodeActivity.this, "二维码获取失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "当前Fragment不是QRCodeProvider类型或无法获取");
                    }
                }
            }
        });

        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(QrCodeActivity.this, EngraveActivity.class);
                    String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                    String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } else if (tvMachineStatusTips.getText().equals("暂停")){
                    // 解除暂停
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("\u0018");
                } else if (tvMachineStatusTips.getText().equals("警告")){
                    // 解除警告
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("$X");
                } else {
                    Log.d(TAG, "无效点击");
                }
            }
        });

        // 文本
        tvTextModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(0);
                tvTextModel.setSelected(true);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // Wi-Fi
        tvWifiModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(1);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(true);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // 名片
        tvBusinessCardModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(2);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(true);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // 复制
        tvCopyModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(3);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(true);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // 音频
        tvAudioModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(4);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(true);
                tvVideoModel.setSelected(false);
            }
        });

        // 视频
        tvVideoModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(5);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(true);
            }
        });
    }

    /**
     * 保存图片至相册
     * @param context 上下文
     * @param bitmap 位图
     * @param fileName 文件名字
     */
    public void saveBitmapToGallery(Context context, Bitmap bitmap, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyApp";
            File imageDir = new File(imagePath);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            File imageFile = new File(imageDir, fileName + ".jpg");
            imageUri = Uri.fromFile(imageFile);
        }

        if (imageUri != null) {
            try {
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(imageUri, values, null, null);
                }
                Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 请求结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // 用户取消扫描
                Toast.makeText(this, "用户取消扫描", Toast.LENGTH_SHORT).show();
            } else {
                // 处理扫描结果
                String scanResult = result.getContents();
                Log.d(TAG, "scanResult=" + scanResult);
                // TODO: 在这里处理扫描结果
                EventBus.getDefault().post(new ScanResultMessageEvent(scanResult));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Activity topActivity = GrblController.getInstance().getTopActivity();

            if (event.getMessage().startsWith("<")) {
                Log.d(TAG, "message=" + event.getMessage().toString());
                String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
                Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);

                if (parts[0].equals(Constants.MACHINE_STATUS_IDLE)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineStatusTips.setText("已连接");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_RUN)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineStatusTips.setText("工作中");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_JOG)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    tvMachineStatusTips.setText("运动中");
                } else if (parts[0].contains(Constants.MACHINE_STATUS_HOLD)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                    tvMachineStatusTips.setText("暂停");
                } else if (parts[0].equals(Constants.MACHINE_STATUS_ALARM)) {
                    tvMachineStatusTips.setBackgroundResource(R.drawable.bg_red_c42b1c_r100);
                    tvMachineStatusTips.setText("警告");
                }
            } else {
                if (topActivity != this) {
                    Log.d(TAG, "当前 Activity 不是顶层，不弹窗");
                    return; // 不是当前页面，直接 return
                }

                if (event.getMessage().contains("MSG:Safe door err") && tvMachineStatusTips.getText().equals("工作中")) { // 开门警告弹窗打开
                    // TODO 开门警告弹窗
                    showDialogDoorWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe door reset") && tvMachineStatusTips.getText().equals("暂停")) { // 开门警告弹窗关闭
                    // 隐藏开门警告弹窗
                    dialogDoorWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Flame err") && tvMachineStatusTips.getText().equals("工作中")) { // 火焰警告弹窗打开
                    // TODO 火焰警告弹窗
                    showDialogFireWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Flame reset") && tvMachineStatusTips.getText().equals("暂停")) { // 火焰警告弹窗关闭
                    // 隐藏火焰警告弹窗
                    dialogFireWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Tilt sensor") && tvMachineStatusTips.getText().equals("工作中")) { // 倾斜警告弹窗打开
                    // TODO 倾斜警告弹窗
                    showDialogProbeWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Probe reset") && tvMachineStatusTips.getText().equals("暂停")) { // 倾斜警告弹窗关闭
                    // 隐藏倾斜警告弹窗
                    dialogProbeWarning.dismiss();
                    // TODO 记录日志

                }
            }
        }
    }

    /**
     * 开门风险提示弹窗
     */
    private void showDialogDoorWarning() {
        dialogDoorWarning = new Dialog(this, R.style.CustomDialog);
        dialogDoorWarning.setContentView(R.layout.dialog_door_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialogDoorWarning.getWindow() != null) {
            dialogDoorWarning.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialogDoorWarning.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialogDoorWarning.isShowing()) {
                    dialogDoorWarning.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialogDoorWarning.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialogDoorWarning.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialogDoorWarning.show();
    }

    /**
     * 火焰风险提示弹窗
     */
    private void showDialogFireWarning() {
        dialogFireWarning = new Dialog(this, R.style.CustomDialog);
        dialogFireWarning.setContentView(R.layout.dialog_fire_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialogFireWarning.getWindow() != null) {
            dialogFireWarning.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialogFireWarning.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialogFireWarning.isShowing()) {
                    dialogFireWarning.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialogFireWarning.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialogFireWarning.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialogFireWarning.show();
    }

    /**
     * 倾斜风险提示弹窗
     */
    private void showDialogProbeWarning() {
        dialogProbeWarning = new Dialog(this, R.style.CustomDialog);
        dialogProbeWarning.setContentView(R.layout.dialog_probe_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialogProbeWarning.getWindow() != null) {
            dialogProbeWarning.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialogProbeWarning.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialogProbeWarning.isShowing()) {
                    dialogProbeWarning.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialogProbeWarning.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialogProbeWarning.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialogProbeWarning.show();
    }

    /**
     * 震动提醒
     * @param context 上下文
     * @param milliseconds 震动时长
     */
    public void vibratePhone(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }

}
