package in.co.gorest.grblcontroller.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.MaterialAdapter;
import in.co.gorest.grblcontroller.databinding.FragmentWorkTabBinding;
import in.co.gorest.grblcontroller.events.APModelUploadEvent;
import in.co.gorest.grblcontroller.events.FilePathEvent;
import in.co.gorest.grblcontroller.events.JogCommandEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.events.ViewPagerItemEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.listeners.FileSenderListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.EffectBean;
import in.co.gorest.grblcontroller.model.PictureBean;
import in.co.gorest.grblcontroller.util.FileManager;
import in.co.gorest.grblcontroller.util.FileUploader;
import in.co.gorest.grblcontroller.util.FileUtils;
import in.co.gorest.grblcontroller.util.GcodeResults;
import in.co.gorest.grblcontroller.util.Image2Gcode;
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


public class WorkTabFragment extends BaseFragment {

    private final String TAG = "WorkTabFragment";
    // 机器状态
    private MachineStatusListener machineStatus;
    // 文件进程
    private FileSenderListener fileSender;
    // sharedPref
    private EnhancedSharedPreferences sharedPref;
    // 素材列表
    private List<PictureBean> pictureList;
    //  // 适配器
    private MaterialAdapter pictureAdapter;
    // CompositeDisposable容器
    private static CompositeDisposable mCompositeDisposable;
    // 业务模式
    private int businessType;
    // 图片路径
    private Uri inputUri;
    // locations
    private float locations;
    // seekBars
    private int seekBars;
    // tabPosition
    private int tabPosition = 1;
    // 分辨率
    private float resols;
    // 锐值
    private int sharp;
    // 初始位图
    private Bitmap initedBitmap;
    // 最终位图
    private Bitmap finalBitmap;
    // 黑白反转标识
    private boolean andReverse;
    // tab title
    private List<String> title = Arrays.asList("灰度图", "黑白图", "轮廓", "素描");
    // 效果
    private List<EffectBean> effectBeans = new ArrayList<>();
    // handler
    public Handler handler = new Handler();
    // 写入的nc
    private List<String> strcontent = new ArrayList<>();

    // 素材
    private ImageView ivMaterial;
    // 锐值
    private LinearLayout rlSeekbarSharp;
    // 黑白反转
    private RelativeLayout rlWhiteAndReverse;
    // 宽度
    private EditText editWidth;
    // 高度
    private EditText editHeight;
    // 深度
    private EditText editDepth;
    // 速度
    private EditText editSpeed;

    // 素材二级弹窗
    private AlertDialog dialogSecond;

    // 初始比例
    private float aspectRatio = 1.0f;
    // 更新状态
    private boolean isUpdating = false;


    public WorkTabFragment() {
    }

    public static WorkTabFragment newInstance() {
        return new WorkTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        machineStatus = MachineStatusListener.getInstance();
        fileSender = FileSenderListener.getInstance();
        sharedPref = EnhancedSharedPreferences.getInstance(requireActivity().getApplicationContext(), getString(R.string.shared_preference_key));
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentWorkTabBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_work_tab, container, false);
        binding.setMachineStatus(machineStatus);
        binding.setFileSender(fileSender);
        View view = binding.getRoot();

        // 素材
        LinearLayout llMaterial = view.findViewById(R.id.ll_material);
        llMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 素材弹窗
                setMaterialDialog();
            }
        });

        // 相册
        LinearLayout llAlbum = view.findViewById(R.id.ll_album);
        llAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.ALBUM_REQUEST_CODE);
                } else {
                    ImgUtil.openAlbum(getActivity());
                }
            }
        });

        // 二维码
//        LinearLayout llQrCode = view.findViewById(R.id.ll_qrcode);
        // 条形码
//        LinearLayout llLineCode = view.findViewById(R.id.ll_linecode);
        // 文本
