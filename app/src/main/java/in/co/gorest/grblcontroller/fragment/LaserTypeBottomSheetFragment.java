package in.co.gorest.grblcontroller.fragment;

import android.content.Context;
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
import in.co.gorest.grblcontroller.events.LaserTypeUpdateMessageEvent;
import in.co.gorest.grblcontroller.events.MachineVauleUpdateMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class LaserTypeBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = LaserTypeBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 激光功率 int
    private int laserType;
    // RadioGroup
    private RadioGroup rgLaserType;
    // RadioButton 定焦5W激光
    private RadioButton rbLaserTypeDl5;
    // RadioButton 定焦10W激光
    private RadioButton rbLaserTypeDl10;
    // RadioButton 定焦20W激光
    private RadioButton rbLaserTypeDl20;

    public LaserTypeBottomSheetFragment() {
    }

    public static LaserTypeBottomSheetFragment newInstance() {
        return new LaserTypeBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_laser_type_bottom_sheet, container, false);
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
        rgLaserType = view.findViewById(R.id.rg_laser_type);
        // RadioButton 定焦5W激光
        rbLaserTypeDl5 = view.findViewById(R.id.rb_laser_type_dl5);
        // RadioButton 定焦10W激光
        rbLaserTypeDl10 = view.findViewById(R.id.rb_laser_type_dl10);
        // RadioButton 定焦20W激光
        rbLaserTypeDl20 = view.findViewById(R.id.rb_laser_type_dl20);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        laserType = sharedPref.getInt(getString(R.string.preference_parameter_laser_type), 1);
        // 设置选中项
        if (laserType == 1) {
            rbLaserTypeDl5.setChecked(true);
        } else if (laserType == 2) {
            rbLaserTypeDl10.setChecked(true);
        } else if (laserType == 3) {
            rbLaserTypeDl20.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgLaserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_laser_type_dl5:
                        laserType = 1;
                        break;
                    case R.id.rb_laser_type_dl10:
                        laserType = 2;
                        break;
                    case R.id.rb_laser_type_dl20:
                        laserType = 3;
                        break;
                }
                // 设置共享偏好设置保存的激光功率
                sharedPref.edit().putInt(getString(R.string.preference_parameter_laser_type), laserType).apply();
                // 发送事件，传递选中的激光类型
                sendLaserTypeEvent(laserType);
                // 隐藏弹窗
                dismiss();
            }
        });
    }

    /**
     * 发送选中的激光类型
      */
    private void sendLaserTypeEvent(int laserType) {
        EventBus.getDefault().post(new LaserTypeUpdateMessageEvent(laserType));
    }
}