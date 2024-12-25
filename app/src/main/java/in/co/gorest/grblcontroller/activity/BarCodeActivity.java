
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class BarCodeActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private static final String TAG = BarCodeActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 下一步
    private Button btnNext;
    // 条形码界面
    private LinearLayout llBarCode;
    // 条形码
    private ImageView ivBarCode;
    // 文字
    private TextView tvBarCode;
    // 选择框
    private ImageView ivShowBarcodeText;
    // 输入框
    private EditText etBarCodeText;
    // 字数限制
    private TextView tvBarCodeLimit;
    // 单选框是否选择
    private boolean isCheck = false;

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
        DataBindingUtil.setContentView(this, R.layout.activity_barcode);

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
        // 条形码界面
        llBarCode = findViewById(R.id.ll_bar_code);
        // 条形码
        ivBarCode = findViewById(R.id.iv_bar_code);
        // 文字
        tvBarCode = findViewById(R.id.tv_bar_code);
        // 选择框
        ivShowBarcodeText = findViewById(R.id.iv_show_barcode_text);
        // 输入框
        etBarCodeText = findViewById(R.id.et_bar_code_text);
        // 字数限制
        tvBarCodeLimit = findViewById(R.id.tv_bar_code_limit);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置条形码
        setBarcodeToImageView(ivBarCode, "BARCODE");
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
                Bitmap bitmap = Bitmap.createBitmap(llBarCode.getWidth(), llBarCode.getHeight(), Bitmap.Config.ARGB_8888);
                // 使用Canvas绘制LinearLayout的内容到Bitmap上
                Canvas canvas = new Canvas(bitmap);
                llBarCode.draw(canvas);
                // 将Bitmap对象设置到ImageView中
                ivBarCode.setImageBitmap(bitmap);
                File barcodeBitmap = ImgUtil.saveBitmap("barcode" + System.currentTimeMillis() + ".png", bitmap);
                Uri imageUris = Uri.fromFile(barcodeBitmap);
                Intent intent = new Intent(BarCodeActivity.this, EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                intent.putExtra("businessType", 1);
                startActivity(intent);
                finish();
            }
        });


        // 选择框
        ivShowBarcodeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck = !isCheck;
                if (isCheck) {
                    Glide.with(BarCodeActivity.this).load(R.drawable.ic_checkbox_select).into(ivShowBarcodeText);
                    tvBarCode.setVisibility(View.VISIBLE);
                } else {
                    Glide.with(BarCodeActivity.this).load(R.drawable.ic_checkbox_unselect).into(ivShowBarcodeText);
                    tvBarCode.setVisibility(View.GONE);
                }
            }
        });

        // 文字输入框
        etBarCodeText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvBarCode.setText(s.toString());
                tvBarCodeLimit.setText(s.length() + "/80");
                if (s.length() > 80) {
                    Toast.makeText(BarCodeActivity.this, "最多输入80个字", Toast.LENGTH_SHORT).show();
                }
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
                        if (!s.toString().equals("") || !s.toString().isEmpty()) {
                            setBarcodeToImageView(ivBarCode,s.toString());
                        } else {
                            tvBarCode.setText("BARCODE");
                            setBarcodeToImageView(ivBarCode, "BARCODE");
                        }
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });
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
            width = 300;
            height = 100;
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
        Code128Writer writer = new Code128Writer();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.CODE_128, width, height, hints);
        Bitmap bitmap = Bitmap.createBitmap(bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        imageView.setImageBitmap(bitmap);
    }

}
