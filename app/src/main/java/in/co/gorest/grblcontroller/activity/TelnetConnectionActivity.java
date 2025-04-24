
package in.co.gorest.grblcontroller.activity;

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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.events.CommonCommandValueMessageEvent;
import in.co.gorest.grblcontroller.events.ConnectStepSetupEvent;
import in.co.gorest.grblcontroller.events.FragmentCommandEvent;
import in.co.gorest.grblcontroller.events.MainShaftLevelVauleUpdateMessageEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.fragment.CommandBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.CommonCommandBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.LaserSetupBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.LaserSetupLineJudgeBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.MainShaftSetupBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.StepSetUpBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.NettyClient;
import me.jessyan.autosize.internal.CustomAdapt;

public class TelnetConnectionActivity extends BaseActivity implements CustomAdapt {
    // 用于日志记录的标签
    private static final String TAG = TelnetConnectionActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 机器名称
    private TextView tvMachineName;
    // 机器状态提示
    private TextView tvMachineStatusTips;
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
    // jog_z_positive
    private ImageView jog_z_positive;
    // jog_z_negative
    private ImageView jog_z_negative;
    // CNC功能模块
    private LinearLayout llCNCFuncation;
    // 启动主轴
    private TextView tvCNCFuncationsMainShaft;
    // 主轴功率
    private TextView tvCNCFuncationsMainShaftLevel;
    // 主轴功率数值
    private int mainShaftLevel;
    //  主轴功率 减
    private TextView tvCNCFuncationsMainShaftLevelSub;
    //  主轴功率 加
    private TextView tvCNCFuncationsMainShaftLevelAdd;
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
    //  激光常用功能
    private LinearLayout llLaserFuncationsCommon;
    // CNC 常用功能
    private LinearLayout llCNCFuncationsCommon;
    // 解除警告(Laser)
    private LinearLayout llCleanAlarm;
    // 解除警告(CNC)
    private LinearLayout llCNCCleanAlarm;
    // 解除暂停(Laser)
    private LinearLayout llCleanHold;
    // 解除暂停(CNC)
    private LinearLayout llCNCCleanHold;
    // X轴清零(Laser)
    private LinearLayout llXYZero;
    // X轴清零(CNC)
    private LinearLayout llCNCXYZero;
    // Z轴清零(Laser)
    private LinearLayout llZZero;
    // Z轴清零(CNC)
    private LinearLayout llCNCZZero;
    // 设置起点
    private LinearLayout llSetOrigin;
    // 回起点(Laser)
    private LinearLayout llGoToOrigin;
    // 激光
    private LinearLayout llLaser;
    // 激光功率
    private int laserLevel;
    // 自动对焦(Laser)
    private LinearLayout llAutoFocus;
    // 回起点(CNC)
    private LinearLayout llCNCGoToOrigin;
    // 自动对刀(CNC)
    private LinearLayout llCNCAutoFocus;
    // 指令1
    private LinearLayout llCNCFuncationsCommonCommandOne;
    // 指令2
    private LinearLayout llCNCFuncationsCommonCommandTwo;
    // 命令
    private LinearLayout llCommand;

    // 队列最大值
    private static final int MAX_HISTORY_SIZE = 5;
    // wposZ值历史记录队列
    private LinkedList<String> wposZHistory = new LinkedList<>();

