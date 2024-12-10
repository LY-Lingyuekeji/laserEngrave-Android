package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.util.Log;
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

public class StepGeneralBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = StepGeneralBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 步长（常规） Double
    private Double stepGeneral;
    // RadioGroup
    private RadioGroup rgStepGeneral;
    // RadioButton 0.5mm
    private RadioButton rb05;
    // RadioButton 1mm
    private RadioButton rb1;
    // RadioButton 2mm
    private RadioButton rb2;

    public StepGeneralBottomSheetFragment() {
    }


    public static StepGeneralBottomSheetFragment newInstance() {
        return new StepGeneralBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_step_general_bottom_sheet, container, false);
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
        rgStepGeneral = view.findViewById(R.id.rg_step_general);
        // RadioButton 0.5mm
        rb05 = view.findViewById(R.id.rb_0_5);
        // RadioButton 1mm
        rb1 = view.findViewById(R.id.rb_1);
        // RadioButton 2mm
        rb2 = view.findViewById(R.id.rb_2);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        stepGeneral = sharedPref.getDouble(getString(R.string.preference_step_general), 1.0);
        // 设置选中项
        if (stepGeneral == 0.5) {
            rb05.setChecked(true);
        } else if (stepGeneral == 1.0) {
            rb1.setChecked(true);
        } else if (stepGeneral == 2.0) {
            rb2.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgStepGeneral.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_0_5:
                        // 设置共享偏好设置保存的步长（常规）运动参数实例为 0.5mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_general), 0.5).apply();
                        // 设置选中
                        rb05.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1:
                        // 设置共享偏好设置保存的步长（常规）运动参数实例为 1mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_general), 1.0).apply();
                        // 设置选中
                        rb1.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_2:
                        // 设置共享偏好设置保存的步长（常规）运动参数实例为 2mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_general), 2.0).apply();
                        // 设置选中
                        rb2.setChecked(true);
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