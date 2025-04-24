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

public class SpeedPrestissimoForCNCBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedPrestissimoForCNCBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 速度（超快） Double
    private Double speedPrestissimo;
    // RadioGroup
    private RadioGroup rgSpeedPrestissimo;
    // RadioButton 1200mm/min
    private RadioButton rb1200;
    // RadioButton 1300mm/min
    private RadioButton rb1300;
    // RadioButton 1400mm/min
    private RadioButton rb1400;
    // RadioButton 1500mm/min
    private RadioButton rb1500;
    // RadioButton 1600mm/min
    private RadioButton rb1600;

    public SpeedPrestissimoForCNCBottomSheetFragment() {
    }


    public static SpeedPrestissimoForCNCBottomSheetFragment newInstance() {
        return new SpeedPrestissimoForCNCBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_speed_prestissimo_for_cnc_bottom_sheet, container, false);
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
        rgSpeedPrestissimo = view.findViewById(R.id.rg_speed_prestissimo);
        // RadioButton 1200mm/min
        rb1200 = view.findViewById(R.id.rb_1200);
        // RadioButton 1300mm/min
        rb1300 = view.findViewById(R.id.rb_1300);
        // RadioButton 1400mm/min
        rb1400 = view.findViewById(R.id.rb_1400);
        // RadioButton 1500mm/min
        rb1500 = view.findViewById(R.id.rb_1500);
        // RadioButton 20000mm/min
        rb1600 = view.findViewById(R.id.rb_1600);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        speedPrestissimo = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo_cnc), 1200.0);
        // 设置选中项
        if (speedPrestissimo == 1200.0) {
            rb1200.setChecked(true);
        } else if (speedPrestissimo == 1300.0) {
            rb1300.setChecked(true);
        } else if (speedPrestissimo == 1400.0) {
            rb1400.setChecked(true);
        } else if (speedPrestissimo == 1500.0) {
            rb1500.setChecked(true);
        } else if (speedPrestissimo == 1600.0) {
            rb1600.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgSpeedPrestissimo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_1200:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 1200mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo_cnc), 1200.0).apply();
                        // 设置选中
                        rb1200.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1300:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 1300mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo_cnc), 1300.0).apply();
                        // 设置选中
                        rb1300.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1400:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 1400mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo_cnc), 1400.0).apply();
                        // 设置选中
                        rb1400.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1500:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 1500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo_cnc), 1500.0).apply();
                        // 设置选中
                        rb1500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_1600:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 1600mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo_cnc), 1600.0).apply();
                        // 设置选中
                        rb1600.setChecked(true);
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