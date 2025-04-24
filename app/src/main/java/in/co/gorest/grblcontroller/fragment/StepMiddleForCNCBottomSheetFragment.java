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

public class StepMiddleForCNCBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = StepMiddleForCNCBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 步长（中） Double
    private Double stepMiddle;
    // RadioGroup
    private RadioGroup rgStepMiddle;
    // RadioButton 5mm
    private RadioButton rb4;
    // RadioButton 5mm
    private RadioButton rb5;
    // RadioButton 6mm
    private RadioButton rb6;
    // RadioButton 8mm
    private RadioButton rb8;

    public StepMiddleForCNCBottomSheetFragment() {
    }


    public static StepMiddleForCNCBottomSheetFragment newInstance() {
        return new StepMiddleForCNCBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_step_middle_for_cnc_bottom_sheet, container, false);
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
        rgStepMiddle = view.findViewById(R.id.rg_step_middle);
        // RadioButton 4mm
        rb4 = view.findViewById(R.id.rb_4);
        // RadioButton 5mm
        rb5 = view.findViewById(R.id.rb_5);
        // RadioButton 6mm
        rb6 = view.findViewById(R.id.rb_6);
        // RadioButton 8mm
        rb8 = view.findViewById(R.id.rb_8);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        stepMiddle = sharedPref.getDouble(getString(R.string.preference_step_middle_cnc), 5.0);
        // 设置选中项
        if (stepMiddle == 4.0) {
            rb4.setChecked(true);
        } else if (stepMiddle == 5.0) {
            rb5.setChecked(true);
        } else if (stepMiddle == 6.0) {
            rb6.setChecked(true);
        } else if (stepMiddle == 8.0) {
            rb8.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgStepMiddle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_4:
                        // 设置共享偏好设置保存的步长（中）运动参数实例为 4mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_middle_cnc), 4.0).apply();
                        // 设置选中
                        rb4.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_5:
                        // 设置共享偏好设置保存的步长（中）运动参数实例为 5mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_middle_cnc), 5.0).apply();
                        // 设置选中
                        rb5.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_6:
                        // 设置共享偏好设置保存的步长（中）运动参数实例为 6mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_middle_cnc), 6.0).apply();
                        // 设置选中
                        rb6.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_8:
                        // 设置共享偏好设置保存的步长（中）运动参数实例为 8mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_middle_cnc), 8.0).apply();
                        // 设置选中
                        rb8.setChecked(true);
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