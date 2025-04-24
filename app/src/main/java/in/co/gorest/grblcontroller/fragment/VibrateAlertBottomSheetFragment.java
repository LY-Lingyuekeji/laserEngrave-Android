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
import in.co.gorest.grblcontroller.events.MachineVauleUpdateMessageEvent;
import in.co.gorest.grblcontroller.events.VibrateAlertTimeUpdateMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class VibrateAlertBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = VibrateAlertBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 激光功率 int
    private int vibrateAlertTime;
    // RadioGroup
    private RadioGroup rgVibrateAlertTime;
    // RadioButton 1S
    private RadioButton rb1;
    // RadioButton 2S
    private RadioButton rb2;
    // RadioButton 3S
    private RadioButton rb3;

    public VibrateAlertBottomSheetFragment() {
    }


    public static VibrateAlertBottomSheetFragment newInstance() {
        return new VibrateAlertBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_vibrate_alert_bottom_sheet, container, false);
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
        rgVibrateAlertTime = view.findViewById(R.id.rg_vibrate_alert_time);
        // RadioButton 1S
        rb1 = view.findViewById(R.id.rb_1);
        // RadioButton 2S
        rb2 = view.findViewById(R.id.rb_2);
        // RadioButton 3S
        rb3 = view.findViewById(R.id.rb_3);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        vibrateAlertTime = sharedPref.getInt(getString(R.string.preference_vibrate_alert_time), 1);
        // 设置选中项
        if (vibrateAlertTime == 1) {
            rb1.setChecked(true);
        } else if (vibrateAlertTime == 2) {
            rb2.setChecked(true);
        } else if (vibrateAlertTime == 3) {
            rb3.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgVibrateAlertTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_1:
                        // 设置共享偏好设置保存的激光功率实例为 1S
                        sharedPref.edit().putInt(getString(R.string.preference_vibrate_alert_time), 1).apply();
                        EventBus.getDefault().post(new VibrateAlertTimeUpdateMessageEvent("1"));
                        // 设置选中
                        rb1.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_2:
                        // 设置共享偏好设置保存的激光功率实例为 2S
                        sharedPref.edit().putInt(getString(R.string.preference_vibrate_alert_time), 2).apply();
                        EventBus.getDefault().post(new VibrateAlertTimeUpdateMessageEvent("2"));
                        // 设置选中
                        rb2.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_3:
                        // 设置共享偏好设置保存的激光功率实例为 3S
                        sharedPref.edit().putInt(getString(R.string.preference_vibrate_alert_time), 3).apply();
                        EventBus.getDefault().post(new VibrateAlertTimeUpdateMessageEvent("3"));
                        // 设置选中
                        rb3.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;

                }
            }
        });
    }

}