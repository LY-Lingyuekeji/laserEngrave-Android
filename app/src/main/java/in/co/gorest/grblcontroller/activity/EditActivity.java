
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.google.android.material.tabs.TabLayout;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.model.EffectBean;
import in.co.gorest.grblcontroller.util.FileManager;
import in.co.gorest.grblcontroller.util.FileUtils;
import in.co.gorest.grblcontroller.util.ImageProcess;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.MySeekBar;
import in.co.gorest.grblcontroller.util.MyTabLayout;
import in.co.gorest.grblcontroller.util.PictureUtil;
import in.co.gorest.grblcontroller.util.ScreenInchUtils;
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
    // CompositeDisposable容器
    private static CompositeDisposable mCompositeDisposable;
    // 页面跳转Code
    private final static int ACTIVITY_CODE_FINISH = 5000;
    // 返回
    private ImageView ivBack;
    // 下一步
    private Button btnNext;
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
    // 高光
    private TextView tvHighlights;
    private MySeekBar seekBarHighlights;
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
    // bitmapWidth
    private int bitmapWidth;
    // bitmapHeight
    private int bitmapHeight;
    // 初始比例
    private float aspectRatio = 1.0f;
    // tab title
    private List<String> title = Arrays.asList("灰度图", "黑白图", "轮廓", "素描");
    // 对比度
    private int contrast = 50;
    // 亮度
    private int brightness = 50;
    // 锐化
    private int sharp = 127;
    // 高光
    private int highlights = 50;
    // tabPosition
    private int tabPosition = 1;
    // 分辨率
    private float resols = 0.08f;
    // 效果
    private List<EffectBean> effectBeans = new ArrayList<>();
    // handler
    public Handler handler = new Handler();


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
        // 锐化
        tvSharpening = findViewById(R.id.tv_sharpening);
        seekBarSharpening = findViewById(R.id.seekbar_sharpening);
        // 高光
        tvHighlights = findViewById(R.id.tv_highlights);
        seekBarHighlights = findViewById(R.id.seekbar_highlights);


    }

    /**
     * 初始化数据
     */
    private void initData() {
        locations = ScreenInchUtils.mmToPx(this, 1) + 1;
        // 初始化进度值
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

        // 初始化参数
        initParameter();


        if (!OpenCVLoader.initLocal()) {
            try {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            } catch (UnsatisfiedLinkError e) {
                Log.e("OpenCV", "Cannot load OpenCV library", e);
            }
        } else {
            // OpenCV initialized successfully
            Log.d(TAG, "OpenCV initialized successfully");
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

                File click_next = ImgUtil.saveBitmap("3_click_next_" + System.currentTimeMillis() + ".png", finalBitmap);
                Uri imageUris = Uri.fromFile(click_next);

                Bundle mCropOptionsBundle = new Bundle();
                mCropOptionsBundle.putString("type", "" + (tabPosition + 1));
                mCropOptionsBundle.putFloat("resols", resols);
                mCropOptionsBundle.putInt("Sharp", sharp);
                mCropOptionsBundle.putBoolean("andReverse", andReverse);
                mCropOptionsBundle.putSerializable("effectBeans", (Serializable) effectBeans);
                mCropOptionsBundle.putParcelable(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                mCropOptionsBundle.putParcelable("initedBitmapUri", Uri.fromFile(ImgUtil.saveBitmap("initedBitmap_" + System.currentTimeMillis() + ".png", initedBitmap)));
                mCropOptionsBundle.putInt("bitmapWidth", bitmapWidth);
                mCropOptionsBundle.putInt("bitmapHeight", bitmapHeight);
                mCropOptionsBundle.putFloat("aspectRatio", aspectRatio);

                if (businessType == 1) {
                    Intent intent = new Intent(EditActivity.this, PreViewActivity.class);
                    intent.putExtras(mCropOptionsBundle);
                    startActivityForResult(intent, ACTIVITY_CODE_FINISH);
                } else {
                    Intent data = new Intent();
                    data.putExtra("data", mCropOptionsBundle);
                    setResult(RESULT_OK, data);
                    finish();
                }


//                intent.putExtra("type", String.valueOf(tabPosition + 1));
//                intent.putExtra("resols", resols);
//                intent.putExtra("Sharp", sharp);
//                intent.putExtra("andReverse", andReverse);
//                intent.putExtra("effectBeans", (Serializable) effectBeans);
//                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
//                intent.putExtra("initedBitmapUri", Uri.fromFile(ImgUtil.saveBitmap("initedBitmap_" + System.currentTimeMillis() + ".png", initedBitmap)));
//                intent.putExtra("bitmapWidth", bitmapWidth);
//                intent.putExtra("bitmapHeight", bitmapHeight);
//                intent.putExtra("aspectRatio", aspectRatio);



//                File finalBitmapFile = ImgUtil.saveBitmap("2_finalBitmap_" + System.currentTimeMillis() + ".png", finalBitmap);
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Image2Gcode image2Gcode = new Image2Gcode();
//                        Bitmap adjustBitmap = ImageProcess.imageResize(finalBitmap, Integer.valueOf(Math.round(initedBitmap.getWidth() * 1.0f / locations)), Integer.valueOf(Math.round(initedBitmap.getHeight() * 1.0f / locations)), resols);
//                        strcontent = image2Gcode.image2Gcode(adjustBitmap, resols, 1000, 200, 0, 0);
//                        FileUtils.writeTxtToFile(strcontent, GrblController.getInstance().getExternalFilesDir(null) + "/laser/", System.currentTimeMillis() + ".nc", new GcodeResults() {
//                            @Override
//                            public void onGcodeResults(String results, File file) {
//                                Log.d(TAG, "file:" + file.getPath());
//
//                                Intent intent = new Intent(EditActivity.this, EngraveActivity.class);
//                                intent.putExtra("imagePath",finalBitmapFile.getAbsolutePath());
//                                intent.putExtra("filePath",file.getPath());
//                                startActivity(intent);
//                                finish();
//                            }
//
//                            @Override
//                            public void onGcodeResults(List<String> gcode) {
//
//                            }
//                        });
//                    }
//                }).start();

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
                Toast.makeText(EditActivity.this, "功能调试中，敬请期待！", Toast.LENGTH_SHORT).show();
            }
        });

        // 模式切换
        tabModel.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabPosition == tab.getPosition())
                    return;
                tabPosition = tab.getPosition();

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
                tvContrast.setText(Progress + "%");
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
                tvBrightness.setText(Progress + "%");
            }

            @Override
            public void onStop(int Progress) {

            }
        });

        // 锐化
        seekBarSharpening.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                sharp = Progress;
                tvSharpening.setText(String.valueOf(Progress));
                createEffectBitmap(tabPosition);
            }

            @Override
            public void onStop(int Progress) {

            }
        });

        // 高光
        seekBarHighlights.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                highlights = Progress;
                tvHighlights.setText(Progress + "%");
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

        int HIGH = ScreenInchUtils.mmToPx(this, 85) + 1;
        int ENTER = ScreenInchUtils.mmToPx(this, 85) + 1;

        Bitmap whiteEdgeRemovalBitmap = ImageProcess.ImageWhiteEdgeRemoval(inputUribitmaps, HIGH, ENTER);
        initedBitmap = whiteEdgeRemovalBitmap;
        Log.e(TAG, "initedBitmap getWidth:" + initedBitmap.getWidth() + ",getHeight:" + initedBitmap.getHeight());

        // 宽度
        bitmapWidth = Math.round((((float) initedBitmap.getWidth()) * 1.0f) / locations);
        // 高度
        bitmapHeight = Math.round((((float) initedBitmap.getHeight()) * 1.0f) / locations);
        // 初始比例
        aspectRatio = ((float) Math.round((((float) initedBitmap.getWidth()) * 1.0f) / locations)) / ((float) Math.round((((float) initedBitmap.getHeight()) * 1.0f) / locations));
    }

    /**
     * 初始化参数
     */
    private void initParameter() {
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

        // 锐化
        seekBarSharpening.setProgressMin(1);
        seekBarSharpening.setProgressMax(255);
        seekBarSharpening.setProgressDefault(127);
        tvSharpening.setText(String.valueOf(seekBarSharpening.getProgressDefault()));

        // 高光
        seekBarHighlights.setProgressMin(1);
        seekBarHighlights.setProgressMax(100);
        seekBarHighlights.setProgressDefault(50);
        tvHighlights.setText(seekBarHighlights.getProgressDefault() + "%");
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
                                finalBitmap = ImageProcess.convertToBlackWhiteImage(initedBitmap, initedBitmap.getWidth(), initedBitmap.getHeight(), 1, sharp);
                                if (andReverse) {
                                    finalBitmap = ImageProcess.ReverseBlackAndWhiteImage(finalBitmap);
                                }
                                break;
                            case 2://轮廓
                                finalBitmap = ImageProcess.convertToOutlineImage(initedBitmap, ivMaterial.getWidth(), false,true);
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
                                break;
                            case 1://黑白图
                                ivMaterial.setImageDrawable(null);
                                seekBars = 1;
                                break;
                            case 2://轮廓
                                ivMaterial.setImageDrawable(null);
                                seekBars = 2;
                                break;
                            case 3://素描
                                seekBars = 3;
                                break;
                        }
                        ivMaterial.setImageBitmap(finalBitmap);
                        Log.e(TAG, "finalBitmap getWidth:" + finalBitmap.getWidth() + ",getHeight:" + finalBitmap.getHeight());
                    }
                }));
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        if (initedBitmap != null)
            initedBitmap.recycle();

        if (finalBitmap != null)
            finalBitmap.recycle();
    }
}
