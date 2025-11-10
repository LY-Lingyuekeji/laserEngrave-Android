
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
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.FontAdapter;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.MySeekBar;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.StrokeTextView;
import in.co.gorest.grblcontroller.util.WebSocketManager;

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
    private StrokeTextView tvContainer;
    // 文本输入框
    private EditText etInput;
    // 确定
    private TextView tvConfirm;
    // seekBar
    private MySeekBar seekbarFontSize;
    // 字体大小
    private TextView tvFontSize;
    // 镂空
    private LinearLayout llHollow;
    // 倾斜
    private LinearLayout llTilt;
    // 下划线
    private LinearLayout llUnderLine;
    // 加粗
    private LinearLayout llBold;
    // 左对齐
    private LinearLayout llAlignLeft;
    // 居中对齐
    private LinearLayout llAlignCenter;
    // 右对齐
    private LinearLayout llAlignRight;
    // 撤销
    private LinearLayout llUndo;
    // 字体
    private LinearLayout llFont;
    // 恢复
    private LinearLayout llRedo;

    // 初始大小
    private int fontSize = 32;
    // 是否加粗
    private boolean isBold = false;
    // 是否倾斜
    private boolean isItalic = false;
    // 是否下划线
    private boolean isUnderline = false;
    // 是否镂空
    private boolean isHollowText = false;
    // 默认左对齐
    private int currentGravity = Gravity.START;

    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // 撤销操作栈
    private Stack<TextOperation> undoStack = new Stack<>();
    // 恢复操作栈
    private Stack<TextOperation> redoStack = new Stack<>();



    private Map<String, String> fontMap = new HashMap<String, String>() {{
        put("超極細ゴシック", "fonts/chogokubosogothic.ttf");
        put("源泉圆体-Regular", "fonts/gensenmarugothictw-regular.ttf");
        put("源云明体", "fonts/GenWanMinTW-Light.ttf");
        put("清松手写体2-Regular", "fonts/JasonHandwriting2-Regular.ttf");
        put("小米MiSans-bold", "fonts/misans-bold.ttf");
        put("阿里妈妈数黑体-bold", "fonts/AlimamaShuHeiTi-Bold.ttf");
        put("阿里妈妈刀隶体-bold", "fonts/AlimamaDaoLiTi.ttf");
        put("TunicNarrow-Regular", "fonts/tuniu-narrow-regular.ttf");
        put("包图小白体", "fonts/包图小白体.ttf");
        put("品如手写体", "fonts/品如手写体.ttf");
        put("庞门正道真贵楷体", "fonts/庞门正道真贵楷体.ttf");
        put("大黑连筋体-条幅黑体", "fonts/大黑连筋体-条幅黑体.ttf");
        put("小米兰亭字体", "fonts/小米兰亭字体.ttf");
        put("迷你花瓣体", "fonts/迷你花瓣体.TTF");
        put("ETHNOCEN", "fonts/ETHNOCEN.TTF");

    }};

    // 封装每次文本样式更改的细节
    class TextOperation {
        private boolean isBoldChange;
        private boolean isItalicChange;
        private boolean isUnderlineChange;
        private boolean isHollowTextChange;
        private int gravityChange;

        public TextOperation(boolean isBoldChange, boolean isItalicChange, boolean isUnderlineChange, boolean isHollowTextChange, int gravityChange) {
            this.isBoldChange = isBoldChange;
            this.isItalicChange = isItalicChange;
            this.isUnderlineChange = isUnderlineChange;
            this.isHollowTextChange = isHollowTextChange;
            this.gravityChange = gravityChange;
        }

        // Apply operation
        public void apply(TextCreateActivity activity) {
            if (isBoldChange) activity.isBold = !activity.isBold;
            if (isItalicChange) activity.isItalic = !activity.isItalic;
            if (isUnderlineChange) activity.isUnderline = !activity.isUnderline;
            if (isHollowTextChange) activity.isHollowText = !activity.isHollowText;


            activity.currentGravity = gravityChange;
            // 恢复对齐方式
            activity.tvContainer.setGravity(gravityChange);
            activity.updateAlignButtonStyles();

            // 更新样式
            activity.updateTextStyle();
        }

        public int getGravityChange() {
            return gravityChange;
        }
    }

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
        // 镂空
        llHollow = findViewById(R.id.ll_hollow);
        // 倾斜
        llTilt = findViewById(R.id.ll_tilt);
        // 下划线
        llUnderLine = findViewById(R.id.ll_underline);
        // 加粗
        llBold = findViewById(R.id.ll_bold);
        // 左对齐
        llAlignLeft = findViewById(R.id.ll_align_left);
        // 居中对齐
        llAlignCenter = findViewById(R.id.ll_align_center);
        // 右对齐
        llAlignRight = findViewById(R.id.ll_align_right);
        // 撤销
        llUndo = findViewById(R.id.ll_undo);
        // 字体
        llFont = findViewById(R.id.ll_font);
        // 恢复
        llRedo = findViewById(R.id.ll_redo);
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
        seekbarFontSize.setProgressMin(12);
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
                } else if (tvMachineStatusTips.getText().equals("暂停")) {
                    // 解除暂停
//                    NettyClient.getInstance(new Handler(new Handler.Callback() {
//                        @Override
//                        public boolean handleMessage(@NonNull Message msg) {
//                            return false;
//                        }
//                    })).sendMsgToServer(("\u0018" + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                    // 替换WebSocket
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("\u0018");
                } else if (tvMachineStatusTips.getText().equals("警告")) {
                    // 解除警告
//                    NettyClient.getInstance(new Handler(new Handler.Callback() {
//                        @Override
//                        public boolean handleMessage(@NonNull Message msg) {
//                            return false;
//                        }
//                    })).sendMsgToServer(("$X" + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                    // 替换WebSocket
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("$X");
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

        // 镂空
        llHollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHollowText = !isHollowText; // 切换镂空状态
                recordOperation(false, false, false, true, -1); // 记录镂空操作
                updateTextStyle();
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

        // 加粗
        llBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBold = !isBold; // 切换加粗状态
                recordOperation(true, false, false, false, -1); // 记录加粗操作
                updateTextStyle();
            }
        });

        // 左对齐
        llAlignLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGravity  = Gravity.START;
                recordOperation(false, false, false, false, currentGravity);
                tvContainer.setGravity(currentGravity);  // 更新 TextView 的对齐方式
                updateTextStyle();
                updateAlignButtonStyles();
            }
        });

        // 居中对齐
        llAlignCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGravity  = Gravity.CENTER;
                recordOperation(false, false, false, false, currentGravity);
                tvContainer.setGravity(currentGravity);  // 更新 TextView 的对齐方式
                updateTextStyle();
                updateAlignButtonStyles();
            }
        });

        // 右对齐
        llAlignRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGravity  = Gravity.END;
                recordOperation(false, false, false, false, currentGravity);
                tvContainer.setGravity(currentGravity);  // 更新 TextView 的对齐方式
                updateTextStyle();
                updateAlignButtonStyles();
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

        // 字体
        llFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建字体选择的底部弹窗
                String[] fontNames = fontMap.keySet().toArray(new String[0]);

                Dialog dialog = new Dialog(TextCreateActivity.this, R.style.CustomDialog);
                dialog.setContentView(R.layout.fragment_font_bottom_sheet);
                // 设置窗口背景为透明，以显示圆角效果
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.getWindow().setGravity(Gravity.BOTTOM); // 设置位置为底部
                }

                // 设置底部弹窗的内容
                RecyclerView recyclerView = dialog.findViewById(R.id.font_list);
                TextView title = dialog.findViewById(R.id.title);
                title.setText("选择字体");

                // 设置 RecyclerView 的适配器来显示字体列表
                FontAdapter fontAdapter = new FontAdapter(fontNames, (fontName) -> {
                    String fontPath = fontMap.get(fontName);
                    Typeface typeface = Typeface.createFromAsset(getAssets(), fontPath);
                    tvContainer.setTypeface(typeface); // 应用字体
                    dialog.dismiss(); // 选择后关闭弹窗
                });
                recyclerView.setLayoutManager(new LinearLayoutManager(TextCreateActivity.this));
                recyclerView.setAdapter(fontAdapter);

                // 设置Dialog的宽高
                if (dialog.getWindow() != null) {
                    // 设置弹窗宽度自适应，高度为屏幕的50%
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (TextCreateActivity.this.getResources().getDisplayMetrics().heightPixels * 0.5));
                }
                // 显示 Dialog
                dialog.show();



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
    }

    /**
     * 记录操作
     *
     * @param isBoldChange       是否加粗
     * @param isItalicChange     是否倾斜
     * @param isUnderlineChange  是否下划线
     * @param isHollowTextChange 是否镂空
     * @param gravityChange      对齐位置
     */
    private void recordOperation(boolean isBoldChange, boolean isItalicChange, boolean isUnderlineChange, boolean isHollowTextChange, int gravityChange) {
        // 如果没有显式设置对齐方式，则记录当前对齐方式
        if (gravityChange == -1) {
            gravityChange = tvContainer.getGravity(); // 获取当前对齐方式
        }

        currentGravity = gravityChange; // ✅ 更新当前对齐状态（用于按钮样式）
        undoStack.push(new TextOperation(isBoldChange, isItalicChange, isUnderlineChange, isHollowTextChange, gravityChange));
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
     * 更新按钮样式
     */
    private void updateButtonStyle() {
        // 镂空
        if (isHollowText) {
            llHollow.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        } else {
            llHollow.setBackgroundResource(R.drawable.bg_gray_999999_r10);
        }

        // 倾斜
        if(isItalic) {
            llTilt.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        } else {
            llTilt.setBackgroundResource(R.drawable.bg_gray_999999_r10);
        }

        // 下划线
        if(isUnderline) {
            llUnderLine.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        } else {
            llUnderLine.setBackgroundResource(R.drawable.bg_gray_999999_r10);
        }

        // 加粗
        if(isBold) {
            llBold.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        } else {
            llBold.setBackgroundResource(R.drawable.bg_gray_999999_r10);
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

        // 更新PaintFlags
        tvContainer.setPaintFlags(flags);
    }

    private void updateAlignButtonStyles() {
        llAlignLeft.setBackgroundResource(R.drawable.bg_gray_999999_r10);
        llAlignCenter.setBackgroundResource(R.drawable.bg_gray_999999_r10);
        llAlignRight.setBackgroundResource(R.drawable.bg_gray_999999_r10);

        Log.d(TAG, "currentGravity=" + currentGravity);

        if (currentGravity == Gravity.START) {
            llAlignLeft.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        } else if (currentGravity == Gravity.CENTER) {
            llAlignCenter.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        } else if (currentGravity == Gravity.END) {
            llAlignRight.setBackgroundResource(R.drawable.bg_green_1e853a_r10);
        }
    }

    /**
     * 更新文本样式
     */
    private void updateTextStyle() {
        // 更新按钮样式
        updateButtonStyle();
        // 更新字体样式（加粗、倾斜）
        updateTypefaceStyle();
        // 更新文本的装饰样式（下划线、删除线）
        updatePaintFlags();
        // 设置文本样式
        tvContainer.setHollowText(isHollowText);
        tvContainer.invalidate();
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
     *
     * @param context      上下文
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
