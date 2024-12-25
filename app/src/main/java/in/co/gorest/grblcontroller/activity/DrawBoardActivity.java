
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import java.io.File;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.fragment.ColorChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.SizeChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.ToolChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.DrawingView;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class DrawBoardActivity extends AppCompatActivity implements ColorChooseBottomSheetFragment.OnColorSelectedListener,
        SizeChooseBottomSheetFragment.OnSizeSelectedListener, ToolChooseBottomSheetFragment.OnToolSelectedListener {
    // 用于日志记录的标签
    private final static String TAG = DrawBoardActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 下一步
    private Button btnNext;
    // 内容中心
    private LinearLayout llContainer;
    // 画板
    private DrawingView drawingView;
    // 文字
    private LinearLayout llText;
    // 条形码
    private LinearLayout llBarCode;
    // 二维码
    private LinearLayout llQrCode;
    // 文本输入框
    private EditText etInput;
    // 确定
    private TextView tvConfirm;
    // 工具
    private LinearLayout llTool;
    // 工具图标
    private ImageView ivTool;
    // 工具名称
    private TextView tvTool;
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

    // 默认模式
    private int defaultModel = -1;

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
        // 下一步
        btnNext = findViewById(R.id.btn_next);
        // 内容中心
        llContainer = findViewById(R.id.ll_container);
        // 画板
        drawingView = findViewById(R.id.drawingView);
        // 文字
        llText = findViewById(R.id.ll_text);
        // 条形码
        llBarCode = findViewById(R.id.ll_bar_code);
        // 二维码
        llQrCode = findViewById(R.id.ll_qr_code);
        // 文本输入框
        etInput = findViewById(R.id.et_input);
        // 确定
        tvConfirm = findViewById(R.id.tv_confirm);
        // 工具
        llTool = findViewById(R.id.ll_tool);
        // 工具图标
        ivTool = findViewById(R.id.iv_tool);
        // 工具名称
        tvTool = findViewById(R.id.tv_tool);
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
        // 获取共享偏好设置保存的运动参数实例
        String strToolName = sharedPref.getString(getString(R.string.preference_draw_board_tool_name), "pen");
        // 设置选中项
        if ("pen".equals(strToolName)) {
            drawingView.setCurrentTool(DrawingView.TOOL_PEN);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_pen_white).into(ivTool);
            tvTool.setText("画笔");
        } else if ("line".equals(strToolName)) {
            drawingView.setCurrentTool(DrawingView.TOOL_LINE);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_line_white).into(ivTool);
            tvTool.setText("直线");
        } else if ("triangle".equals(strToolName)) {
            drawingView.setCurrentTool(DrawingView.TOOL_TRIANGLE);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_triangle_white).into(ivTool);
            tvTool.setText("三角形");
        } else if ("rectangle".equals(strToolName)) {
            drawingView.setCurrentTool(DrawingView.TOOL_RECTANGLE);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_rectangle_white).into(ivTool);
            tvTool.setText("矩形");
        } else if ("circle".equals(strToolName)) {
            drawingView.setCurrentTool(DrawingView.TOOL_CIRCLE);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_circle_white).into(ivTool);
            tvTool.setText("圆形");
        } else if ("eraser".equals(strToolName)) {
            drawingView.setCurrentTool(DrawingView.TOOL_ERASER);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_eraser_white).into(ivTool);
            tvTool.setText("橡皮擦");
        } else if ("text".equals(strToolName)){
            drawingView.setCurrentTool(DrawingView.TOOL_TEXT);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_text).into(ivTool);
            tvTool.setText("文字");
            llText.setSelected(true);
            llBarCode.setSelected(false);
            llQrCode.setSelected(false);
        } else if ("barcode".equals(strToolName)){
            drawingView.setCurrentTool(DrawingView.TOOL_BARCODE);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_bar_code).into(ivTool);
            tvTool.setText("条形码");
            llText.setSelected(false);
            llBarCode.setSelected(true);
            llQrCode.setSelected(false);
        } else if ("qrcode".equals(strToolName)){
            drawingView.setCurrentTool(DrawingView.TOOL_QR_CODE);
            Glide.with(DrawBoardActivity.this).load(R.drawable.icon_qr_code).into(ivTool);
            tvTool.setText("二维码");
            llText.setSelected(false);
            llBarCode.setSelected(false);
            llQrCode.setSelected(true);
        }
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
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                intent.putExtra("businessType", 1);
                startActivity(intent);
                finish();
            }
        });

        // 文字
        llText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llText.setSelected(true);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                defaultModel = 0;
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_text).into(ivTool);
                tvTool.setText("文字");
                sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "text").apply();
                // 设置输入框为不受限制
                etInput.setFilters(new InputFilter[0]);  // 移除任何过滤器
            }
        });

        // 条形码
        llBarCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llText.setSelected(false);
                llBarCode.setSelected(true);
                llQrCode.setSelected(false);
                defaultModel = 1;
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_bar_code).into(ivTool);
                tvTool.setText("条形码");
                sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "barcode").apply();
                // 限制条形码只接受字母和数字
                InputFilter[] filters = new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                // 只允许字母和数字
                                if (source != null && !source.toString().matches("[a-zA-Z0-9]*")) {
                                    return "";  // 非字母或数字的字符将被忽略
                                }
                                return null;
                            }
                        }
                };
                etInput.setFilters(filters);  // 设置过滤器
            }
        });

        // 二维码
        llQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(true);
                defaultModel = 2;
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_qr_code).into(ivTool);
                tvTool.setText("二维码");
                sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "qrcode").apply();
                // 设置输入框为不受限制
                etInput.setFilters(new InputFilter[0]);  // 移除任何过滤器
            }
        });

        // 确认
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultModel == -1) {
                    Toast.makeText(DrawBoardActivity.this, "请先选中模式后插入", Toast.LENGTH_SHORT).show();
                } else {
                    String inputText = etInput.getText().toString();
                    if (TextUtils.isEmpty(inputText)) {
                        Toast.makeText(DrawBoardActivity.this, "请输入要插入的内容", Toast.LENGTH_SHORT).show();
                    } else {
                        switch (defaultModel) {
                            case 0:
                                drawingView.setCurrentTool(DrawingView.TOOL_TEXT);
                                DrawingView.TextShape textShape = new DrawingView.TextShape(inputText, 100, 100, new Paint());
                                drawingView.drawShape(textShape);
                                break;
                            case 1:
                                drawingView.setCurrentTool(DrawingView.TOOL_BARCODE);
                                DrawingView.BarcodeShape barcodeShape = new DrawingView.BarcodeShape(inputText, 100, 100, new Paint());
                                drawingView.drawShape(barcodeShape);
                                break;
                            case 2:
                                drawingView.setCurrentTool(DrawingView.TOOL_QR_CODE);
                                DrawingView.QRCodeShape qrCodeShape = new DrawingView.QRCodeShape(inputText, 100, 100, new Paint());
                                drawingView.drawShape(qrCodeShape);  // 绘制二维码
                                break;
                        }

                        // 清空输入框
                        etInput.setText("");
                        // 收起键盘
                        hideKeyboard(etInput);
                        // 让输入框失去焦点
                        etInput.clearFocus();
                    }
                }
            }
        });

        // 工具
        llTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示工具选择弹窗
                ToolChooseBottomSheetFragment toolChooseBottomSheetFragment = new ToolChooseBottomSheetFragment();
                toolChooseBottomSheetFragment.show(getSupportFragmentManager(), "");
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

            }
        });

        // 恢复
        llRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        // 清除
        llClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用 DrawingView 的 clear() 方法清除所有内容
                drawingView.clear();
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
     * 颜色选择
     *
     * @param color 颜色
     */
    @Override
    public void onColorSelected(int color) {
        Log.d(TAG, "color=" + color);
        ivColor.setBackgroundColor(color);
        drawingView.setPenColor(color);
    }

    /**
     * 粗细选择
     *
     * @param size 粗细
     */
    @Override
    public void onSizeSelected(int size) {
        Log.d(TAG, "size=" + size);
        sharedPref.edit().putInt(getString(R.string.preference_draw_board_pen_size), size).apply();
        drawingView.setPenWidth(size);
    }

    /**
     * 工具选择
     *
     * @param tool 工具
     */
    @Override
    public void onToolSelected(String tool) {
        Log.d(TAG, "tool=" + tool);
        switch (tool) {
            case "pen":
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_pen_white).into(ivTool);
                tvTool.setText("画笔");
                drawingView.setCurrentTool(DrawingView.TOOL_PEN);
                // 清除 文字 & 条形码 & 二维码的选中
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                break;
            case "line":
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_line_white).into(ivTool);
                tvTool.setText("直线");
                drawingView.setCurrentTool(DrawingView.TOOL_LINE);
                // 清除 文字 & 条形码 & 二维码的选中
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                break;
            case "triangle":
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_triangle_white).into(ivTool);
                tvTool.setText("三角形");
                drawingView.setCurrentTool(DrawingView.TOOL_TRIANGLE);
                // 清除 文字 & 条形码 & 二维码的选中
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                break;
            case "rectangle":
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_rectangle_white).into(ivTool);
                tvTool.setText("矩形");
                drawingView.setCurrentTool(DrawingView.TOOL_RECTANGLE);
                // 清除 文字 & 条形码 & 二维码的选中
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                break;
            case "circle":
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_circle_white).into(ivTool);
                tvTool.setText("圆形");
                drawingView.setCurrentTool(DrawingView.TOOL_CIRCLE);
                // 清除 文字 & 条形码 & 二维码的选中
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                break;
            case "eraser":
                Glide.with(DrawBoardActivity.this).load(R.drawable.icon_tool_eraser_white).into(ivTool);
                tvTool.setText("橡皮擦");
                drawingView.setCurrentTool(DrawingView.TOOL_ERASER);
                // 清除 文字 & 条形码 & 二维码的选中
                llText.setSelected(false);
                llBarCode.setSelected(false);
                llQrCode.setSelected(false);
                break;
        }
    }
}
