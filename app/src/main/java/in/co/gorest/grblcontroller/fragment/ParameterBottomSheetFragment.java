package in.co.gorest.grblcontroller.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.MaterialLibraryActivity;
import in.co.gorest.grblcontroller.adapters.LaserMaterialAdapter;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.LaserParameter;
import in.co.gorest.grblcontroller.model.Material;

public class ParameterBottomSheetFragment extends BottomSheetDialogFragment implements LaserMaterialAdapter.OnItemSelectedListener{

    // 用于日志记录的标签
    private final static String TAG = ParameterBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 素材列表
    private RecyclerView materialRecyclerView;
    // 更多素材
    private ImageView ivMoreMaterial;
    // 材料类型
    private TextView tvMaterialName;
    // 激光功率（整体）
    private LinearLayout llParameterLaserLevel;
    // 激光功率
    private TextView tvParameterLaserLevel;
    // 雕刻速度（整体）
    private LinearLayout llParameterSpeedLevel;
    // 雕刻速度
    private TextView tvParameterSpeedLevel;
    // 确定
    private TextView tvConfirm;
    // 激光参数列表
    private List<LaserParameter> laserParameters;
    // 激光型号
    private String laserModule;


    // 定义一个接口，用于传递数据
    public interface OnLaserParametersSelectedListener {
        void onLaserParametersSelected(int power, int speed);
    }

    // 创建一个接口实例变量
    private OnLaserParametersSelectedListener listener;

    // 构造参数
    public ParameterBottomSheetFragment() {
    }

    // 单例模式
    public static ParameterBottomSheetFragment newInstance() {
        return new ParameterBottomSheetFragment();
    }

