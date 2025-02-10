
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.LinkedList;
import java.util.List;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.ControltoPreViewMessageEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.fragment.ControlBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.ParameterBottomSheetFragment;
import in.co.gorest.grblcontroller.model.Constants;
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

public class PreViewActivity extends BaseActivity implements ParameterBottomSheetFragment.OnLaserParametersSelectedListener {
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
                if (strMachineStatus.equals(Constants.MACHINE_STATUS_RUN)) {
                    BaseDialog.showCustomDialog(PreViewActivity.this, "温馨提示",
                            "检测到设备正在雕刻是否跳转到雕刻界面？",
                            "跳转", "取消",
                            v1 -> {
                                Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                                String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                                String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                                intent.putExtra("imagePath", imagePath);
                                intent.putExtra("filePath", filePath);
                                startActivity(intent);
                            },
                            v1 -> {
                                Log.d(TAG, "用户选择取消");
                            });
                } else if (strMachineStatus.equals(Constants.MACHINE_STATUS_HOLD)) {
                    sendJogCommand("\u0018");
                } else {
                    // 检查速度和激光功率
                    checkSpeedAndLaserLevel();
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


        // 速度
        llSpeedlevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterBottomSheetFragment parameterBottomSheetFragment = new ParameterBottomSheetFragment();
                parameterBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });


        // 激光功率
        llLaserlevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterBottomSheetFragment parameterBottomSheetFragment = new ParameterBottomSheetFragment();
                parameterBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 巡边
        tvLineJudge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示巡边弹窗
                showDialogLineJugde();
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
                ParameterBottomSheetFragment parameterBottomSheetFragment = new ParameterBottomSheetFragment();
                parameterBottomSheetFragment.show(getSupportFragmentManager(), "");
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
        if (tvLaserlevel.getText().equals("") || tvSpeedlevel.getText().equals("")) {
            // TODO 弹窗提示先设置速度和激光功率
            // 创建自定义弹窗对象
            BaseAlertDialog baseAlertDialog = new BaseAlertDialog(PreViewActivity.this);

            // 显示弹窗并传入标题、内容以及确认按钮的点击事件
            baseAlertDialog.show("温馨提示", "当前未设置雕刻相关参数，请设置后进行雕刻！", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击确认按钮后执行的操作
                    Log.d(TAG, "用户点击了确认按钮");
                    // 打开弹窗
                    ParameterBottomSheetFragment parameterBottomSheetFragment = new ParameterBottomSheetFragment();
                    parameterBottomSheetFragment.show(getSupportFragmentManager(), "");
                }
            });
        } else {

            // TODO 显示巡边或雕刻弹窗
            showDialogLineJugdeOrEngrave();
        }
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


        // 记录X和Y坐标初始值
        final String originalStutas = strMachineStatus;

        // 开始巡边
        lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
        Log.d(TAG, "lineJudgeLaserLevel=" + lineJudgeLaserLevel);
        sendJogCommand("G0 X" + etXpos.getText().toString() + " Y" + etYpos.getText().toString());
        sendJogCommand("M3 S" + lineJudgeLaserLevel * 10);
        sendJogCommand("F3500");
        sendJogCommand("G1 Y" + String.valueOf(Integer.valueOf(etHeight.getText().toString()) + Integer.valueOf(etYpos.getText().toString())));
        sendJogCommand("G1 X" + String.valueOf(Integer.valueOf(etWidth.getText().toString()) + Integer.valueOf(etXpos.getText().toString())));
        sendJogCommand("G1 Y" + etYpos.getText().toString());
        sendJogCommand("G1 X" + etXpos.getText().toString());
        sendJogCommand("M5");
        sendJogCommand("G0 X" + etXpos.getText().toString() + " Y" + etYpos.getText().toString());

        // 开始轮询检查坐标是否恢复
        checkCoordinatesUntilOriginal(dialog,originalStutas);

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


        // 预览范围
        TextView tvDialogLineJugde = dialog.findViewById(R.id.tv_dialog_linejugde);
        // 雕刻
        TextView tvDialogEngrave = dialog.findViewById(R.id.tv_dialog_engrave);

        // 预览范围
        tvDialogLineJugde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示巡边弹窗
                showDialogLineJugde();
            }
        });
        // 雕刻
        tvDialogEngrave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO 准备雕刻
                beginToEngrave();

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
     * 准备雕刻
     */
    private void beginToEngrave() {
        // 获取当前模式
        String connectType = sharedPref.getString(getString(R.string.preference_connect_type), "AP");
        if (!connectType.equals("AP")) {
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
                                        , Integer.valueOf(tvSpeedlevel.getText().toString()), Integer.valueOf(tvLaserlevel.getText().toString()) * 10, zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
                                break;
                            case "2"://黑白图
                            case "4":// 素描模式
                                strcontent = image2Gcode.image2Gcode(adjustBitmap, resol
                                        , Integer.valueOf(tvSpeedlevel.getText().toString()), Integer.valueOf(tvLaserlevel.getText().toString()) * 10, zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
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
                                strcontent = image2Gcode.outlineImage2Gcode(outlineAdjustBitmap, printWidth, printHeight, Integer.valueOf(tvSpeedlevel.getText().toString()),
                                        Integer.valueOf(tvLaserlevel.getText().toString()) * 10, zoomViewBean.getEditWideX(), zoomViewBean.getEditHighY());
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
//            // 设置内容
//            content.setText("切片中，请耐心等待！");
            // 显示弹窗
            dialogTransform.show();
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


                    // 对刀弹窗
                    showDialogKinfe(finalBitmapFile.getAbsolutePath(), file.getPath());


                }
            });
        } else {
            Toast.makeText(PreViewActivity.this, "上传失败，请检查并重试", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 对刀弹窗
     * @param bitmapFilePath 图片路径
     * @param filePath 文件路径
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
                            // 跳转雕刻页面
                            Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                            intent.putExtra("imagePath", bitmapFilePath);
                            intent.putExtra("filePath", filePath);
                            startActivity(intent);
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
                    showNotKinfeConfirm(bitmapFilePath, filePath);
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
     * @param bitmapFilePath
     * @param filePath
     */
    private void showNotKinfeConfirm(String bitmapFilePath, String filePath) {
        BaseDialog.showCustomDialog(this,
                "温馨提示",
                "不对刀存在一定的风险，可能导致雕刻达不到预期的效果，是否取消？",
                "确定",
                "取消",
                v -> {
                    // 跳转雕刻页面
                    Intent intent = new Intent(PreViewActivity.this, EngraveActivity.class);
                    intent.putExtra("imagePath", bitmapFilePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
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
     * @param dialog 巡边弹窗
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

    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty() && event.getMessage().startsWith("<")) {
            Log.d(TAG, "message=" + event.getMessage().toString());
            String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
            Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);
            strMachineStatus = parts[0];

            String[] WposParts = parts[2].substring(5, parts[2].length()).split(",");
            Log.d(TAG, "Wpos X=" + WposParts[0] + " Y=" + WposParts[1] + " Z=" + WposParts[2]);
            wposZ = WposParts[2];
        }
    }




    /**
     * 添加视图
     */
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


    @Override
    public void onLaserParametersSelected(int power, int speed) {
        Log.d(TAG, "power=" + power + "----speed=" + speed);
        // 设置速度
        tvSpeedlevel.setText(speed + "");
        // 设置激光功率
        tvLaserlevel.setText(power + "");
    }
}
