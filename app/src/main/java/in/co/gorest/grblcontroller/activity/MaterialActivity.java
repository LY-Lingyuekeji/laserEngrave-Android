
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.MaterialAdapter;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.PictureBean;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class MaterialActivity extends AppCompatActivity {

    // TAG
    private final String TAG = MaterialActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // 页面跳转Code
    private final static int ACTIVITY_CODE_FINISH = 5000;
    private final static int ACTIVITY_CODE_DATA = 5001;
    // 返回
    private ImageView ivBack;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
    // 素材
    private RecyclerView mRecycleView;
    // 适配器
    private MaterialAdapter materialAdapter;
    // 素材列表
    private List<PictureBean> PictureList;

    // 业务模式
    private int businessType;

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
        DataBindingUtil.setContentView(this, R.layout.activity_material);

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
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
        // 素材
        mRecycleView = findViewById(R.id.material_recycle_view);
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
        // 获取业务模式
        businessType = getIntent().getIntExtra("businessType", 1);
        Log.d(TAG, "businessType=" + businessType);
        // 设置布局管理器
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(gridLayoutManager);
        // 添加素材
        PictureList = new ArrayList<>();
        PictureBean pictureBeana = new PictureBean();
        pictureBeana.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_a));
        PictureList.add(pictureBeana);
        PictureBean pictureBeanb = new PictureBean();
        pictureBeanb.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_b));
        PictureList.add(pictureBeanb);
        PictureBean pictureBeanc = new PictureBean();
        pictureBeanc.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_c));
        PictureList.add(pictureBeanc);
        PictureBean pictureBeand = new PictureBean();
        pictureBeand.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_d));
        PictureList.add(pictureBeand);
        PictureBean pictureBeane = new PictureBean();
        pictureBeane.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_e));
        PictureList.add(pictureBeane);
        PictureBean pictureBeanf = new PictureBean();
        pictureBeanf.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_f));
        PictureList.add(pictureBeanf);
        PictureBean pictureBeang = new PictureBean();
        pictureBeang.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_g));
        PictureList.add(pictureBeang);
        PictureBean pictureBeanh = new PictureBean();
        pictureBeanh.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_h));
        PictureList.add(pictureBeanh);
        PictureBean pictureBeani = new PictureBean();
        pictureBeani.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_i));
        PictureList.add(pictureBeani);
        PictureBean pictureBeanj = new PictureBean();
        pictureBeanj.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_j));
        PictureList.add(pictureBeanj);
        PictureBean pictureBeank = new PictureBean();
        pictureBeank.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_k));
        PictureList.add(pictureBeank);
        PictureBean pictureBeandl = new PictureBean();
        pictureBeandl.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_l));
        PictureList.add(pictureBeandl);
        PictureBean pictureBeanm = new PictureBean();
        pictureBeanm.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_m));
        PictureList.add(pictureBeanm);
        PictureBean pictureBeann = new PictureBean();
        pictureBeann.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_n));
        PictureList.add(pictureBeann);
        PictureBean pictureBeano = new PictureBean();
        pictureBeano.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_o));
        PictureList.add(pictureBeano);
        PictureBean pictureBeanp = new PictureBean();
        pictureBeanp.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_p));
        PictureList.add(pictureBeanp);
        PictureBean pictureBeanq = new PictureBean();
        pictureBeanq.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_q));
        PictureList.add(pictureBeanq);
        PictureBean pictureBeanr = new PictureBean();
        pictureBeanr.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_r));
        PictureList.add(pictureBeanr);
        PictureBean pictureBeans = new PictureBean();
        pictureBeans.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_s));
        PictureList.add(pictureBeans);
        PictureBean pictureBeandt = new PictureBean();
        pictureBeandt.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_t));
        PictureList.add(pictureBeandt);
        PictureBean pictureBeanu = new PictureBean();
        pictureBeanu.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_u));
        PictureList.add(pictureBeanu);
        PictureBean pictureBeanv = new PictureBean();
        pictureBeanv.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_v));
        PictureList.add(pictureBeanv);
        PictureBean pictureBeanw = new PictureBean();
        pictureBeanw.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_w));
        PictureList.add(pictureBeanw);
        PictureBean pictureBeanx = new PictureBean();
        pictureBeanx.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_x));
        PictureList.add(pictureBeanx);
        PictureBean pictureBeany = new PictureBean();
        pictureBeany.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_y));
        PictureList.add(pictureBeany);
        PictureBean pictureBeanz = new PictureBean();
        pictureBeanz.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_z));
        PictureList.add(pictureBeanz);

        PictureBean pictureBean77 = new PictureBean();
        pictureBean77.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_77));
        PictureList.add(pictureBean77);

        PictureBean pictureBeanCNC = new PictureBean();
        pictureBeanCNC.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.test_cnc));
        PictureList.add(pictureBeanCNC);


        // 实例化素材适配器
        materialAdapter = new MaterialAdapter(getApplicationContext(), PictureList);
        mRecycleView.setAdapter(materialAdapter);

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

        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(MaterialActivity.this, EngraveActivity.class);
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


        // 素材
        materialAdapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
                int position = viewHolder.getAbsoluteAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Intent intent = new Intent(MaterialActivity.this, EditActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", PictureList.get(position).getUrl());
                intent.putExtra("businessType", businessType);
                startActivityForResult(intent, businessType == 1 ? ACTIVITY_CODE_FINISH : ACTIVITY_CODE_DATA);
                Log.d(TAG, PictureList.get(position).getUrl().toString());
            }
        });
    }

    /**
     * 请求结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_CODE_FINISH:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ACTIVITY_CODE_DATA:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
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
