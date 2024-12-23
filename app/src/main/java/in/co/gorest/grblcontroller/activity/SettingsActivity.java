
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.QuestionAdapter;
import in.co.gorest.grblcontroller.base.BaseActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.model.FAQ;
import in.co.gorest.grblcontroller.ui.FileSenderTabFragment;
import in.co.gorest.grblcontroller.util.NettyClient;

public class SettingsActivity extends BaseActivity {

    // 用于日志记录的标签
    private static final String TAG = SettingsActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 自动连接
    private Switch switchAutoConnect;
    // AP模式
    private LinearLayout llApModel;
    // STA模式配置
    private LinearLayout llStaModel;
    // 机器参数设置
    private LinearLayout llMachineValueSetting;
    // 电池优化设置
    private LinearLayout llBattery;


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
        DataBindingUtil.setContentView(this, R.layout.activity_settings);

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
        // 自动连接
        switchAutoConnect = findViewById(R.id.switch_auto_connect);
        // AP模式
        llApModel = findViewById(R.id.ll_ap_model);
        // STA模式配置
        llStaModel = findViewById(R.id.ll_sta_model);
        // 机器参数设置
        llMachineValueSetting = findViewById(R.id.ll_machine_value_setting);
        // 电池优化设置
        llBattery = findViewById(R.id.ll_battery);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取自动连接
        boolean isAutoConnect = sharedPref.getBoolean(getString(R.string.preference_auto_connect), false);
        switchAutoConnect.setChecked(isAutoConnect);
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

        // 自动连接
        switchAutoConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "autoConnect=" + isChecked);
                if (isChecked) {
                    sharedPref.edit().putBoolean(getString(R.string.preference_auto_connect), true).apply();
                } else {
                    sharedPref.edit().putBoolean(getString(R.string.preference_auto_connect), false).apply();
                    sharedPref.edit().putString(getString(R.string.preference_wifi_ssid), "preference_wifi_ssid").apply();
                    sharedPref.edit().putString(getString(R.string.preference_wifi_password), "12345678").apply();
                }
            }
        });

        // AP模式
        llApModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 切换模式
                boolean isConnected = NettyClient.getInstance(null).getConnectStatus();
                if (isConnected) {
                    NettyClient.getInstance(null).sendMsgToServer("$50=1\r\n".getBytes(StandardCharsets.UTF_8), null);
                    // 取消保存
                    sharedPref.edit().putString(getString(R.string.preference_sta_host), "").apply();
                    sharedPref.edit().putString(getString(R.string.preference_sta_ssid), "").apply();
                    // Toast
                    Toast.makeText(SettingsActivity.this, "模式切换成功，请返回重新连接设备", Toast.LENGTH_SHORT).show();

                    //重启应用
                    Intent launchIntent = getApplication().getPackageManager().getLaunchIntentForPackage(getPackageName());
                    if (launchIntent != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(launchIntent);
                                //添加activity切换动画效果
                                overridePendingTransition(0, 0);
                                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                                am.killBackgroundProcesses(getPackageName());
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(0);
                                finish();
                            }
                        }, 400);
                    }
                } else {
                    Toast.makeText(SettingsActivity.this, "请先连接设备后设置", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // STA模式
        llStaModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, STAModelActivity.class));
            }
        });

        // 机器参数设置
        llMachineValueSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MachineValueSettingActivity.class));
            }
        });

        // 电池优化
        llBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent();
                    myIntent.setAction("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
                    SettingsActivity.this.startActivity(myIntent);
                } catch (RuntimeException e2) {
                }

            }
        });
    }
}
