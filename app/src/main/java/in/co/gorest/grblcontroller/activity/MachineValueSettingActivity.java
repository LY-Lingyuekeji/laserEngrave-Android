
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.events.MachineVauleUpdateMessageEvent;
import in.co.gorest.grblcontroller.events.ModelChangeEvent;
import in.co.gorest.grblcontroller.fragment.CommandBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.LaserSetupLineJudgeBottomSheetFragment;

public class MachineValueSettingActivity extends BaseActivity {

    // 用于日志记录的标签
    private static final String TAG = MachineValueSettingActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 激光功率设置
    private RelativeLayout rlLaserLevel;
    // 激光功率
    private TextView tvLaserLevel;


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

    }

    /**
     * 初始化数据
     */
    private void initData() {
        int laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
        tvLaserLevel.setText(String.valueOf(laserLevel));
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMachineVauleUpdateMessageEvent(MachineVauleUpdateMessageEvent event) {
        if (event.getMessage() != null) {
            initData();
        }
    }
}
