package in.co.gorest.grblcontroller.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.MaterialLibraryActivity;
import in.co.gorest.grblcontroller.adapters.LaserMaterialAdapter;
import in.co.gorest.grblcontroller.events.MaterialSelectedEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.LaserParameter;
import in.co.gorest.grblcontroller.model.Material;
import in.co.gorest.grblcontroller.model.ScanDirection;

public class ParameterBottomSheetFragment extends BottomSheetDialogFragment implements LaserMaterialAdapter.OnItemSelectedListener, ParameterOperationModeBottomSheetFragment.OnOperationModeSelectedListener {

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
    // 加工类型(标题)
    private LinearLayout llParameterOperationMode;
    // 加工类型（整体-激光雕刻/激光切割）
    private LinearLayout llParameterEngraveOrCutting;
    // 加工类型
    private TextView tvParameterEngraveOrCutting;
    // 激光功率（整体）
    private LinearLayout llParameterLaserLevel;
    // 激光功率
    private TextView tvParameterLaserLevel;
    // 雕刻次数（整体）
    private LinearLayout llParameterEngraveCount;
    // 雕刻次数
    private TextView tvParameterEngraveCount;
    // 扫描间隙（整体）
    private LinearLayout llParameterScanningGap;
    // 扫描间隙
    private TextView tvParameterScanningGap;
    // 扫描方向（整体）
    private LinearLayout llParameterScanningOrientation;
    // 扫描方向
    private TextView tvParameterScanningOrientation;

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

    // 定义加工模式，默认为雕刻
    private String selectedOperationMode = "engraving";

    // 雕刻次数
    private int totalEngraveCount = 1;

    // 设置适配器
    LaserMaterialAdapter adapter;

    private static final String ARG_INDEX = "target_index";
    private int targetIndex = -1;


    // 定义一个接口，用于传递数据
    public interface OnLaserParametersSelectedListener {
        void onLaserParametersSelected(int targetIndex, int power, int speed, float resols, ScanDirection scanDirection, int totalEngraveCount, boolean isAir, int zDown);
    }

    // 创建一个接口实例变量
    private OnLaserParametersSelectedListener listener;

    // 构造参数
    public ParameterBottomSheetFragment() {
    }