    // 在 Fragment 中设置 Activity 的回调
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLaserParametersSelectedListener) {
            listener = (OnLaserParametersSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnLaserParametersSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;  // 避免内存泄漏
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = EnhancedSharedPreferences.getInstance(getActivity(), getString(R.string.shared_preference_key));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parameter_bottom_sheet, container, false);
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
        // 素材列表
        materialRecyclerView = view.findViewById(R.id.recycler_view_material);
        // 更多素材
        ivMoreMaterial = view.findViewById(R.id.iv_more_material);
        // 材料类型
        tvMaterialName = view.findViewById(R.id.tv_material_name);
        // 激光功率（整体）
        llParameterLaserLevel = view.findViewById(R.id.ll_parameter_laser_level);
        // 激光功率
        tvParameterLaserLevel = view.findViewById(R.id.tv_parameter_laser_level);
        // 雕刻速度（整体）
        llParameterSpeedLevel = view.findViewById(R.id.ll_parameter_speed_level);
        // 雕刻速度
        tvParameterSpeedLevel = view.findViewById(R.id.tv_parameter_speed_level);
        // 确定
        tvConfirm = view.findViewById(R.id.tv_confirm);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化 RecyclerView
        materialRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 创建数据源
        List<Material> materialList = new ArrayList<>();
        materialList.add(new Material("木板", R.mipmap.ic_sc_muban));
        materialList.add(new Material("纸板", R.mipmap.ic_sc_zhiban));
        materialList.add(new Material("平安树叶", R.mipmap.ic_sc_pinganye));
        materialList.add(new Material("不锈钢", R.mipmap.ic_sc_buxiugang));
        materialList.add(new Material("皮革", R.mipmap.ic_sc_pige));
        materialList.add(new Material("亚克力", R.mipmap.ic_sc_yakeli));

        // 设置适配器
        LaserMaterialAdapter adapter = new LaserMaterialAdapter(requireContext(), materialList, this);
        materialRecyclerView.setAdapter(adapter);

        // 激光型号
        laserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
        Log.d(TAG, "激光型号=" + laserModule);
        // 激光参数列表
        laserParameters = new ArrayList<>();
        // 木材
        laserParameters.add(new LaserParameter("木板", "LdT-3W", 3000, 80));
        laserParameters.add(new LaserParameter("木板", "LdT4-10W", 7000, 50));
        laserParameters.add(new LaserParameter("木板", "LdT4-20W", 7000, 30));
        // 纸板
        laserParameters.add(new LaserParameter("纸板", "LdT-3W", 3000, 80));
        laserParameters.add(new LaserParameter("纸板", "LdT4-10W", 10000, 60));
        laserParameters.add(new LaserParameter("纸板", "LdT4-20W", 15000, 50));
        // 平安树叶
        laserParameters.add(new LaserParameter("平安树叶", "LdT-3W", 8000, 100));
        laserParameters.add(new LaserParameter("平安树叶", "LdT4-10W", 20000, 50));
        laserParameters.add(new LaserParameter("平安树叶", "LdT4-20W", 25000, 40));
        // 不锈钢
        laserParameters.add(new LaserParameter("不锈钢", "LdT4-10W", 300, 100));
        laserParameters.add(new LaserParameter("不锈钢", "LdT4-20W", 500, 100));
        // 皮革
        laserParameters.add(new LaserParameter("皮革", "LdT-3W", 3000, 80));
        laserParameters.add(new LaserParameter("皮革", "LdT4-10W", 20000, 50));
        laserParameters.add(new LaserParameter("皮革", "LdT4-20W", 20000, 40));
        // 亚克力
        laserParameters.add(new LaserParameter("亚克力", "LdT-3W", 3000, 100));
        laserParameters.add(new LaserParameter("亚克力", "LdT4-10W", 10000, 70));
        laserParameters.add(new LaserParameter("亚克力", "LdT4-20W", 15000, 70));

        // 查找并设置推荐的功率和速度
        setRecommendedLaserParameters("木板");
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 更多素材
        ivMoreMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转素材库
                startActivity(new Intent(requireActivity(), MaterialLibraryActivity.class));
            }
        });

        // 激光功率
        llParameterLaserLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaserLevelBottomSheetFragment laserLevelBottomSheetFragment = new LaserLevelBottomSheetFragment();
                laserLevelBottomSheetFragment.setOnLaserPowerSelectedListener(new LaserLevelBottomSheetFragment.OnLaserPowerSelectedListener() {
                    @Override
                    public void onLaserPowerSelected(String laserPower) {
                        // 这里你可以接收选中的名称，并在 Fragment 中做处理
                        Log.d(TAG, "Selected laserPower=" + laserPower);
                        // 设置激光功率
                        tvParameterLaserLevel.setText(laserPower);
                    }
                });
                laserLevelBottomSheetFragment.show(getParentFragmentManager(), "");
            }
        });

        // 雕刻速度
        llParameterSpeedLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedLevelBottomSheetFragment speedLevelBottomSheetFragment = new SpeedLevelBottomSheetFragment();
                speedLevelBottomSheetFragment.setOnSpeedLevelSelectedListener(new SpeedLevelBottomSheetFragment.OnSpeedLevelSelectedListener() {
                    @Override
                    public void OnSpeedLevelSelectedListener(String speedLevel) {
                        // 这里你可以接收选中的名称，并在 Fragment 中做处理
                        Log.d(TAG, "Selected speedLevel=" + speedLevel);
                        // 设置激光功率
                        tvParameterSpeedLevel.setText(speedLevel);
                    }
                });
                speedLevelBottomSheetFragment.show(getParentFragmentManager(), "");
            }
        });


        // 确定
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 获取激光功率和速度
                int power = Integer.parseInt(tvParameterLaserLevel.getText().toString().replace("%", ""));
                int speed = Integer.parseInt(tvParameterSpeedLevel.getText().toString().replace("mm/min", ""));

                Log.d(TAG, "power=" + power + "------speed=" + speed);

                // 通过接口回调传递数据给 Activity
                if (listener != null) {
                    listener.onLaserParametersSelected(power, speed);
                }

                // 关闭当前的底部弹窗
                dismiss();

            }
        });
    }

    @Override
    public void onItemSelected(String materialName) {
        // 这里你可以接收选中的名称，并在 Fragment 中做处理
        Log.d(TAG, "Selected Material=" + materialName);
        // 设置材料类型
        tvMaterialName.setText(materialName);

        // 查找并设置推荐的功率和速度
        setRecommendedLaserParameters(materialName);
    }

    private void setRecommendedLaserParameters(String materialName) {
        for (LaserParameter parameter : laserParameters) {
            // 找到对应的材料和激光模块
            if (parameter.getMaterialType().equals(materialName) && parameter.getLaserModel().equals(laserModule)) {
                // 设置推荐的功率和速度
                tvParameterLaserLevel.setText(parameter.getRecommendedPower() + "%");
                tvParameterSpeedLevel.setText(parameter.getRecommendedSpeed() + "mm/min");

                // TODO 保存功率和速度
                sharedPref.edit().putInt(getString(R.string.preference_recommended_power), parameter.getRecommendedPower()).apply();
                sharedPref.edit().putInt(getString(R.string.preference_recommended_speed), parameter.getRecommendedSpeed()).apply();


                break;
            }
        }
    }

}