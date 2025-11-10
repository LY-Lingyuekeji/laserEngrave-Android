
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.MainActivity;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.ControltoPreViewMessageEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.fragment.ControlBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.ParameterBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.EffectBean;
import in.co.gorest.grblcontroller.model.EngraveHistoryRecord;
import in.co.gorest.grblcontroller.model.ScanDirection;
import in.co.gorest.grblcontroller.model.ZoomViewRecord;
import in.co.gorest.grblcontroller.util.FileManager;
import in.co.gorest.grblcontroller.util.FileUtil;
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
import in.co.gorest.grblcontroller.util.WebSocketManager;
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
import okhttp3.OkHttpClient;

public class PreViewActivity extends AppCompatActivity implements ParameterBottomSheetFragment.OnLaserParametersSelectedListener {
    // 用于日志记录的标签
    private final static String TAG = PreViewActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // 页面跳转Code
    private final static int ACTIVITY_CODE_FINISH = 5000;
    private final static int ACTIVITY_CODE_DATA = 5001;
    // CompositeDisposable容器
    private static CompositeDisposable mCompositeDisposable;
    // 图片路径
    private Uri inputUri;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
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
    private LinearLayout llSpeedlevel;
    // 速度（TextView）
    private TextView tvSpeedlevel;
    // 激光功率
    private LinearLayout llLaserlevel;
    // 激光功率（TextView）
    private TextView tvLaserlevel;
    // 巡边
    private TextView tvLineJudge;
    // 控制
    private LinearLayout llControl;
    // 参数
    private LinearLayout llParameter;
    // 巡边功率
    private int lineJudgeLaserLevel;
    // 分辨率
    private float resols = 0.02f;
    // 最终位图
    private Bitmap finalBitmap;
    // 写入的nc
    private List<String> strcontent = new ArrayList<>();
    // 最终位图文件
    private File finalBitmapFile;
    // 切片弹窗
    private Dialog dialogTransform;
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

    boolean editWideXhasFocus, editHighYhasFocus, editHighhasFocus, editWidehasFocus;

    int zoomViewPosition, VerticalX;
    // locations
    private float locations;
    // 队列最大值
    private static final int MAX_HISTORY_SIZE = 5;
    // wposZ值历史记录队列
    private LinkedList<String> wposZHistory = new LinkedList<>();
    // 当前的wposZ值
    private String wposZ;
    // 当前的机器状态
    private String strMachineStatus;
    // 用来跟踪连续匹配的次数
    private int consecutiveMatches = 0;
    // 是否正在对刀
    private boolean isKinfe = false;
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

    // 切片进度条
    private ProgressBar transformProgressBar;
    // 切片进度值
    private TextView transformProgressText;

    // 雕刻次数
    private int totalEngraveCount = 1;
    // 创建主线程的 Handler，用于处理延迟任务
    private final Handler handler = new Handler(Looper.getMainLooper());
    // 用于记录当前的校验任务（防止重复触发）
    private Runnable validateRunnable;
    // 防抖的延迟时间（毫秒）——当用户停止输入超过这个时间才进行校验
    private static final long DEBOUNCE_DELAY = 500;
    // 当前模式
    private String connectType;
    // 自动原点
    boolean isAutoHome;
    // 结束提醒
    boolean isSoundAlert;

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
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
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
        // X
        etXpos = findViewById(R.id.et_x_pos);
        // Y
        etYpos = findViewById(R.id.et_y_pos);
        // 宽
        etWidth = findViewById(R.id.et_width);
        // 高
        etHeight = findViewById(R.id.et_height);
        // 速度
        llSpeedlevel = findViewById(R.id.ll_speedlevel);
        // 速度（TextView）
        tvSpeedlevel = findViewById(R.id.tv_speedlevel);
        // 激光功率
        llLaserlevel = findViewById(R.id.ll_laserlevel);
        // 激光功率（TextView）
        tvLaserlevel = findViewById(R.id.tv_laserlevel);
        // 巡边
        tvLineJudge = findViewById(R.id.tv_line_judge);
        // 控制
        llControl = findViewById(R.id.ll_control);
        // 命令
        llParameter = findViewById(R.id.ll_parameter);


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
        // 根据机器设置布局
        String machineName = getIntent().getStringExtra("machineName");
        if (!TextUtils.isEmpty(machineName)) {
            tvMachineName.setText(machineName);
        }

        // 获取当前模式
        connectType = sharedPref.getString(getString(R.string.preference_connect_type), "AP");
        Log.d(TAG, "connectType=" + connectType);

