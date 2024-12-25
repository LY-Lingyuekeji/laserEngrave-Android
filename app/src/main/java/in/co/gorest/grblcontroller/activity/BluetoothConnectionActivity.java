
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.fragment.CommandBottomSheetFragment;
import in.co.gorest.grblcontroller.fragment.WifiChooseBottomSheetFragment;

public class BluetoothConnectionActivity extends AppCompatActivity {

    // 返回
    private ImageView ivBack;
    // 步长和速度设置
    private ImageView ivStepSetting;
    // 命令
    private LinearLayout llCommand;
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
        DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_connection);

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
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 步长和速度设置
        ivStepSetting = findViewById(R.id.iv_step_setting);
        // 命令
        llCommand = findViewById(R.id.ll_command);
    }

    /**
     * 初始化数据
     */
    private void initData() {

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

        // 步长和速度设置
        ivStepSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiChooseBottomSheetFragment wifiChooseBottomSheetFragment = new WifiChooseBottomSheetFragment();
                wifiChooseBottomSheetFragment.show(getSupportFragmentManager(),"");
            }
        });


        // 命令
        llCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandBottomSheetFragment commandBottomSheetFragment = new CommandBottomSheetFragment();
                commandBottomSheetFragment.show(getSupportFragmentManager(),"");
            }
        });
    }
}
