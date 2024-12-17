
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.events.ControltoPreViewMessageEvent;
import in.co.gorest.grblcontroller.fragment.AddPhotoBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.CommandBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.ControlBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.SizeChooseBottomSheetFragment;
import in.co.gorest.grblcontroller.model.EffectBean;
import in.co.gorest.grblcontroller.model.GcodesBean;
import in.co.gorest.grblcontroller.util.FileManager;
import in.co.gorest.grblcontroller.util.FileUtils;
import in.co.gorest.grblcontroller.util.GcodeResults;
import in.co.gorest.grblcontroller.util.GridRelativeLayout;
import in.co.gorest.grblcontroller.util.Image2Gcode;
import in.co.gorest.grblcontroller.util.ImageProcess;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.ObservableSSScrollView;
import in.co.gorest.grblcontroller.util.ObservableScrollView;
import in.co.gorest.grblcontroller.util.PictureUtil;
import in.co.gorest.grblcontroller.util.RxTimer;
import in.co.gorest.grblcontroller.util.ScaleView;
import in.co.gorest.grblcontroller.util.ScreenInchUtils;
import in.co.gorest.grblcontroller.util.VerticalScaleView;
import in.co.gorest.grblcontroller.util.ZoomView;
import in.co.gorest.grblcontroller.util.ZoomViewBean;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

public class PreViewActivity extends BaseActivity {
    // 用于日志记录的标签
    private final static String TAG = PreViewActivity.class.getSimpleName();
    // 页面跳转Code
    private final static int ACTIVITY_CODE_FINISH = 5000;
    private final static int ACTIVITY_CODE_DATA = 5001;
    // CompositeDisposable容器
    private static CompositeDisposable mCompositeDisposable;
    // 图片路径
    private Uri inputUri;
    // 宽度
    private int bitmapWidth;
    // 高度
    private int bitmapHeight;
    // 初始比例
    private float aspectRatio;
    // 返回
    private ImageView ivBack;
    // 添加图片
    private ImageView ivAddPhoto;
    // 底部弹窗
    private RelativeLayout rlAddPhotoTab;
    // 素材
    private RelativeLayout rlMaterial;
    // 相册
    private RelativeLayout rlPhoto;
    // 拍照
    private RelativeLayout rlCamera;
    // 雕刻
    private Button btnEngrave;
    // 素材图片
//    private ImageView ivPreview;
    // X
    private EditText etXpos;
    // Y
    private EditText etYpos;
    // 宽
    private EditText etWidth;
    // 高
    private EditText etHeight;
    // 速度
    private EditText etSpeedlevel;
    // 激光功率
    private EditText etLaserlevel;
    // 巡边
    private TextView tvLineJudge;
    // 控制
    private LinearLayout llControl;
    // 命令
    private LinearLayout llCommand;
    // 巡边功率
    private int lineJudgeLaserLevel;
    // 分辨率
    private float resols = 0.08f;
    // 最终位图
    private Bitmap finalBitmap;
    // 是否正在改变标志类
    private boolean isUpdating = false;
    // 是否巡边标志类
    private boolean isLineJudge = false;
    // 写入的nc
    private List<String> strcontent = new ArrayList<>();
    // 最终位图文件
    private File finalBitmapFile;

    // 切片弹窗
    private AlertDialog dialogTransform;
    // 上传弹窗
    private AlertDialog dialogUpload;
    // 最大重试次数
    private int MAX_RETRY_NUM = 5;

    Bitmap mBitmap;
    private List<ZoomViewBean> zoomViewBeanslist = new ArrayList<>();


    ScaleView svCross;

    VerticalScaleView svVertical;

    GridRelativeLayout grl;


    ObservableScrollView sv_grl;

    ObservableSSScrollView my_hsc_view;

    ObservableSSScrollView sh_grl;
    FrameLayout flCanvas;