        // 图片路径
        inputUri = getIntent().getParcelableExtra(BuildConfig.APPLICATION_ID + ".InputUri");
        Log.d(TAG, "inputUri=" + inputUri);
        String type = getIntent().getStringExtra("type");
        // 加工模式
        int operationMode = getIntent().getIntExtra("operationMode", -1);
        Log.d(TAG, "operationMode=" + operationMode);
        mCompositeDisposable = new CompositeDisposable();
        locations = ScreenInchUtils.mmToPx(this, 1) + 1;
        // 巡边功率
        lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 2);
        // 分辨率
        resols = getIntent().getFloatExtra("resols", 0.02f);

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
                        addDragView(mBitmap, true, type, inputUri, getIntent().getParcelableExtra("initedBitmapUri"), operationMode);
                    }
                }));

        // 获取保存的危险警报震动提醒实例值
        isOpenVibrateAlert = sharedPref.getBoolean(getString(R.string.preference_vibrate_alert), true);
        // 获取保存的危险警报震动提醒时长实例值
        vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);

        // 获取保存的自动原点实例值
        isAutoHome = sharedPref.getBoolean(getString(R.string.preference_auto_home), false);
        // 获取保存的结束提醒实例值
        isSoundAlert = sharedPref.getBoolean(getString(R.string.preference_sound_alert), true);
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
                if (!TextUtils.isEmpty(strMachineStatus) && strMachineStatus.equals(Constants.MACHINE_STATUS_RUN)) {
                    BaseDialog.showCustomDialog(PreViewActivity.this, "温馨提示",
                            "检测到设备正在雕刻是否跳转到雕刻界面？",
                            "跳转", "取消",
                            v1 -> {
                                Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                                String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                                String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");

                                intent.putExtra("machineName", tvMachineName.getText().toString());
                                intent.putExtra("imagePath", imagePath);
                                intent.putExtra("filePath", filePath);
                                startActivity(intent);
                            },
                            v1 -> {
                                Log.d(TAG, "用户选择取消");
                            });
                } else if (!TextUtils.isEmpty(strMachineStatus) && strMachineStatus.equals(Constants.MACHINE_STATUS_HOLD)) {
                    sendJogCommand("\u0018");
                } else {
                    // 检查速度和激光功率
                    checkSpeedAndLaserLevel();
                }

            }
        });


        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                    String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                    String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                    intent.putExtra("machineName", tvMachineName.getText().toString());
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } else if (tvMachineStatusTips.getText().equals("暂停")) {
                    // 解除暂停
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("\u0018");
                } else if (tvMachineStatusTips.getText().equals("警告")) {
                    // 解除警告
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.send("$X");
                } else {
                    Log.d(TAG, "无效点击");
                }
            }
        });


        // Xpos
        etXpos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editWideXhasFocus = true;
            }
        });
        etXpos.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editWideXhasFocus = hasFocus;
                if (hasFocus) {
                    // 获得焦点
                    etXpos.setSelection(etXpos.getText().toString().length());
                } else {
                    // 失去焦点
                    if (etXpos.getText().toString().length() < 1 && !etXpos.getText().toString().trim().contains("-"))
                        etXpos.setText(zoomViewBeanslist.get(zoomViewPosition).getEditWideX() + "");
                }
            }
        });
        etXpos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 获得焦点
                if (editWideXhasFocus && editable.toString().trim().length() > 0) {
                    ZoomViewBean zoomViewBean = zoomViewBeanslist.get(zoomViewPosition);
                    int xs = Integer.parseInt(editable.toString().trim());
                    zoomViewBean.setEditWideX(xs);
                    // 获得焦点
                    zoomViewBean.getZoomView().initTranslationX(xs * locations);
                }
            }
        });

        // Ypos
        etYpos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editHighYhasFocus = true;
            }
        });
        etYpos.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editHighYhasFocus = hasFocus;
                if (hasFocus) {
                    // 获得焦点
                    etYpos.setSelection(etYpos.getText().toString().length());
                } else {
                    // 失去焦点
                    if (etYpos.getText().toString().length() < 1 && !etYpos.getText().toString().trim().contains("-"))
                        etYpos.setText(zoomViewBeanslist.get(zoomViewPosition).getEditHighY() + "");
                }
            }
        });
        etYpos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editHighYhasFocus && editable.toString().trim().length() > 0) {
                    int ys = Integer.parseInt(editable.toString().trim());
                    zoomViewBeanslist.get(zoomViewPosition).setEditHighY(ys);
                    zoomViewBeanslist.get(zoomViewPosition).getZoomView().initTranslationY(ys * locations);
                }
            }
        });

        // 宽度
        etWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editWidehasFocus = true;
            }
        });
        etWidth.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editWidehasFocus = hasFocus;
                if (hasFocus) {
                    // 获得焦点
                    etWidth.setSelection(etWidth.getText().toString().length());
                } else {
                    // 失去焦点
                    if (etWidth.getText().toString().length() < 1 && !etWidth.getText().toString().trim().contains("-"))
                        etWidth.setText(zoomViewBeanslist.get(zoomViewPosition).getWide() + "");
                }

            }


        });
        etWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 获得焦点
                if (editWidehasFocus && s.toString().trim().length() > 0 && !s.toString().trim().contains("-")) {
                    int xss = Integer.valueOf(s.toString().trim()).intValue();
                    if (xss > sharedPref.getInt(getString(R.string.preference_machine_width), 85)) {
                        String sds = s.toString().trim();
                        etWidth.setText(sds.substring(0, sds.length() - 1));
                    } else {
                        float xs = Float.parseFloat(s.toString().trim()) * locations / zoomViewBeanslist.get(zoomViewPosition).getIvIcon().getWidth();
                        zoomViewBeanslist.get(zoomViewPosition).setScaleX(xs);
                        zoomViewBeanslist.get(zoomViewPosition).setScaleY(xs);

                        zoomViewBeanslist.get(zoomViewPosition).getZoomView().setScalesY(xs);
                        zoomViewBeanslist.get(zoomViewPosition).getZoomView().setScalesX(xs);
                        zoomViewBeanslist.get(zoomViewPosition).setHeight(Math.round(zoomViewBeanslist.get(zoomViewPosition).getIvIcon().getHeight() * zoomViewBeanslist.get(zoomViewPosition).getScaleY() / locations));
                        etHeight.setText(zoomViewBeanslist.get(zoomViewPosition).getHeight() + "");
                        zoomViewBeanslist.get(zoomViewPosition).setWide(Math.round(zoomViewBeanslist.get(zoomViewPosition).getIvIcon().getWidth() * zoomViewBeanslist.get(zoomViewPosition).getScaleX() / locations));

                        String x = etXpos.getText().toString().trim();
                        String y = etYpos.getText().toString().trim();
                        if (!x.equals("") && !y.equals("")) {
                            zoomViewBeanslist.get(zoomViewPosition).getZoomView().initTranslationX(Integer.parseInt(x) * locations);
                            zoomViewBeanslist.get(zoomViewPosition).getZoomView().initTranslationY(Integer.parseInt(y) * locations);
                        }
                    }
                }

            }
        });

        // 高度
        etHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editHighhasFocus = true;
            }
        });
        etHeight.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editHighhasFocus = hasFocus;
                if (hasFocus) {
                    // 获得焦点
                    etHeight.setSelection(etHeight.getText().toString().length());
                } else {
                    // 失去焦点
                    if (etHeight.getText().toString().length() < 1 && !etHeight.getText().toString().trim().contains("-"))
                        etHeight.setText(zoomViewBeanslist.get(zoomViewPosition).getHeight() + "");
                }
            }
        });
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 获得焦点
                if (editHighhasFocus && s.toString().trim().length() > 0 && !s.toString().trim().contains("-")) {
                    int xss = Integer.valueOf(s.toString().trim()).intValue();
                    if (xss > sharedPref.getInt(getString(R.string.preference_machine_height), 85)) {
                        String sds = s.toString().trim();
                        etHeight.setText(sds.substring(0, sds.length() - 1));
                    } else {
                        float xs = Float.parseFloat(s.toString().trim()) * locations / zoomViewBeanslist.get(zoomViewPosition).getIvIcon().getHeight();
                        zoomViewBeanslist.get(zoomViewPosition).setScaleX(xs);
                        zoomViewBeanslist.get(zoomViewPosition).setScaleY(xs);
                        zoomViewBeanslist.get(zoomViewPosition).getZoomView().setScalesY(xs);
                        zoomViewBeanslist.get(zoomViewPosition).getZoomView().setScalesX(xs);
                        zoomViewBeanslist.get(zoomViewPosition).setWide(Math.round(zoomViewBeanslist.get(zoomViewPosition).getIvIcon().getWidth() * zoomViewBeanslist.get(zoomViewPosition).getScaleX() / locations));
                        etWidth.setText(zoomViewBeanslist.get(zoomViewPosition).getWide() + "");
                        zoomViewBeanslist.get(zoomViewPosition).setHeight(Math.round(zoomViewBeanslist.get(zoomViewPosition).getIvIcon().getHeight() * zoomViewBeanslist.get(zoomViewPosition).getScaleY() / locations));
                        String x = etXpos.getText().toString().trim();
                        String y = etYpos.getText().toString().trim();
                        if (!x.equals("") && !y.equals("")) {
                            zoomViewBeanslist.get(zoomViewPosition).getZoomView().initTranslationX(Integer.parseInt(x));
                            zoomViewBeanslist.get(zoomViewPosition).getZoomView().initTranslationY(Integer.parseInt(y));
                        }
                    }
                }

            }
        });

        // 速度
        llSpeedlevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterBottomSheetFragment parameterBottomSheetFragment = ParameterBottomSheetFragment.newInstance(zoomViewPosition);
                if (tvMachineName.getText().toString().contains("CNC")) {
                    if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isCutting");
                    } else {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isEngraving");
                    }
                } else {
                    if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isCutting");
                    } else {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isEngraving");
                    }
                }
            }
        });

        // 激光功率
        llLaserlevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterBottomSheetFragment parameterBottomSheetFragment = ParameterBottomSheetFragment.newInstance(zoomViewPosition);
                if (tvMachineName.getText().toString().contains("CNC")) {
                    if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isCutting");
                    } else {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isEngraving");
                    }
                } else {
                    if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isCutting");
                    } else {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isEngraving");
                    }
                }
            }
        });

        // 巡边
        tvLineJudge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (zoomViewBeanslist.size() < 1) {
                    Toast.makeText(PreViewActivity.this, "请先添加图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                WebSocketManager webSocketManager = WebSocketManager.getInstance();
                boolean isConnected = webSocketManager.isConnected();
                if (isConnected) {
                    if (tvMachineStatusTips.getText().toString().equals("暂停")) {
                        BaseDialog.showCustomDialog(PreViewActivity.this, "温馨提示",
                                "检测到机器处于暂停状态，无法进行操作！\r\n\r\n是否解除当前状态？",
                                "确定", "取消",
                                v1 -> {
                                    // 解除暂停
                                    sendJogCommand("\u0018");
                                },
                                v1 -> {
                                    Log.d(TAG, "用户选择取消");
                                });
                    } else if (tvMachineStatusTips.getText().toString().equals("警告")) {
                        BaseDialog.showCustomDialog(PreViewActivity.this, "温馨提示",
                                "检测到机器处于警告状态，无法进行操作！\r\n\r\n是否解除当前状态？",
                                "确定", "取消",
                                v1 -> {
                                    // 解除警告
                                    sendJogCommand("$X");
                                },
                                v1 -> {
                                    Log.d(TAG, "用户选择取消");
                                });
                    } else if (tvMachineStatusTips.getText().toString().equals("工作中")) {
                        BaseDialog.showCustomDialog(PreViewActivity.this, "温馨提示",
                                "检测到机器处于工作状态，无法进行操作！\r\n\r\n是否停止当前工作？",
                                "确定", "取消",
                                v1 -> {
                                    // 暂停雕刻
                                    sendJogCommand("!");
                                    // 终止雕刻
                                    sendJogCommand("\u0018");
                                },
                                v1 -> {
                                    Log.d(TAG, "用户选择取消");
                                });
                    } else {
                        // 显示巡边弹窗
                        showDialogLineJugde();
                    }

                } else {
                    BaseDialog.showCustomDialog(PreViewActivity.this,
                            "温馨提示", "检测到您还未连接设备，无法进行雕刻！\r\n\r\n是否连接设备？",
                            "确定", "取消",
                            vbd -> {
                                Intent intent = new Intent(PreViewActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            },
                            vbd -> {
                                Log.d(TAG, "用户点击取消");
                            });
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

        // 参数
        llParameter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterBottomSheetFragment parameterBottomSheetFragment = ParameterBottomSheetFragment.newInstance(zoomViewPosition);
                if (tvMachineName.getText().toString().contains("CNC")) {
                    if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isCutting");
                    } else {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isEngraving");
                    }
                } else {
                    if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isCutting");
                    } else {
                        parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isEngraving");
                    }
                }

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
     * 检查速度和激光功率
     */
    private void checkSpeedAndLaserLevel() {
        if (zoomViewBeanslist.isEmpty()) {
            Toast.makeText(this, "没有可雕刻的图层", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < zoomViewBeanslist.size(); i++) {
            ZoomViewBean bean = zoomViewBeanslist.get(i);
            if (!bean.isSpeedSet() || !bean.isLaserSet()) {
                // 创建自定义弹窗对象
                BaseAlertDialog baseAlertDialog = new BaseAlertDialog(PreViewActivity.this);

                // 显示弹窗并传入标题、内容以及确认按钮的点击事件
                baseAlertDialog.show("温馨提示", "第 " + (i + 1) + " 张图像未设置激光功率和速度, 请设置后进行雕刻！", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 点击确认按钮后执行的操作
                        Log.d(TAG, "用户点击了确认按钮");
                        // 打开弹窗
                        ParameterBottomSheetFragment parameterBottomSheetFragment = ParameterBottomSheetFragment.newInstance(zoomViewPosition);
                        if (tvMachineName.getText().toString().contains("CNC")) {
                            if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                                parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isCutting");
                            } else {
                                parameterBottomSheetFragment.show(getSupportFragmentManager(), "CNC|isEngraving");
                            }
                        } else {
                            if (zoomViewBeanslist.get(zoomViewPosition).getOperationMode() == 2) {
                                parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isCutting");
                            } else {
                                parameterBottomSheetFragment.show(getSupportFragmentManager(), "Laser|isEngraving");
                            }
                        }

                    }
                });
                return;
            }
        }

        // TODO 显示巡边或雕刻弹窗
        showDialogLineJugdeOrEngrave();
    }

    /**
     * 显示巡边弹窗
     */
    private void showDialogLineJugde() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_linejugde);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 设置可取消（点击空白处取消）
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

        // 巡边提示
        TextView tvDialogLinejugdeTips = dialog.findViewById(R.id.tv_dialog_linejugde_tips);
        // 定义一个计数器，用来循环显示点数（模拟巡边正在动态进行）
        final int[] dotCount = {0};  // 用数组包裹，方便在Runnable中修改
        final String baseText = "自动巡边中，请耐心等待";  // 基础文字
        // 创建 Handler 和 Runnable 来更新显示的内容
        Handler handler = new Handler();  // 使用主线程的 Looper
        Runnable loadingRunnable = new Runnable() {
            @Override
            public void run() {
                // 根据当前的点数决定显示的文本
                StringBuilder loadingText = new StringBuilder(baseText);

                // 增加点数
                for (int i = 0; i < dotCount[0]; i++) {
                    loadingText.append(".");
                }

                // 更新 TextView 显示
                tvDialogLinejugdeTips.setText(loadingText.toString());

                // 更新点数，最多到 3 个点后重置
                dotCount[0]++;
                if (dotCount[0] > 3) {
                    dotCount[0] = 0;  // 重置点数
                }

                // 每 500 毫秒更新一次
                handler.postDelayed(this, 500);
            }
        };
        // 启动动画
        handler.post(loadingRunnable);


        // 记录机器初始状态
        final String originalStutas = strMachineStatus;

        // 开始巡边 激光功率不能设置太大,强行改为乘10（原为100）
        lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 2);
        Log.d(TAG, "lineJudgeLaserLevel=" + lineJudgeLaserLevel);

        int maxEditWideX = 0;
        int maxEditHighY = 0;
        int minEditWideX = Integer.MAX_VALUE;
        int minEditHighY = Integer.MAX_VALUE;

        for (ZoomViewBean bean : zoomViewBeanslist) {
            int startX = bean.getEditWideX();
            int startY = bean.getEditHighY();
            int endX = startX + bean.getWide();
            int endY = startY + bean.getHeight();

            maxEditWideX = Math.max(maxEditWideX, endX);
            maxEditHighY = Math.max(maxEditHighY, endY);
            minEditWideX = Math.min(minEditWideX, startX);
            minEditHighY = Math.min(minEditHighY, startY);
        }
        Log.d(TAG, "最小X=" + minEditWideX + "---最小Y=" + minEditHighY + "---最大X=" + maxEditWideX + "---最大Y=" + maxEditHighY);
        // 定义指令列表
        List<String> jogCommands = new ArrayList<>();
        jogCommands.add("G0 X" + minEditWideX + " Y" + minEditHighY);                   // 起点
        jogCommands.add("M3 S" + (lineJudgeLaserLevel * 10));                          // 打开激光
        jogCommands.add("F3500");                                                     // 设置速度
        jogCommands.add("G1 X" + maxEditWideX + " Y" + minEditHighY);  // 右下角
        jogCommands.add("G1 X" + maxEditWideX + " Y" + maxEditHighY);  // 右上角
        jogCommands.add("G1 X" + minEditWideX + " Y" + maxEditHighY);  // 左上角
        jogCommands.add("G1 X" + minEditWideX + " Y" + minEditHighY);  // 左下角
        jogCommands.add("M5");                                                        // 关闭激光
        jogCommands.add("G0 X" + minEditWideX + " Y" + minEditHighY);  // 回到起点

        // 使用 Handler 分批延迟发送
        Handler jogHandler = new Handler(Looper.getMainLooper());
        final int[] index = {0};  // 指令索引

        Runnable sendNextJogCommand = new Runnable() {
            @Override
            public void run() {
                if (index[0] < jogCommands.size()) {
                    sendJogCommand(jogCommands.get(index[0]));
                    index[0]++;
                    jogHandler.postDelayed(this, 500);  // 每条指令间隔 500ms
                } else {
                    // 指令发送完毕后执行恢复检测
                    checkCoordinatesUntilOriginal(dialog, originalStutas);
                }
            }
        };

        // 启动指令发送流程
        jogHandler.post(sendNextJogCommand);
        // 显示 Dialog
        dialog.show();
    }

    /**
     * 显示巡边或雕刻弹窗
     */
    private void showDialogLineJugdeOrEngrave() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_linejugde_or_engrave);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 设置可取消（点击空白处取消）
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

        // 雕刻次数 -
        TextView tvEngraveNumSub = dialog.findViewById(R.id.tv_engrave_num_sub);
        // 雕刻次数
        EditText etEngraveNum = dialog.findViewById(R.id.et_engrave_num);
        // 雕刻次数 +
        TextView tvEngraveNumAdd = dialog.findViewById(R.id.tv_engrave_num_add);
        // 预览范围
        TextView tvDialogLineJugde = dialog.findViewById(R.id.tv_dialog_linejugde);
        // 雕刻
        TextView tvDialogEngrave = dialog.findViewById(R.id.tv_dialog_engrave);

        // 设置初始值
        etEngraveNum.setText(totalEngraveCount + "");

        // 最大和最小值
        final int MIN_COUNT = 1;
        final int MAX_COUNT = 100;

        // 减按钮
        tvEngraveNumSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etEngraveNum.getText().toString().trim();
                int count = TextUtils.isEmpty(input) ? MIN_COUNT : Integer.parseInt(input);
                if (count > MIN_COUNT) {
                    count--;
                    etEngraveNum.setText(count + "");
                    totalEngraveCount = Integer.valueOf(etEngraveNum.getText().toString().trim());
                } else {
                    Toast.makeText(PreViewActivity.this, "最少为 " + MIN_COUNT + " 次", Toast.LENGTH_SHORT).show();
                    etEngraveNum.setText(String.valueOf(MIN_COUNT));
                    totalEngraveCount = Integer.valueOf(etEngraveNum.getText().toString().trim());
                }
            }
        });

        // 加按钮
        tvEngraveNumAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etEngraveNum.getText().toString().trim();
                int count = TextUtils.isEmpty(input) ? MIN_COUNT : Integer.parseInt(input);
                if (count < MAX_COUNT) {
                    count++;
                    etEngraveNum.setText(count + "");
                    totalEngraveCount = Integer.valueOf(etEngraveNum.getText().toString().trim());
                } else {
                    Toast.makeText(PreViewActivity.this, "最多为 " + MAX_COUNT + " 次", Toast.LENGTH_SHORT).show();
                    etEngraveNum.setText(String.valueOf(MAX_COUNT));
                    totalEngraveCount = Integer.valueOf(etEngraveNum.getText().toString().trim());
                }
            }
        });

        // 限制输入框只能输入数字，最大为 2 位数
        etEngraveNum.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(3)
        });

        // 给 EditText 添加文本变化监听器
        etEngraveNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文本变化后的回调，这里实现防抖逻辑

                // 1. 如果有上一次未执行的校验任务，先取消它
                if (validateRunnable != null) {
                    handler.removeCallbacks(validateRunnable);
                }

                // 2. 创建一个新的校验任务
                validateRunnable = () -> {
                    // 在延迟结束后执行的逻辑，比如检查数值是否在范围内
                    validateEngraveNum(etEngraveNum, MIN_COUNT, MAX_COUNT);
                };

                // 3. 将任务延迟执行，只有在延迟期间没有新的输入，任务才会真正执行
                handler.postDelayed(validateRunnable, DEBOUNCE_DELAY);
            }
        });


        // 预览范围
        tvDialogLineJugde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (zoomViewBeanslist.size() < 1) {
                    Toast.makeText(PreViewActivity.this, "请先添加图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                WebSocketManager webSocketManager = WebSocketManager.getInstance();
                boolean isConnected = webSocketManager.isConnected();
                if (isConnected) {
                    // 显示巡边弹窗
                    showDialogLineJugde();
                } else {
                    BaseDialog.showCustomDialog(PreViewActivity.this,
                            "温馨提示", "检测到您还未连接设备，无法进行雕刻！\r\n\r\n是否连接设备？",
                            "确定", "取消",
                            vbd -> {
                                Intent intent = new Intent(PreViewActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            },
                            vbd -> {
                                Log.d(TAG, "用户点击取消");
                            });
                }
            }
        });
        // 雕刻
        tvDialogEngrave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebSocketManager webSocketManager = WebSocketManager.getInstance();
                boolean isConnected = webSocketManager.isConnected();
                if (isConnected) {
                    // TODO 准备雕刻
                    beginToEngrave();
                } else {
                    BaseDialog.showCustomDialog(PreViewActivity.this,
                            "温馨提示", "检测到您还未连接设备，无法进行雕刻！\r\n\r\n是否连接设备？",
                            "确定", "取消",
                            vbd -> {
                                Intent intent = new Intent(PreViewActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            },
                            vbd -> {
                                Log.d(TAG, "用户点击取消");
                            });
                }

                // 隐藏弹窗
                dialog.dismiss();
            }
        });

        // 设置关闭按钮
        ImageView ivCancel = dialog.findViewById(R.id.iv_cancel);
        ivCancel.setOnClickListener(v -> dialog.dismiss());  // 点击关闭按钮时关闭 Dialog

        // 显示 Dialog
        dialog.show();
    }


    /**
     * 校验雕刻次数
     *
     * @param et        输入框
     * @param MIN_COUNT 最小雕刻次数
     * @param MAX_COUNT 最大雕刻次数
     * @return true(合规) or false(不合规)
     */
    private boolean validateEngraveNum(EditText et, int MIN_COUNT, int MAX_COUNT) {
        String input = et.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "请输入雕刻次数", Toast.LENGTH_SHORT).show();
            et.setText(String.valueOf(MIN_COUNT));
            totalEngraveCount = Integer.valueOf(et.getText().toString().trim());
            return false;
        }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            et.setText(String.valueOf(MIN_COUNT));
            totalEngraveCount = Integer.valueOf(et.getText().toString().trim());
            return false;
        }

        if (value < MIN_COUNT) {
            Toast.makeText(this, "最少为 " + MIN_COUNT + " 次", Toast.LENGTH_SHORT).show();
            et.setText(String.valueOf(MIN_COUNT));
            totalEngraveCount = Integer.valueOf(et.getText().toString().trim());
            return false;
        } else if (value > MAX_COUNT) {
            Toast.makeText(this, "最多为 " + MAX_COUNT + " 次", Toast.LENGTH_SHORT).show();
            et.setText(String.valueOf(MAX_COUNT));
            totalEngraveCount = Integer.valueOf(et.getText().toString().trim());
            return false;
        }

        return true;
    }


    /**
     * 准备雕刻
     */
    private void beginToEngrave() {
        if (connectType.equals("AP") || connectType.equals("STA")) {
            Bitmap mergedBitmap = combineBitmapsFromZoomBeans(
                    PreViewActivity.this,
                    zoomViewBeanslist,
                    locations
            );
            Log.d("单位换算", "1mm 对应像素：" + locations);


            // 保存成文件
            finalBitmapFile = ImgUtil.saveBitmap("2_finalBitmap_" + System.currentTimeMillis() + ".png", mergedBitmap);

            new Thread(() -> {
                try {
                    // 生成切片弹窗
                    transformData();

                    Image2Gcode image2Gcode = new Image2Gcode();
                    int totalImages = zoomViewBeanslist.size();
                    int[] currentIndex = {0}; // 当前处理到第几张图

                    // 创建输出路径
                    String FILE_NAME = "";
                    long timestamp = System.currentTimeMillis();
                    Date date = new Date(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    String formattedDate = sdf.format(date);
                    if (tvMachineName.getText().toString().contains("CNC")) {
                        FILE_NAME = "CNC_" + formattedDate + ".nc";
                    } else {
                        FILE_NAME = "Laser_" + formattedDate + ".nc";
                    }
                    String filePath = GrblController.getInstance().getExternalFilesDir(null) + "/laser/";
                    String strFilePath = filePath + "/" + FILE_NAME;
                    File file = new File(strFilePath);
                    if (file.exists()) file.delete();

                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

                    for (int i = 0; i < zoomViewBeanslist.size(); i++) {
                        BitmapFactory.Options mOptions = new BitmapFactory.Options();
                        mOptions.inScaled = false;
                        ZoomViewBean zoomViewBean = zoomViewBeanslist.get(i);

                        InputStream input = getContentResolver().openInputStream(zoomViewBean.getUri());
                        Bitmap bitmaps = BitmapFactory.decodeStream(input, null, mOptions);

                        int printWidth = zoomViewBean.getWide();
                        int printHeight = zoomViewBean.getHeight();
                        float resol = zoomViewBean.getResols();

                        Bitmap adjustBitmap = ImageProcess.imageResize(bitmaps, printWidth, printHeight, resol);
                        FileManager.get().addDelPath(zoomViewBean.getUri().getPath());

                        List<String> strcontent = new ArrayList<>();

                        Image2Gcode.GcodeProgressCallback callback = percent -> {
                            int overallPercent = (int) ((currentIndex[0] + percent / 100f) * 100 / totalImages);
                            runOnUiThread(() -> {
                                Log.d(TAG, "转换中 " + overallPercent + "%");

                            });
                        };

                        switch (zoomViewBean.getTypes()) {
                            case "1": // 灰度图
                            case "2": // 黑白图
                            case "4": // 素描图
                                if (tvMachineName.getText().toString().contains("CNC")) {
                                    strcontent = image2Gcode.image2GcodeForCNC(adjustBitmap, zoomViewBean.getWide(), zoomViewBean.getHeight(), 200, 5,
                                            zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
                                } else {
                                    strcontent = image2Gcode.image2Gcode(adjustBitmap, printWidth, printHeight, resol,
                                            zoomViewBean.getSpeedLevel(), zoomViewBean.getLaserLevel() * 10,
                                            zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY(),
                                            zoomViewBean.getScanDirection(), callback);
                                }
                                break;
                            case "3": // 轮廓模式
                                Bitmap initBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(zoomViewBean.getInitBitmapUri()),
                                        null, mOptions);
                                Matrix m = new Matrix();
                                for (EffectBean effectBean : zoomViewBean.getEffectBeans()) {
                                    if (effectBean.getEffectType() == 1) {
                                        m.postScale(-1, 1); // 镜像水平翻转
                                    } else if (effectBean.getEffectType() == 2) {
                                        m.postRotate(effectBean.getRotate());
                                    }
                                }
                                initBitmap = Bitmap.createBitmap(initBitmap, 0, 0, initBitmap.getWidth(), initBitmap.getHeight(), m, true);
                                Bitmap outlineBitmap = ImageProcess.imageResize(initBitmap, printWidth, printHeight, resol);
                                strcontent = image2Gcode.outlineImage2Gcode(outlineBitmap, printWidth, printHeight,
                                        zoomViewBean.getSpeedLevel(), zoomViewBean.getLaserLevel() * 10,
                                        zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY(), zoomViewBean.isAir(), zoomViewBean.getzDown());
                                break;
                        }

                        // 写入重复次数（雕刻多次）
                        for (int t = 0; t < totalEngraveCount; t++) {
                            for (String line : strcontent) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }

                        writer.flush();
                        currentIndex[0]++;
                    }

                    // 添加结尾命令
                    if (isAutoHome) {
                        writer.write("$H");
                        writer.newLine();
                    }
                    if (isSoundAlert) {
                        writer.write("$bb");
                        writer.newLine();
                    }

                    writer.flush();
                    writer.close();

                    // 可选：回调或提示生成完成
                    runOnUiThread(() -> {
                        Toast.makeText(PreViewActivity.this, "G-code 生成完成", Toast.LENGTH_SHORT).show();
                        uploadFile(file, MAX_RETRY_NUM);
                    });

                } catch (Exception e) {
                    Log.e(TAG, "G-code 转换异常", e);
                    runOnUiThread(() -> {
                        Toast.makeText(PreViewActivity.this, "G-code 转换失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();


        } else {
            Toast.makeText(PreViewActivity.this, "蓝牙模式暂不支持TF上传，敬请期待下一版本", Toast.LENGTH_SHORT).show();
        }
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
        // 获取 ProgressBar 和 TextView
        transformProgressBar = dialogView.findViewById(R.id.progressBar);
        transformProgressText = dialogView.findViewById(R.id.progressText);
        // UI线程
        runOnUiThread(() -> {
            // 创建弹窗
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("温馨提示");
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setCancelable(false);
            dialogTransform = alertDialogBuilder.create();

            // 定义一个计数器，用来循环显示点数
            final int[] dotCount = {0};  // 用数组包裹，方便在Runnable中修改
            final String baseText = "处理中，请耐心等待";  // 基础文字

            // 创建 Handler 和 Runnable 来更新显示的内容
            Handler handler = new Handler();  // 使用主线程的 Looper

            Runnable loadingRunnable = new Runnable() {
                @Override
                public void run() {
                    // 根据当前的点数决定显示的文本
                    StringBuilder loadingText = new StringBuilder(baseText);

                    // 增加点数
                    for (int i = 0; i < dotCount[0]; i++) {
                        loadingText.append(".");
                    }

                    // 更新 TextView 显示
                    content.setText(loadingText.toString());

                    // 更新点数，最多到 3 个点后重置
                    dotCount[0]++;
                    if (dotCount[0] > 3) {
                        dotCount[0] = 0;  // 重置点数
                    }

                    // 每 500 毫秒更新一次
                    handler.postDelayed(this, 500);
                }
            };

            // 启动动画
            handler.post(loadingRunnable);
            // 显示弹窗
            dialogTransform.show();
        });
    }

    private void updateTransformProgress(int percent) {
        runOnUiThread(() -> {
            // 设置进度条
            transformProgressBar.setProgress(percent);
            // 设置进度值
            transformProgressText.setText(percent + "%");
        });
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
            alertDialogBuilder.setTitle("温馨提示");
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setCancelable(false);
            // UI线程
            runOnUiThread(() -> {
                dialogUpload = alertDialogBuilder.create();
                // 显示弹窗
                dialogUpload.show();
            });


            WebSocketManager webSocketManager = WebSocketManager.getInstance();
            webSocketManager.disconnect();

            // 初始化
            OkHttpUtils.getInstance();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)  // 连接超时
                    .readTimeout(120, TimeUnit.SECONDS)     // 读取超时
                    .writeTimeout(120, TimeUnit.SECONDS)    // 写入超时
                    .build();

            OkHttpUtils.initClient(client);

            if (connectType.equals("AP") || connectType.equals("STA")) {
                String ipAddress = sharedPref.getString(getString(R.string.preference_sta_type_ipaddress), "");
                if (!TextUtils.isEmpty(ipAddress)) {
                    Log.d(TAG, "ipAddress = " + ipAddress);
                    // 文件上传
                    OkHttpUtils.post().addFile("myfile[]", file.getName(), file)
                            .url("http://" + ipAddress + "/upload")
                            .addParams("path", "/")
                            .addParams("/" + file.getName(), String.valueOf(file.length()))
                            .tag(this).build().execute(new StringCallback() {

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
                                    // 重新连接
                                    webSocketManager.connect(ipAddress);
                                    // 对刀弹窗
                                    showDialogKinfe(finalBitmapFile.getAbsolutePath(), file.getPath());

                                }
                            });
                } else {
                    Toast.makeText(PreViewActivity.this, "上传地址为空，请联系客服！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PreViewActivity.this, "蓝牙模式暂不支持TF上传，敬请期待下一版本", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PreViewActivity.this, "上传失败，请检查并重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 对刀弹窗
     *
     * @param bitmapFilePath 图片路径
     * @param filePath       文件路径
     */
    private void showDialogKinfe(String bitmapFilePath, String filePath) {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_knife);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 设置可取消（点击空白处取消）
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

        // 对焦
        TextView tvDialogKnife = dialog.findViewById(R.id.tv_dialog_knife);
        tvDialogKnife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 禁用按钮，防止再次点击
                tvDialogKnife.setEnabled(false);
                // 修改按钮背景颜色为灰色
                tvDialogKnife.setBackgroundResource(R.drawable.bg_gray_999999_r30);

                // 定义一个计数器，用来循环显示点数
                final int[] dotCount = {0};  // 用数组包裹，方便在Runnable中修改
                final String baseText = "对刀中，请耐心等待";  // 基础文字

                // 创建 Handler 和 Runnable 来更新显示的内容
                Handler handler = new Handler();  // 使用主线程的 Looper
                Runnable loadingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // 根据当前的点数决定显示的文本
                        StringBuilder loadingText = new StringBuilder(baseText);

                        // 增加点数
                        for (int i = 0; i < dotCount[0]; i++) {
                            loadingText.append(".");
                        }

                        // 更新 TextView 显示
                        tvDialogKnife.setText(loadingText.toString());

                        // 更新点数，最多到 3 个点后重置
                        dotCount[0]++;
                        if (dotCount[0] > 3) {
                            dotCount[0] = 0;  // 重置点数
                        }

                        // 每 500 毫秒更新一次
                        handler.postDelayed(this, 500);
                    }
                };
                // 启动动画
                handler.post(loadingRunnable);

                // 对焦
                sendJogCommand("[esp212]");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 检查wposZ的连续一致性
                        if (checkWposZConsistency(wposZ)) {
                            // 隐藏弹窗
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            // 保存历史记录
                            saveEngraveRecord();

                            // 跳转雕刻页面
                            Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                            intent.putExtra("machineName", tvMachineName.getText().toString());
                            intent.putExtra("imagePath", bitmapFilePath);
                            intent.putExtra("filePath", filePath);
                            intent.putExtra("totalEngraveCount", totalEngraveCount);
                            Log.d(TAG, "totalEngraveCount=" + totalEngraveCount);
                            // 启动跳转
                            startActivity(intent);
                            // 关闭当前页面
                            finish();
                        } else {
                            // 如果3次不一致，则继续等待
                            new Handler().postDelayed(this, 500);  // 每秒检查2次
                        }
                    }
                }, 1000);  // 10秒后执行


                isKinfe = true;
            }
        });

        // 取消
        ImageView ivCancel = dialog.findViewById(R.id.iv_cancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isKinfe) {
                    Toast.makeText(PreViewActivity.this, "正在对刀中，无法取消", Toast.LENGTH_SHORT).show();
                } else {
                    // 不对刀确认弹窗
                    showNotKinfeConfirm(bitmapFilePath, filePath, totalEngraveCount);
                    // 隐藏弹窗
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        });

        // 显示 Dialog
        dialog.show();
    }

    /**
     * 不对刀确认弹窗
     *
     * @param bitmapFilePath
     * @param filePath
     */
    private void showNotKinfeConfirm(String bitmapFilePath, String filePath, int engraveCount) {
        BaseDialog.showCustomDialog(this,
                "温馨提示",
                "不对焦存在一定的风险，可能导致雕刻达不到预期的效果，是否取消？",
                "确定",
                "取消",
                v -> {
                    // 保存历史记录
                    saveEngraveRecord();
                    // 跳转雕刻页面
                    Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                    intent.putExtra("machineName", tvMachineName.getText().toString());
                    intent.putExtra("imagePath", bitmapFilePath);
                    intent.putExtra("filePath", filePath);
                    intent.putExtra("totalEngraveCount", engraveCount);
                    // 启动跳转
                    startActivity(intent);
                    // 关闭当前页面
                    finish();
                },
                v -> {
                    // 对刀弹窗
                    showDialogKinfe(bitmapFilePath, filePath);
                }
        );
    }

    /**
     * 开始轮询检查巡边是否完成
     *
     * @param dialog         巡边弹窗
     * @param originalStutas 机器状态
     */
    private void checkCoordinatesUntilOriginal(Dialog dialog, String originalStutas) {
        // 创建一个 Handler 来进行轮询检查
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 检查当前坐标是否已恢复到原始坐标
                if (strMachineStatus.equals(originalStutas)) {
                    // 如果坐标匹配，增加连续匹配的次数
                    consecutiveMatches++;

                    // 如果连续三次匹配，关闭进度对话框
                    if (consecutiveMatches >= 3) {
                        dialog.dismiss();
                        consecutiveMatches = 0;  // 重置匹配次数
                    } else {
                        // 如果匹配不够三次，继续检查
                        checkCoordinatesUntilOriginal(dialog, originalStutas);
                    }
                } else {
                    // 如果坐标不匹配，重置匹配次数
                    consecutiveMatches = 0;
                    // 继续检查
                    checkCoordinatesUntilOriginal(dialog, originalStutas);
                }
            }
        }, 500); // 每隔500毫秒检查一次坐标
    }


    /**
     * 检查wposZ是否连续5次相同
     */
    private boolean checkWposZConsistency(String newValue) {
        // 保存最新的wposZ值
        if (wposZHistory.size() == MAX_HISTORY_SIZE) {
            wposZHistory.removeFirst(); // 保持队列大小为3
        }
        wposZHistory.add(newValue);

        // 如果队列已经满了，检查所有值是否相同
        if (wposZHistory.size() == MAX_HISTORY_SIZE) {
            for (int i = 1; i < wposZHistory.size(); i++) {
                if (!wposZHistory.get(i).equals(wposZHistory.get(0))) {
                    return false; // 如果有任何一个不相同，则返回false
                }
            }
            return true; // 如果所有值都相同，返回true
        }

        return false; // 如果队列没有满5个值，返回false
    }

    /**
     * 发送命令
     *
     * @param command
     */
    private void sendJogCommand(String command) {
        Log.d(TAG, "command=" + command);
        WebSocketManager webSocketManager = WebSocketManager.getInstance();
        webSocketManager.send(command);
    }

    /**
     * 保存历史记录
     */
    private void saveEngraveRecord() {
        EngraveHistoryRecord record = new EngraveHistoryRecord();
        record.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        record.setMachineName(tvMachineName.getText().toString());
        List<ZoomViewRecord> recordBeans = new ArrayList<>();
        for (ZoomViewBean bean : zoomViewBeanslist) {
            ZoomViewRecord rec = new ZoomViewRecord();
            rec.setResols(bean.getResols());
            rec.setDepthProgress(bean.getDepthProgress());
            rec.setSpeedProgress(bean.getSpeedProgress());
            rec.setSpeedLevel(bean.getSpeedLevel());
            rec.setLaserLevel(bean.getLaserLevel());
            rec.setOperationMode(bean.getOperationMode());
            rec.setScaleX(bean.getScaleX());
            rec.setScaleY(bean.getScaleY());
            rec.setAndReverse(bean.isAndReverse());
            rec.setSharp(bean.getSharp());
            rec.setWide(bean.getWide());
            rec.setHeight(bean.getHeight());
            rec.setEditScrollX(bean.getEditScrollX());
            rec.setEditWideX(bean.getEditWideX());
            rec.setEditHighY(bean.getEditHighY());
            rec.setGcodes(bean.getGcodes());
            rec.setTypes(bean.getTypes());
            rec.setImagePath(bean.getUri() != null ? bean.getUri().toString() : "");
            recordBeans.add(rec);
        }
        record.setZoomViewRecords(recordBeans);
        record.setTotalEngraveCount(totalEngraveCount);
        record.setLocations(locations);
        record.setImagePath(finalBitmapFile != null ? finalBitmapFile.getAbsolutePath() : "");

        // 创建文件路径及目录
        File directory = new File(GrblController.getInstance().getExternalFilesDir(null) + "/history");
        if (!directory.exists()) directory.mkdirs(); // 确保目录存在

        File file = new File(directory, "engrave_history.json");
        List<EngraveHistoryRecord> allHistory = FileUtil.readEngraveHistoryFromFile(file);
        allHistory.add(record);
        FileUtil.saveEngraveHistoryToFile(allHistory, file);
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

                strMachineStatus = parts[0];

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

                String[] WposParts = parts[2].substring(5, parts[2].length()).split(",");
                Log.d(TAG, "Wpos X=" + WposParts[0] + " Y=" + WposParts[1] + " Z=" + WposParts[2]);
                wposZ = WposParts[2];

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

    /**
     * 添加视图
     */
    public void addDragView(Bitmap bm, boolean lean, String type, Uri url, Uri initBitmapUri, int operationMode) {
        Log.d(TAG, "Uri=" + url.getPath());
        Log.d(TAG, "operationMode=" + operationMode);
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
        zoomViewBean.setOperationMode(operationMode);
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
                    initEdit();
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
                float xs = sharedPref.getInt(getString(R.string.preference_machine_width), 85) / (zoomViewBean.getIvIcon().getWidth() / locations);
                float ys = sharedPref.getInt(getString(R.string.preference_machine_height), 85) / (zoomViewBean.getIvIcon().getHeight() / locations);
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
            if (!zoomViewBean.isSpeedSet()) {
                tvSpeedlevel.setText("");
            } else {
                tvSpeedlevel.setText(zoomViewBean.getSpeedLevel() + "");
            }

            if (!zoomViewBean.isLaserSet()) {
                tvLaserlevel.setText("");
            } else {
                tvLaserlevel.setText(zoomViewBean.getLaserLevel() + "");
            }

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

    private void initEdit() {
        if (editWideXhasFocus || editHighYhasFocus || editHighhasFocus || editWidehasFocus) {
            editWideXhasFocus = false;
            editHighYhasFocus = false;
            editHighhasFocus = false;
            editWidehasFocus = false;
            etWidth.setEnabled(false);
            etHeight.setEnabled(false);
            etXpos.setEnabled(false);
            etYpos.setEnabled(false);
            etWidth.setEnabled(true);
            etHeight.setEnabled(true);
            etXpos.setEnabled(true);
            etYpos.setEnabled(true);
        }
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
                                    addDragView(results, true, bundleData.getString("type"), bundleData.getParcelable(BuildConfig.APPLICATION_ID + ".InputUri"), bundleData.getParcelable("initedBitmapUri"), bundleData.getInt("operationMode"));
                                }
                            }));
                }
                break;
            }
        }
    }

    /**
     * 缝合图层到位图
     *
     * @param context           上下文
     * @param zoomViewBeanslist 图层数据
     * @param unitSize
     * @return
     */
    public static Bitmap combineBitmapsFromZoomBeans(Context context, List<ZoomViewBean> zoomViewBeanslist, float unitSize) {
        if (zoomViewBeanslist == null || zoomViewBeanslist.isEmpty()) {
            return null;
        }

        Log.d("合成调试", "🌟 开始合成，unitSize = " + unitSize);

        // 计算画布大小
        int maxEditWideX = 0;
        int maxEditHighY = 0;
        for (ZoomViewBean bean : zoomViewBeanslist) {
            int endX = bean.getEditWideX() + bean.getWide();   // 宽度格子数
            int endY = bean.getEditHighY() + bean.getHeight(); // 高度格子数
            maxEditWideX = Math.max(maxEditWideX, endX);
            maxEditHighY = Math.max(maxEditHighY, endY);
        }

        int canvasWidth = Math.round(maxEditWideX * unitSize);
        int canvasHeight = Math.round(maxEditHighY * unitSize);

        Log.d("合成调试", "📍 原点基准 minX = 0.0，minY = 0.0");
        Log.d("合成调试", "🖼️ 最终画布大小：width = " + canvasWidth + ", height = " + canvasHeight);

        Bitmap result = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        for (int i = 0; i < zoomViewBeanslist.size(); i++) {
            ZoomViewBean zoomViewBean = zoomViewBeanslist.get(i);
            Bitmap bitmap = zoomViewBean.getBitmap();
            if (bitmap == null || bitmap.isRecycled()) continue;

            int rawWidth = zoomViewBean.getWide();
            int rawHeight = zoomViewBean.getHeight();

            // 缩放 bitmap 到实际要显示的像素大小
            int targetWidth = Math.round(rawWidth * unitSize);
            int targetHeight = Math.round(rawHeight * unitSize);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

            // 计算位置（以左下角为原点）
            float posX = zoomViewBean.getEditWideX() * unitSize;
            float posY = (maxEditHighY - zoomViewBean.getEditHighY() - rawHeight) * unitSize;

            Log.d("合成调试", "✅ 第 " + i + " 张图：");
            Log.d("合成调试", "     editWideX = " + zoomViewBean.getEditWideX()
                    + ", editHighY = " + zoomViewBean.getEditHighY());
            Log.d("合成调试", "     scaleX = 1.0, scaleY = 1.0");
            Log.d("合成调试", "     合成位置 px => posX = " + posX + ", posY = " + posY
                    + ", width = " + targetWidth + ", height = " + targetHeight);

            canvas.drawBitmap(scaledBitmap, posX, posY, paint);
        }

        Log.d("合成调试", "✅ 合成完成");
        return result;
    }


    @Override
    public void onLaserParametersSelected(int index, int power, int speed, float resols, ScanDirection scanDirection, int engraveCount, boolean isAir, int zDown) {
        Log.d(TAG, "index=" + index + "------power=" + power + "------speed=" + speed
                + "------resols=" + resols + "------scanDirection=" + scanDirection
                + "------engraveCount=" + engraveCount + "------isAir=" + isAir +  "------zDown=" + zDown);

        if (index >= 0 && index < zoomViewBeanslist.size()) {
            ZoomViewBean bean = zoomViewBeanslist.get(index);
            bean.setLaserLevel(power);
            bean.setSpeedLevel(speed);
            bean.setResols(resols);
            bean.setScanDirection(scanDirection);
            bean.setAir(isAir);
            bean.setzDown(zDown);


            if (index == zoomViewPosition) {
                tvLaserlevel.setText(power + "");
                tvSpeedlevel.setText(speed + "");
            }
        }

        totalEngraveCount = engraveCount;
    }
}
