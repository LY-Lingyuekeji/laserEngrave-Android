
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import org.greenrobot.eventbus.EventBus;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.ModelChangeEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class ModelActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = ModelActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 模式
    private RadioGroup radioGroupModel;
    // 提示
    private TextView tvModelTips;
    // 操作模式偏好值
    private String operationMode;
    private String operationModeOld;
    // 简易模式
    private RadioButton radioButtonSimple;
    // 专业模式
    private RadioButton radioButtonPro;
    // 确认切换
    private TextView tvConfirmChange;

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
        DataBindingUtil.setContentView(this, R.layout.activity_model);

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
        // 模式
        radioGroupModel = findViewById(R.id.radio_group_model);
        // 提示
        tvModelTips = findViewById(R.id.tv_model_tips);
        // 简易模式
        radioButtonSimple = findViewById(R.id.radio_button_simple);
        // 专业模式
        radioButtonPro = findViewById(R.id.radio_button_pro);
        // 确认切换
        tvConfirmChange = findViewById(R.id.tv_confirm_change);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取模式偏好设置
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        operationMode = sharedPref.getString(getString(R.string.preference_operation_mode), "simple");
        // 赋值新操作模式偏好值
        operationModeOld = operationMode;
        Log.d(TAG, "operationModeOld=" + operationModeOld + "-----operationMode=" + operationMode);
        // 设置选中项及提示文字
        if ("simple".equals(operationMode)) {
            tvModelTips.setText("简易模式适合初次接触雕刻的入门玩家，我们精简了大部分操作，以便您能够更快的上手，体验雕刻带来的无穷可取");
            radioButtonSimple.setChecked(true);
        } else if ("pro".equals(operationMode)){
            tvModelTips.setText("专业模式适合经常接触雕刻的资深玩家，我们提供全方位的设置，高自由的自定义等功能，让您可以尽情发挥您的想像力和创造力");
            radioButtonPro.setChecked(true);
        }
        // 设置 确认切换 按钮可点性及可用性
        if (operationMode.equals(operationModeOld)) {
            tvConfirmChange.setClickable(false);
            tvConfirmChange.setEnabled(false);
        } else {

            tvConfirmChange.setClickable(true);
            tvConfirmChange.setEnabled(true);
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

        // 模式
        radioGroupModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.radio_button_simple:
                        // 设置为简易模式
                        operationMode = "simple";
                        // 设置提示文字
                        tvModelTips.setText("简易模式适合初次接触雕刻的入门玩家，我们精简了大部分操作，以便您能够更快的上手，体验雕刻带来的无穷可取");
                        // 设置 确认切换 按钮可点性及可用性
                        if (operationMode.equals(operationModeOld)) {
                            tvConfirmChange.setClickable(false);
                            tvConfirmChange.setEnabled(false);
                        } else {

                            tvConfirmChange.setClickable(true);
                            tvConfirmChange.setEnabled(true);
                        }
                        break;
                    case R.id.radio_button_pro:
                        // 设置为简易模式
                        operationMode = "pro";
                        // 设置提示文字
                        tvModelTips.setText("专业模式适合经常接触雕刻的资深玩家，我们提供全方位的设置，高自由的自定义等功能，让您可以尽情发挥您的想像力和创造力");
                        // 设置 确认切换 按钮可点性及可用性
                        if (operationMode.equals(operationModeOld)) {
                            tvConfirmChange.setClickable(false);
                            tvConfirmChange.setEnabled(false);
                        } else {

                            tvConfirmChange.setClickable(true);
                            tvConfirmChange.setEnabled(true);
                        }
                        break;
                }
            }
        });

        // 确认切换
        tvConfirmChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "operationMode=" + operationMode);
                sharedPref.edit().putString(getString(R.string.preference_operation_mode), operationMode).apply();
                // 分发事件订阅
                EventBus.getDefault().post(new ModelChangeEvent(operationMode));

                finish();
            }
        });
    }
}
