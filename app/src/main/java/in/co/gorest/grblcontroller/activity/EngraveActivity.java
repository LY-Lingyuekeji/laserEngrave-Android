
package in.co.gorest.grblcontroller.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.databinding.ActivityEngraveBinding;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class EngraveActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private static final String TAG = EngraveActivity.class.getSimpleName();
    // 用于监听和管理机器状态的监听器

    // 用于监听和管理文件进程的监听器
//    private FileSenderListener fileSender;
    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 机器状态
    private TextView tvMachineStatus;
    // 机器状态
    private String machineStatus;
    // 雕刻速度
    private TextView tvSpeed;
    // 激光功率
    private TextView tvLaserlevel;
    // X轴坐标
    private TextView tvWposX;
    // Y轴坐标
    private TextView tvWposY;
    // Z轴坐标
    private TextView tvWposZ;

    // 预览图
    private ImageView ivPreview;
    // 预览图遮罩层
    private View maskView;
    // 雕刻次数 TextView
    private TextView tvEngraveCount;
    // 雕刻次数
    private int totalEngraveCount;

    // 文件名
    private TextView tvFilename;
    // 进度条
    private ProgressBar progressBar;
    // 百分比
    private TextView tvProgress;
    // 雕刻时间
    private TextView tvExpenditureTime;
    // 预计时间
    private TextView tvEstimatedTime;
    // 开始/暂停
    private TextView tvStartOrPause;
    // 终止
    private TextView tvStop;


    // 耗时线程
    private Handler elapsedTimeHandler = new Handler();
    // 开始时间
    private long startTime = 0;
    // 文件总行号
    private int totalLines;
    // 耗时
    private long elapsedTime = 0;
    //  当前是否是暂停状态
    private boolean isPaused = false;
    private long pausedElapsedTime = 0;   // 暂停时累计的耗时
    // 进度更新线程
    private Handler progressHandler = new Handler();
    // 是否更新标识
    private boolean isStreaming = false;

    // 数据同步弹窗
    private AlertDialog dialogSycn;

    // 是否震动提醒
    private boolean isOpenVibrateAlert;
    // 震动提醒持续时长
    private int vibrateAlertTime;

    // 添加这个变量用于记录上一次状态
    private String lastMachineStatus = "";
    // 当前进度记录
    private int currentProgress = 0;
    // 机器名称
    private String machineName;
    // 图片路径
    private String imagePath;
    // 文件路径
    private String filePath;

    // 门警告弹窗
    private Dialog dialogDoorWarning;
    // 火焰警告弹窗
    private Dialog dialogFireWarning;
    // 倾斜警告弹窗
    private Dialog dialogProbeWarning;


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
        ActivityEngraveBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_engrave);
//        binding.setFileSender(fileSender);

        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));

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

        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
        // 销毁时移除所有回调和消息
        elapsedTimeHandler.removeCallbacks(runnableElapsedTime);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 机器状态
        tvMachineStatus = findViewById(R.id.tv_machine_status);
        // 雕刻速度
        tvSpeed = findViewById(R.id.tv_speed);
        // 激光功率
        tvLaserlevel = findViewById(R.id.tv_laserlevel);
        // X轴坐标（工件）
        tvWposX = findViewById(R.id.tv_wpos_x);
        // Y轴坐标（工件）
        tvWposY = findViewById(R.id.tv_wpos_y);
        // Z轴坐标（工件）
        tvWposZ = findViewById(R.id.tv_wpos_z);
        // 预览图
        ivPreview = findViewById(R.id.iv_preview);
        // 预览图遮罩层
        maskView = findViewById(R.id.maskView);
        // 雕刻次数
        tvEngraveCount = findViewById(R.id.tv_engrave_count);
        // 文件名
        tvFilename = findViewById(R.id.tv_filename);
        // 进度条
        progressBar = findViewById(R.id.progressBar);
        // 百分比
        tvProgress = findViewById(R.id.tv_progress);
        // 雕刻时间
        tvExpenditureTime = findViewById(R.id.tv_expenditure_time);
        // 预计时间
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        // 开始雕刻
        tvStartOrPause = findViewById(R.id.tv_start_or_pause);
        // 终止雕刻
        tvStop = findViewById(R.id.tv_stop);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 同步数据
        syncData();
        // 初始化文件进程
