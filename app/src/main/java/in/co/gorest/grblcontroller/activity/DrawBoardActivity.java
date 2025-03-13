
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.king.drawboard.view.DrawBoardView;

import java.io.File;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.fragment.ColorChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.SizeChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class DrawBoardActivity extends AppCompatActivity implements ColorChooseBottomSheetFragment.OnColorSelectedListener, SizeChooseBottomSheetFragment.OnSizeSelectedListener {
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
}
