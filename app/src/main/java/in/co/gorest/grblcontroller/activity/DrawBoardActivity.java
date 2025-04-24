
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.king.drawboard.view.DrawBoardView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.nio.charset.StandardCharsets;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.fragment.ColorChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.SizeChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.NettyClient;

public class DrawBoardActivity extends AppCompatActivity implements ColorChooseBottomSheetFragment.OnColorSelectedListener, SizeChooseBottomSheetFragment.OnSizeSelectedListener {
    // 用于日志记录的标签
    private final static String TAG = DrawBoardActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 下一步
    private Button btnNext;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
    // 内容中心
    private LinearLayout llContainer;
    // 画板
    private DrawBoardView drawBoardView;
    // 绘画模式
    private TextView tvDrawMode;
    // 路径
    private LinearLayout llPath;
    // 直线
    private LinearLayout llLine;
    // 矩形
    private LinearLayout llRectangle;
    // 椭圆
    private LinearLayout llOval;
    // 圆形
    private LinearLayout llCircle;
    // 马赛克
    private LinearLayout llMosic;
    // 橡皮擦
    private LinearLayout llEraser;
    // 颜色
    private LinearLayout llColor;
    // 颜色显示
    private ImageView ivColor;
    // 粗细
    private LinearLayout llSize;
    // 撤销
    private LinearLayout llUndo;
    // 恢复
    private LinearLayout llRedo;
    // 清除
    private LinearLayout llClean;

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
        DataBindingUtil.setContentView(this, R.layout.activity_drawboard);

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
        // 下一步
        btnNext = findViewById(R.id.btn_next);
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
        // 内容中心
        llContainer = findViewById(R.id.ll_container);
        // 画板
        drawBoardView = findViewById(R.id.drawBoardView);
        // 绘画模式
        tvDrawMode = findViewById(R.id.tv_draw_mode);
        // 路径
        llPath = findViewById(R.id.ll_path);
        // 直线
        llLine = findViewById(R.id.ll_line);
        // 矩形
        llRectangle = findViewById(R.id.ll_rectangle);
        // 椭圆
        llOval = findViewById(R.id.ll_oval);
        // 圆形
        llCircle = findViewById(R.id.ll_circle);
        // 马赛克
        llMosic = findViewById(R.id.ll_mosic);
        // 橡皮擦
        llEraser = findViewById(R.id.ll_eraser);
        // 颜色
        llColor = findViewById(R.id.ll_color);
        // 颜色显示
        ivColor = findViewById(R.id.iv_color);
        // 粗细
        llSize = findViewById(R.id.ll_size);
        // 撤销
        llUndo = findViewById(R.id.ll_undo);
        // 恢复
        llRedo = findViewById(R.id.ll_redo);
        // 清除
        llClean = findViewById(R.id.ll_clean);
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


        // 绘画模式
        switch (drawBoardView.getDrawMode()) {
            case 1:
                tvDrawMode.setText("路径");
                break;
            case 2:
                tvDrawMode.setText("点");
                break;
            case 3:
                tvDrawMode.setText("线");
                break;
            case 4:
                tvDrawMode.setText("矩形");
                break;
            case 5:
                tvDrawMode.setText("椭圆");
                break;
            case 6:
                tvDrawMode.setText("圆");
                break;
            case 7:
                tvDrawMode.setText("文本");
                break;
            case 9:
                tvDrawMode.setText("橡皮擦");
                break;
            case 10:
                tvDrawMode.setText("马赛克");
                break;
        }


        // 设置画笔颜色
        drawBoardView.setPaintColor(Color.parseColor("#000000"));


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

        // 下一步
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个与LinearLayout大小相同的Bitmap对象
                Bitmap bitmap = Bitmap.createBitmap(llContainer.getWidth(), llContainer.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                llContainer.draw(canvas);
                File barcodeBitmap = ImgUtil.saveBitmap("drawboard" + System.currentTimeMillis() + ".png", bitmap);
                Uri imageUris = Uri.fromFile(barcodeBitmap);
                Intent intent = new Intent(DrawBoardActivity.this, EditActivity.class);
                intent.putExtra("machineName", tvMachineName.getText().toString());
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                intent.putExtra("businessType", 1);
                startActivity(intent);
                finish();
            }
        });

        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(DrawBoardActivity.this, EngraveActivity.class);
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


        // 路径
        llPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为路径
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.DRAW_PATH);
            }
        });

        // 直线
        llLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为直线
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.DRAW_LINE);
            }
        });

        // 矩形
        llRectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为矩形
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.DRAW_RECT);
            }
        });

        // 椭圆
        llOval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为椭圆
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.DRAW_OVAL);
            }
        });

        // 圆形
        llCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为圆形
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.DRAW_CIRCLE);
            }
        });

        // 马赛克
        llMosic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为马赛克
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.MOSAIC);
            }
        });

        // 橡皮擦
        llEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置模式为橡皮擦
                drawBoardView.setDrawMode(DrawBoardView.DrawMode.ERASER);
            }
        });

        // 颜色
        llColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示颜色选择弹窗
                ColorChooseBottomSheetFragment colorChooseBottomSheetFragment = new ColorChooseBottomSheetFragment();
                colorChooseBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 粗细
        llSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示粗细选择弹窗
                SizeChooseBottomSheetFragment sizeChooseBottomSheetFragment = new SizeChooseBottomSheetFragment();
                sizeChooseBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 撤销
        llUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawBoardView.undo();
            }
        });

        // 恢复
        llRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawBoardView.redo();
            }
        });


        // 清除
        llClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawBoardView.clear();
            }
        });
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * 画笔颜色
     *
     * @param color 选择的画笔颜色
     */
    @Override
    public void onColorSelected(int color) {
        Log.d(TAG, "color=" + color);
        ivColor.setBackgroundColor(color);
        drawBoardView.setPaintColor(color);
    }

    /**
     * 画笔粗细
     *
     * @param size
     */
    @Override
    public void onSizeSelected(int size) {
        Log.d(TAG, "size=" + size);
        sharedPref.edit().putInt(getString(R.string.preference_draw_board_pen_size), size).apply();
        switch (drawBoardView.getDrawMode()) {
            case 9:
                drawBoardView.setEraserStrokeWidth(size);
                break;
            case 10:
                drawBoardView.setMosaicStrokeWidth(size);
                break;
            default:
                drawBoardView.setLineStrokeWidth(size);
                break;
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
            } else{
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