//        LinearLayout llText = view.findViewById(R.id.ll_text);

        // 设置businessType
        businessType = 1;
        // 设置locations
        locations = ScreenInchUtils.mmToPx(getActivity(), 1) + 1;
        // 设置seekBars
        seekBars = 1;
        return view;
    }

    /**
     * 素材弹窗
     */
    private void setMaterialDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_material, null, false);
        // 弹窗设置
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("cancel", (dialog, id) -> {

                });
        // 创建弹窗
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.setCancelable(false);
        dialog.show();

        // 素材列表
        RecyclerView rvPicture = view.findViewById(R.id.recyclerview_picture);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvPicture.setLayoutManager(gridLayoutManager);
        // 添加素材
        pictureList = new ArrayList<>();
        PictureBean pictureBean1 = new PictureBean();
        pictureBean1.setUrl(Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.mipmap.icon_a));
        pictureList.add(pictureBean1);
        PictureBean pictureBean2 = new PictureBean();
        pictureBean2.setUrl(Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.mipmap.icon_b));
        pictureList.add(pictureBean2);
        // 适配器
        pictureAdapter = new MaterialAdapter(getContext(), pictureList);
        rvPicture.setAdapter(pictureAdapter);
        // Item点击事件
        pictureAdapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
                int position = viewHolder.getAbsoluteAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                Log.d(TAG, pictureList.get(position).getUrl().toString());
                // 获取inputUri
                inputUri = pictureList.get(position).getUrl();
                if (inputUri != null) {
                    Log.d(TAG, inputUri.toString());
                    // 隐藏弹窗
                    dialog.dismiss();
                    // 素材弹窗
                    setMaterialSecondDialog();
                }
            }
        });
    }

    /**
     * 素材二级弹窗
     */
    private void setMaterialSecondDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_material_second, null, false);
        // 弹窗设置
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("cancel", (dialog, id) -> {

                });
        // 创建弹窗
        dialogSecond = alertDialogBuilder.create();
        dialogSecond.setCancelable(false);
        dialogSecond.show();

        // 素材
        ivMaterial = view.findViewById(R.id.iv_material);
        // 镜像
        ImageView ivMirror = view.findViewById(R.id.iv_mirror);
        ivMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix m = new Matrix();
                m.postScale(-1, 1);   //镜像水平翻转
                finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.getWidth(), finalBitmap.getHeight(), m, true);
                ivMaterial.setImageBitmap(finalBitmap);
                effectBeans.add(new EffectBean(1, 0));
            }
        });
        // 反转
        ImageView ivRotate = view.findViewById(R.id.iv_rotate);
        ivRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                effectBeans.add(new EffectBean(2, 90));
                Matrix m = new Matrix();
                m.postRotate(90);  //旋转-90度
                finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.getWidth(), finalBitmap.getHeight(), m, true);
            }
        });
        // tab_model
        MyTabLayout tabModel = view.findViewById(R.id.tab_model);
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
        // 锐值
        rlSeekbarSharp = view.findViewById(R.id.rl_seekbar_sharp);
        MySeekBar seekbarSharp = view.findViewById(R.id.seekbar_sharp);
        seekbarSharp.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                sharp = Progress;
                createEffectBitmap(tabPosition);
            }

            @Override
            public void onStop(int Progress) {

            }
        });
        // 黑白反转
        rlWhiteAndReverse = view.findViewById(R.id.rl_white_and_reverse);
        rlWhiteAndReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                andReverse = !andReverse;
                createEffectBitmap(tabPosition);
            }
        });
        // 宽度
        editWidth = view.findViewById(R.id.edit_width);
        editWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                try {
                    int newWidth = Integer.parseInt(s.toString());
                    int newHeight = Math.round(newWidth / aspectRatio);
                    isUpdating = true;
                    editHeight.setText(String.valueOf(newHeight));
                } catch (NumberFormatException e) {
                    // Handle exception
                } finally {
                    isUpdating = false;
                }
            }
        });
        // 高度
        editHeight = view.findViewById(R.id.edit_height);
        editHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                try {
                    int newHeight = Integer.parseInt(s.toString());
                    int newWidth = Math.round(newHeight * aspectRatio);
                    isUpdating = true;
                    editWidth.setText(String.valueOf(newWidth));
                } catch (NumberFormatException e) {
                    // Handle exception
                } finally {
                    isUpdating = false;
                }
            }
        });
        // 深度
        editDepth = view.findViewById(R.id.edit_depth);
        // 速度
        editSpeed = view.findViewById(R.id.edit_speed);
        // 生成G代码
        Button btnGenerate = view.findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateCode();
            }
        });

        rlSeekbarSharp.setVisibility(View.INVISIBLE);
        rlWhiteAndReverse.setVisibility(View.GONE);

        try {
            initBitmap();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "initBitmap_failed" + e);
            dialogSecond.dismiss();
        }
        // CompositeDisposable容器
        mCompositeDisposable = new CompositeDisposable();
        // 初始化分辨率
        resols = 0.08f;
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
        // 初始化锐值
        sharp = 127;
        seekbarSharp.setProgressMin(1);
        seekbarSharp.setProgressMax(255);
        seekbarSharp.setProgressDefault(127);

        // 设置深度
        editDepth.setText("20");
        // 设置速度
        editSpeed.setText("100");
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

        int HIGH = ScreenInchUtils.mmToPx(getActivity(), 85) + 1;
        int ENTER = ScreenInchUtils.mmToPx(getActivity(), 85) + 1;

        Bitmap whiteEdgeRemovalBitmap = ImageProcess.ImageWhiteEdgeRemoval(inputUribitmaps, HIGH, ENTER);
        initedBitmap = whiteEdgeRemovalBitmap;

        Log.e(TAG, "initedBitmap getWidth:" + initedBitmap.getWidth() + ",getHeight:" + initedBitmap.getHeight());


        // 设置宽度
        editWidth.setText(String.valueOf(Math.round(initedBitmap.getWidth() * 1.0f / locations)));
        // 设置高度
        editHeight.setText(String.valueOf(Math.round(initedBitmap.getHeight() * 1.0f / locations)));

        // 初始比例
        aspectRatio = (float) Math.round(initedBitmap.getWidth() * 1.0f / locations) / Math.round(initedBitmap.getHeight() * 1.0f / locations);


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
        InputStream input = getActivity().getContentResolver().openInputStream(uri);
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = false;
        mOptions.inSampleSize = inSampleSize;
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, mOptions);
        if (bitmap.getWidth() >= 1000 || bitmap.getHeight() >= 1000) {
            return getBitmap(inSampleSize + 1, uri);
        } else {
            bitmap = PictureUtil.rotaingImageView(PictureUtil.readPictureDegree(FileUtils.getPathFromUri(getContext(), uri)), bitmap);
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
                                rlSeekbarSharp.setVisibility(View.INVISIBLE);
                                rlWhiteAndReverse.setVisibility(View.GONE);
                                seekBars = 0;
                                break;
                            case 1://黑白图
                                ivMaterial.setImageDrawable(null);
                                rlSeekbarSharp.setVisibility(View.VISIBLE);
                                rlWhiteAndReverse.setVisibility(View.VISIBLE);
                                seekBars = 1;
                                break;
                            case 2://轮廓
                                ivMaterial.setImageDrawable(null);
                                rlSeekbarSharp.setVisibility(View.INVISIBLE);
                                rlWhiteAndReverse.setVisibility(View.GONE);
                                seekBars = 2;
                                break;
                            case 3://素描
                                rlSeekbarSharp.setVisibility(View.INVISIBLE);
                                rlWhiteAndReverse.setVisibility(View.GONE);
                                seekBars = 3;
                                break;
                        }
                        ivMaterial.setImageBitmap(finalBitmap);
                        Log.e(TAG, "finalBitmap getWidth:" + finalBitmap.getWidth() + ",getHeight:" + finalBitmap.getHeight());
                    }
                }));
    }

    /**
     * 生成G代码
     */
    private void generateCode() {
        AlertDialog.Builder tipsAlertDialogBuilder = new AlertDialog.Builder(getActivity());
        tipsAlertDialogBuilder.setTitle("G-Code转换");
        tipsAlertDialogBuilder.setMessage("正在生成G代码，请耐心等待...");
        // 创建弹窗
        AlertDialog dialogTips = tipsAlertDialogBuilder.create();
        dialogTips.setCancelable(false);
        dialogTips.show();

        File finalBitmapFile = ImgUtil.saveBitmap("2_finalBitmap_" + System.currentTimeMillis() + ".png", finalBitmap);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Image2Gcode image2Gcode = new Image2Gcode();
                Bitmap adjustBitmap = ImageProcess.imageResize(finalBitmap, Integer.valueOf(editWidth.getText().toString()), Integer.valueOf(editHeight.getText().toString()), resols);
                strcontent = image2Gcode.image2Gcode(adjustBitmap, resols, Integer.valueOf(editSpeed.getText().toString()) * 10, Integer.valueOf(editDepth.getText().toString()) * 10, 0, 0);
                FileUtils.writeTxtToFile(strcontent, GrblController.getInstance().getExternalFilesDir(null) + "/laser/", System.currentTimeMillis() + ".nc", new GcodeResults() {
                    @Override
                    public void onGcodeResults(String results, File file) {
                        Log.d(TAG, "file:" + file.getPath());
                        dialogTips.dismiss();
                        dialogSecond.dismiss();
                        // 装载文件
                        String connectType = sharedPref.getString(getString(R.string.connect_type), "AP");
                        fileSender.setGcodeFile(new File(file.getPath()));
                        if ("BT".equals(connectType)) {
                            if (fileSender.getGcodeFile().exists()) {
                                fileSender.setElapsedTime("00:00:00");
                                new FileSenderTabFragment.ReadFileAsyncTask().execute(fileSender.getGcodeFile());
                                sharedPref.edit().putString(getString(R.string.most_recent_selected_file), fileSender.getGcodeFile().getAbsolutePath()).apply();
                            } else {
                                EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_file_not_found), true, true));
                            }
                        } else {
                           EventBus.getDefault().post(new APModelUploadEvent(file.getPath().toString()));
                        }

                        // 跳转文件装载页面
                        EventBus.getDefault().post(new ViewPagerItemEvent(1));

                    }

                    @Override
                    public void onGcodeResults(List<String> gcode) {

                    }
                });
            }
        }).start();
    }


    /**
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFilePathEvent(FilePathEvent event){
            switch (event.getFrom()) {
                case ImgUtil.CHOOSE_PHOTO:
                    Log.d(TAG, "inputUri=" + event.getPath());
                    inputUri = Uri.parse(event.getPath());
                    setMaterialSecondDialog();
                    break;
            }

    }

}

