
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.util.Stack;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.MySeekBar;

public class TextCreateActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = TextCreateActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 下一步
    private Button btnNext;
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
    // 标记是否初始化过对齐方式
    private boolean isGravityInitialized = false;

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
        // 字体大小
        seekbarFontSize.setProgressMin(1);
        seekbarFontSize.setProgressMax(100);
        seekbarFontSize.setProgressDefault(fontSize);
        tvFontSize.setText(String.valueOf(seekbarFontSize.getProgressDefault()));
        tvContainer.setTextSize(fontSize);
        // 设置字体样式
        Typeface boldTypeface = Typeface.create("Roboto", Typeface.NORMAL);
        tvContainer.setTypeface(boldTypeface);
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
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                intent.putExtra("businessType", 1);
                startActivity(intent);
                finish();
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
}
