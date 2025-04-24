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

public class SpeedFastForCNCBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedFastForCNCBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 速度（快） Double
    private Double speedFast;
    // RadioGroup
    private RadioGroup rgSpeedFast;
    // RadioButton 700mm/min
    private RadioButton rb700;
    // RadioButton 800mm/min
    private RadioButton rb800;
    // RadioButton 900mm/min
    private RadioButton rb900;
    // RadioButton 1000mm/min
    private RadioButton rb1000;
    // RadioButton 1100mm/min
    private RadioButton rb1100;

    public SpeedFastForCNCBottomSheetFragment() {
    }


    public static SpeedFastForCNCBottomSheetFragment newInstance() {
        return new SpeedFastForCNCBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_speed_fast_for_cnc_bottom_sheet, container, false);
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
        rgSpeedFast = view.findViewById(R.id.rg_speed_fast);
        // RadioButton 700mm/min
        rb700 = view.findViewById(R.id.rb_700);
        // RadioButton 800mm/min
        rb800 = view.findViewById(R.id.rb_800);
        // RadioButton 900mm/min
        rb900 = view.findViewById(R.id.rb_900);
        // RadioButton 1000mm/min
        rb1000 = view.findViewById(R.id.rb_1000);
        // RadioButton 1100mm/min
        rb1100 = view.findViewById(R.id.rb_1100);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        speedFast = sharedPref.getDouble(getString(R.string.preference_speed_fast_cnc), 800.0);
        // 设置选中项
        if (speedFast == 700.0) {
            rb700.setChecked(true);
        } else if (speedFast == 800.0) {
            rb800.setChecked(true);
        } else if (speedFast == 900.0) {
            rb900.setChecked(true);
        } else if (speedFast == 1000.0) {
            rb1000.setChecked(true);
        } else if (speedFast == 1100.0) {
            rb1100.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgSpeedFast.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_700:
                        // 设置共享偏好设置保存的速度（快）运动参数实例为 700mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_fast_cnc), 700.0).apply();
                        // 设置选中
                        rb700.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_800:
                        // 设置共享偏好设置保存的速度（快）运动参数实例为 800mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_fast_cnc), 800.0).apply();
                        // 设置选中
                        rb800.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_900:
                        // 设置共享偏好设置保存的速度（快）运动参数实例为 900mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_fast_cnc), 900.0).apply();
                        // 设置选中
                        rb900.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1000:
                        // 设置共享偏好设置保存的速度（快）运动参数实例为 1000mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_fast_cnc), 1000.0).apply();
                        // 设置选中
                        rb1000.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1100:
                        // 设置共享偏好设置保存的速度（快）运动参数实例为 1100mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_fast_cnc), 1100.0).apply();
                        // 设置选中
                        rb1100.setChecked(true);
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