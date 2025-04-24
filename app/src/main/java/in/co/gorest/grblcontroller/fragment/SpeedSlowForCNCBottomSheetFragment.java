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

public class SpeedSlowForCNCBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedSlowForCNCBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 速度（慢） Double
    private Double speedSlow;
    // RadioGroup
    private RadioGroup rgSpeedSlow;
    // RadioButton 10mm/min
    private RadioButton rb10;
    // RadioButton 30mm/min
    private RadioButton rb30;
    // RadioButton 50mm/min
    private RadioButton rb50;
    // RadioButton 80mm/min
    private RadioButton rb80;
    // RadioButton 100mm/min
    private RadioButton rb100;

    public SpeedSlowForCNCBottomSheetFragment() {
    }


    public static SpeedSlowForCNCBottomSheetFragment newInstance() {
        return new SpeedSlowForCNCBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_speed_slow_for_cnc_bottom_sheet, container, false);
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
        // RadioButton 10mm/min
        rb10 = view.findViewById(R.id.rb_10);
        // RadioButton 30mm/min
        rb30 = view.findViewById(R.id.rb_30);
        // RadioButton 50mm/min
        rb50 = view.findViewById(R.id.rb_50);
        // RadioButton 80mm/min
        rb80 = view.findViewById(R.id.rb_80);
        // RadioButton 100mm/min
        rb100 = view.findViewById(R.id.rb_100);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        speedSlow = sharedPref.getDouble(getString(R.string.preference_speed_slow_cnc), 100.0);
        // 设置选中项
        if (speedSlow == 10.0) {
            rb10.setChecked(true);
        } else if (speedSlow == 30.0) {
            rb30.setChecked(true);
        } else if (speedSlow == 50.0) {
            rb50.setChecked(true);
        } else if (speedSlow == 80.0) {
            rb80.setChecked(true);
        } else if (speedSlow == 100.0) {
            rb100.setChecked(true);
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
                    case R.id.rb_10:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 10mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow_cnc), 10.0).apply();
                        // 设置选中
                        rb10.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_30:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 30mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow_cnc), 30.0).apply();
                        // 设置选中
                        rb30.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_50:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 50mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow_cnc), 50.0).apply();
                        // 设置选中
                        rb50.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_80:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 1000mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow_cnc), 80.0).apply();
                        // 设置选中
                        rb80.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_100:
                        // 设置共享偏好设置保存的速度（慢）运动参数实例为 100mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_slow_cnc), 100.0).apply();
                        // 设置选中
                        rb100.setChecked(true);
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