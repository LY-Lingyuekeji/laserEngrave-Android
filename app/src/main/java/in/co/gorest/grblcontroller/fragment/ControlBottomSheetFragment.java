package in.co.gorest.grblcontroller.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.PreViewActivity;
import in.co.gorest.grblcontroller.events.ControltoPreViewMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.NettyClient;

public class ControlBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private final static String TAG = ControlBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
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


    public ControlBottomSheetFragment() {
    }

    public static ControlBottomSheetFragment newInstance() {
        return new ControlBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化界面
        initView(view);
        // 初始化数据
        initData();
        // 初始化事件监听
        setupListeners();
    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // home
        llHome = view.findViewById(R.id.ll_home);
        // jog_x_positive
        jog_x_positive = view.findViewById(R.id.jog_x_positive);
        // jog_x_negative
        jog_x_negative = view.findViewById(R.id.jog_x_negative);
        // jog_y_positive
        jog_y_positive = view.findViewById(R.id.jog_y_positive);
        // jog_y_negative
        jog_y_negative = view.findViewById(R.id.jog_y_negative);
        // 步长
        rgStep = view.findViewById(R.id.rg_step);
        // 步长（短）
        rbStepShort = view.findViewById(R.id.rb_step_short);
        // 步长（常规）
        rbStepGeneral = view.findViewById(R.id.rb_step_general);
        // 步长（中）
        rbStepMiddle = view.findViewById(R.id.rb_step_middle);
        // 步长（长）
        rbStepLong = view.findViewById(R.id.rb_step_long);
        // 速度
        rgSpeed = view.findViewById(R.id.rg_speed);
        // 速度（慢）
        rbSpeedSlow = view.findViewById(R.id.rb_speed_slow);
        // 速度（中等）
        rbSpeedMiddle = view.findViewById(R.id.rb_speed_middle);
        // 速度（快）
        rbSpeedFast = view.findViewById(R.id.rb_speed_fast);
        // 速度（超快）
        rbSpeedPrestissimo = view.findViewById(R.id.rb_speed_prestissimo);
        // 步长和速度设置
        ivStepSetting = view.findViewById(R.id.iv_step_setting);
        // 解除警告
        llCleanAlarm = view.findViewById(R.id.ll_clean_alarm);
        // X轴清零
        llXZero = view.findViewById(R.id.ll_x_zero);
        // Y轴清零
        llYZero = view.findViewById(R.id.ll_y_zero);
        // Z轴清零
        llZZero = view.findViewById(R.id.ll_z_zero);
        // 设置起点
        llSetOrigin = view.findViewById(R.id.ll_set_origin);
        // 回起点
        llGoToOrigin = view.findViewById(R.id.ll_go_to_origin);
        // 激光
        llLaser = view.findViewById(R.id.ll_laser);
        // 巡边
        llLineJudge = view.findViewById(R.id.ll_line_judge);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));

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
     * 初始化事件监听
     */
    private void setupListeners() {
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
                stepSetUpBottomSheetFragment.show(getParentFragmentManager(), "");
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
                laserSetupBottomSheetFragment.show(getParentFragmentManager(), "");
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
                laserSetupLineJudgeBottomSheetFragment.show(getParentFragmentManager(), "");
                return true;
            }
        });
    }



    /**
     * 发送命令
     * @param command
     */
    private void sendJogCommand(String command) {
        Log.d(TAG, "command=" + command);
        EventBus.getDefault().post(new ControltoPreViewMessageEvent(command));
    }
}