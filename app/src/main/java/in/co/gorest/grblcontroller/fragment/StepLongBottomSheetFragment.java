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

public class StepLongBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = StepLongBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 步长（长） Double
    private Double stepLong;
    // RadioGroup
    private RadioGroup rgStepLong;
    // RadioButton 10mm
    private RadioButton rb10;
    // RadioButton 20mm
    private RadioButton rb20;
    // RadioButton 50mm
    private RadioButton rb50;
    // RadioButton 100mm
    private RadioButton rb100;
    // RadioButton 200mm
    private RadioButton rb200;

    public StepLongBottomSheetFragment() {
    }


    public static StepLongBottomSheetFragment newInstance() {
        return new StepLongBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_step_long_bottom_sheet, container, false);
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
        rgStepLong = view.findViewById(R.id.rg_step_long);
        // RadioButton 10mm
        rb10 = view.findViewById(R.id.rb_10);
        // RadioButton 20mm
        rb20 = view.findViewById(R.id.rb_20);
        // RadioButton 50mm
        rb50 = view.findViewById(R.id.rb_50);
        // RadioButton 100mm
        rb100 = view.findViewById(R.id.rb_100);
        // RadioButton 200mm
        rb200 = view.findViewById(R.id.rb_200);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        stepLong = sharedPref.getDouble(getString(R.string.preference_step_long), 10.0);
        // 设置选中项
        if (stepLong == 10.0) {
            rb10.setChecked(true);
        } else if (stepLong == 20.0) {
            rb20.setChecked(true);
        } else if (stepLong == 50.0) {
            rb50.setChecked(true);
        } else if (stepLong == 100.0) {
            rb100.setChecked(true);
        } else if (stepLong == 200.0) {
            rb200.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgStepLong.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_10:
                        // 设置共享偏好设置保存的步长（长）运动参数实例为 10mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_long), 10.0).apply();
                        // 设置选中
                        rb10.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_20:
                        // 设置共享偏好设置保存的步长（长）运动参数实例为 20mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_long), 20.0).apply();
                        // 设置选中
                        rb20.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_50:
                        // 设置共享偏好设置保存的步长（长）运动参数实例为 50mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_long), 50.0).apply();
                        // 设置选中
                        rb50.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_100:
                        // 设置共享偏好设置保存的步长（长）运动参数实例为 100mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_long), 100.0).apply();
                        // 设置选中
                        rb100.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_200:
                        // 设置共享偏好设置保存的步长（长）运动参数实例为 200mm
                        sharedPref.edit().putDouble(getString(R.string.preference_step_long), 200.0).apply();
                        // 设置选中
                        rb200.setChecked(true);
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