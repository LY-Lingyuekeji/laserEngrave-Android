
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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.EventBus;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.LanguageChangeEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.LanguageUtil;

public class LanguageActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = LanguageActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 跟随系统
    private RelativeLayout rlDefault;
    private ImageView ivDefault;
    // 中文
    private RelativeLayout rlZH;
    private ImageView ivZH;
    // 英语
    private RelativeLayout rlEN;
    private ImageView ivEN;
    // 俄语
    private RelativeLayout rlRU;
    private ImageView ivRU;
    // 西班牙语
    private RelativeLayout rlES;
    private ImageView ivES;
    // 德语
    private RelativeLayout rlDE;
    private ImageView ivDE;
    // 日语
    private RelativeLayout rlJA;
    private ImageView ivJA;

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
        DataBindingUtil.setContentView(this, R.layout.activity_language);

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
        // 跟随系统
        rlDefault = findViewById(R.id.rl_default);
        ivDefault = findViewById(R.id.iv_default);
        // 中文
        rlZH = findViewById(R.id.rl_zh);
        ivZH = findViewById(R.id.iv_zh);
        // 英语
        rlEN = findViewById(R.id.rl_en);
        ivEN = findViewById(R.id.iv_en);
        // 俄语
        rlRU = findViewById(R.id.rl_ru);
        ivRU = findViewById(R.id.iv_ru);
        // 西班牙语
        rlES = findViewById(R.id.rl_es);
        ivES = findViewById(R.id.iv_es);
        // 德语
        rlDE = findViewById(R.id.rl_de);
        ivDE = findViewById(R.id.iv_de);
        // 日语
        rlJA = findViewById(R.id.rl_ja);
        ivJA = findViewById(R.id.iv_ja);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置语言选中项
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        switch (sharedPref.getInt(getString(R.string.preference_language), 0)) {
            case 0:
                ivDefault.setSelected(true);
                break;
            case 1:
                ivZH.setSelected(true);
                break;
            case 2:
                ivEN.setSelected(true);
                break;
            case 3:
                ivRU.setSelected(true);
                break;
            case 4:
                ivES.setSelected(true);
                break;
            case 5:
                ivDE.setSelected(true);
                break;
            case 6:
                ivJA.setSelected(true);
                break;
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
                finish();
            }
        });


        // 跟随系统
        ivDefault.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(0);
            }
        });

        // 中文
        ivZH.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(1);
            }
        });

        // 英语
        ivEN.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(2);
            }
        });

        // 俄语
        ivRU.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(3);
            }
        });

        // 西班牙语
        ivES.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(4);
            }
        });

        // 德语
        ivDE.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(5);
            }
        });

        // 日语
        ivJA.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                changeLanguage(6);
            }
        });
    }

    /**
     * 如果是7.0以下，我们需要调用changeAppLanguage方法，
     * 如果是7.0及以上系统，直接把我们想要切换的语言类型保存在SharedPreferences中,然后重新启动MainActivity即可
     *
     * @param language
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeLanguage(int language) {
        Log.d(TAG, "language=" + language);
        if (sharedPref.getInt(getString(R.string.preference_language), 0) == language) {
            return;
        }
        ivDefault.setSelected(false);
        ivZH.setSelected(false);
        ivEN.setSelected(false);
        ivRU.setSelected(false);
        ivES.setSelected(false);
        ivDE.setSelected(false);
        ivJA.setSelected(false);
        switch (language) {
            case 0:
                ivDefault.setSelected(true);
                break;
            case 1:
                ivZH.setSelected(true);
                break;
            case 2:
                ivEN.setSelected(true);
                break;
            case 3:
                ivRU.setSelected(true);
                break;
            case 4:
                ivES.setSelected(true);
                break;
            case 5:
                ivDE.setSelected(true);
                break;
            case 6:
                ivJA.setSelected(true);
                break;
        }
        LanguageUtil.changeAppLanguage(getApplicationContext(), language);
        sharedPref.edit().putInt(getString(R.string.preference_language), language).apply();
        EventBus.getDefault().post(new LanguageChangeEvent(String.valueOf(language)));

        finish();
        // 重启应用
//        final Intent launchIntent = getApplication().getPackageManager().getLaunchIntentForPackage(getPackageName());
//        if (launchIntent != null) {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(launchIntent);
//                    //添加activity切换动画效果
//                    overridePendingTransition(0, 0);
//                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//                    am.killBackgroundProcesses(getPackageName());
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(0);
//                    finish();
//                }
//            }, 400);
//        }
    }
}
