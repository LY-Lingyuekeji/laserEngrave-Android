
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.MySeekBar;
import in.co.gorest.grblcontroller.util.NettyClient;

public class TextCreateActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = TextCreateActivity.class.getSimpleName();
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
    // 文本区域
    private LinearLayout llContainer;
    // 文本
    private TextView tvContainer;
    // 文本输入框
    private EditText etInput;
    // 确定
    private TextView tvConfirm;
    // seekBar
    private MySeekBar seekbarFontSize;
    // 字体大小
    private TextView tvFontSize;
    // 撤销
    private LinearLayout llUndo;
    // 加粗
    private LinearLayout llBold;
    // 恢复
    private LinearLayout llRedo;
    // 倾斜
    private LinearLayout llTilt;
    // 下划线
    private LinearLayout llUnderLine;
    // 删除线
    private LinearLayout llDeleteLine;
    // 左对齐
    private LinearLayout llAlignLeft;
    // 居中对齐
    private LinearLayout llAlignCenter;
    // 右对齐
    private LinearLayout llAlignRight;
    // 初始大小
    private int fontSize = 32;
    // 是否加粗
    private boolean isBold = false;
    // 是否倾斜
    private boolean isItalic = false;
    // 是否下划线
    private boolean isUnderline = false;
    // 是否删除线
    private boolean isDeleteline = false;

    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // 撤销操作栈
    private Stack<TextOperation> undoStack = new Stack<>();
    // 恢复操作栈
    private Stack<TextOperation> redoStack = new Stack<>();

    // 封装每次文本样式更改的细节
    class TextOperation {
        private boolean isBoldChange;
        private boolean isItalicChange;
        private boolean isUnderlineChange;
        private boolean isDeletelineChange;
        private int gravityChange;

        public TextOperation(boolean isBoldChange, boolean isItalicChange, boolean isUnderlineChange, boolean isDeletelineChange, int gravityChange) {
            this.isBoldChange = isBoldChange;
            this.isItalicChange = isItalicChange;
            this.isUnderlineChange = isUnderlineChange;
            this.isDeletelineChange = isDeletelineChange;
            this.gravityChange = gravityChange;
        }

        // Apply operation
        public void apply(TextCreateActivity activity) {
            if (isBoldChange) activity.isBold = !activity.isBold;
            if (isItalicChange) activity.isItalic = !activity.isItalic;
            if (isUnderlineChange) activity.isUnderline = !activity.isUnderline;
            if (isDeletelineChange) activity.isDeleteline = !activity.isDeleteline;

            // 恢复对齐方式
            activity.tvContainer.setGravity(gravityChange);

            // 更新样式
            activity.updateTextStyle();
        }

        public int getGravityChange() {
            return gravityChange;
        }
    }

    // 是否震动提醒
    private boolean isOpenVibrateAlert;
    // 震动提醒持续时长
    private int vibrateAlertTime;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 绑定视图
        DataBindingUtil.setContentView(this, R.layout.activity_textcreate);

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
        // 注销毁EventBus
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
        // 文本区域
        llContainer = findViewById(R.id.ll_container);
        // 文本
        tvContainer = findViewById(R.id.tv_container);
        // 文本输入框
        etInput = findViewById(R.id.et_input);
        // 确定
        tvConfirm = findViewById(R.id.tv_confirm);
        // seekBar
        seekbarFontSize = findViewById(R.id.seekbar_font_size);
        // 字体大小
        tvFontSize = findViewById(R.id.tv_font_size);
        // 撤销
        llUndo = findViewById(R.id.ll_undo);
        // 加粗
        llBold = findViewById(R.id.ll_bold);
        // 恢复
        llRedo = findViewById(R.id.ll_redo);
        // 倾斜
        llTilt = findViewById(R.id.ll_tilt);
        // 下划线
        llUnderLine = findViewById(R.id.ll_underline);
        // 删除线
        llDeleteLine = findViewById(R.id.ll_deleteline);
        // 左对齐
        llAlignLeft = findViewById(R.id.ll_align_left);
        // 居中对齐
        llAlignCenter = findViewById(R.id.ll_align_center);
        // 右对齐
        llAlignRight = findViewById(R.id.ll_align_right);
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

        // 字体大小
        seekbarFontSize.setProgressMin(1);
        seekbarFontSize.setProgressMax(100);
        seekbarFontSize.setProgressDefault(fontSize);
        tvFontSize.setText(String.valueOf(seekbarFontSize.getProgressDefault()));
        tvContainer.setTextSize(fontSize);
        // 设置字体样式
        Typeface boldTypeface = Typeface.create("Roboto", Typeface.NORMAL);
        tvContainer.setTypeface(boldTypeface);


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
                File barcodeBitmap = ImgUtil.saveBitmap("textcreate" + System.currentTimeMillis() + ".png", bitmap);
                Uri imageUris = Uri.fromFile(barcodeBitmap);
                Intent intent = new Intent(TextCreateActivity.this, EditActivity.class);
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
                    Intent intent = new Intent(TextCreateActivity.this, EngraveActivity.class);
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


        // 让输入框获取焦点并填充文字
        etInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 输入框获得焦点时，填充界面上的文字
                    etInput.setText(tvContainer.getText());
                }
            }
        });

        // 确定
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = etInput.getText().toString();
                if (!TextUtils.isEmpty(inputText)) {
                    tvContainer.setText(inputText); // 更新界面上的文字
                }
                // 清空输入框
                etInput.setText("");
                // 收起键盘
                hideKeyboard(etInput);
                // 让输入框失去焦点
                etInput.clearFocus();
            }
        });

        // seekbar
        seekbarFontSize.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                fontSize = Progress;
                tvFontSize.setText(String.valueOf(Progress));
                tvContainer.setTextSize(Progress);
            }

            @Override
            public void onStop(int Progress) {

            }
        });

        // 撤销
        llUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!undoStack.isEmpty()) {
                    TextOperation operation = undoStack.pop();
                    operation.apply(TextCreateActivity.this);
                    redoStack.push(operation);
                }
            }
        });

        // 加粗
        llBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBold = !isBold; // 切换加粗状态
                recordOperation(true, false, false, false, -1); // 记录加粗操作
                updateTextStyle();
            }
        });

        // 恢复
        llRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!redoStack.isEmpty()) {
                    TextOperation operation = redoStack.pop();
                    operation.apply(TextCreateActivity.this);
                    undoStack.push(operation);
                }
            }
        });

        // 倾斜
        llTilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isItalic = !isItalic; // 切换倾斜状态
                recordOperation(false, true, false, false, -1); // 记录倾斜操作
                updateTextStyle();
            }
        });

        // 下划线
        llUnderLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUnderline = !isUnderline; // 切换下划线状态
                recordOperation(false, false, true, false, -1); // 记录下划线操作
                updateTextStyle();
            }
        });

        // 删除线
        llDeleteLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeleteline = !isDeleteline; // 切换删除线状态
                recordOperation(false, false, false, true, -1); // 记录删除线操作
                updateTextStyle();
            }
        });

        // 左对齐
        llAlignLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gravity = Gravity.START;
                recordOperation(false, false, false, false, gravity);
                tvContainer.setGravity(gravity);  // 更新 TextView 的对齐方式
                updateTextStyle();
            }
        });

        // 居中对齐
        llAlignCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gravity = Gravity.CENTER;
                recordOperation(false, false, false, false, gravity);
                tvContainer.setGravity(gravity);  // 更新 TextView 的对齐方式
                updateTextStyle();
            }
        });

        // 右对齐
        llAlignRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gravity = Gravity.END;
                recordOperation(false, false, false, false, gravity);
                tvContainer.setGravity(gravity);  // 更新 TextView 的对齐方式
                updateTextStyle();
            }
        });
    }

    /**
     * 记录操作
     * @param isBoldChange 是否加粗
     * @param isItalicChange 是否倾斜
     * @param isUnderlineChange 是否下划线
     * @param isDeletelineChange 是否删除线
     * @param gravityChange 对齐位置
     */
    private void recordOperation(boolean isBoldChange, boolean isItalicChange, boolean isUnderlineChange, boolean isDeletelineChange, int gravityChange) {
        // 如果没有显式设置对齐方式，则记录当前对齐方式
        if (gravityChange == -1) {
            gravityChange = tvContainer.getGravity(); // 获取当前对齐方式
        }

        undoStack.push(new TextOperation(isBoldChange, isItalicChange, isUnderlineChange, isDeletelineChange, gravityChange));
        redoStack.clear();  // 清空 redo 栈
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
     * 更新文本样式（加粗、倾斜）
     */
    private void updateTypefaceStyle() {
        // 获取当前字体样式
        Typeface currentTypeface = tvContainer.getTypeface();

        int style = Typeface.NORMAL;

        // 判断加粗和倾斜的状态
        if (isBold && isItalic) {
            style = Typeface.BOLD_ITALIC;  // 同时加粗和倾斜
        } else if (isBold) {
            style = Typeface.BOLD;  // 仅加粗
        } else if (isItalic) {
            style = Typeface.ITALIC;  // 仅倾斜
        }

        // 更新字体样式
        tvContainer.setTypeface(Typeface.create(currentTypeface, style));
    }

    /**
     * 更新文本的PaintFlags（下划线、删除线）
     */
    private void updatePaintFlags() {
        // 获取当前的PaintFlags
        int flags = tvContainer.getPaintFlags();

        // 设置下划线
        if (isUnderline) {
            flags |= Paint.UNDERLINE_TEXT_FLAG;  // 启用下划线
        } else {
            flags &= ~Paint.UNDERLINE_TEXT_FLAG;  // 禁用下划线
        }

        // 设置删除线
        if (isDeleteline) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;  // 启用删除线
        } else {
            flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;  // 禁用删除线
        }

        // 更新PaintFlags
        tvContainer.setPaintFlags(flags);
    }

    /**
     * 更新文本样式
     */
    private void updateTextStyle() {
        // 更新字体样式（加粗、倾斜）
        updateTypefaceStyle();

        // 更新文本的装饰样式（下划线、删除线）
        updatePaintFlags();
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
