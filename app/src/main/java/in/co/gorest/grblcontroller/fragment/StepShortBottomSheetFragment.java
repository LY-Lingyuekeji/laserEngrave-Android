package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.WifiChooseBottomSheetAdapter;
import in.co.gorest.grblcontroller.events.StepSetupEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class StepShortBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = StepShortBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 步长（短） Double
    private Double stepShort;
    // RadioGroup
    private RadioGroup rgStepShort;
    // RadioButton 0.1mm
    private RadioButton rb01;
    // RadioButton 0.2mm
    private RadioButton rb02;
    // RadioButton 0.5mm
    private RadioButton rb05;

    public StepShortBottomSheetFragment() {
    }


    public static StepShortBottomSheetFragment newInstance() {
        return new StepShortBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_step_short_bottom_sheet, container, false);
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
        rgStepShort = view.findViewById(R.id.rg_step_short);
        // RadioButton 0.1mm
        rb01 = view.findViewById(R.id.rb_0_1);
        // RadioButton 0.2mm
        rb02 = view.findViewById(R.id.rb_0_2);
        // RadioButton 0.5mm
        rb05 = view.findViewById(R.id.rb_0_5);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        stepShort = sharedPref.getDouble(getString(R.string.preference_step_short), 0.1);
        Log.d(TAG, "s=" + stepShort.toString());
        // 设置选中项
        if (stepShort == 0.1) {
            rb01.setChecked(true);
        } else if (stepShort == 0.2) {
            rb02.setChecked(true);
        } else if (stepShort == 0.5) {
            rb05.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgStepShort.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_0_1:
                        // 设置共享偏好设置保存的步长（短）运动参数实例为 0.1mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_short), 0.1).apply();
                        // 设置选中
                        rb01.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_0_2:
                        // 设置共享偏好设置保存的步长（短）运动参数实例为 0.2mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_short), 0.2).apply();
                        // 设置选中
                        rb02.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_0_5:
                        // 设置共享偏好设置保存的步长（短）运动参数实例为 0.5mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_short), 0.5).apply();
                        // 设置选中
                        rb05.setChecked(true);
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