    // 单例模式
    public static ParameterBottomSheetFragment newInstance(int index) {
        ParameterBottomSheetFragment fragment = new ParameterBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
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

        if (getArguments() != null) {
            targetIndex = getArguments().getInt(ARG_INDEX, -1);
        }

        // 注册EventBus
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
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
        // 加工类型（整体）
        llParameterOperationMode = view.findViewById(R.id.ll_parameter_operation_mode);
        // 加工类型（整体-激光雕刻/激光切割）
        llParameterEngraveOrCutting = view.findViewById(R.id.ll_parameter_engrave_or_cutting);
        // 加工类型
        tvParameterEngraveOrCutting = view.findViewById(R.id.tv_parameter_engrave_or_cutting);
        // 雕刻次数（整体）
        llParameterEngraveCount = view.findViewById(R.id.ll_parameter_engrave_count);
        // 雕刻次数
        tvParameterEngraveCount = view.findViewById(R.id.tv_parameter_engrave_count);
        // 扫描间隙（整体）
        llParameterScanningGap = view.findViewById(R.id.ll_parameter_scanning_gap);
        // 扫描间隙
        tvParameterScanningGap = view.findViewById(R.id.tv_parameter_scanning_gap);
        // 扫描方向（整体）
        llParameterScanningOrientation = view.findViewById(R.id.ll_parameter_scanning_orientation);
        // 扫描方向
        tvParameterScanningOrientation = view.findViewById(R.id.tv_parameter_scanning_orientation);
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
        // 获取传递的 tag
        String tag = getTag();
        if (tag != null) {
            String[] parts = tag.split("\\|");
            String deviceType = "";
            String mode = "";
            if (parts.length == 2) {
                deviceType = parts[0];  // CNC / Laser
                mode = parts[1];        // isCutting / isEngraving
            }

            if ("isCutting".equals(mode)) {
                // 处理切割模式
                llParameterOperationMode.setVisibility(View.VISIBLE);
            } else if ("isEngraving".equals(mode)) {
                // 处理雕刻模式
                llParameterOperationMode.setVisibility(View.GONE);
            }
        }



        // 设置加工类型
        if (selectedOperationMode.equals("engraving")) {
            tvParameterEngraveOrCutting.setText("雕刻");
        } else {
            tvParameterEngraveOrCutting.setText("切割");
        }

        // 激光型号
        laserModule = sharedPref.getString(getString(R.string.preference_laser_module), "LdT-3W");
        Log.d(TAG, "激光型号=" + laserModule);


        // 初始化 RecyclerView
        materialRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 创建数据源
        List<Material> materialList = new ArrayList<>();
        materialList.add(new Material("胶合板(2mm)", R.mipmap.ic_sc_muban));
        materialList.add(new Material("胶合板(5mm)", R.mipmap.ic_sc_muban));
        materialList.add(new Material("胶合板(8mm)", R.mipmap.ic_sc_muban));
        materialList.add(new Material("纸板(2mm)", R.mipmap.ic_sc_zhiban));
        materialList.add(new Material("牛皮纸(250g)", R.mipmap.ic_sc_niupizhi));
        materialList.add(new Material("平安树叶", R.mipmap.ic_sc_pinganye));
        materialList.add(new Material("不锈钢", R.mipmap.ic_sc_buxiugang));
        materialList.add(new Material("金属漆面", R.mipmap.ic_sc_jinshuqimian));
        materialList.add(new Material("皮革（1mm）", R.mipmap.ic_sc_pige));
        materialList.add(new Material("PVC/塑料", R.mipmap.ic_sc_suliao));
        materialList.add(new Material("黑色亚克力", R.mipmap.ic_sc_yakeli));
        materialList.add(new Material("橡胶印章", R.mipmap.ic_sc_xiangjiaoyinzhang));
        materialList.add(new Material("MDF板", R.mipmap.ic_sc_miduban));
        materialList.add(new Material("竹子", R.mipmap.ic_sc_zhuzi));
        materialList.add(new Material("软磁贴片", R.mipmap.ic_sc_ruancitiepian));
        materialList.add(new Material("食物", R.mipmap.ic_sc_shiwu));
        materialList.add(new Material("玻璃", R.mipmap.ic_sc_boli));
        materialList.add(new Material("布料", R.mipmap.ic_sc_buliao));
        materialList.add(new Material("陶瓷", R.mipmap.ic_sc_taoci));
        materialList.add(new Material("黄铜", R.mipmap.ic_sc_huangtong));
        materialList.add(new Material("纯铝", R.mipmap.ic_sc_chunlv));
        materialList.add(new Material("电路铜板", R.mipmap.ic_sc_dianlutongban));
        materialList.add(new Material("板岩", R.mipmap.ic_sc_banyan));

        adapter = new LaserMaterialAdapter(requireContext(), materialList, this);
        materialRecyclerView.setAdapter(adapter);


        // 激光参数列表
        laserParameters = new ArrayList<>();

        /************************************* 胶合板(2mm) *************************************/
        // 胶合板(2mm) 雕刻
        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT-3W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT4-10W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT4-20W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(2mm)", "Cd-100W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 胶合板(2mm) 切割
        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT-3W", 500, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT4-10W", 500, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT4-20W", 700, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
//        laserParameters.add(new LaserParameter("胶合板(2mm)", "LdT4-1064nm-2W", 700, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 胶合板(5mm) *************************************/
        // 胶合板(5mm) 雕刻
        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT-3W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT4-10W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT4-20W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT4-1064nm-2W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 胶合板(5mm) 切割
        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT-3W", 250, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT4-10W", 250, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
//        laserParameters.add(new LaserParameter("胶合板(5mm)", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 胶合板(8mm) *************************************/
        // 胶合板(8mm) 雕刻
        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT-3W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT4-10W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT4-20W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT4-1064nm-2W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 胶合板(8mm) 切割
//        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT-3W", 100, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT4-10W", 100, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 8));
        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT4-20W", 230, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 8));
//        laserParameters.add(new LaserParameter("胶合板(8mm)", "LdT4-1064nm-2W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 纸板(2mm) *************************************/
        // 纸板(2mm) 雕刻
        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT-3W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT4-10W", 10000, 60, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT4-20W", 15000, 40, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT4-1064nm-2W", 15000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 纸板(2mm) 切割
//        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT-3W", 500, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT4-10W", 500, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT4-20W", 500, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
//        laserParameters.add(new LaserParameter("纸板(2mm)", "LdT4-1064nm-2W", 500, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 牛皮纸(250g) *************************************/
        // 牛皮纸(250g)) 雕刻
        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT-3W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT4-10W", 20000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT4-20W", 20000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT4-1064nm-2W", 20000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 牛皮纸(250g) 切割
//        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT-3W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT4-10W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT4-20W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
//        laserParameters.add(new LaserParameter("牛皮纸(250g)", "LdT4-1064nm-2W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 平安树叶 *************************************/
        // 平安树叶 雕刻
        laserParameters.add(new LaserParameter("平安树叶", "LdT-3W", 8000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("平安树叶", "LdT4-10W", 22000, 60, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("平安树叶", "LdT4-20W", 22000, 40, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("平安树叶", "LdT4-1064nm-2W", 22000, 40, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 平安树叶 切割
//        laserParameters.add(new LaserParameter("平安树叶", "LdT-3W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("平安树叶", "LdT4-10W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("平安树叶", "LdT4-20W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("平安树叶", "LdT4-1064nm-2W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));


        /************************************* 不锈钢 *************************************/
        // 不锈钢 雕刻
        laserParameters.add(new LaserParameter("不锈钢", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("不锈钢", "LdT4-10W", 600, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("不锈钢", "LdT4-20W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("不锈钢", "LdT4-1064nm-2W", 3000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 不锈钢 切割
//        laserParameters.add(new LaserParameter("不锈钢", "LdT-3W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("不锈钢", "LdT4-10W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("不锈钢", "LdT4-20W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("不锈钢", "LdT4-1064nm-2W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));


        /************************************* 金属漆面 *************************************/
        // 金属漆面 雕刻
        laserParameters.add(new LaserParameter("金属漆面", "LdT-3W", 5000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("金属漆面", "LdT4-10W", 600, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("金属漆面", "LdT4-20W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("金属漆面", "LdT4-1064nm-2W", 5000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 金属漆面 切割
//        laserParameters.add(new LaserParameter("金属漆面", "LdT-3W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("金属漆面", "LdT4-10W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("金属漆面", "LdT4-20W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("金属漆面", "LdT4-1064nm-2W", 300, 70, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 皮革(1mm) *************************************/
        // 皮革(1mm) 雕刻
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT-3W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT4-10W", 10000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT4-20W", 15000, 40, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT4-1064nm-2W", 3000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 皮革(1mm) 切割
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT4-20W", 500, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
//        laserParameters.add(new LaserParameter("皮革(1mm)", "LdT4-1064nm-2W", 500, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* PVC/塑料 *************************************/
        // PVC/塑料 雕刻
        laserParameters.add(new LaserParameter("PVC/塑料", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("PVC/塑料", "LdT4-10W", 10000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("PVC/塑料", "LdT4-20W", 15000, 60, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("PVC/塑料", "LdT4-1064nm-2W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // PVC/塑料 切割
//        laserParameters.add(new LaserParameter("PVC/塑料", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("PVC/塑料", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
        laserParameters.add(new LaserParameter("PVC/塑料", "LdT4-20W", 800, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 0));
//        laserParameters.add(new LaserParameter("PVC/塑料", "LdT4-1064nm-2W", 800, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* PVC/塑料 *************************************/
        // 黑色亚克力 雕刻
        laserParameters.add(new LaserParameter("黑色亚克力", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("黑色亚克力", "LdT4-10W", 10000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("黑色亚克力", "LdT4-20W", 15000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("黑色亚克力", "LdT4-1064nm-2W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        // 黑色亚克力 切割
//        laserParameters.add(new LaserParameter("黑色亚克力", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("黑色亚克力", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
        laserParameters.add(new LaserParameter("黑色亚克力", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
//        laserParameters.add(new LaserParameter("黑色亚克力", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 橡胶印章 *************************************/
        // 橡胶印章 雕刻
        laserParameters.add(new LaserParameter("橡胶印章", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("橡胶印章", "LdT4-10W", 5000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("橡胶印章", "LdT4-20W", 8000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("橡胶印章", "LdT4-1064nm-2W", 8000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 橡胶印章 切割
//        laserParameters.add(new LaserParameter("橡胶印章", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("橡胶印章", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("橡胶印章", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("橡胶印章", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* MDF板 *************************************/
        // MDF板 雕刻
        laserParameters.add(new LaserParameter("MDF板", "LdT-3W", 1200, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("MDF板", "LdT4-10W", 10000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("MDF板", "LdT4-20W", 15000, 60, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-1064nm-2W", 15000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // MDF板 切割
//        laserParameters.add(new LaserParameter("MDF板", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 竹子 *************************************/
        // 竹子 雕刻
        laserParameters.add(new LaserParameter("竹子", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("竹子", "LdT4-10W", 8000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("竹子", "LdT4-20W", 10000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("竹子", "LdT4-1064nm-2W", 10000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // MDF板 切割
//        laserParameters.add(new LaserParameter("MDF板", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("MDF板", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 软磁贴片 *************************************/
        // 软磁贴片 雕刻
        laserParameters.add(new LaserParameter("软磁贴片", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-10W", 5000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-20W", 8000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-1064nm-2W", 8000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 软磁贴片 切割
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 食物 *************************************/
        // 食物 雕刻
        laserParameters.add(new LaserParameter("食物", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("食物", "LdT4-10W", 10000, 80, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("食物", "LdT4-20W", 15000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("食物", "LdT4-1064nm-2W", 15000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 食物 切割
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("软磁贴片", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));


        /************************************* 玻璃 *************************************/
        // 玻璃 雕刻
        laserParameters.add(new LaserParameter("玻璃", "LdT-3W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("玻璃", "LdT4-10W", 600, 90, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("玻璃", "LdT4-20W", 800, 90, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("玻璃", "LdT4-1064nm-2W", 800, 90, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 玻璃 切割
//        laserParameters.add(new LaserParameter("玻璃", "LdT-3W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("玻璃", "LdT4-10W", 150, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("玻璃", "LdT4-20W", 200, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("玻璃", "LdT4-1064nm-2W", 300, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 布料 *************************************/
        // 布料 雕刻
        laserParameters.add(new LaserParameter("布料", "LdT-3W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("布料", "LdT4-10W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("布料", "LdT4-20W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("布料", "LdT4-1064nm-2W", 7000, 30, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 布料 切割
//        laserParameters.add(new LaserParameter("布料", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("布料", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
        laserParameters.add(new LaserParameter("布料", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, true, 2));
//        laserParameters.add(new LaserParameter("布料", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 陶瓷 *************************************/
        // 陶瓷 雕刻
        laserParameters.add(new LaserParameter("陶瓷", "LdT-3W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("陶瓷", "LdT4-10W", 7000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("陶瓷", "LdT4-20W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("陶瓷", "LdT4-1064nm-2W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 陶瓷 切割
//        laserParameters.add(new LaserParameter("布料", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("布料", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("布料", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("布料", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 黄铜 *************************************/
        // 黄铜
//        laserParameters.add(new LaserParameter("黄铜", "LdT-3W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("黄铜", "LdT4-10W", 7000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("黄铜", "LdT4-20W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("黄铜", "LdT4-1064nm-2W", 4000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 陶瓷 切割
//        laserParameters.add(new LaserParameter("黄铜", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("黄铜", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("黄铜", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("黄铜", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 纯铝 *************************************/
        // 纯铝 雕刻
//        laserParameters.add(new LaserParameter("纯铝", "LdT-3W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("纯铝", "LdT4-10W", 7000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("纯铝", "LdT4-20W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("纯铝", "LdT4-1064nm-2W", 10000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 纯铝 切割
//        laserParameters.add(new LaserParameter("纯铝", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("纯铝", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("纯铝", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("纯铝", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 电路铜板 *************************************/
        // 电路铜板
//        laserParameters.add(new LaserParameter("电路铜板", "LdT-3W", 1800, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("电路铜板", "LdT4-10W", 7000, 70, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("电路铜板", "LdT4-20W", 7000, 50, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("电路铜板", "LdT4-1064nm-2W", 1500, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 电路铜板 切割
//        laserParameters.add(new LaserParameter("电路铜板", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("电路铜板", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("电路铜板", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("电路铜板", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        /************************************* 板岩 *************************************/
        // 板岩
//        laserParameters.add(new LaserParameter("板岩", "LdT-3W", 1500, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("板岩", "LdT4-10W", 6000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
        laserParameters.add(new LaserParameter("板岩", "LdT4-20W", 10000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("板岩", "LdT4-1064nm-2W", 10000, 100, "engraving", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 板岩 切割
//        laserParameters.add(new LaserParameter("板岩", "LdT-3W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("板岩", "LdT4-10W", 600, 100, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("板岩", "LdT4-20W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));
//        laserParameters.add(new LaserParameter("板岩", "LdT4-1064nm-2W", 300, 90, "cutting", 0.05f, ScanDirection.HORIZONTAL, false, 0));

        // 查找并设置推荐的功率和速度
        setRecommendedLaserParameters("胶合板(2mm)");

        // 雕刻次数
        tvParameterEngraveCount.setText(totalEngraveCount + "");

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

        // 加工类型
        llParameterEngraveOrCutting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterOperationModeBottomSheetFragment parameterOperationModeBottomSheetFragment = new ParameterOperationModeBottomSheetFragment();
                parameterOperationModeBottomSheetFragment.setListener(ParameterBottomSheetFragment.this);
                parameterOperationModeBottomSheetFragment.show(getParentFragmentManager(), selectedOperationMode);
            }
        });

        // 雕刻次数
        llParameterEngraveCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EngraveCountBottomSheetFragment engraveCountBottomSheetFragment = new EngraveCountBottomSheetFragment();
                engraveCountBottomSheetFragment.setOnEngraveCountSelectedListener(new EngraveCountBottomSheetFragment.OnEngraveCountSelectedListener() {
                    @Override
                    public void onEngraveCountSelected(String engraveCount) {
                        // 这里你可以接收选中的名称，并在 Fragment 中做处理
                        Log.d(TAG, "Selected EngraveCount=" + engraveCount);
                        // 设置雕刻次数
                        tvParameterEngraveCount.setText(engraveCount);

                        totalEngraveCount = Integer.valueOf(engraveCount);
                    }
                });
                engraveCountBottomSheetFragment.show(getParentFragmentManager(), "");

            }
        });

        // 扫描间隙
        llParameterScanningGap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanningGapBottomSheetFragment scanningGapBottomSheetFragment = new ScanningGapBottomSheetFragment();
                scanningGapBottomSheetFragment.setOnScanningGapSelectedListener(new ScanningGapBottomSheetFragment.OnScanningGapSelectedListener() {
                    @Override
                    public void onScanningGapSelected(String scanningGap) {
                        // 这里你可以接收选中的名称，并在 Fragment 中做处理
                        Log.d(TAG, "Selected scanningGap=" + scanningGap);
                        // 设置扫描间隙
                        tvParameterScanningGap.setText(scanningGap);
                    }
                });
                scanningGapBottomSheetFragment.show(getParentFragmentManager(), "");

            }
        });

        // 扫描方向
        llParameterScanningOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanningOrientationBottomSheetFragment scanningOrientationBottomSheetFragment = new ScanningOrientationBottomSheetFragment();
                scanningOrientationBottomSheetFragment.setOnScanningOrientationSelectedListener(new ScanningOrientationBottomSheetFragment.OnScanningOrientationSelectedListener() {
                    @Override
                    public void onScanningOrientationSelected(ScanDirection direction) {
                        // 这里你可以接收选中的名称，并在 Fragment 中做处理
                        Log.d(TAG, "Selected direction=" + direction);
                        // 设置扫描方向
                        tvParameterScanningOrientation.setText(getDirectionName(direction));
                    }
                });
                scanningOrientationBottomSheetFragment.show(getParentFragmentManager(), "");

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
                speedLevelBottomSheetFragment.show(getParentFragmentManager(), selectedOperationMode);
            }
        });


        // 确定
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取激光功率和速度
                int power = Integer.parseInt(tvParameterLaserLevel.getText().toString().replace("%", ""));
                int speed = Integer.parseInt(tvParameterSpeedLevel.getText().toString().replace("mm/min", ""));
                float gap = Float.parseFloat(tvParameterScanningGap.getText().toString());
                ScanDirection scanDirection = getScanDirectionFromText(tvParameterScanningOrientation.getText().toString());

                boolean isAir  = sharedPref.getBoolean(getString(R.string.preference_recommended_air), false);
                int zDown  = sharedPref.getInt(getString(R.string.preference_recommended_zdown), 0);

                Log.d(TAG, "targetIndex=" + targetIndex + "------power=" + power + "------speed=" + speed
                        + "------gap=" + gap + "------scanDirection=" + scanDirection
                        + "------totalEngraveCount=" + totalEngraveCount + "------isAir=" + isAir + "------zDown=" + zDown);

                // 通过接口回调传递数据给 Activity
                if (listener != null) {
                    listener.onLaserParametersSelected(targetIndex, power, speed, gap, scanDirection, totalEngraveCount, isAir, zDown);
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

        boolean isSupported = false;

        for (LaserParameter parameter : laserParameters) {
            // 找到对应的材料和激光模块
            if (parameter.getMaterialType().equals(materialName) && parameter.getLaserModel().equals(laserModule) && parameter.getOperationMode().equals(selectedOperationMode)) {
                // 设置推荐的功率和速度
                tvParameterLaserLevel.setText(parameter.getRecommendedPower() + "%");
                tvParameterSpeedLevel.setText(parameter.getRecommendedSpeed() + "mm/min");
                tvParameterScanningGap.setText(String.valueOf(parameter.getRecommendedGap()));

                // 设置扫描方向
                ScanDirection direction = parameter.getScanDirection();
                tvParameterScanningOrientation.setText(getDirectionName(direction));


                // TODO 保存功率和速度
                sharedPref.edit().putInt(getString(R.string.preference_recommended_power), parameter.getRecommendedPower()).apply();
                sharedPref.edit().putInt(getString(R.string.preference_recommended_speed), parameter.getRecommendedSpeed()).apply();
                sharedPref.edit().putFloat(getString(R.string.preference_recommended_scanning_gap), parameter.getRecommendedGap()).apply();
                sharedPref.edit().putInt(getString(R.string.preference_recommended_scanning_orientation), direction.ordinal()).apply();
                sharedPref.edit().putBoolean(getString(R.string.preference_recommended_air), parameter.isAir()).apply();
                sharedPref.edit().putInt(getString(R.string.preference_recommended_zdown), parameter.getzDown()).apply();

                isSupported = true;

                // 设置按钮可用
                tvConfirm.setEnabled(true);
                tvConfirm.setClickable(true);
                tvConfirm.setBackgroundResource(R.drawable.bg_green_1e853a_r100);

                break;
            }
        }

        // 如果没有找到匹配的参数，提示用户不支持
        if (!isSupported) {
            // 也可以弹出提示框或 Toast
            Toast.makeText(requireContext(), "当前材料或激光模组不支持", Toast.LENGTH_SHORT).show();

            // 设置按钮可用
            tvConfirm.setEnabled(false);
            tvConfirm.setClickable(false);
            tvConfirm.setBackgroundResource(R.drawable.bg_gray_999999_r30);

        }
    }


    @Override
    public void onOperationModeSelected(String operationMode) {
        Log.d(TAG, "operationMode=" + operationMode);
        selectedOperationMode = operationMode;
        if (operationMode.equals("engraving")) {
            tvParameterEngraveOrCutting.setText("雕刻");
            initData();
        } else if (operationMode.equals("cutting")) {
            tvParameterEngraveOrCutting.setText("切割");
            initData();
        }
    }

    private ScanDirection getScanDirectionFromText(String text) {
        switch (text) {
            case "横向":
                return ScanDirection.HORIZONTAL;
            case "纵向":
                return ScanDirection.VERTICAL;
            case "左上→右下":
                return ScanDirection.DIAGONAL_LD_RU;
            case "左下→右上":
                return ScanDirection.DIAGONAL_LU_RD;
            default:
                return ScanDirection.HORIZONTAL; // 默认兜底处理
        }
    }

    private String getDirectionName(ScanDirection direction) {
        switch (direction) {
            case HORIZONTAL:
                return "横向";
            case VERTICAL:
                return "纵向";
            case DIAGONAL_LD_RU:
                return "左上→右下";
            case DIAGONAL_LU_RD:
                return "左下→右上";
            default:
                return "未知";
        }
    }

    /**
     * MaterialSelectedEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMaterialSelectedEventEvent(MaterialSelectedEvent event) {
        if (!event.getMessage().isEmpty()) {
            String materialName = event.getMessage().toString();
            // 这里你可以接收选中的名称，并在 Fragment 中做处理
            Log.d(TAG, "Selected Material=" + materialName);
            // 设置材料类型
            tvMaterialName.setText(materialName);
            // 查找并设置推荐的功率和速度
            setRecommendedLaserParameters(materialName);
            //
            if (adapter != null) {
                adapter.setSelectedMaterial(materialName);

                materialRecyclerView.scrollToPosition(adapter.getSelectedPosition());
            }
        }
    }

}