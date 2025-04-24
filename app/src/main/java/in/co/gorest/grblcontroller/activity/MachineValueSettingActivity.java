
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.MachineVauleUpdateMessageEvent;
import in.co.gorest.grblcontroller.events.VibrateAlertTimeUpdateMessageEvent;
import in.co.gorest.grblcontroller.fragment.LaserSetupLineJudgeBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.VibrateAlertBottomSheetFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class MachineValueSettingActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private static final String TAG = MachineValueSettingActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 激光功率设置
    private RelativeLayout rlLaserLevel;
    // 激光功率
    private TextView tvLaserLevel;
    // 震动提醒
    private Switch switchVibrateAlert;
    // 是否开启危险警报震动提醒
    private boolean isOpenVibrateAlert;
    // 震动持续时长 RelativeLayout
    private RelativeLayout rlVibrateAlertTime;
    // 震动持续时长 TextView
    private TextView tvVibrateAlertTime;


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
        DataBindingUtil.setContentView(this, R.layout.activity_machine_value_setting);

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
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 激光功率设置
        rlLaserLevel = findViewById(R.id.rl_laser_level);
        // 激光功率
        tvLaserLevel = findViewById(R.id.tv_laser_level);
        // 危险警报震动提醒
        switchVibrateAlert = findViewById(R.id.switch_vibrate_alert);
        // 震动持续时长 RelativeLayout
        rlVibrateAlertTime = findViewById(R.id.rl_vibrate_alert_time);
        // 震动持续时长 TextView
        tvVibrateAlertTime = findViewById(R.id.tv_vibrate_alert_time);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取保存的激光功率实例值
        int laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 2);
        tvLaserLevel.setText(String.valueOf(laserLevel));

        // 获取保存的危险警报震动提醒实例值
        isOpenVibrateAlert = sharedPref.getBoolean(getString(R.string.preference_vibrate_alert), true);
        switchVibrateAlert.setChecked(isOpenVibrateAlert);
        // 设置震动持续时长显示
        rlVibrateAlertTime.setVisibility(isOpenVibrateAlert ? View.VISIBLE : View.GONE);

        // 获取保存的激光功率实例值
        int vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);
        tvVibrateAlertTime.setText(String.valueOf(vibrateAlertTime));
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

        // 设置激光功率
        rlLaserLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaserSetupLineJudgeBottomSheetFragment laserSetupLineJudgeBottomSheetFragment = new LaserSetupLineJudgeBottomSheetFragment();
                laserSetupLineJudgeBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

        // 危险警报震动提醒
        switchVibrateAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "isOpenVibrateAlert=" + isChecked);
                if (isChecked) {
                    sharedPref.edit().putBoolean(getString(R.string.preference_vibrate_alert), true).apply();
                    // 设置震动持续时长显示
                    rlVibrateAlertTime.setVisibility(View.VISIBLE);
                } else {
                    sharedPref.edit().putBoolean(getString(R.string.preference_vibrate_alert), false).apply();
                    // 设置震动持续时长隐藏
                    rlVibrateAlertTime.setVisibility(View.GONE);
                }
            }
        });

        // 设置震动持续时长
        rlVibrateAlertTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VibrateAlertBottomSheetFragment vibrateAlertBottomSheetFragment = new VibrateAlertBottomSheetFragment();
                vibrateAlertBottomSheetFragment.show(getSupportFragmentManager(), "");
            }
        });

    }

    /**
     * 激光功率
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMachineVauleUpdateMessageEvent(MachineVauleUpdateMessageEvent event) {
        if (event.getMessage() != null) {
            initData();
        }
    }


    /**
     * 震动持续时长
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVibrateAlertTimeUpdateMessageEvent(VibrateAlertTimeUpdateMessageEvent event) {
        if (event.getMessage() != null) {
            initData();
        }
    }
}
