
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.widget.IconButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.databinding.ActivityEngraveBinding;
import in.co.gorest.grblcontroller.events.APModelUploadEvent;
import in.co.gorest.grblcontroller.events.FragmentCommandEvent;
import in.co.gorest.grblcontroller.events.GrblRealTimeCommandEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.listeners.FileSenderListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.CommandHistory;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.GcodeCommand;
import in.co.gorest.grblcontroller.service.FileStreamerIntentService;
import in.co.gorest.grblcontroller.service.GrblTelnetSerialService;
import in.co.gorest.grblcontroller.util.FileUploader;
import in.co.gorest.grblcontroller.util.GrblUtils;
import in.co.gorest.grblcontroller.util.NettyClient;

public class EngraveActivity extends BaseActivity {

    // 用于日志记录的标签
    private static final String TAG = EngraveActivity.class.getSimpleName();
    // 用于监听和管理机器状态的监听器

    // 用于监听和管理文件进程的监听器
    private FileSenderListener fileSender;
    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 预览图
    private ImageView ivPreview;
    // 文件名
    private TextView tvFilename;
    // 雕刻速度
    private TextView tvSpeed;
    // 激光功率
    private TextView tvLaserlevel;
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

    // 机器状态
    private String machineStatus;
    // 耗时线程
    private Handler elapsedTimeHandler = new Handler();
    // 开始时间
    private long startTime;
    // 文件总行号
    private int totalLines;
    // 耗时
    private long elapsedTime;


    // 进度更新线程
    private Handler progressHandler = new Handler();
    // 是否更新标识
    private boolean isStreaming = false;