    int zoomViewPosition, VerticalX;
    // locations
    private float locations;


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
        DataBindingUtil.setContentView(this, R.layout.activity_preview);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

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
    public void onDestroy() {
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
        // 添加图片
        ivAddPhoto = findViewById(R.id.iv_add_photo);
        // 添加图片底部弹窗
        rlAddPhotoTab = findViewById(R.id.rl_add_photo_tab);
        // 素材
        rlMaterial = findViewById(R.id.rl_material);
        // 相册
        rlPhoto = findViewById(R.id.rl_photo);
        // 拍照
        rlCamera = findViewById(R.id.rl_camera);
        // 雕刻
        btnEngrave = findViewById(R.id.btn_engrave);
        // 素材图片
//        ivPreview = findViewById(R.id.iv_preview);
        // X
        etXpos = findViewById(R.id.et_x_pos);
        // Y
        etYpos = findViewById(R.id.et_y_pos);
        // 宽
        etWidth = findViewById(R.id.et_width);
        // 高
        etHeight = findViewById(R.id.et_height);
        // 速度
        etSpeedlevel = findViewById(R.id.et_speedlevel);
        // 激光功率
        etLaserlevel = findViewById(R.id.et_laserlevel);
        // 巡边
        tvLineJudge = findViewById(R.id.tv_line_judge);

        // 控制
        llControl = findViewById(R.id.ll_control);
        // 命令
        llCommand = findViewById(R.id.ll_command);


        svCross = findViewById(R.id.sv_cross);
        svVertical = findViewById(R.id.sv_Vertical);
        grl = findViewById(R.id.grl);
        flCanvas = findViewById(R.id.ll_grl);
        sv_grl = findViewById(R.id.sv_grl);
        my_hsc_view = findViewById(R.id.my_hsc_view);
        sh_grl = findViewById(R.id.sh_grl);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 图片路径
        inputUri = getIntent().getParcelableExtra(BuildConfig.APPLICATION_ID + ".InputUri");
        Log.d(TAG, "inputUri=" + inputUri);
        String type = getIntent().getStringExtra("type");

        mCompositeDisposable = new CompositeDisposable();
        locations = ScreenInchUtils.mmToPx(this, 1) + 1;
        // 设置素材图片
//        ivPreview.setImageURI(inputUri);
        // 宽度
        bitmapWidth = getIntent().getIntExtra("bitmapWidth", 0);
        // 高度
        bitmapHeight = getIntent().getIntExtra("bitmapHeight", 0);
        // 初始比例
        aspectRatio = getIntent().getFloatExtra("aspectRatio", 0.0f);
        // 设置宽度
        etWidth.setText(String.valueOf(bitmapWidth));
        // 设置高度
        etHeight.setText(String.valueOf(bitmapHeight));
        // 巡边功率
        lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
        // 分辨率
        resols = getIntent().getFloatExtra("resols", 0.08f);

        try {
            finalBitmap = getBitmap(1, inputUri);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        mCompositeDisposable.add(Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(final ObservableEmitter<String> e) throws Exception {

                        Bitmap smallBitmap = ImgUtil.getBitmapFormUri(PreViewActivity.this, inputUri);
                        File smallBitmaFile = ImgUtil.saveBitmap("4.0_smallBitmap_" + System.currentTimeMillis() + ".png", smallBitmap);
                        FileManager.get().addDelPath(inputUri.getPath());
                        inputUri = Uri.fromFile(smallBitmaFile);
                        mBitmap = ImgUtil.getImageToChange(smallBitmap);
                        e.onNext("gcodes");
                        e.onComplete();


                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String results) throws Exception {
                        Log.d(TAG, "Uri" + inputUri.getPath());
                        addDragView(mBitmap, true, type, inputUri, getIntent().getParcelableExtra("initedBitmapUri"));
                    }
                }));
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
        // 添加图片
        ivAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示添加图片弹窗
//                AddPhotoBottomSheetFragment addPhotoBottomSheetFragment = new AddPhotoBottomSheetFragment();
//                addPhotoBottomSheetFragment.show(getSupportFragmentManager(), "");
                rlAddPhotoTab.setVisibility(View.VISIBLE);
            }
        });
        // 素材
        rlMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlAddPhotoTab.setVisibility(View.GONE);
                Intent intent = new Intent(PreViewActivity.this, MaterialActivity.class);
                intent.putExtra("businessType", 2);
                startActivityForResult(intent, ACTIVITY_CODE_DATA);
            }
        });
        // 相册
        rlPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlAddPhotoTab.setVisibility(View.GONE);
                ImgUtil.openAlbum(PreViewActivity.this);
            }
        });
        // 拍照
        rlCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlAddPhotoTab.setVisibility(View.GONE);
                //打开相机-兼容7.0
                ImgUtil.openCamera(PreViewActivity.this);
            }
        });

        // 雕刻
        btnEngrave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前模式
                String connectType = sharedPref.getString(getString(R.string.preference_connect_type), "Telnet");
                if (!connectType.equals("Telnet")) {
                    Toast.makeText(PreViewActivity.this, "蓝牙模式暂不支持TF上传，敬请期待下一版本", Toast.LENGTH_SHORT).show();
                } else {
                    // 保存成文件
                    finalBitmapFile = ImgUtil.saveBitmap("2_finalBitmap_" + System.currentTimeMillis() + ".png", finalBitmap);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Transform 切片弹窗
                            transformData();


                            // 切片转换
                            Image2Gcode image2Gcode = new Image2Gcode();
                            GcodesBean gcodesBean = new GcodesBean();

                            for (int i = 0; i < zoomViewBeanslist.size(); i++) {

                                GcodesBean.GcodesItemBean gcodesItemBean = new GcodesBean.GcodesItemBean();
                                BitmapFactory.Options mOptions = new BitmapFactory.Options();
                                mOptions.inScaled = false;
                                List<String> strcontent = new ArrayList<>();
                                ZoomViewBean zoomViewBean = zoomViewBeanslist.get(i);
                                Log.d(TAG, "path=" + zoomViewBean.getUri().getPath());
                                InputStream input = null;
                                try {
                                    input = getContentResolver().openInputStream(zoomViewBean.getUri());
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                                Bitmap bitmaps = BitmapFactory.decodeStream(input, null, mOptions);
                                int printWidth = zoomViewBean.getWide();
                                int printHeight = zoomViewBean.getHeight();
                                float resol = zoomViewBean.getResols();
                                Bitmap adjustBitmap = ImageProcess.imageResize(bitmaps, printWidth, printHeight, resol);
                                FileManager.get().addDelPath(zoomViewBean.getUri().getPath());


                                switch (zoomViewBean.getTypes()) {
                                    case "1"://灰度图
                                        strcontent = image2Gcode.image2Gcode(adjustBitmap, resol
                                                , Integer.valueOf(etSpeedlevel.getText().toString()), Integer.valueOf(etLaserlevel.getText().toString()) * 10, zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
                                        break;
                                    case "2"://黑白图
                                    case "4":// 素描模式
                                        strcontent = image2Gcode.image2Gcode(adjustBitmap, resol
                                                , Integer.valueOf(etSpeedlevel.getText().toString()), Integer.valueOf(etLaserlevel.getText().toString()) * 10, zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
                                        break;
                                    case "3"://轮廓模式//这里要传入原始图像
                                        Bitmap initBitmap = null;
                                        try {
                                            initBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(zoomViewBean.getInitBitmapUri()),
                                                    null, mOptions);
                                        } catch (FileNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                        Matrix m = new Matrix();
                                        for (EffectBean effectBean : zoomViewBean.getEffectBeans()) {
                                            if (effectBean.getEffectType() == 1) {
                                                m.postScale(-1, 1);   //镜像水平翻转
                                            } else if (effectBean.getEffectType() == 2) {
                                                m.postRotate(effectBean.getRotate());  //旋转
                                            }
                                        }
                                        initBitmap = Bitmap.createBitmap(initBitmap, 0, 0, initBitmap.getWidth(), initBitmap.getHeight(), m, true);
                                        Bitmap outlineAdjustBitmap = ImageProcess.imageResize(initBitmap, printWidth, printHeight, resol);
                                        strcontent = image2Gcode.outlineImage2Gcode(outlineAdjustBitmap, printWidth, printHeight, Integer.valueOf(etSpeedlevel.getText().toString()),
                                                Integer.valueOf(etLaserlevel.getText().toString()) * 10, zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
                                        break;
                                }
//                                FileManager.get().addDelPath(zoomViewBean.getInitBitmapUri().getPath());
//                                gcodesItemBean.setUri(ImgUtil.saveBitmap(System.currentTimeMillis() + ".png", zoomViewBean.getBitmap()).getPath());
//                                FileManager.get().addDelPath(gcodesItemBean.getUri());
//                                gcodesItemBean.setTypes(zoomViewBean.getTypes());
//                                gcodesItemBean.setHeight(zoomViewBean.getHeight());
//                                gcodesItemBean.setWide(zoomViewBean.getWide());
//                                gcodesItemBean.setEditHighY(zoomViewBean.getEditHighY());
//                                gcodesItemBean.setEditWideX(zoomViewBean.getEditWideX());
//                                gcodesItemBean.setDepthProgress(zoomViewBean.getDepthProgress() + "");
//                                gcodesItemBean.setSpeedProgress(zoomViewBean.getSpeedProgress() + "");

                                FileUtils.writeTxtToFile(strcontent, GrblController.getInstance().getExternalFilesDir(null) + "/laser/", System.currentTimeMillis() + ".nc", new GcodeResults() {
                                    @Override
                                    public void onGcodeResults(String results, File file) {
                                        Log.d(TAG, "file:" + file.getPath());
                                        // 隐藏切片弹窗
                                        runOnUiThread(() -> {
                                            dialogTransform.dismiss();
                                        });

                                        // 开始切片
                                        if (file != null) {
                                            uploadFile(file, MAX_RETRY_NUM);
                                        } else {
                                            Toast.makeText(PreViewActivity.this, "切片失败，请检查并重试！", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onGcodeResults(List<String> gcode) {
                                        // 隐藏切片弹窗
                                        runOnUiThread(() -> {
                                            dialogTransform.dismiss();
                                        });
                                    }
                                });

                            }

//                            Bitmap adjustBitmap = ImageProcess.imageResize(finalBitmap, Integer.valueOf(etWidth.getText().toString()), Integer.valueOf(etHeight.getText().toString()), resols);
//                            strcontent = image2Gcode.image2Gcode(adjustBitmap, resols, Integer.valueOf(etSpeedlevel.getText().toString()), Integer.valueOf(etLaserlevel.getText().toString()) * 10, Integer.valueOf(etXpos.getText().toString()), Integer.valueOf(etYpos.getText().toString()));


                        }
                    }).start();
                }
            }
        });

        // 宽度
        etWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }
                try {
                    int newWidth = Integer.parseInt(s.toString());
                    int newHeight = Math.round(newWidth / aspectRatio);
                    isUpdating = true;
                    etHeight.setText(String.valueOf(newHeight));
                } catch (NumberFormatException e) {
                    isUpdating = false;
                    e.printStackTrace();
                }
                isUpdating = false;


            }
        });

        // 高度
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }
                try {
                    int newHeight = Integer.parseInt(s.toString());
                    int newWidth = Math.round(newHeight / aspectRatio);
                    isUpdating = true;
                    etWidth.setText(String.valueOf(newWidth));
                } catch (NumberFormatException e) {
                    isUpdating = false;
                    e.printStackTrace();
                }
                isUpdating = false;

            }
        });

        // 激光功率
        etLaserlevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    etLaserlevel.setText("0");
                } else if (Integer.valueOf(s.toString()).intValue() > 100) {
                    Toast.makeText(PreViewActivity.this, "激光功率最大为100%", Toast.LENGTH_SHORT).show();
                    etLaserlevel.setText("100");
                }
            }
        });

        // 描框
        tvLineJudge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLineJudge == false) {
                    lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
                    Log.d(TAG, "lineJudgeLaserLevel=" + lineJudgeLaserLevel);
                    sendJogCommand("G0 X0 Y0");
                    sendJogCommand("M3 S" + lineJudgeLaserLevel * 10);
                    sendJogCommand("F" + etSpeedlevel.getText().toString());
                    sendJogCommand("G1 Y" + etHeight.getText().toString());
                    sendJogCommand("G1 X" + etWidth.getText().toString());
                    sendJogCommand("G1 Y0");
                    sendJogCommand("G1 X0");
                    sendJogCommand("M5");
                    sendJogCommand("G0 X0 Y0");
                    isLineJudge = true;
                } else {
                    sendJogCommand("\u0018");
                    sendJogCommand("$X");
                    sendJogCommand("G0 X0 Y0");
                    isLineJudge = false;
                }
            }
        });

        // 控制
        llControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlBottomSheetFragment controlBottomSheetFragment = new ControlBottomSheetFragment();
                controlBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 命令
        llCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandBottomSheetFragment commandBottomSheetFragment = new CommandBottomSheetFragment();
                commandBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });


        sv_grl.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                sv_grl.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        my_hsc_view.setScrollViewListener(new ObservableSSScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableSSScrollView scrollView, int x, int y, int oldx, int oldy) {
                sh_grl.setScrollX(x);
                Log.d(TAG, "sh_grl=" + x + "==" + oldx);
            }
        });
        sh_grl.setScrollViewListener(new ObservableSSScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableSSScrollView scrollView, int x, int y, int oldx, int oldy) {
                my_hsc_view.setScrollX(x);
                Log.d(TAG, "==sh_grl=" + x + "==" + oldx);
                zoomViewBeanslist.get(zoomViewPosition).setEditScrollX((int) (oldx / locations));
            }
        });
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
     * 上传文件
     *
     * @param file 文件
     * @param num  重试次数
     */
    private void uploadFile(File file, final int num) {
        Log.d(TAG, "upload num=" + num);
        if (num > 0) {
            // 使用自定义布局创建 AlertDialog
            LayoutInflater inflater = LayoutInflater.from(PreViewActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_upload, null);
            // 获取 ProgressBar 和 TextView
            ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
            TextView progressText = dialogView.findViewById(R.id.progressText);
            // 创建弹窗
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PreViewActivity.this);
            alertDialogBuilder.setTitle("提示");
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setCancelable(false);
            // UI线程
            runOnUiThread(() -> {
                dialogUpload = alertDialogBuilder.create();
                // 显示弹窗
                dialogUpload.show();
            });

            // 初始化
            OkHttpUtils.getInstance();
            // 文件上传
            OkHttpUtils.post().addFile("myfile[]", file.getName(), file).url("http://192.168.4.1/upload").addParams("path", "/").addParams("/" + file.getName() + "S", String.valueOf(file.length())).tag(this).build().execute(new StringCallback() {


                @Override
                public void inProgress(float f, long j, int i) {
                    super.inProgress(f, j, i);
                    Log.e(TAG, "onResponse  inProgress=" + f + "---" + j + "---" + i);
                    runOnUiThread(() -> {
                        progressBar.setProgress((int) (f * 100.0f));
                        progressText.setText(((int) (f * 100.0f)) + "%");
                    });

                }

                @Override
                public void onError(Call call, Exception exc, int i) {
                    // 隐藏上传弹窗
                    runOnUiThread(() -> {
                        dialogUpload.dismiss();
                    });
                    Log.d(TAG, "e=" + exc.getMessage().toString());
                    exc.printStackTrace();
                    Toast.makeText(PreViewActivity.this, "上传失败，请检查并重试", Toast.LENGTH_SHORT).show();
                    uploadFile(file, MAX_RETRY_NUM--);
                }

                @Override
                public void onResponse(String str3, int i) {
                    Log.e(TAG, "onResponse=" + str3);
                    Toast.makeText(PreViewActivity.this, "上传完成", Toast.LENGTH_SHORT).show();
                    // 隐藏上传弹窗
                    runOnUiThread(() -> {
                        dialogUpload.dismiss();
                    });
                    // 跳转雕刻页面
                    Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                    intent.putExtra("imagePath", finalBitmapFile.getAbsolutePath());
                    intent.putExtra("filePath", file.getPath());
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            Toast.makeText(PreViewActivity.this, "上传失败，请检查并重试", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 上传弹窗
     */
    private void uploadData() {


    }

    /**
     * 切片弹窗
     */
    private void transformData() {
        // 使用自定义布局创建 AlertDialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_transform, null);
        // content
        TextView content = dialogView.findViewById(R.id.dialog_content);
        // UI线程
        runOnUiThread(() -> {
            // 创建弹窗
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("提示");
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setCancelable(false);
            dialogTransform = alertDialogBuilder.create();
            // 设置内容
            content.setText("切片中，请耐心等待！");
            // 显示弹窗
            dialogTransform.show();
        });
    }

    /**
     * 发送命令
     *
     * @param command
     */
    private void sendJogCommand(String command) {
        Log.d(TAG, "command=" + command);
        NettyClient.getInstance(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                return false;
            }
        })).sendMsgToServer((command + "\r\n").getBytes(StandardCharsets.UTF_8), null);
    }

    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onControltoPreViewMessageEvent(ControltoPreViewMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Log.d(TAG, "message=" + event.getMessage().toString());
            sendJogCommand(event.getMessage().toString());
        }
    }


    public void addDragView(Bitmap bm, boolean lean, String type, Uri url, Uri initBitmapUri) {
        Log.d(TAG, "Uri=" + url.getPath());
        ZoomViewBean zoomViewBean = new ZoomViewBean();
        LayoutInflater inflater2 = LayoutInflater.from(this);
        View selfView = inflater2.inflate(R.layout.view_setting_item, null);

        ZoomView zoomView = selfView.findViewById(R.id.ll_container);
        ImageView iv_close = selfView.findViewById(R.id.iv_close);
        ImageView iv_icon = selfView.findViewById(R.id.iv_icon);
        zoomView.setImageView(iv_close);
        zoomView.setSelected(lean);
        iv_icon.setImageBitmap(bm);
        zoomViewBean.setZoomView(zoomView);
        zoomViewBean.setBitmap(bm);
        zoomViewBean.setIvClose(iv_close);
        zoomViewBean.setIvIcon(iv_icon);
        zoomViewBean.setView(selfView);
        zoomViewBean.setTypes(type);
        zoomViewBean.setDepthProgress(20);
        zoomViewBean.setSpeedProgress(100);
        zoomViewBean.setResols(getIntent().getFloatExtra("resols", 0.08f));
        zoomViewBean.setUri(url);
        zoomViewBean.setInitBitmapUri(initBitmapUri);
        zoomViewBean.setEffectBeans((List<EffectBean>) getIntent().getSerializableExtra("effectBeans"));
        zoomViewBeanslist.add(zoomViewBean);
        if (lean) {
            grl.setlimit(svCross.getWidth(), svVertical.getHeight());
            zoomViewPosition = 0;
        }
        iv_close.setVisibility(lean ? View.VISIBLE : View.GONE);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < zoomViewBeanslist.size(); i++) {
                    if (zoomViewBeanslist.get(i).getView() == selfView) {
                        zoomViewBeanslist.remove(i);
                        i = zoomViewBeanslist.size();
                    }
                }
                flCanvas.removeView(selfView);
            }
        });
        zoomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "zoomView=" + zoomView.isSelected() + "==" + zoomView.getMoveType());
                if (zoomView.getMoveType() == 4) {
                    for (int i = 0; i < zoomViewBeanslist.size(); i++) {
                        zoomViewBeanslist.get(i).getZoomView().setSelected(false);
                        zoomViewBeanslist.get(i).getIvClose().setVisibility(View.GONE);
                    }
                    zoomView.setSelected(!zoomView.isSelected());
                    iv_close.setVisibility(zoomView.isSelected() ? View.VISIBLE : View.GONE);
                    setLocation(selfView, false, 0);
                }
            }
        });
        zoomView.setZoom(new ZoomView.OnZoom() {
            @Override
            public void onZoom(int i, float scalex, float scaley) {
                if (i == -1) {
//                    initEdit();
                } else {
                    zoomViewBean.setScaleX(scalex);
                    zoomViewBean.setScaleY(scaley);
                    zoomViewBean.getIvClose().setScaleX(1);
                    zoomViewBean.getIvClose().setScaleY(1);
                    setLocation(selfView, false, i);
                }
            }
        });

        flCanvas.addView(selfView);
        VerticalX = zoomViewBeanslist.get(zoomViewPosition).getEditWideX();
        new RxTimer().timer(100, new RxTimer.RxAction() {
            @Override
            public void action(long number) {
                setLocation(selfView, true, 0);

            }
        });
    }


    private void setLocation(View selfView, boolean lean, int type) {
        ZoomViewBean zoomViewBean = null;
        for (int i = 0; i < zoomViewBeanslist.size(); i++) {
            if (zoomViewBeanslist.get(i).getView() == selfView) {
                zoomViewPosition = i;
                zoomViewBean = zoomViewBeanslist.get(i);
                i = zoomViewBeanslist.size();
            }
        }
        if (zoomViewBean != null) {
            Rect rect = new Rect();
            zoomViewBean.getIvIcon().getLocalVisibleRect(rect);

            Rect rect2 = new Rect();
            zoomViewBean.getIvClose().getLocalVisibleRect(rect2);
            if (lean) {
                zoomViewBean.getZoomView().initTranslation(0, flCanvas.getHeight() - zoomViewBean.getBitmap().getHeight() - rect2.height() / 2, flCanvas.getWidth() - rect.width() - rect2.width() / 2);

                zoomViewBean.getIvClose().setTranslationY((zoomViewBean.getIvClose().getHeight() / 2) * -1);
                float xs = 85 / (zoomViewBean.getIvIcon().getWidth() / locations);
                float ys = 85 / (zoomViewBean.getIvIcon().getHeight() / locations);
                zoomViewBean.getZoomView().setBig(xs > ys ? ys : xs);
            }
            zoomViewBean.setWide(Math.round(zoomViewBean.getIvIcon().getWidth() * zoomViewBean.getScaleX() / locations));
            zoomViewBean.setHeight(Math.round(zoomViewBean.getIvIcon().getHeight() * zoomViewBean.getScaleY() / locations));
            etWidth.setText(zoomViewBean.getWide() + "");
            etHeight.setText(zoomViewBean.getHeight() + "");
            int[] xy = getNewXY(zoomViewBean);

            if (xy[0] < 0) {
                zoomViewBean.getZoomView().initTranslationX(0);
            }
            if (xy[1] < 0) {
                zoomViewBean.getZoomView().initTranslationY(0);
            }
            zoomViewBean.setEditWideX(xy[0]);
            zoomViewBean.setEditHighY(xy[1]);
            etXpos.setText(zoomViewBean.getEditWideX() + "");
            etYpos.setText(zoomViewBean.getEditHighY() + "");
        }
    }


    public int[] getNewXY(ZoomViewBean zoomViewBean) {
        int[] xy = new int[2];
        int[] position = new int[2];
        zoomViewBean.getIvIcon().getLocationOnScreen(position);
        int[] position2 = new int[2];
        flCanvas.getLocationOnScreen(position2);
        int ewiw = ((int) ((position[0] - svVertical.getWidth()) / locations)) + zoomViewBean.getEditScrollX();
        int ehigh = ((int) ((flCanvas.getHeight() - (position[1] - position2[1]) - zoomViewBean.getIvIcon().getHeight() * zoomViewBean.getScaleY()) / locations));
        if (ewiw < 0) {
            ewiw = 0;
        }
        if (ehigh < 0) {
            ehigh = 0;
        }
        xy[0] = ewiw;
        xy[1] = ehigh;
        return xy;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImgUtil.CHOOSE_PHOTO:
                    Uri selectedImageUri = data.getData();
                    Intent intentChoosePhoto = new Intent(PreViewActivity.this, EditActivity.class);
                    intentChoosePhoto.putExtra("type", "5");
                    intentChoosePhoto.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", selectedImageUri);
                    intentChoosePhoto.putExtra("businessType", 2);
                    startActivityForResult(intentChoosePhoto, ACTIVITY_CODE_DATA);
                    break;
                case ImgUtil.TAKE_PHOTO:
                    Intent intentTakePhoto = new Intent(PreViewActivity.this, EditActivity.class);
                    intentTakePhoto.putExtra("type", "5");
                    intentTakePhoto.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", ImgUtil.imageUri);
                    intentTakePhoto.putExtra("businessType", 2);
                    startActivityForResult(intentTakePhoto, ACTIVITY_CODE_DATA);
                    break;
                case ACTIVITY_CODE_FINISH:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ACTIVITY_CODE_DATA: {
                    Bundle bundleData = data.getBundleExtra("data");
                    mCompositeDisposable.add(Observable.create(new ObservableOnSubscribe<Bitmap>() {
                                @Override
                                public void subscribe(final ObservableEmitter<Bitmap> e) throws Exception {
                                    Bitmap bitmap = ImgUtil.getImageToChange(ImgUtil.getBitmapFormUri(PreViewActivity.this, bundleData.getParcelable(BuildConfig.APPLICATION_ID + ".InputUri")));
                                    e.onNext(bitmap);
                                    e.onComplete();
                                }
                            }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Bitmap>() {
                                @Override
                                public void accept(Bitmap results) throws Exception {
                                    addDragView(results, true, bundleData.getString("type"), bundleData.getParcelable(BuildConfig.APPLICATION_ID + ".InputUri"), bundleData.getParcelable("initedBitmapUri"));
                                }
                            }));
                }
                break;
            }
        }
    }


}
