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

public class SpeedPrestissimoBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedPrestissimoBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 速度（超快） Double
    private Double speedPrestissimo;
    // RadioGroup
    private RadioGroup rgSpeedPrestissimo;
    // RadioButton 10000mm/min
    private RadioButton rb10000;
    // RadioButton 12500mm/min
    private RadioButton rb12500;
    // RadioButton 15000mm/min
    private RadioButton rb15000;
    // RadioButton 17500mm/min
    private RadioButton rb17500;
    // RadioButton 20000mm
    private RadioButton rb20000;

    public SpeedPrestissimoBottomSheetFragment() {
    }


    public static SpeedPrestissimoBottomSheetFragment newInstance() {
        return new SpeedPrestissimoBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_speed_prestissimo_bottom_sheet, container, false);
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
        // RadioButton 10000mm/min
        rb10000 = view.findViewById(R.id.rb_10000);
        // RadioButton 12500mm/min
        rb12500 = view.findViewById(R.id.rb_12500);
        // RadioButton 15000mm/min
        rb15000 = view.findViewById(R.id.rb_15000);
        // RadioButton 17500mm/min
        rb17500 = view.findViewById(R.id.rb_17500);
        // RadioButton 20000mm/min
        rb20000 = view.findViewById(R.id.rb_20000);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        speedPrestissimo = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0);
        // 设置选中项
        if (speedPrestissimo == 10000.0) {
            rb10000.setChecked(true);
        } else if (speedPrestissimo == 12500.0) {
            rb12500.setChecked(true);
        } else if (speedPrestissimo == 15000.0) {
            rb15000.setChecked(true);
        } else if (speedPrestissimo == 17500.0) {
            rb17500.setChecked(true);
        } else if (speedPrestissimo == 20000.0) {
            rb20000.setChecked(true);
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
                    case R.id.rb_10000:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 10000mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo), 10000.0).apply();
                        // 设置选中
                        rb10000.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_12500:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 12500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo), 12500.0).apply();
                        // 设置选中
                        rb12500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_15000:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 15000mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo), 15000.0).apply();
                        // 设置选中
                        rb15000.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_17500:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 17500mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo), 17500.0).apply();
                        // 设置选中
                        rb17500.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        // 通知更新
                        EventBus.getDefault().post(new StepSetupEvent("update"));
                        break;
                    case R.id.rb_20000:
                        // 设置共享偏好设置保存的速度（超快）运动参数实例为 20000mm/min
                        sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo), 20000.0).apply();
                        // 设置选中
                        rb20000.setChecked(true);
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