
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.nio.charset.StandardCharsets;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.events.ConnectStepSetupEvent;
import in.co.gorest.grblcontroller.events.FragmentCommandEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.fragment.CommandBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.LaserSetupBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.LaserSetupLineJudgeBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.StepSetUpBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.NettyClient;

public class TelnetConnectionActivity extends BaseActivity {

    // 用于日志记录的标签
    private static final String TAG = TelnetConnectionActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 机器状态
    private TextView tvMachineStatus;
    // X轴机械坐标
    private TextView tvMposX;
    // Y轴机械坐标
    private TextView tvMposY;
    // Z轴机械坐标
    private TextView tvMposZ;
    // X轴工件坐标
    private TextView tvWposX;
    // Y轴工件坐标
    private TextView tvWposY;
    // Z轴工件坐标
    private TextView tvWposZ;
    // home
    private LinearLayout llHome;
    // jog_x_positive
    private ImageView jog_x_positive;
    // jog_x_negative
    private ImageView jog_x_negative;
    // jog_y_positive
    private ImageView jog_y_positive;
    // jog_y_negative
    private ImageView jog_y_negative;
    // 步长
    private RadioGroup rgStep;
    // 步长 Double
    private Double stepValue;
    // 步长（短）
    private RadioButton rbStepShort;
    // 步长（常规）
    private RadioButton rbStepGeneral;
    // 步长（中）
    private RadioButton rbStepMiddle;
    // 步长（长）
    private RadioButton rbStepLong;
    // 速度
    private RadioGroup rgSpeed;
    // 速度 Double
    private Double speedValue;
    // 速度（慢）
    private RadioButton rbSpeedSlow;
    // 速度（中等）
    private RadioButton rbSpeedMiddle;
    // 速度（快）
    private RadioButton rbSpeedFast;
    // 速度（超快）
    private RadioButton rbSpeedPrestissimo;
    // 步长和速度设置
    private ImageView ivStepSetting;
    // 解除警告
    private LinearLayout llCleanAlarm;
    // X轴清零
    private LinearLayout llXZero;
    // Y轴清零
    private LinearLayout llYZero;
    // Z轴清零
    private LinearLayout llZZero;
    // 设置起点
    private LinearLayout llSetOrigin;
    // 回起点
    private LinearLayout llGoToOrigin;
    // 激光
    private LinearLayout llLaser;
    // 激光功率
    private int laserLevel;
    // 巡边
    private LinearLayout llLineJudge;
    // 巡边激光功率
    private int lineJudgeLaserLevel;
    // 是否巡边标志类
    private boolean isLineJudge = false;
    // 命令
    private LinearLayout llCommand;

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
        DataBindingUtil.setContentView(this, R.layout.activity_telnet_connection);
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
        // 机器状态
        tvMachineStatus = findViewById(R.id.tv_machine_status);
        // X轴机械坐标
        tvMposX = findViewById(R.id.tv_mpos_x);
        // Y轴机械坐标
        tvMposY = findViewById(R.id.tv_mpos_y);
        // Z轴机械坐标
        tvMposZ = findViewById(R.id.tv_mpos_z);
        // X轴工件坐标
        tvWposX = findViewById(R.id.tv_wpos_x);
        // Y轴工件坐标
        tvWposY = findViewById(R.id.tv_wpos_y);
        // Z轴工件坐标
        tvWposZ = findViewById(R.id.tv_wpos_z);
        // home
        llHome = findViewById(R.id.ll_home);
        // jog_x_positive
        jog_x_positive = findViewById(R.id.jog_x_positive);
        // jog_x_negative
        jog_x_negative = findViewById(R.id.jog_x_negative);
        // jog_y_positive
        jog_y_positive = findViewById(R.id.jog_y_positive);
        // jog_y_negative
        jog_y_negative = findViewById(R.id.jog_y_negative);
        // 步长
        rgStep = findViewById(R.id.rg_step);
        // 步长（短）
        rbStepShort = findViewById(R.id.rb_step_short);
        // 步长（常规）
        rbStepGeneral = findViewById(R.id.rb_step_general);
        // 步长（中）
        rbStepMiddle = findViewById(R.id.rb_step_middle);
        // 步长（长）
        rbStepLong = findViewById(R.id.rb_step_long);
        // 速度
        rgSpeed = findViewById(R.id.rg_speed);
        // 速度（慢）
        rbSpeedSlow = findViewById(R.id.rb_speed_slow);
        // 速度（中等）
        rbSpeedMiddle = findViewById(R.id.rb_speed_middle);
        // 速度（快）
        rbSpeedFast = findViewById(R.id.rb_speed_fast);
        // 速度（超快）
        rbSpeedPrestissimo = findViewById(R.id.rb_speed_prestissimo);
        // 步长和速度设置
        ivStepSetting = findViewById(R.id.iv_step_setting);
        // 解除警告
        llCleanAlarm = findViewById(R.id.ll_clean_alarm);
        // X轴清零
        llXZero = findViewById(R.id.ll_x_zero);
        // Y轴清零
        llYZero = findViewById(R.id.ll_y_zero);
        // Z轴清零
        llZZero = findViewById(R.id.ll_z_zero);
        // 设置起点
        llSetOrigin = findViewById(R.id.ll_set_origin);
        // 回起点
        llGoToOrigin = findViewById(R.id.ll_go_to_origin);
        // 激光
        llLaser = findViewById(R.id.ll_laser);
        // 巡边
        llLineJudge = findViewById(R.id.ll_line_judge);
        // 命令
        llCommand = findViewById(R.id.ll_command);
    }

    /**
     * 初始化数据`
     */
    private void initData() {
        // 同步数据
        syncData();

        // 获取共享偏好设置保存的运动参数实例
        int radioButtonStep = sharedPref.getInt(getString(R.string.preference_radio_button_step), 1);
        int radioButtonSpeed = sharedPref.getInt(getString(R.string.preference_radio_button_speed), 1);
        // 步长（短）
        Double stepShort = sharedPref.getDouble(getString(R.string.preference_step_short), 0.1);
        rbStepShort.setText(stepShort + "mm");
        // 步长（常规）
        Double stepGeneral = sharedPref.getDouble(getString(R.string.preference_step_general), 1.0);
        rbStepGeneral.setText(stepGeneral + "mm");
        // 步长（中）
        Double stepMiddle = sharedPref.getDouble(getString(R.string.preference_step_middle), 5.0);
        rbStepMiddle.setText(stepMiddle + "mm");
        // 步长（长）
        Double stepLong = sharedPref.getDouble(getString(R.string.preference_step_long), 10.0);
        rbStepLong.setText(stepLong + "mm");


        // 设置步长选中项
        if (radioButtonStep == 2) {
            rbStepGeneral.setChecked(true);
            Log.d(TAG, "stepGeneral=" + stepGeneral);
            stepValue = stepGeneral;
        } else if (radioButtonStep == 3) {
            rbStepMiddle.setChecked(true);
            Log.d(TAG, "stepMiddle=" + stepMiddle);
            stepValue = stepMiddle;
        } else if (radioButtonStep == 4) {
            rbStepLong.setChecked(true);
            Log.d(TAG, "stepLong=" + stepLong);
            stepValue = stepLong;
        } else {
            rbStepShort.setChecked(true);
            Log.d(TAG, "stepShort=" + stepShort);
            stepValue = stepShort;
        }

        // 设置速度选中项
        if (radioButtonSpeed == 2) {
            rbSpeedMiddle.setChecked(true);
            Log.d(TAG, "speedMiddle=" + sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0));
            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0);
        } else if (radioButtonSpeed == 3) {
            rbSpeedFast.setChecked(true);
            Log.d(TAG, "speedFast=" + sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0));
            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0);
        } else if (radioButtonSpeed == 4) {
            rbSpeedPrestissimo.setChecked(true);
            Log.d(TAG, "speedPrestissimo=" + sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0));
            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0);
        } else {
            rbSpeedSlow.setChecked(true);
            Log.d(TAG, "speedSlow=" + sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0));
            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0);
        }

        // 激光功率
        laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level), 10);
        lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
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

        llHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("$H");
            }
        });

        // jog_x_positive
        jog_x_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jog = String.format(jog_x_positive.getTag().toString(), "G21", stepValue, speedValue);
                sendJogCommand(jog);
            }
        });

        // jog_x_negative
        jog_x_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jog = String.format(jog_x_negative.getTag().toString(), "G21", stepValue, speedValue);
                sendJogCommand(jog);
            }
        });

        // jog_y_positive
        jog_y_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jog = String.format(jog_y_positive.getTag().toString(), "G21", stepValue, speedValue);
                sendJogCommand(jog);
            }
        });

        // jog_y_negative
        jog_y_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jog = String.format(jog_y_negative.getTag().toString(), "G21", stepValue, speedValue);
                sendJogCommand(jog);
            }
        });

        // 步长
        rgStep.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_step_short:
                        rbStepShort.setChecked(true);
                        Log.d(TAG, "stepShort=" + sharedPref.getDouble(getString(R.string.preference_step_short), 0.1));
                        stepValue = sharedPref.getDouble(getString(R.string.preference_step_short), 0.1);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 1).apply();
                        break;
                    case R.id.rb_step_general:
                        rbStepGeneral.setChecked(true);
                        Log.d(TAG, "stepGeneral=" + sharedPref.getDouble(getString(R.string.preference_step_general), 1.0));
                        stepValue = sharedPref.getDouble(getString(R.string.preference_step_general), 1.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 2).apply();
                        break;
                    case R.id.rb_step_middle:
                        rbStepMiddle.setChecked(true);
                        Log.d(TAG, "stepMiddle=" + sharedPref.getDouble(getString(R.string.preference_step_middle), 5.0));
                        stepValue = sharedPref.getDouble(getString(R.string.preference_step_middle), 5.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 3).apply();
                        break;
                    case R.id.rb_step_long:
                        rbStepLong.setChecked(true);
                        Log.d(TAG, "stepLong=" + sharedPref.getDouble(getString(R.string.preference_step_long), 10.0));
                        stepValue = sharedPref.getDouble(getString(R.string.preference_step_long), 10.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 4).apply();
                        break;
                }
            }
        });

        // 速度
        rgSpeed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_speed_slow:
                        rbSpeedSlow.setChecked(true);
                        Log.d(TAG, "speedSlow=" + sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0));
                        speedValue = sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 1).apply();
                        break;
                    case R.id.rb_speed_middle:
                        rbSpeedMiddle.setChecked(true);
                        Log.d(TAG, "speedMiddle=" + sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0));
                        speedValue = sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 2).apply();
                        break;
                    case R.id.rb_speed_fast:
                        rbSpeedFast.setChecked(true);
                        Log.d(TAG, "speedFast=" + sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0));
                        speedValue = sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 3).apply();
                        break;
                    case R.id.rb_speed_prestissimo:
                        rbSpeedPrestissimo.setChecked(true);
                        Log.d(TAG, "speedPrestissimo=" + sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0));
                        speedValue = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0);
                        sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 4).apply();
                        break;
                }
            }
        });


        // 步长和速度设置
        ivStepSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepSetUpBottomSheetFragment stepSetUpBottomSheetFragment = new StepSetUpBottomSheetFragment();
                stepSetUpBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 解除警告
        llCleanAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("$X");
            }
        });


        // X轴清零
        llXZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 X 0");
            }
        });

        // Y轴清零
        llYZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 Y 0");
            }
        });

        // Z轴清零
        llZZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 Z 0");
            }
        });

        // 设置起点
        llSetOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 X0 Y0 Z0");
            }
        });

        // 回起点
        llGoToOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G0 X0 Y0 Z0");
            }
        });

        // 激光
        llLaser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals("openLaser")) {
                    laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level), 10);
                    Log.d(TAG, "laserLevel=" + laserLevel);
                    sendJogCommand("M3 S" + laserLevel);
                    sendJogCommand("G1 F1000");
                    llLaser.setTag("closeLaser");
                } else {
                    sendJogCommand("M5");
                    llLaser.setTag("openLaser");
                }
            }
        });

        // 激光
        llLaser.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LaserSetupBottomSheetFragment laserSetupBottomSheetFragment = new LaserSetupBottomSheetFragment();
                laserSetupBottomSheetFragment.show(getSupportFragmentManager(), "");
                return true;
            }
        });

        // 巡边
        llLineJudge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLineJudge == false) {
                    lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
                    Log.d(TAG, "lineJudgeLaserLevel=" + lineJudgeLaserLevel);
                    sendJogCommand("G0 X0 Y0");
                    sendJogCommand("M3 S" + lineJudgeLaserLevel);
                    sendJogCommand("F1000");
                    sendJogCommand("G1 Y20");
                    sendJogCommand("G1 X20");
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

        // 激光
        llLineJudge.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LaserSetupLineJudgeBottomSheetFragment laserSetupLineJudgeBottomSheetFragment = new LaserSetupLineJudgeBottomSheetFragment();
                laserSetupLineJudgeBottomSheetFragment.show(getSupportFragmentManager(), "");
                return true;
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
    }

    /**
     * 发送命令
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


    /**
     * 步长数据更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStepSetupEvent(ConnectStepSetupEvent event) {
        if (!event.getMessage().isEmpty()) {
            initData();
        }
    }

    /**
     * FragmentCommandEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFragmentCommandEvent(FragmentCommandEvent event) {
        if (!event.getMessage().isEmpty()) {
            onGcodeCommandReceived(event.getMessage());
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
            // 隐藏弹窗
            dialogSycn.dismiss();

            Log.d(TAG, "message=" + event.getMessage().toString());
            String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
            Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);
            tvMachineStatus.setText(parts[0]);
            String[] MposParts = parts[1].substring(5, parts[1].length()).split(",");
            Log.d(TAG, "Mpos X=" + MposParts[0] + " Y=" + MposParts[1] + " Z=" + MposParts[2]);
            tvMposX.setText(MposParts[0]);
            tvMposY.setText(MposParts[1]);
            tvMposZ.setText(MposParts[2]);
            String[] WposParts = parts[2].substring(5, parts[2].length()).split(",");
            Log.d(TAG, "Wpos X=" + WposParts[0] + " Y=" + WposParts[1] + " Z=" + WposParts[2]);
            tvWposX.setText(WposParts[0]);
            tvWposY.setText(WposParts[1]);
            tvWposZ.setText(WposParts[2]);
        }
    }
}
