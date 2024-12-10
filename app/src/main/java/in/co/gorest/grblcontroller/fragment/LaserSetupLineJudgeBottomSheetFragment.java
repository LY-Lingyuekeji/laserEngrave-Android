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
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class LaserSetupLineJudgeBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = LaserSetupLineJudgeBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 激光功率 int
    private int laserLevel;
    // RadioGroup
    private RadioGroup rgLaserLevel;
    // RadioButton 1%
    private RadioButton rb1;
    // RadioButton 2%
    private RadioButton rb2;
    // RadioButton 3%
    private RadioButton rb3;
    // RadioButton 4%
    private RadioButton rb4;
    // RadioButton 5%
    private RadioButton rb5;

    public LaserSetupLineJudgeBottomSheetFragment() {
    }


    public static LaserSetupLineJudgeBottomSheetFragment newInstance() {
        return new LaserSetupLineJudgeBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_laser_setup_line_judge_bottom_sheet, container, false);
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
        rgLaserLevel = view.findViewById(R.id.rg_laser_level);
        // RadioButton 1%
        rb1 = view.findViewById(R.id.rb_1);
        // RadioButton 2%
        rb2 = view.findViewById(R.id.rb_2);
        // RadioButton 3%
        rb3 = view.findViewById(R.id.rb_3);
        // RadioButton 4%
        rb4 = view.findViewById(R.id.rb_4);
        // RadioButton 5%
        rb5 = view.findViewById(R.id.rb_5);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 1);
        // 设置选中项
        if (laserLevel == 1) {
            rb1.setChecked(true);
        } else if (laserLevel == 2) {
            rb2.setChecked(true);
        } else if (laserLevel == 3) {
            rb3.setChecked(true);
        } else if (laserLevel == 4) {
            rb4.setChecked(true);
        } else if (laserLevel == 5) {
            rb5.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgLaserLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_1:
                        // 设置共享偏好设置保存的激光功率实例为 1%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level_line_judge_setting), 1).apply();
                        EventBus.getDefault().post(new MachineVauleUpdateMessageEvent("1"));
                        // 设置选中
                        rb1.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_2:
                        // 设置共享偏好设置保存的激光功率实例为 2%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level_line_judge_setting), 2).apply();
                        EventBus.getDefault().post(new MachineVauleUpdateMessageEvent("2"));
                        // 设置选中
                        rb2.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_3:
                        // 设置共享偏好设置保存的激光功率实例为 3%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level_line_judge_setting), 3).apply();
                        EventBus.getDefault().post(new MachineVauleUpdateMessageEvent("3"));
                        // 设置选中
                        rb3.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_4:
                        // 设置共享偏好设置保存的激光功率实例为 4%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level_line_judge_setting), 4).apply();
                        EventBus.getDefault().post(new MachineVauleUpdateMessageEvent("4"));
                        // 设置选中
                        rb4.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_5:
                        // 设置共享偏好设置保存的激光功率实例为 5%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level_line_judge_setting), 5).apply();
                        EventBus.getDefault().post(new MachineVauleUpdateMessageEvent("5"));
                        // 设置选中
                        rb5.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                }
            }
        });
    }

}