//        fileSender = FileSenderListener.getInstance();
        // 根据机器设置布局
        machineName = getIntent().getStringExtra("machineName");
        // 接收图像预览地址
        imagePath = getIntent().getStringExtra("imagePath");
        // 设置预览图像
        if (imagePath.isEmpty() || imagePath.equals("")) {
            Glide.with(getApplicationContext()).load(R.mipmap.ic_unknow_404).into(ivPreview);
        } else {
            Log.d(TAG, "imagePath=" + imagePath);
            Glide.with(getApplicationContext()).load(imagePath).into(ivPreview);
        }
        // 雕刻次数
        totalEngraveCount = getIntent().getIntExtra("totalEngraveCount", 1);
        Log.d(TAG, "totalEngraveCount=" + totalEngraveCount);
        // 接收文件地址
        filePath = getIntent().getStringExtra("filePath");
        Log.d(TAG, "filePath=" + filePath);
//        fileSender.setGcodeFile(new File(filePath));
        File file = new File(filePath);
        // 设置文件名
        tvFilename.setText(file.getName());
        // 保存图片地址和文件地址
        sharedPref.edit().putString(getString(R.string.preference_image_path), imagePath).apply();
        sharedPref.edit().putString(getString(R.string.preference_file_path), filePath).apply();

