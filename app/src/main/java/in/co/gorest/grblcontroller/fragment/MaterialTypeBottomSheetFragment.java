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
import in.co.gorest.grblcontroller.events.LaserTypeUpdateMessageEvent;
import in.co.gorest.grblcontroller.events.MaterialTypeUpdateMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class MaterialTypeBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = MaterialTypeBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 材料类型 int
    private int materialType;
    // RadioGroup
    private RadioGroup rgMaterialType;
    // RadioButton 胶合板
    private RadioButton rbMaterialTypeJHB;
    // RadioButton 松木板
    private RadioButton rbMaterialTypeSMB;
    // RadioButton 铜木板
    private RadioButton rbMaterialTypeTMB;
    // RadioButton 纸板
    private RadioButton rbMaterialTypeZB;
    // RadioButton 纸张（非白纸）
    private RadioButton rbMaterialTypeZZFBZ;
    // RadioButton 牛皮纸
    private RadioButton rbMaterialTypeNPZ;

    public MaterialTypeBottomSheetFragment() {
    }

    public static MaterialTypeBottomSheetFragment newInstance() {
        return new MaterialTypeBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_material_type_bottom_sheet, container, false);
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
        rgMaterialType = view.findViewById(R.id.rg_material_type);
        // RadioButton 胶合板
        rbMaterialTypeJHB = view.findViewById(R.id.rb_material_type_jhb);
        // RadioButton 松木板
        rbMaterialTypeSMB = view.findViewById(R.id.rb_material_type_smb);
        // RadioButton 铜木板
        rbMaterialTypeTMB = view.findViewById(R.id.rb_material_type_tnb);
        // RadioButton 纸板
        rbMaterialTypeZB = view.findViewById(R.id.rb_material_type_zb);
        // RadioButton 纸张（非白纸）
        rbMaterialTypeZZFBZ = view.findViewById(R.id.rb_material_type_zzfbz);
        // RadioButton 牛皮纸
        rbMaterialTypeNPZ = view.findViewById(R.id.rb_material_type_npz);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        materialType = sharedPref.getInt(getString(R.string.preference_parameter_material_type), 1);
        // 设置选中项
        if (materialType == 1) {
            rbMaterialTypeJHB.setChecked(true);
        } else if (materialType == 2) {
            rbMaterialTypeSMB.setChecked(true);
        } else if (materialType == 3) {
            rbMaterialTypeTMB.setChecked(true);
        } else if (materialType == 4) {
            rbMaterialTypeZB.setChecked(true);
        } else if (materialType == 5) {
            rbMaterialTypeZZFBZ.setChecked(true);
        } else if (materialType == 6) {
            rbMaterialTypeNPZ.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgMaterialType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_material_type_jhb:
                        materialType = 1;
                        break;
                    case R.id.rb_material_type_smb:
                        materialType = 2;
                        break;
                    case R.id.rb_material_type_tnb:
                        materialType = 3;
                        break;
                    case R.id.rb_material_type_zb:
                        materialType = 4;
                        break;
                    case R.id.rb_material_type_zzfbz:
                        materialType = 5;
                        break;
                    case R.id.rb_material_type_npz:
                        materialType = 6;
                        break;
                }
                // 设置共享偏好设置保存的材料
                sharedPref.edit().putInt(getString(R.string.preference_parameter_material_type), materialType).apply();
                // 发送事件，传递选中的材料类型
                sendMaterialTypeEvent(materialType);
                // 隐藏弹窗
                dismiss();
            }
        });
    }

    /**
     * 发送选中的材料类型
      */
    private void sendMaterialTypeEvent(int materialType) {
        EventBus.getDefault().post(new MaterialTypeUpdateMessageEvent(materialType));
    }
}