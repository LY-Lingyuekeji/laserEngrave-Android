package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class ParameterOperationModeBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = ParameterOperationModeBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 加工类型
    private String operationMode;
    // RadioGroup
    private RadioGroup rgOperationMode;
    // RadioButton 雕刻
    private RadioButton rbEngrave;
    // RadioButton 切割
    private RadioButton rbCutting;

    private OnOperationModeSelectedListener listener;

    public void setListener(OnOperationModeSelectedListener listener) {
        this.listener = listener;
    }

    public ParameterOperationModeBottomSheetFragment() {
    }


    public static ParameterOperationModeBottomSheetFragment newInstance() {
        return new ParameterOperationModeBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_parameter_operation_mode_bottom_sheet, container, false);
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
        rgOperationMode = view.findViewById(R.id.rg_operation_mode);
        // RadioButton 雕刻
        rbEngrave = view.findViewById(R.id.rb_engrave);
        // RadioButton 切割
        rbCutting = view.findViewById(R.id.rb_cutting);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取传递的 tag
        String tag = getTag();
        if (!TextUtils.isEmpty(tag)) {
            operationMode = tag;
        }
        // 设置选中项
        if ("engraving".equals(operationMode)) {
            rbEngrave.setChecked(true);
        } else if ("cutting".equals(operationMode)) {
            rbCutting.setChecked(true);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        rgOperationMode.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedMode = (checkedId == R.id.rb_engrave) ? "engraving" : "cutting";

            // 调用接口回调，将选中的值传递出去
            if (listener != null) {
                listener.onOperationModeSelected(selectedMode);
            }
            dismiss();
        });

    }

    // 定义接口
    public interface OnOperationModeSelectedListener {
        void onOperationModeSelected(String operationMode);
    }

}