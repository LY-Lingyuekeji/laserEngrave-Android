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

public class LaserSetupBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = LaserSetupBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 激光功率 int
    private int laserLevel;
    // RadioGroup
    private RadioGroup rgLaserLevel;
    // RadioButton 10%
    private RadioButton rb10;
    // RadioButton 20%
    private RadioButton rb20;
    // RadioButton 30%
    private RadioButton rb30;
    // RadioButton 40%
    private RadioButton rb40;
    // RadioButton 50%
    private RadioButton rb50;

    public LaserSetupBottomSheetFragment() {
    }


    public static LaserSetupBottomSheetFragment newInstance() {
        return new LaserSetupBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_laser_setup_bottom_sheet, container, false);
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
        // RadioButton 10%
        rb10 = view.findViewById(R.id.rb_10);
        // RadioButton 20%
        rb20 = view.findViewById(R.id.rb_20);
        // RadioButton 30%
        rb30 = view.findViewById(R.id.rb_30);
        // RadioButton 40%
        rb40 = view.findViewById(R.id.rb_40);
        // RadioButton 50%
        rb50 = view.findViewById(R.id.rb_50);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        laserLevel = sharedPref.getInt(getString(R.string.preference_laser_level), 10);
        // 设置选中项
        if (laserLevel == 10) {
            rb10.setChecked(true);
        } else if (laserLevel == 20) {
            rb20.setChecked(true);
        } else if (laserLevel == 30) {
            rb30.setChecked(true);
        } else if (laserLevel == 40) {
            rb40.setChecked(true);
        } else if (laserLevel == 50) {
            rb50.setChecked(true);
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
                    case R.id.rb_10:
                        // 设置共享偏好设置保存的激光功率实例为 10%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level), 10).apply();
                        // 设置选中
                        rb10.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_20:
                        // 设置共享偏好设置保存的激光功率实例为 20%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level), 20).apply();
                        // 设置选中
                        rb20.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_30:
                        // 设置共享偏好设置保存的激光功率实例为 30%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level), 30).apply();
                        // 设置选中
                        rb30.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_40:
                        // 设置共享偏好设置保存的激光功率实例为 40%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level), 40).apply();
                        // 设置选中
                        rb40.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_50:
                        // 设置共享偏好设置保存的激光功率实例为 50%
                        sharedPref.edit().putInt(getString(R.string.preference_laser_level), 50).apply();
                        // 设置选中
                        rb50.setChecked(true);
                        // 隐藏弹窗
                        dismiss();
                        break;
                }
            }
        });
    }

}