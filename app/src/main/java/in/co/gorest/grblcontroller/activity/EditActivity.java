
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.google.android.material.tabs.TabLayout;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.EffectBean;
import in.co.gorest.grblcontroller.util.FileManager;
import in.co.gorest.grblcontroller.util.FileUtils;
import in.co.gorest.grblcontroller.util.ImageProcess;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.MySeekBar;
import in.co.gorest.grblcontroller.util.MyTabLayout;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.PictureUtil;
import in.co.gorest.grblcontroller.util.ScreenInchUtils;
import in.co.gorest.grblcontroller.util.WebSocketManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EditActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = EditActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // CompositeDisposable容器
    private static CompositeDisposable mCompositeDisposable;
    // 页面跳转Code
    private final static int ACTIVITY_CODE_FINISH = 5000;
    // 返回
    private ImageView ivBack;
    // 下一步
    private Button btnNext;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
    // 素材
    private ImageView ivMaterial;
    // 镜像
    private ImageView ivMirror;
    // 旋转
    private ImageView ivRotate;
    // 黑白反转
    private ImageView ivContrast;
    // 保存
    private ImageView ivSave;
    // tab_model
    private MyTabLayout tabModel;
    // 对比度
    private TextView tvContrast;
    private MySeekBar seekBarContrast;
    // 亮度
    private TextView tvBrightness;
    private MySeekBar seekBarBrightness;
    // 锐化
    private TextView tvSharpening;
    private MySeekBar seekBarSharpening;
    // seekBars类型
    private int seekBars;
    // 业务模式
    private int businessType;
    // 图片路径
    private Uri inputUri;
    // 初始位图
    private Bitmap initedBitmap;
    // 最终位图
    private Bitmap finalBitmap;
    // 黑白反转标识
    private boolean andReverse;
    // locations
    private float locations;
    // tab title
    private List<String> title = Arrays.asList("灰度图", "黑白图", "轮廓", "素描");
    // 锐化
    private int sharp = 127;
    // 对比度
    private int contrast = 50;
    private float contrastLevel = 1.0f; // 初始对比度

    // tabPosition
    private int tabPosition = 1;
    // 分辨率
    private float resols = 0.05f;
    // 亮度
    private int brightness = 50;
    private float brightnessLevel = 0; // 初始亮度
    // 效果
    private List<EffectBean> effectBeans = new ArrayList<>();
    // handler
    public Handler handler = new Handler();

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
        DataBindingUtil.setContentView(this, R.layout.activity_edit);

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
        // 下一步
        btnNext = findViewById(R.id.btn_next);
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
        // 素材
        ivMaterial = findViewById(R.id.iv_material);
        // 镜像
        ivMirror = findViewById(R.id.iv_mirror);
        // 反转
        ivRotate = findViewById(R.id.iv_rotate);
        // 黑白反转
        ivContrast = findViewById(R.id.iv_contrast);
        // 保存
        ivSave = findViewById(R.id.iv_save);
        // tab_model
        tabModel = findViewById(R.id.tab_model);
        // 对比度
        tvContrast = findViewById(R.id.tv_contrast);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        // 亮度
        tvBrightness = findViewById(R.id.tv_brightness);
        seekBarBrightness = findViewById(R.id.seekbar_brightness);
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

        locations = ScreenInchUtils.mmToPx(this, 1) + 1;
        seekBars = 1;
        businessType = getIntent().getIntExtra("businessType", 1);
        Log.d(TAG, "businessType=" + businessType);
        // 图片路径
        inputUri = getIntent().getParcelableExtra(BuildConfig.APPLICATION_ID + ".InputUri");
        Log.d(TAG, "inputUri=" + inputUri);

        try {
            initBitmap();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "initBitmap_failed" + e);
            finish();
        }
        mCompositeDisposable = new CompositeDisposable();

        // 初始化Tab
        tabModel.setTitle(title);
        tabModel.getTabAt(1).select();

        // 创建素材
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createEffectBitmap(tabPosition);
            }
        }, 100);


        // 对比度
        seekBarContrast.setProgressMin(1);
        seekBarContrast.setProgressMax(100);
        seekBarContrast.setProgressDefault(50);
        tvContrast.setText(seekBarContrast.getProgressDefault() + "%");

        // 亮度
        seekBarBrightness.setProgressMin(1);
        seekBarBrightness.setProgressMax(100);
        seekBarBrightness.setProgressDefault(50);
        tvBrightness.setText(seekBarBrightness.getProgressDefault() + "%");

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

                File click_next = ImgUtil.saveBitmap("3_click_next_" + System.currentTimeMillis() + ".png", finalBitmap);
                Uri imageUris = Uri.fromFile(click_next);

                Bundle mCropOptionsBundle = new Bundle();
                mCropOptionsBundle.putString("type", "" + (tabPosition + 1));
                mCropOptionsBundle.putFloat("resols", resols);
                mCropOptionsBundle.putInt("Sharp", sharp);
                mCropOptionsBundle.putInt("operationMode", tabPosition);
                mCropOptionsBundle.putBoolean("andReverse", andReverse);
                mCropOptionsBundle.putSerializable("effectBeans", (Serializable) effectBeans);
                mCropOptionsBundle.putParcelable(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                mCropOptionsBundle.putParcelable("initedBitmapUri", Uri.fromFile(ImgUtil.saveBitmap("initedBitmap_" + System.currentTimeMillis() + ".png", initedBitmap)));

                if (businessType == 1) {
                    Intent intent = new Intent(EditActivity.this, PreViewActivity.class);
                    intent.putExtra("machineName", tvMachineName.getText().toString());
//                    intent.putExtra("operationMode", tabPosition);
                    intent.putExtras(mCropOptionsBundle);
                    startActivityForResult(intent, ACTIVITY_CODE_FINISH);
                } else {
                    Intent data = new Intent();
                    data.putExtra("data", mCropOptionsBundle);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });

        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(EditActivity.this, EngraveActivity.class);
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


        // 镜像
        ivMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matrix m = new Matrix();
                m.postScale(-1, 1);   //镜像水平翻转
                finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.getWidth(), finalBitmap.getHeight(), m, true);
                ivMaterial.setImageBitmap(finalBitmap);
                effectBeans.add(new EffectBean(1, 0));
            }
        });

        // 旋转
        ivRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectBeans.add(new EffectBean(2, 90));
                Matrix m = new Matrix();
                m.postRotate(90);  //旋转-90度
                finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.getWidth(), finalBitmap.getHeight(), m, true);
                ivMaterial.setImageBitmap(finalBitmap);
            }
        });

        // 黑白反转
        ivContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                andReverse = !andReverse;
                createEffectBitmap(tabPosition);
            }
        });

        // 保存
        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取 ImageView 的 Bitmap
                ivSave.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(ivSave.getDrawingCache());
                ivSave.setDrawingCacheEnabled(false);

                if (bitmap != null) {
                    saveBitmapToGallery(v.getContext(), finalBitmap, "my_image_" + System.currentTimeMillis());
                } else {
                    Toast.makeText(v.getContext(), "获取图片失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 模式切换
        tabModel.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabPosition == tab.getPosition())
                    return;
                tabPosition = tab.getPosition();

                Log.d(TAG, "tabPosition=" + tabPosition);

                createEffectBitmap(tabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // 对比度
        seekBarContrast.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                contrast = Progress;

                // 映射对比度值到 0.5f ~ 2.0f
                contrastLevel = 0.5f + (contrast - 1) * (1.5f / 99);

                tvContrast.setText(Progress + "%");

                // 重新生成图片效果
                createEffectBitmap(tabPosition);
            }

            @Override
            public void onStop(int Progress) {

            }
        });

        // 亮度
        seekBarBrightness.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                brightness = Progress;

                // 映射亮度值到 -255 ~ 255
                brightnessLevel = (brightness - 50) * (255f / 50);

                tvBrightness.setText(Progress + "%");

                // 重新生成图片效果
                createEffectBitmap(tabPosition);
            }

            @Override
            public void onStop(int Progress) {

            }
        });
    }

    /**
     * 初始化素材位图
     */
    private void initBitmap() throws FileNotFoundException {
        Bitmap inputUribitmaps = getBitmap(1, inputUri);
        FileManager.get().addDelPath(inputUri.getPath());

        File saveBitmap = ImgUtil.saveBitmap("1_inputUribitmaps.png", inputUribitmaps);
        inputUribitmaps = PictureUtil.getBitmap(saveBitmap.getPath(), 1);
        inputUribitmaps = ImageProcess.addWhiteBg(inputUribitmaps);
        FileManager.get().addDelPath(saveBitmap.getPath());

        int HIGH = ScreenInchUtils.mmToPx(this, sharedPref.getInt(getString(R.string.preference_machine_height), 85)) + 1;
        int ENTER = ScreenInchUtils.mmToPx(this, sharedPref.getInt(getString(R.string.preference_machine_width), 85)) + 1;

        Bitmap whiteEdgeRemovalBitmap = ImageProcess.ImageWhiteEdgeRemoval(inputUribitmaps, HIGH, ENTER);
        initedBitmap = whiteEdgeRemovalBitmap;
        Log.e(TAG, "initedBitmap getWidth:" + initedBitmap.getWidth() + ",getHeight:" + initedBitmap.getHeight());
    }

    /**
     * 获取位图
     *
     * @param inSampleSize 大小
     * @param uri          路径
     * @return bitmap
     * @throws FileNotFoundException
     */
    private Bitmap getBitmap(int inSampleSize, Uri uri) throws FileNotFoundException {
        InputStream input = getContentResolver().openInputStream(uri);
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = false;
        mOptions.inSampleSize = inSampleSize;
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, mOptions);
        if (bitmap.getWidth() >= 1000 || bitmap.getHeight() >= 1000) {
            return getBitmap(inSampleSize + 1, uri);
        } else {
            bitmap = PictureUtil.rotaingImageView(PictureUtil.readPictureDegree(FileUtils.getPathFromUri(getApplicationContext(), uri)), bitmap);
            return bitmap;
        }
    }

    /**
     * 创建素材位图
     *
     * @param effect
     */
    private void createEffectBitmap(int effect) {
        mCompositeDisposable.add(Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(final ObservableEmitter<String> e) throws Exception {
                        switch (effect) {
                            case 0://灰度图
                                finalBitmap = ImageProcess.convertToGreyImage(initedBitmap, initedBitmap.getWidth(), initedBitmap.getHeight(), 1);
                                break;
                            case 1://黑白图
                                finalBitmap = ImageProcess.convertToBlackWhiteImage(initedBitmap, initedBitmap.getWidth(), initedBitmap.getHeight(),  1, sharp);
                                if (andReverse) {
                                    finalBitmap = ImageProcess.ReverseBlackAndWhiteImage(finalBitmap);
                                }
                                break;
                            case 2://轮廓
                                finalBitmap = ImageProcess.convertToOutlineImage(initedBitmap, ivMaterial.getWidth(), false);
                                break;
                            case 3://素描
                                finalBitmap = ImageProcess.ImageDithering(initedBitmap, 1, true);
                                break;
                        }
                        Matrix m = new Matrix();
                        for (EffectBean effectBean : effectBeans) {
                            if (effectBean.getEffectType() == 1) {
                                m.postScale(-1, 1);   //镜像水平翻转
                            } else if (effectBean.getEffectType() == 2) {
                                m.postRotate(effectBean.getRotate());  //旋转
                            }
                        }
                        finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.getWidth(), finalBitmap.getHeight(), m, true);
                        // 调整对比度
                        finalBitmap = adjustContrast(finalBitmap, contrastLevel);
                        // 调整亮度
                        finalBitmap = adjustBrightness(finalBitmap, brightnessLevel);
                        e.onNext("gcodes");
                        e.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String results) throws Exception {
                        switch (effect) {
                            case 0://灰度图
                                ivMaterial.setImageDrawable(null);
                                seekBars = 0;
                                enableSeekBars(true);
                                break;
                            case 1://黑白图
                                ivMaterial.setImageDrawable(null);
                                seekBars = 1;
                                enableSeekBars(true);
                                break;
                            case 2://轮廓
                                ivMaterial.setImageDrawable(null);
                                seekBars = 2;
                                enableSeekBars(false);
                                break;
                            case 3://素描
                                seekBars = 3;
                                enableSeekBars(true);
                                break;
                        }
                        ivMaterial.setImageBitmap(finalBitmap);
                        Log.e(TAG, "finalBitmap getWidth:" + finalBitmap.getWidth() + ",getHeight:" + finalBitmap.getHeight());
                    }
                }));
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
     * 调整对比度
     * @param bitmap  原始位图
     * @param contrast 对比度调整值 (1.0f = 原始值, 0.5f = 低对比度, 2.0f = 高对比度)
     * @return 调整后的位图
     */
    public static Bitmap adjustContrast(Bitmap bitmap, float contrast) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        float scale = contrast;
        float translate = (-0.5f * scale + 0.5f) * 255.f;
        colorMatrix.set(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return output;
    }

    /**
     * 调整亮度
     * @param bmp  原始位图
     * @param brightnessLevel 亮度调整值 (-255 ~ 255)，正值变亮，负值变暗
     * @return 调整后的位图
     */
    public Bitmap adjustBrightness(Bitmap bmp, float brightnessLevel) {
        Bitmap bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int brightness = (int) brightnessLevel;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = bitmap.getPixel(x, y);

                int A = Color.alpha(pixelColor);

                // 🚫 跳过完全透明像素，避免“边框”问题
                if (A == 0) continue;

                int R = Color.red(pixelColor);
                int G = Color.green(pixelColor);
                int B = Color.blue(pixelColor);

                R = Math.min(Math.max(R + brightness, 0), 255);
                G = Math.min(Math.max(G + brightness, 0), 255);
                B = Math.min(Math.max(B + brightness, 0), 255);

                bitmap.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bitmap;
    }



    /**
     * 启用或禁用 亮度 和 对比度的 seekBar
     * @param enable 是否启用
     */
    private void enableSeekBars(boolean enable) {
        seekBarBrightness.setDraggable(enable);
        seekBarContrast.setDraggable(enable);

        // 设置不可点击、不可聚焦，完全禁用交互
        seekBarBrightness.setFocusable(enable);
        seekBarBrightness.setClickable(enable);
        seekBarBrightness.setAlpha(enable ? 1.0f : 0.5f);

        seekBarContrast.setFocusable(enable);
        seekBarContrast.setClickable(enable);
        seekBarContrast.setAlpha(enable ? 1.0f : 0.5f);
    }


    /**
     * 将原始图像中的透明区域恢复到处理后的灰度图中
     */
    public Bitmap restoreAlphaFromOriginal(Bitmap grayBitmap, Bitmap original) {
        Bitmap output = grayBitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = output.getWidth();
        int height = output.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (Color.alpha(original.getPixel(x, y)) == 0) {
                    // 设置为全透明
                    output.setPixel(x, y, Color.argb(0, 0, 0, 0));
                }
            }
        }
        return output;
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_CODE_FINISH:
                    FileManager.get().clearPaths();
                    setResult(RESULT_OK);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);

        // 释放资源
        if (initedBitmap != null)
            initedBitmap.recycle();

        if (finalBitmap != null)
            finalBitmap.recycle();
    }
}
