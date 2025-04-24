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

public class SpeedMiddleForCNCBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedMiddleForCNCBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 速度（中等） Double
    private Double speedMiddle;
    // RadioGroup
    private RadioGroup rgSpeedMiddle;
    // RadioButton 200mm/min
    private RadioButton rb200;
    // RadioButton 300mm/min
    private RadioButton rb300;
    // RadioButton 400mm/min
    private RadioButton rb400;
    // RadioButton 500mm/min
    private RadioButton rb500;
    // RadioButton 600mm/min
    private RadioButton rb600;

    public SpeedMiddleForCNCBottomSheetFragment() {
    }


    public static SpeedMiddleForCNCBottomSheetFragment newInstance() {
        return new SpeedMiddleForCNCBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_speed_middle_for_cnc_bottom_sheet, container, false);
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
        rgSpeedMiddle = view.findViewById(R.id.rg_speed_middle);
        // RadioButton 200mm/min
        rb200 = view.findViewById(R.id.rb_200);
        // RadioButton 300mm/min
        rb300 = view.findViewById(R.id.rb_300);
        // RadioButton 400mm/min
        rb400 = view.findViewById(R.id.rb_400);
        // RadioButton 500mm/min
        rb500 = view.findViewById(R.id.rb_500);
        // RadioButton 600mm/min
        rb600 = view.findViewById(R.id.rb_600);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        speedMiddle = sharedPref.getDouble(getString(R.string.preference_speed_middle_cnc), 300.0);
        // 设置选中项
        if (speedMiddle == 200.0) {
            rb200.setChecked(true);
        } else if (speedMiddle == 300.0) {
            rb300.setChecked(true);
        } else if (speedMiddle == 400.0) {
            rb400.setChecked(true);
        } else if (speedMiddle == 500.0) {
            rb500.setChecked(true);
        } else if (speedMiddle == 600.0) {
            rb600.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgSpeedMiddle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_200:
                        // 设置共享偏好设置保存的速度（中等）运动参数实例为 200mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_middle_cnc), 200.0).apply();
                        // 设置选中
                        rb200.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_300:
                        // 设置共享偏好设置保存的速度（中等）运动参数实例为 300mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_middle_cnc), 300.0).apply();
                        // 设置选中
                        rb300.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_400:
                        // 设置共享偏好设置保存的速度（中等）运动参数实例为 400mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_middle_cnc), 400.0).apply();
                        // 设置选中
                        rb400.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_500:
                        // 设置共享偏好设置保存的速度（中等）运动参数实例为 500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_middle_cnc), 500.0).apply();
                        // 设置选中
                        rb500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_600:
                        // 设置共享偏好设置保存的速度（中等）运动参数实例为 300mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_middle_cnc), 600.0).apply();
                        // 设置选中
                        rb600.setChecked(true);
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