package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.StepSetupEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class SpeedSlowBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedSlowBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 速度（慢） Double
    private Double speedSlow;
    // RadioGroup
    private RadioGroup rgSpeedSlow;
    // RadioButton 250mm/min
    private RadioButton rb250;
    // RadioButton 500mm/min
    private RadioButton rb500;
    // RadioButton 750mm/min
    private RadioButton rb750;
    // RadioButton 1000mm/min
    private RadioButton rb1000;
    // RadioButton 1500mm/min
    private RadioButton rb1500;
    // RadioButton 2500mm
    private RadioButton rb2500;

    public SpeedSlowBottomSheetFragment() {
    }


    public static SpeedSlowBottomSheetFragment newInstance() {
        return new SpeedSlowBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed_slow_bottom_sheet, container, false);
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
        // RadioGroup
        rgSpeedSlow = view.findViewById(R.id.rg_speed_slow);
        // RadioButton 250mm/min
        rb250 = view.findViewById(R.id.rb_250);
        // RadioButton 500mm/min
        rb500 = view.findViewById(R.id.rb_500);
        // RadioButton 750mm/min
        rb750 = view.findViewById(R.id.rb_750);
        // RadioButton 1000mm/min
        rb1000 = view.findViewById(R.id.rb_1000);
        // RadioButton 1500mm/min
        rb1500 = view.findViewById(R.id.rb_1500);
        // RadioButton 2500mm/min
        rb2500 = view.findViewById(R.id.rb_2500);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        speedSlow = sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0);
        // 设置选中项
        if (speedSlow == 250.0) {
            rb250.setChecked(true);
        } else if (speedSlow == 500.0) {
            rb500.setChecked(true);
        } else if (speedSlow == 750.0) {
            rb750.setChecked(true);
        } else if (speedSlow == 1000.0) {
            rb1000.setChecked(true);
        } else if (speedSlow == 1500.0) {
            rb1500.setChecked(true);
        } else if (speedSlow == 2500.0) {
            rb2500.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgSpeedSlow.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_250:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 250mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 250.0).apply();
                        // 设置选中
                        rb250.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_500:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 500.0).apply();
                        // 设置选中
                        rb500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_750:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 750mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 750.0).apply();
                        // 设置选中
                        rb750.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1000:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 1000mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 1000.0).apply();
                        // 设置选中
                        rb1000.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1500:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 1500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 1500.0).apply();
                        // 设置选中
                        rb1500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_2500:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 2500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 2500.0).apply();
                        // 设置选中
                        rb2500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                }
            }
        });
    }

}