    // 数据同步弹窗
    private AlertDialog dialogSycn;


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
        binding.setFileSender(fileSender);

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
        // 预览图
        ivPreview = findViewById(R.id.iv_preview);
        // 文件名
        tvFilename = findViewById(R.id.tv_filename);
        // 雕刻速度
        tvSpeed = findViewById(R.id.tv_speed);
        // 激光功率
        tvLaserlevel = findViewById(R.id.tv_laserlevel);
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
        fileSender = FileSenderListener.getInstance();
        // 图像预览地址
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath.isEmpty() || imagePath.equals("")) {
            Glide.with(getApplicationContext()).load(R.mipmap.ic_empty).into(ivPreview);
        } else {
            Log.d(TAG, "imagePath=" + imagePath);
            Glide.with(getApplicationContext()).load(imagePath).into(ivPreview);
        }
        // 文件地址
        String filePath = getIntent().getStringExtra("filePath");
        Log.d(TAG, "filePath=" + filePath);
        fileSender.setGcodeFile(new File(filePath));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                String connectType = sharedPref.getString(getString(R.string.preference_connect_type), "Telnet");
                if (machineStatus.equals("") || machineStatus.isEmpty()) {
                    Toast.makeText(EngraveActivity.this, "未能获取机器状态，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    // Wi-Fi
                    if (connectType.equals("Telnet")) {
                        // 机器状态为IDLE 认为机器是空闲状态
                        if (machineStatus.equals(Constants.MACHINE_STATUS_IDLE)) {
                            // 发送离线雕刻命令
                            sendJogCommand("$SD/Run=/" + tvFilename.getText());
                            // 开始时间
                            startTime = SystemClock.elapsedRealtime();
                            // 设置定时器，每1000毫秒（1秒）更新一次
                            elapsedTimeHandler.postDelayed(runnableElapsedTime, 1000);
                        } else if (machineStatus.equals(Constants.MACHINE_STATUS_HOLD)) {  // 机器状态为HOLD 认为机器是暂停中
                            sendJogCommand("~");
                        } else if (machineStatus.equals(Constants.MACHINE_STATUS_RUN)) { // 机器状态为RUN 认为机器是雕刻中
                            // 暂停雕刻
                            sendJogCommand("!");
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
                String connectType = sharedPref.getString(getString(R.string.preference_connect_type), "Telnet");
                if (machineStatus.equals("") || machineStatus.isEmpty()) {
                    Toast.makeText(EngraveActivity.this, "未能获取机器状态，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    // Wi-Fi
                    if (connectType.equals("Telnet")) {
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

            }
        });
    }

    /**
     * 读取文件
     */
    static class ReadFileAsyncTask extends AsyncTask<File, Integer, Integer> {

        protected void onPreExecute() {
            FileSenderListener.getInstance().setStatus(FileSenderListener.STATUS_READING);
            this.initFileSenderListener();
        }

        protected Integer doInBackground(File... file) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);

            Integer lines = 0;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file[0]));
                String sCurrentLine;
                GcodeCommand gcodeCommand = new GcodeCommand();
                while ((sCurrentLine = reader.readLine()) != null) {
                    gcodeCommand.setCommand(sCurrentLine);
                    if (gcodeCommand.getCommandString().length() > 0) {
                        lines++;
                        if (gcodeCommand.getCommandString().length() >= 79) {
                            EventBus.getDefault().post(new UiToastEvent(GrblController.getInstance().getString(R.string.text_gcode_length_warning) + sCurrentLine, true, true));
                            initFileSenderListener();
                            FileSenderListener.getInstance().setStatus(FileSenderListener.STATUS_IDLE);
                            cancel(true);
                        }
                    }
                    if (lines % 2500 == 0) publishProgress(lines);
                }
                reader.close();
            } catch (IOException e) {
                this.initFileSenderListener();
                FileSenderListener.getInstance().setStatus(FileSenderListener.STATUS_IDLE);
                Log.e(TAG, e.getMessage(), e);
            }

            return lines;
        }

        public void onProgressUpdate(Integer... progress) {
            FileSenderListener.getInstance().setRowsInFile(progress[0]);
        }

        public void onPostExecute(Integer lines) {
            FileSenderListener.getInstance().setRowsInFile(lines);
            FileSenderListener.getInstance().setStatus(FileSenderListener.STATUS_IDLE);
        }

        private void initFileSenderListener() {
            FileSenderListener.getInstance().setRowsInFile(0);
            FileSenderListener.getInstance().setRowsSent(0);
        }
    }

    /**
     * 进度更新线程
     */
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStreaming) {
                int currentLine = fileSender.getRowsSent();
                int totalLines = fileSender.getRowsInFile();
                int progress = (int) (((float) currentLine / totalLines) * 100);
                updateProgressBar(progress);

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
        alertDialogBuilder.setTitle("提示");
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
            // 计算耗时
            elapsedTime = SystemClock.elapsedRealtime() - startTime;

            // 格式化耗时
            String formattedElapsedTime = formatElapsedTime(elapsedTime);

            // 更新UI（确保在主线程中更新UI）
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,  "耗时：" + elapsedTime + "毫秒");
                    tvExpenditureTime.setText(formattedElapsedTime);
                }
            });

            // 再次设置定时器
            elapsedTimeHandler.postDelayed(this, 1000);
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
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty() && event.getMessage().startsWith("<")) {

            if (dialogSycn.isShowing()) {
                // 隐藏弹窗
                dialogSycn.dismiss();
            }

            Log.d(TAG, "message=" + event.getMessage().toString());
            String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
            Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);
            machineStatus = parts[0];
            for (String part : parts) {
                if (part.startsWith("FS")) {
                    String[] speedAndLaserLevel = part.substring(3, part.length() - 1).split(",");
                    tvSpeed.setText(speedAndLaserLevel[0]);
                    tvLaserlevel.setText(String.valueOf(Integer.valueOf(speedAndLaserLevel[1]) / 10));
                }

                if (part.startsWith("SD")) {
                    String[] progressStrings = part.substring(3, part.length() - 1).split(",");
                    Float progress = Float.valueOf(progressStrings[0]);
                    updateProgressBar(Integer.valueOf(Math.round(progress)));

                    if (Integer.valueOf(Math.round(progress)) == 100) {
                        Toast.makeText(EngraveActivity.this, "雕刻完成", Toast.LENGTH_SHORT).show();
                        // 销毁时移除所有回调和消息
                        elapsedTimeHandler.removeCallbacks(runnableElapsedTime);
                    }
                }

            }
        }
    }

}
