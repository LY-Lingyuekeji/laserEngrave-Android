
package in.co.gorest.grblcontroller.activity;

import static in.co.gorest.grblcontroller.util.ImgUtil.REQUEST_CODE_CAMERA;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yalantis.ucrop.UCrop;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.EngraveListItemAdapter;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.EngraveListItem;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.NettyClient;

public class BeginEngraveActivity extends AppCompatActivity implements EngraveListItemAdapter.OnItemClickListener{

    // 用于日志记录的标签
    private final static String TAG = BeginEngraveActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
    // 管理
    private TextView tvManager;
    // 列表
    private RecyclerView recyclerView;
    // 默认数据
    List<EngraveListItem> items = new ArrayList<>();
    // Adapter
    private EngraveListItemAdapter adapter;
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
        DataBindingUtil.setContentView(this, R.layout.activity_begin_engrave);

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

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
        // 管理
        tvManager = findViewById(R.id.tv_manager);
        // 列表
        recyclerView = findViewById(R.id.recycler_view);
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

        // 获取保存的列表数据
        String itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);

        if (itemsJsonArray == null) {
            // 默认数据
            items.add(new EngraveListItem(R.drawable.ic_star, "素材库", true));
//            items.add(new EngraveListItem(R.drawable.ic_file, "文件", true));
            items.add(new EngraveListItem(R.drawable.ic_photo, "相册", true));
            items.add(new EngraveListItem(R.drawable.ic_camera, "相机", true));
//            items.add(new EngraveListItem(R.drawable.ic_create, "画图", true));
//            items.add(new EngraveListItem(R.drawable.ic_text, "文字", true));
//            items.add(new EngraveListItem(R.drawable.ic_calendar, "条形码", true));
//            items.add(new EngraveListItem(R.drawable.ic_qr_code, "二维码", true));

            // 转换为JSONArray
            JSONArray jsonArray = new JSONArray();
            for (EngraveListItem item : items) {
                jsonArray.put(item.toJSON());
            }
            sharedPref.edit().putString(getString(R.string.preference_engrave_list_item), jsonArray.toString()).apply();
            itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        }

        // 从 Prefs 获取数据源
        List<EngraveListItem> itemList = loadListFromPrefs(itemsJsonArray);
        // 设置LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 初始化adapter
        adapter = new EngraveListItemAdapter(getApplicationContext(), itemList, this);
        // 设置Adapter适配器
        recyclerView.setAdapter(adapter);


        // 获取保存的危险警报震动提醒实例值
        isOpenVibrateAlert = sharedPref.getBoolean(getString(R.string.preference_vibrate_alert), true);
        // 获取保存的危险警报震动提醒时长实例值
        vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 从 Prefs 获取数据源
        String itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);
        // 数据源
        List<EngraveListItem> itemList = loadListFromPrefs(itemsJsonArray);
        // 设置LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 初始化adapter
        adapter = new EngraveListItemAdapter(getApplicationContext(), itemList, this);
        // 设置Adapter适配器
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
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

        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(BeginEngraveActivity.this, EngraveActivity.class);
                    String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                    String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } else if (tvMachineStatusTips.getText().equals("暂停")){
                    // 解除暂停
                    NettyClient.getInstance(new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            return false;
                        }
                    })).sendMsgToServer(("\u0018" + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                } else if (tvMachineStatusTips.getText().equals("警告")){
                    // 解除警告
                    NettyClient.getInstance(new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            return false;
                        }
                    })).sendMsgToServer(("$X" + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                } else {
                    Log.d(TAG, "无效点击");
                }
            }
        });

        // 管理
        tvManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BeginEngraveActivity.this, CardManagerActivity.class));
            }
        });
    }

    /**
     * 从 Prefs 获取数据源
     * @return itemList
     */
    public List<EngraveListItem> loadListFromPrefs(String itemsJsonArray) {
        // 数据源
        List<EngraveListItem> itemList = new ArrayList<>();
        if (itemsJsonArray != null) {
            Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);
            try {
                JSONArray array = new JSONArray(itemsJsonArray);
                for (int i = 0; i < array.length(); i++) {
                    EngraveListItem item = EngraveListItem.fromJSON(array.getString(i));
                    if (item != null && item.isVisible()) {
                        itemList.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return itemList;
    }

    /**
     * item点击事件
     * @param item 子项
     */
    @Override
    public void onItemClick(EngraveListItem item) {
        switch (item.getText()) {
            case "素材库":
                Intent intent = new Intent(this, MaterialActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                startActivity(intent);
                break;
            case "文件":
                startActivity(new Intent(this, FileActivity.class));
                break;
            case "相册":
                ImgUtil.openAlbum(this);
                break;
            case "相机":
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 请求相机权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
                } else {
                    // 已经有权限，直接打开相机
                    ImgUtil.openCamera(this);
                }
                break;
            case "画图":
                startActivity(new Intent(this, DrawBoardActivity.class));
                break;
            case "文字":
                startActivity(new Intent(this, TextCreateActivity.class));
                break;
            case "条形码":
                startActivity(new Intent(this, BarCodeActivity.class));
                break;
            case "二维码":
                startActivity(new Intent(this, QrCodeActivity.class));
                break;
        }
    }


    /**
     * 请求结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri destinationUri = getImageOutputUri();
            if (requestCode == ImgUtil.CHOOSE_PHOTO) {
                Uri selectedImageUri = data.getData();
                UCrop.of(selectedImageUri, destinationUri)
                        .start(this);
            } else if (requestCode == ImgUtil.TAKE_PHOTO) {
                UCrop.of(ImgUtil.imageUri, destinationUri)
                        .start(this);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);

                Intent intent = new Intent(BeginEngraveActivity.this, EditActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", resultUri);
                intent.putExtra("businessType", 1);
                startActivity(intent);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，打开相机
                ImgUtil.openCamera(this);
            } else {
                // 权限被拒绝，提示用户需要权限
                Toast.makeText(BeginEngraveActivity.this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 输出裁剪的图片文件路径
     * @return 图片文件路径
     */
    private Uri getImageOutputUri() {
        File file = new File(getExternalCacheDir(), "cropped_image.jpg"); // 指定输出文件路径
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
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

                if (event.getMessage().contains("MSG:Safe door err!")  && tvMachineStatusTips.getText().equals("工作中")) {
                    // TODO 开门弹窗
                    showDialogDoorWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Flame err!")  && tvMachineStatusTips.getText().equals("工作中")) {
                    // TODO 火焰弹窗
                    showDialogFireWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Probe err!")  && tvMachineStatusTips.getText().equals("工作中")) {
                    // TODO 倾斜弹窗
                    showDialogProbeWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                }
            }

        }
    }

    /**
     * 开门风险提示弹窗
     */
    private void showDialogDoorWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_door_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
    }

    /**
     * 火焰风险提示弹窗
     */
    private void showDialogFireWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_fire_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
    }

    /**
     * 倾斜风险提示弹窗
     */
    private void showDialogProbeWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_probe_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
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