//        if (fileSender.getGcodeFile().exists()) {
//            fileSender.setElapsedTime("00:00:00");
//            new ReadFileAsyncTask().execute(fileSender.getGcodeFile());
//            sharedPref.edit().putString(getString(R.string.most_recent_selected_file), fileSender.getGcodeFile().getPath()).apply();
//        } else {
//            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_file_not_found), true, true));
//        }

        // 文件总行号
        try {
            totalLines = countTotalLines(filePath);
            Log.d(TAG, "文件总行号=" + totalLines);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "文件不存在: " + filePath);
            totalLines = -1; // -1 表示无法获取
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 确保遮罩层完全覆盖图片
        maskView.post(new Runnable() {
            @Override
            public void run() {
                // 获取 ivPreview 的高度
                int ivHeight = ivPreview.getHeight();
                // 设置 maskView 高度
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) maskView.getLayoutParams();
                params.height = ivHeight;
                maskView.setLayoutParams(params);
            }
        });

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
                if (machineStatus.equals(Constants.MACHINE_STATUS_RUN) || machineStatus.equals(Constants.MACHINE_STATUS_HOLD)) {
                    BaseDialog.showCustomDialog(EngraveActivity.this,
                            "温馨提示",
                            "设备正在雕刻中\r\n\r\n是否返回并终止雕刻？",
                            "确定", "取消",
                            v -> {
                                // 终止
                                sendJogCommand("\u0018");
                                // 销毁时移除所有回调和消息
                                elapsedTimeHandler.removeCallbacks(runnableElapsedTime);
                            },
                            v -> {
                                Log.d(TAG, "用户点击取消");
                            });
                }

                finish();
            }
        });

        // 开始雕刻/暂停雕刻
        tvStartOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String connectType = sharedPref.getString(getString(R.string.preference_connect_type), "AP");
                Log.d(TAG, "connectType=" + connectType);
                if (machineStatus.equals("") || machineStatus.isEmpty()) {
                    Toast.makeText(EngraveActivity.this, "未能获取机器状态，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    // Wi-Fi
                    if (connectType.equals("AP") || connectType.equals("STA")) {
                        // 机器状态为IDLE 认为机器是空闲状态
                        if (machineStatus.equals(Constants.MACHINE_STATUS_IDLE)) {
                            BaseDialog.showCustomDialog(EngraveActivity.this,
                                    "温馨提示", "您确定要开始运行机器吗？",
                                    "确定", "取消",
                                    v1 -> {
                                        // 发送离线雕刻命令
                                        sendJogCommand("$SD/Run=/" + tvFilename.getText());
                                        // 开始时间
                                        startTime = SystemClock.elapsedRealtime();

                                        pausedElapsedTime = 0;
                                        isPaused = false;

                                        // 设置定时器，每1000毫秒（1秒）更新一次
                                        elapsedTimeHandler.postDelayed(runnableElapsedTime, 1000);

                                    },
                                    v1 -> {
                                        Log.d(TAG, "用户点击取消");
                                    });
                        } else if (machineStatus.contains(Constants.MACHINE_STATUS_HOLD)) {  // 机器状态为HOLD 认为机器是暂停中
                            BaseDialog.showCustomDialog(EngraveActivity.this,
                                    "温馨提示", "您确定要开始运行机器吗？",
                                    "确定", "取消",
                                    v1 -> {
                                        sendJogCommand("~");

                                        // 重新设置 startTime
                                        startTime = SystemClock.elapsedRealtime() - pausedElapsedTime;
                                        isPaused = false;
                                        elapsedTimeHandler.postDelayed(runnableElapsedTime, 1000);
                                    },
                                    v1 -> {
                                        Log.d(TAG, "用户点击取消");
                                    });
                        } else if (machineStatus.equals(Constants.MACHINE_STATUS_RUN)) { // 机器状态为RUN 认为机器是雕刻中
                            // 暂停雕刻
                            sendJogCommand("!");

                            // 停止计时器
                            isPaused = true;
                            elapsedTimeHandler.removeCallbacks(runnableElapsedTime);
                        }
                    } else {
                        // BT
                    }
                }

            }
        });

        // 终止雕刻
        tvStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseDialog.showCustomDialog(EngraveActivity.this,
                        "温馨提示", "您确定要停止机器吗？您的雕刻作品将不会被保存",
                        "确定", "取消",
                        v1 -> {
                            String connectType = sharedPref.getString(getString(R.string.preference_connect_type), "AP");
                            if (machineStatus.equals("") || machineStatus.isEmpty()) {
                                Toast.makeText(EngraveActivity.this, "未能获取机器状态，请重试", Toast.LENGTH_SHORT).show();
                            } else {
                                // Wi-Fi
                                if (connectType.equals("AP") || connectType.equals("STA")) {
                                    // 终止雕刻
                                    sendJogCommand("\u0018");
                                    // 销毁时移除所有回调和消息
                                    elapsedTimeHandler.removeCallbacks(runnableElapsedTime);
                                } else {
                                    // BT
                                }
                                // 停止雕刻的逻辑
                                isStreaming = false;
                                progressHandler.removeCallbacks(progressRunnable); // 停止进度更新线程
                                updateProgressBar(100); // 设置进度条为100%
                            }

                        },
                        v1 -> {
                            Log.d(TAG, "用户点击取消");
                        });
            }
        });
    }

    /**
     * 进度更新线程
     */
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStreaming) {
                updateProgressBar(currentProgress);

                // 如果还在雕刻，继续更新
                progressHandler.postDelayed(this, 500); // 每0.5秒更新一次
            }
        }
    };

    /**
     * 更新进度
     *
     * @param progress 进度
     */
    private void updateProgressBar(int progress) {
        // 调用方法更新遮罩层
        updateMaskViewHeightWithAnimation(progress / 100f);

        // 更新UI上的进度条
        progressBar.setProgress(progress);
        tvProgress.setText(progress + "%");
        long time = estimateRemainingTime(totalLines, progress, elapsedTime);
        String estimatedTimeStr = formatElapsedTime(time);
        tvEstimatedTime.setText(estimatedTimeStr);
    }


    /**
     * 同步数据
     */
    private void syncData() {
        // 使用自定义布局创建 AlertDialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_transform, null);
        // content
        TextView content = dialogView.findViewById(R.id.dialog_content);
        // 创建弹窗
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("温馨提示");
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);
        dialogSycn = alertDialogBuilder.create();
        // 设置内容
        content.setText("数据同步中，请稍等~");
        // 显示弹窗
        runOnUiThread(() -> {
            dialogSycn.show();
        });
    }

    private Runnable runnableElapsedTime = new Runnable() {
        @Override
        public void run() {
            if (!isPaused) {
                // 计算耗时
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                pausedElapsedTime = elapsedTime; // 更新为最新的暂停时刻

                // 格式化耗时
                String formattedElapsedTime = formatElapsedTime(elapsedTime);

                // 更新UI（确保在主线程中更新UI）
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "耗时：" + elapsedTime + "毫秒");
                        tvExpenditureTime.setText(formattedElapsedTime);
                    }
                });

                // 再次设置定时器
                elapsedTimeHandler.postDelayed(this, 1000);
            }
        }
    };

    /**
     * 计算文件的总行数。
     *
     * @param filePath 文件的路径。
     * @return 文件的总行数。
     * @throws IOException 如果读取文件时发生错误。
     */
    public static int countTotalLines(String filePath) throws IOException {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) {
                lines++;
            }
        }
        return lines;
    }

    /**
     * 估算处理文件剩余时间的方法。
     *
     * @param totalLines      文件的总行数。
     * @param currentProgress 当前处理的进度百分比（0到100之间）。
     * @return 估算的剩余时间（单位：毫秒）。
     */
    public static long estimateRemainingTime(int totalLines, int currentProgress, long timeElapsed) {
        // 计算当前已处理的行数
        int linesProcessed = (int) Math.ceil(totalLines * (double) currentProgress / 100);
        // 计算剩余行数
        int remainingLines = totalLines - linesProcessed;
        // 每行平均处理时间
        double timePerLine = timeElapsed / linesProcessed;
        // 估算剩余时间
        long estimatedRemainingTime = remainingLines * (long) timePerLine;

        return estimatedRemainingTime;
    }

    /**
     * 格式化时间
     *
     * @param elapsedTime 时间
     * @return 格式化的时间 string
     */
    private String formatElapsedTime(long elapsedTime) {
        // 将毫秒转换为秒
        long totalSeconds = elapsedTime / 1000;

        // 计算小时、分钟和秒
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // 使用String.format()格式化字符串
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Activity topActivity = GrblController.getInstance().getTopActivity();
            if (event.getMessage().startsWith("<")) {
                if (dialogSycn.isShowing()) {
                    // 隐藏弹窗
                    dialogSycn.dismiss();
                }

                Log.d(TAG, "message=" + event.getMessage().toString());
                String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
                Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);

                // 设置机器状态
                lastMachineStatus = machineStatus; // 在更新前记录旧状态
                machineStatus = parts[0];          // 更新当前状态
                tvMachineStatus.setText(machineStatus);
                // 设置状态颜色
                if (machineStatus.equals(Constants.MACHINE_STATUS_IDLE)) {
                    tvMachineStatus.setTextColor(Color.parseColor("#000000"));
                    tvStartOrPause.setText("开始雕刻");
                } else if (machineStatus.contains(Constants.MACHINE_STATUS_HOLD)) {
                    tvMachineStatus.setTextColor(Color.parseColor("#c42b1c"));
                    tvStartOrPause.setText("开始雕刻");
                } else if (machineStatus.equals(Constants.MACHINE_STATUS_ALARM)) {
                    tvMachineStatus.setTextColor(Color.parseColor("#fd8400"));
                } else if (machineStatus.equals(Constants.MACHINE_STATUS_RUN)) {
                    tvMachineStatus.setTextColor(Color.parseColor("#1e853a"));
                    tvStartOrPause.setText("暂停雕刻");
                }

                // 设置工件坐标
                String[] WposParts = parts[2].substring(5, parts[2].length()).split(",");
                Log.d(TAG, "Wpos X=" + WposParts[0] + " Y=" + WposParts[1] + " Z=" + WposParts[2]);
                tvWposX.setText("X：" + WposParts[0]);
                tvWposY.setText("Y：" + WposParts[1]);
                tvWposZ.setText("Z：" + WposParts[2]);


                // 检查是否从 Run -> Idle，并且进度没到100
                if (lastMachineStatus.equals(Constants.MACHINE_STATUS_RUN) && machineStatus.equals(Constants.MACHINE_STATUS_IDLE) && currentProgress <= 100) {
                    Log.d(TAG, "检测到状态由Run变为Idle，即从工作状态到空闲状态，但进度未满，强制设为100");
                    updateProgressBar(100);
                    // 销毁时移除所有回调和消息
                    elapsedTimeHandler.removeCallbacks(runnableElapsedTime);
                    // 更新当前进度
                    currentProgress = 100;

                    // 雕刻完成
                    Toast.makeText(EngraveActivity.this, "雕刻完成，共计 " + totalEngraveCount + " 次", Toast.LENGTH_LONG).show();
                    // 跳转雕刻完成页面
                    Intent intent = new Intent(EngraveActivity.this, FinishEngraveActivity.class);
                    intent.putExtra("machineName", machineName);
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    intent.putExtra("totalEngraveCount", totalEngraveCount);
                    // 启动跳转
                    startActivity(intent);
                    // 关闭当前页面
                    finish();
                }

                for (String part : parts) {
                    if (part.startsWith("FS")) {
                        String[] speedAndLaserLevel = part.substring(3, part.length()).split(",");
                        tvSpeed.setText("F：" + speedAndLaserLevel[0]);
                        tvLaserlevel.setText("S：" + Integer.valueOf(speedAndLaserLevel[1]) / 10);
                    }

                    if (part.startsWith("SD")) {
                        String[] progressStrings = part.substring(3, part.length() - 1).split(",");
                        Float progress = Float.valueOf(progressStrings[0]);
                        Log.d(TAG, "progress=" + progress);
                        int roundedProgress = Math.round(progress);
                        currentProgress = roundedProgress; // 更新当前进度记录
                        updateProgressBar(roundedProgress);

                        // 计算当前是第几次雕刻
                        int currentEngraveCount = Math.min(
                                (int) Math.floor(progress / (100f / totalEngraveCount)) + 1,
                                totalEngraveCount
                        );

                        // 更新UI显示
                        tvEngraveCount.setText("第" + currentEngraveCount + "次/" + totalEngraveCount + "次");
                    }

                }
            } else {
                if (topActivity != this) {
                    Log.d(TAG, "当前 Activity 不是顶层，不弹窗");
                    return; // 不是当前页面，直接 return
                }

                if (event.getMessage().contains("MSG:Safe door err") && machineStatus.equals(Constants.MACHINE_STATUS_RUN)) { // 开门警告弹窗打开
                    // TODO 开门警告弹窗
                    showDialogDoorWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe door reset") && machineStatus.contains(Constants.MACHINE_STATUS_HOLD)) { // 开门警告弹窗关闭
                    // 隐藏开门警告弹窗
                    dialogDoorWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Flame err") && machineStatus.equals(Constants.MACHINE_STATUS_RUN)) { // 火焰警告弹窗打开
                    // TODO 火焰警告弹窗
                    showDialogFireWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Flame reset") && machineStatus.contains(Constants.MACHINE_STATUS_HOLD)) { // 火焰警告弹窗关闭
                    // 隐藏火焰警告弹窗
                    dialogFireWarning.dismiss();
                    // TODO 记录日志

                } else if (event.getMessage().contains("MSG:Tilt sensor") && machineStatus.equals(Constants.MACHINE_STATUS_RUN)) { // 倾斜警告弹窗打开
                    // TODO 倾斜警告弹窗
                    showDialogProbeWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Safe Probe reset") && machineStatus.contains(Constants.MACHINE_STATUS_HOLD)) { // 倾斜警告弹窗关闭
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
     * 根据雕刻进度消除遮罩层
     *
     * @param progress 雕刻进度
     */
    private void updateMaskViewHeightWithAnimation(float progress) {
        // 获取 ImageView 的高度
        int screenHeight = ivPreview.getHeight();

        // 计算目标高度：根据进度从底部消除遮罩
        int targetHeight = (int) (screenHeight * (1 - progress));

        // 如果遮罩层已经在目标高度附近，避免重复执行动画
        if (maskView.getLayoutParams().height == targetHeight) {
            return;
        }

        // 动画过渡遮罩层高度
        ValueAnimator animator = ValueAnimator.ofInt(maskView.getLayoutParams().height, targetHeight);
        animator.setDuration(500); // 设置动画持续时间
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 计算动画中的当前高度
                int animatedHeight = (int) valueAnimator.getAnimatedValue();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) maskView.getLayoutParams();
                params.height = animatedHeight;
                maskView.setLayoutParams(params);
            }
        });
        animator.start();
    }
}