    // 数据同步弹窗
    private Dialog dialogSycn;

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
        // 机器名称
        tvMachineName = findViewById(R.id.tv_machine_name);
        // 机器状态提示
        tvMachineStatusTips = findViewById(R.id.tv_machine_status_tips);
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
        // jog_z_positive
        jog_z_positive = findViewById(R.id.jog_z_positive);
        // jog_z_negative
        jog_z_negative = findViewById(R.id.jog_z_negative);
        // CNC功能模块
        llCNCFuncation = findViewById(R.id.ll_cnc_funcation);
        // 主轴启动
        tvCNCFuncationsMainShaft = findViewById(R.id.tv_cnc_funcations_main_shaft);
        // 主轴功率
        tvCNCFuncationsMainShaftLevel = findViewById(R.id.tv_cnc_funcations_main_shaft_level);
        // 主轴功率 减
        tvCNCFuncationsMainShaftLevelSub = findViewById(R.id.tv_cnc_funcations_main_shaft_level_sub);
        // 主轴功率 加
        tvCNCFuncationsMainShaftLevelAdd = findViewById(R.id.tv_cnc_funcations_main_shaft_level_add);
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
        // 激光常用功能
        llLaserFuncationsCommon = findViewById(R.id.ll_laser_funcations_common);
        // CNC常用功能
        llCNCFuncationsCommon = findViewById(R.id.ll_cnc_funcations_common);
        // 解除警告(Laser)
        llCleanAlarm = findViewById(R.id.ll_clean_alarm);
        // 解除警告(CNC)
        llCNCCleanAlarm = findViewById(R.id.ll_cnc_clean_alarm);
        // 解除暂停(Laser)
        llCleanHold = findViewById(R.id.ll_clean_hold);
        // 解除暂停(CNC)
        llCNCCleanHold = findViewById(R.id.ll_cnc_clean_hold);
        // X轴清零(Laser)
        llXYZero = findViewById(R.id.ll_xy_zero);
        // X轴清零(CNC)
        llCNCXYZero = findViewById(R.id.ll_cnc_xy_zero);
        // Z轴清零(Laser)
        llZZero = findViewById(R.id.ll_z_zero);
        // Z轴清零(CNC)
        llCNCZZero = findViewById(R.id.ll_cnc_z_zero);
        // 设置起点
        llSetOrigin = findViewById(R.id.ll_set_origin);
        // 回起点(Laser)
        llGoToOrigin = findViewById(R.id.ll_go_to_origin);
        // 激光
        llLaser = findViewById(R.id.ll_laser);
        // 自动对焦(Laser)
        llAutoFocus = findViewById(R.id.ll_auto_focus);
        // 回起点(CNC)
        llCNCGoToOrigin = findViewById(R.id.ll_cnc_go_to_origin);
        // 自动对刀(CNC)
        llCNCAutoFocus = findViewById(R.id.ll_cnc_auto_focus);
        // 指令1
        llCNCFuncationsCommonCommandOne = findViewById(R.id.ll_cnc_funcations_common_command_one);
        // 指令2
        llCNCFuncationsCommonCommandTwo = findViewById(R.id.ll_cnc_funcations_common_command_two);
        // 命令
        llCommand = findViewById(R.id.ll_command);
    }

    /**
     * 初始化数据`
     */
    private void initData() {
        // 同步数据
        syncData();

        // 根据机器设置布局
        String machineName = getIntent().getStringExtra("machineName");
        if (!TextUtils.isEmpty(machineName)) {
            tvMachineName.setText(machineName);
        }

        // 设置CNC功能模块
        if (tvMachineName.getText().toString().contains("CNC")) {
            llCNCFuncation.setVisibility(View.VISIBLE);
            // 隐藏激光常用功能模块
            llLaserFuncationsCommon.setVisibility(View.GONE);
            // 显示CNC常用功能模块
            llCNCFuncationsCommon.setVisibility(View.VISIBLE);
        }
        // 获取共享偏好设置保存的主轴功率实例
        mainShaftLevel = sharedPref.getInt(getString(R.string.preference_main_shaft_level), 50);
        Log.d(TAG, "mainShaftLevel=" + mainShaftLevel);
        tvCNCFuncationsMainShaftLevel.setText(mainShaftLevel + "%");

        // 获取共享偏好设置保存的运动参数实例
        if (machineName.contains("CNC")) {
            int radioButtonStepCNC = sharedPref.getInt(getString(R.string.preference_radio_button_step_cnc), 3);
            int radioButtonSpeedCNC = sharedPref.getInt(getString(R.string.preference_radio_button_speed_cnc), 2);

            // 步长（短）
            Double stepShort = sharedPref.getDouble(getString(R.string.preference_step_short_cnc), 0.1);
            rbStepShort.setText(stepShort + "mm");
            // 步长（常规）
            Double stepGeneral = sharedPref.getDouble(getString(R.string.preference_step_general_cnc), 1.0);
            rbStepGeneral.setText(stepGeneral + "mm");
            // 步长（中）
            Double stepMiddle = sharedPref.getDouble(getString(R.string.preference_step_middle_cnc), 5.0);
            rbStepMiddle.setText(stepMiddle + "mm");
            // 步长（长）
            Double stepLong = sharedPref.getDouble(getString(R.string.preference_step_long_cnc), 10.0);
            rbStepLong.setText(stepLong + "mm");

            // 设置步长选中项
            if (radioButtonStepCNC == 2) {
                rbStepGeneral.setChecked(true);
                Log.d(TAG, "stepGeneral=" + stepGeneral);
                stepValue = stepGeneral;
            } else if (radioButtonStepCNC == 3) {
                rbStepMiddle.setChecked(true);
                Log.d(TAG, "stepMiddle=" + stepMiddle);
                stepValue = stepMiddle;
            } else if (radioButtonStepCNC == 4) {
                rbStepLong.setChecked(true);
                Log.d(TAG, "stepLong=" + stepLong);
                stepValue = stepLong;
            } else {
                rbStepShort.setChecked(true);
                Log.d(TAG, "stepShort=" + stepShort);
                stepValue = stepShort;
            }

            // 设置速度选中项
            if (radioButtonSpeedCNC == 2) {
                rbSpeedMiddle.setChecked(true);
                Log.d(TAG, "speedMiddle=" + sharedPref.getDouble(getString(R.string.preference_speed_middle_cnc), 300.0));
                speedValue = sharedPref.getDouble(getString(R.string.preference_speed_middle_cnc), 300.0);
            } else if (radioButtonSpeedCNC == 3) {
                rbSpeedFast.setChecked(true);
                Log.d(TAG, "speedFast=" + sharedPref.getDouble(getString(R.string.preference_speed_fast_cnc), 800.0));
                speedValue = sharedPref.getDouble(getString(R.string.preference_speed_fast_cnc), 800.0);
            } else if (radioButtonSpeedCNC == 4) {
                rbSpeedPrestissimo.setChecked(true);
                Log.d(TAG, "speedPrestissimo=" + sharedPref.getDouble(getString(R.string.preference_speed_prestissimo_cnc), 1200.0));
                speedValue = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo_cnc), 1200.0);
            } else {
                rbSpeedSlow.setChecked(true);
                Log.d(TAG, "speedSlow=" + sharedPref.getDouble(getString(R.string.preference_speed_slow_cnc), 100.0));
                speedValue = sharedPref.getDouble(getString(R.string.preference_speed_slow_cnc), 100.0);
            }

        } else {
            int radioButtonStep = sharedPref.getInt(getString(R.string.preference_radio_button_step), 3);
            int radioButtonSpeed = sharedPref.getInt(getString(R.string.preference_radio_button_speed), 3);

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
        }

        // 激光功率
        laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level), 10);


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

        // 机器状态
        tvMachineStatusTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvMachineStatusTips.getText().equals("工作中")) {
                    Intent intent = new Intent(TelnetConnectionActivity.this, EngraveActivity.class);
                    String imagePath = sharedPref.getString(getString(R.string.preference_image_path), "");
                    String filePath = sharedPref.getString(getString(R.string.preference_file_path), "");
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } else if (tvMachineStatusTips.getText().equals("暂停")) {
                    // 解除暂停
                    sendJogCommand("\u0018");
                } else if (tvMachineStatusTips.getText().equals("警告")) {
                    // 解除警告
                    sendJogCommand("$X");
                } else {
                    Log.d(TAG, "无效点击");
                }
            }
        });

        // home
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

        // jog_z_positive
        jog_z_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jog = String.format(jog_z_positive.getTag().toString(), "G21", stepValue, speedValue);
                sendJogCommand(jog);
            }
        });

        // jog_z_negative
        jog_z_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jog = String.format(jog_z_negative.getTag().toString(), "G21", stepValue, speedValue);
                sendJogCommand(jog);
            }
        });

        //主轴启动
        tvCNCFuncationsMainShaft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals("openMainShaft")) {
                    sendJogCommand("M3 S" + mainShaftLevel * 10);
                    tvCNCFuncationsMainShaft.setTag("closeMainShaft");

                    // 设置背景为绿色
                    tvCNCFuncationsMainShaft.setBackgroundResource(R.drawable.bg_green_1e853a_r100);
                    // 设置文字为关闭主轴
                    tvCNCFuncationsMainShaft.setText("关闭主轴");
                    // 设置文字颜色为白色
                    tvCNCFuncationsMainShaft.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    sendJogCommand("M5");
                    tvCNCFuncationsMainShaft.setTag("openMainShaft");

                    // 设置背景为绿色
                    tvCNCFuncationsMainShaft.setBackgroundResource(R.drawable.bg_gray_edebee_r100);
                    // 设置文字为关闭主轴
                    tvCNCFuncationsMainShaft.setText("启动主轴");
                    // 设置文字颜色为白色
                    tvCNCFuncationsMainShaft.setTextColor(Color.parseColor("#000000"));
                }
            }
        });

        // 主轴功率
        tvCNCFuncationsMainShaftLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainShaftSetupBottomSheetFragment mainShaftSetupBottomSheetFragment = new MainShaftSetupBottomSheetFragment();
                mainShaftSetupBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 主轴功率  减
        tvCNCFuncationsMainShaftLevelSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainShaftLevel > 10) {
                    mainShaftLevel -= 10;
                    // 设置共享偏好设置保存的主轴功率实例
                    sharedPref.edit().putInt(getString(R.string.preference_main_shaft_level), mainShaftLevel).apply();
                    // 获取共享偏好设置保存的主轴功率实例
                    mainShaftLevel = sharedPref.getInt(getString(R.string.preference_main_shaft_level), 50);
                    // 更新文字
                    tvCNCFuncationsMainShaftLevel.setText(mainShaftLevel + "%");
                    // 更新速度
                    sendJogCommand("S" + mainShaftLevel * 10);
                } else {
                    Toast.makeText(TelnetConnectionActivity.this, "无法再减少", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 主轴功率  加
        tvCNCFuncationsMainShaftLevelAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainShaftLevel < 50) {
                    mainShaftLevel += 10;
                    // 设置共享偏好设置保存的主轴功率实例
                    sharedPref.edit().putInt(getString(R.string.preference_main_shaft_level), mainShaftLevel).apply();
                    // 获取共享偏好设置保存的主轴功率实例
                    mainShaftLevel = sharedPref.getInt(getString(R.string.preference_main_shaft_level), 50);
                    // 更新文字
                    tvCNCFuncationsMainShaftLevel.setText(mainShaftLevel + "%");
                    // 更新速度
                    sendJogCommand("S" + mainShaftLevel * 10);
                } else {
                    Toast.makeText(TelnetConnectionActivity.this, "无法继续增加", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 步长
        rgStep.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String machineName = getIntent().getStringExtra("machineName");
                switch (checkedId) {
                    case R.id.rb_step_short:
                        rbStepShort.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "stepShort=" + sharedPref.getDouble(getString(R.string.preference_step_short_cnc), 0.1));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_short_cnc), 0.1);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step_cnc), 1).apply();
                        } else {
                            Log.d(TAG, "stepShort=" + sharedPref.getDouble(getString(R.string.preference_step_short), 0.1));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_short), 0.1);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 1).apply();
                        }
                        break;
                    case R.id.rb_step_general:
                        rbStepGeneral.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "stepGeneral=" + sharedPref.getDouble(getString(R.string.preference_step_general_cnc), 1.0));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_general_cnc), 1.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step_cnc), 2).apply();
                        } else {
                            Log.d(TAG, "stepGeneral=" + sharedPref.getDouble(getString(R.string.preference_step_general), 1.0));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_general), 1.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 2).apply();
                        }
                        break;
                    case R.id.rb_step_middle:
                        rbStepMiddle.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "stepMiddle=" + sharedPref.getDouble(getString(R.string.preference_step_middle_cnc), 5.0));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_middle_cnc), 5.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step_cnc), 3).apply();
                        } else {
                            Log.d(TAG, "stepMiddle=" + sharedPref.getDouble(getString(R.string.preference_step_middle), 5.0));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_middle), 5.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 3).apply();
                        }
                        break;
                    case R.id.rb_step_long:
                        rbStepLong.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "stepLong=" + sharedPref.getDouble(getString(R.string.preference_step_long_cnc), 10.0));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_long_cnc), 10.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step_cnc), 4).apply();
                        } else {
                            Log.d(TAG, "stepLong=" + sharedPref.getDouble(getString(R.string.preference_step_long), 10.0));
                            stepValue = sharedPref.getDouble(getString(R.string.preference_step_long), 10.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_step), 4).apply();
                        }
                        break;
                }
            }
        });

        // 速度
        rgSpeed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String machineName = getIntent().getStringExtra("machineName");
                switch (checkedId) {
                    case R.id.rb_speed_slow:
                        rbSpeedSlow.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "speedSlow=" + sharedPref.getDouble(getString(R.string.preference_speed_slow_cnc), 100.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_slow_cnc), 100.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed_cnc), 1).apply();
                        } else {
                            Log.d(TAG, "speedSlow=" + sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 1).apply();
                        }
                        break;
                    case R.id.rb_speed_middle:
                        rbSpeedMiddle.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "speedMiddle=" + sharedPref.getDouble(getString(R.string.preference_speed_middle_cnc), 300.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_middle_cnc), 300.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed_cnc), 2).apply();
                        } else {
                            Log.d(TAG, "speedMiddle=" + sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 2).apply();
                        }
                        break;
                    case R.id.rb_speed_fast:
                        rbSpeedFast.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "speedFast=" + sharedPref.getDouble(getString(R.string.preference_speed_fast_cnc), 800.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_fast_cnc), 800.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed_cnc), 3).apply();
                        } else {
                            Log.d(TAG, "speedFast=" + sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 3).apply();
                        }
                        break;
                    case R.id.rb_speed_prestissimo:
                        rbSpeedPrestissimo.setChecked(true);
                        if (machineName.contains("CNC")) {
                            Log.d(TAG, "speedPrestissimo=" + sharedPref.getDouble(getString(R.string.preference_speed_prestissimo_cnc), 1200.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo_cnc), 1200.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed_cnc), 4).apply();
                        } else {
                            Log.d(TAG, "speedPrestissimo=" + sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0));
                            speedValue = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0);
                            sharedPref.edit().putInt(getString(R.string.preference_radio_button_speed), 4).apply();
                        }
                        break;
                }
            }
        });


        // 步长和速度设置
        ivStepSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String machineName = getIntent().getStringExtra("machineName");
                StepSetUpBottomSheetFragment stepSetUpBottomSheetFragment = new StepSetUpBottomSheetFragment();
                if (machineName.contains("CNC")) {
                    stepSetUpBottomSheetFragment.show(getSupportFragmentManager(), "cnc");
                } else {
                    stepSetUpBottomSheetFragment.show(getSupportFragmentManager(), "laser");
                }

            }
        });

        // 解除警告（Laser）
        llCleanAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("$X");
            }
        });
        //  解除警告（CNC）
        llCNCCleanAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("$X");
            }
        });

        // 解除暂停（Laser）
        llCleanHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("\u0018");
            }
        });

        // 解除暂停（CNC）
        llCNCCleanHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("\u0018");
            }
        });


        // XY清零（Laser）
        llXYZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 X 0 Y 0");
            }
        });

        // XY清零（CNC）
        llCNCXYZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 X 0 Y 0");
            }
        });

        // Z轴清零（Laser）
        llZZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G92 Z 0");
            }
        });

        // Z轴清零（CNC）
        llCNCZZero.setOnClickListener(new View.OnClickListener() {
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

        // 自动对焦（Laser）
        llAutoFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示对刀弹窗
                showDialogKinfe();
            }
        });

        // 回起点（CNC）
        llCNCGoToOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJogCommand("G0 X0 Y0 Z0");
            }
        });

        // 自动对刀（CNC）
        llCNCAutoFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 指令1
        llCNCFuncationsCommonCommandOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonCommandBottomSheetFragment commonCommandBottomSheetFragment = new CommonCommandBottomSheetFragment();
                commonCommandBottomSheetFragment.show(getSupportFragmentManager(), "common_command_one");
            }
        });

        // 指令2
        llCNCFuncationsCommonCommandTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonCommandBottomSheetFragment commonCommandBottomSheetFragment = new CommonCommandBottomSheetFragment();
                commonCommandBottomSheetFragment.show(getSupportFragmentManager(), "common_command_two");
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
     * 对刀弹窗
     */
    private void showDialogKinfe() {
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

                // 对焦
                sendJogCommand("[esp212]");

                // 10秒后隐藏弹窗
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 检查wposZ的连续一致性
                        if (checkWposZConsistency(tvWposZ.getText().toString())) {
                            // 隐藏弹窗
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        } else {
                            // 如果5次不一致，则继续等待
                            new Handler().postDelayed(this, 500);  // 每秒检查2次
                        }
                    }
                }, 1000);  // 10秒后执行
            }
        });

        // 显示 Dialog
        dialog.show();
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
     * 同步数据
     */
    private void syncData() {
        dialogSycn = new Dialog(this, R.style.CustomDialog);
        dialogSycn.setContentView(R.layout.dialog_data_sync);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialogSycn.getWindow() != null) {
            dialogSycn.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 设置可取消（点击空白处取消）
        dialogSycn.setCancelable(false);
        dialogSycn.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

        // content
        TextView content = dialogSycn.findViewById(R.id.dialog_content);

        // 定义一个计数器，用来循环显示点数
        final int[] dotCount = {0};  // 用数组包裹，方便在Runnable中修改
        final String baseText = "数据同步中，请稍等";  // 基础文字

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

        // 显示 Dialog
        dialogSycn.show();

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
        if (!event.getMessage().isEmpty()) {
            Activity topActivity = GrblController.getInstance().getTopActivity();
            if (event.getMessage().startsWith("<")) {
                // 隐藏弹窗
                if (dialogSycn != null && dialogSycn.isShowing()) {
                    dialogSycn.dismiss();
                }

                Log.d(TAG, "message=" + event.getMessage().toString());
                String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
                Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);
                tvMachineStatus.setText(parts[0]);

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
            } else {
                if (topActivity != this) {
                    Log.d(TAG, "当前 Activity 不是顶层，不弹窗");
                    return; // 不是当前页面，直接 return
                }

                if (event.getMessage().contains("MSG:Safe door err!")  && tvMachineStatusTips.getText().equals("工作中")) {
                    // TODO 开门弹窗
                    showDialogDoorWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Flame err!")  && tvMachineStatusTips.getText().equals("工作中")) {
                    // TODO 火焰弹窗
                    showDialogFireWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                } else if (event.getMessage().contains("MSG:Probe err!")  && tvMachineStatusTips.getText().equals("工作中")) {
                    // TODO 倾斜弹窗
                    showDialogProbeWarning();
                    if (isOpenVibrateAlert) {
                        vibratePhone(this, vibrateAlertTime * 1000);
                    }
                }
            }
        }
    }

    /**
     * MainShaftLevelVauleUpdateMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainShaftLevelVauleUpdateMessageEvent(MainShaftLevelVauleUpdateMessageEvent event) {
        if (event.getMessage() != null) {
            // 获取共享偏好设置保存的主轴功率实例
            mainShaftLevel = sharedPref.getInt(getString(R.string.preference_main_shaft_level), 50);
            Log.d(TAG, "mainShaftLevel=" + mainShaftLevel);
            tvCNCFuncationsMainShaftLevel.setText(mainShaftLevel + "%");

            sendJogCommand("S" + mainShaftLevel * 10);
        }
    }

    /**
     * MainShaftLevelVauleUpdateMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonCommandValueMessageEvent(CommonCommandValueMessageEvent event) {
        if (event.getMessage() != null) {
            sendJogCommand(event.getMessage().toString());
        }
    }


    @Override
    public boolean isBaseOnWidth() {
        if (isTablet(getApplicationContext())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public float getSizeInDp() {
        if (isTablet(getApplicationContext())) {
            return 580;
        } else {
            String machineName = getIntent().getStringExtra("machineName");
            if (machineName.contains("CNC")) {
                return 1100;
            } else {
                return 950;
            }
        }
    }


    /**
     * 开门风险提示弹窗
     */
    private void showDialogDoorWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_door_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
    }

    /**
     * 火焰风险提示弹窗
     */
    private void showDialogFireWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_fire_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
    }

    /**
     * 倾斜风险提示弹窗
     */
    private void showDialogProbeWarning() {
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_probe_warning);
        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // 确认
        TextView tvDialogRiskWarningConfirm = dialog.findViewById(R.id.tv_dialog_door_warning_confirm);
        // 确定
        tvDialogRiskWarningConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (this.getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 显示 Dialog
        dialog.show();
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
}
