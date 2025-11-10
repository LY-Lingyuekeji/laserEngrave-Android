package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class ScanningGapBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = ScanningGapBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 记录选中项的索引
    private int selectedPosition = -1;  // 默认没有选中任何项
    // 自定义激光功率
    private EditText etCustomizeScanningGap;
    // 确定
    private TextView tvConfirmCustomizeScanningGap;
    // ListView
    private ListView listView;
    // 选项
    private String[] options = new String[]{"0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.1"};

    private OnScanningGapSelectedListener listener;

    public ScanningGapBottomSheetFragment() {
    }

    public static ScanningGapBottomSheetFragment newInstance() {
        return new ScanningGapBottomSheetFragment();
    }

    public void setOnScanningGapSelectedListener(OnScanningGapSelectedListener listener) {
        this.listener = listener;
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
        return inflater.inflate(R.layout.fragment_scanning_gap_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 禁用 BottomSheetDialogFragment 的滑动
        if (getDialog() != null && getDialog().getWindow() != null) {
            View bottomSheet = getDialog().getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setDraggable(false); // 禁止底部弹窗滑动
        }

        // 获取保存的扫描间隙
        float savedLaserLevel = sharedPref.getFloat(getString(R.string.preference_recommended_scanning_gap), 0.05f);  // 默认为10%

        // 恢复上次选中的项
        selectedPosition = getIndexFromGap(savedLaserLevel);

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
        // ListView
        listView = view.findViewById(R.id.lv_scanning_gap_options);
        // 自定义扫描间隙
        etCustomizeScanningGap = view.findViewById(R.id.et_customize_scanning_gap);
        // 确定
        tvConfirmCustomizeScanningGap = view.findViewById(R.id.tv_confirm_customize_scanning_gap);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setGravity(Gravity.CENTER);  // 文字居中

                // 根据选中状态设置背景
                if (position == selectedPosition) {  // 判断当前项是否被选中
                    view.setBackgroundResource(R.drawable.bg_green_401e853a_r10);  // 选中背景
                } else {
                    view.setBackgroundResource(R.drawable.bg_white_ffffff_r10);  // 未选中背景
                }

                return view;
            }
        };
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // 自动定位到选中的项
        if (selectedPosition != -1) {
            listView.setSelection(selectedPosition);  // 滚动到选中的位置
        }

        // 确定
        tvConfirmCustomizeScanningGap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CustomizeScanning=" + etCustomizeScanningGap.getText().toString());

                if (TextUtils.isEmpty(etCustomizeScanningGap.getText().toString())) {
                    Toast.makeText(requireContext(), "请先输入要自定义的扫描间隙", Toast.LENGTH_SHORT).show();
                    return;
                }

                float inputGap = Float.parseFloat(etCustomizeScanningGap.getText().toString());

                if (inputGap <= 0) {
                    Toast.makeText(requireContext(), "请输入非0的扫描间隙", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (inputGap > 1.0f) {
                    Toast.makeText(requireContext(), "最大扫描间隙仅支持1", Toast.LENGTH_SHORT).show();
                    etCustomizeScanningGap.setText("1.0");
                    return;
                }

                listener.onScanningGapSelected(inputGap + "");
                sharedPref.edit().putFloat(getString(R.string.preference_recommended_scanning_gap), inputGap).apply();
                dismiss();
            }
        });
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 设置点击事件来更改选中项
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "Item clicked: " + options[position]); // 确认点击了哪个项
            selectedPosition = position;  // 更新选中项的索引
            ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();  // 更新视图

            if (listener != null) {
                Log.d(TAG, "Calling listener onScanningGapSelected");
                listener.onScanningGapSelected(options[position]);
                sharedPref.edit().putFloat(getString(R.string.preference_recommended_scanning_gap), Float.parseFloat(options[position])).apply();
            }
            dismiss();  // 选择后关闭底部弹窗
        });
    }


    /**
     * 根据激光功率值确定选中的位置
     *
     * @param gap 间隙
     * @return 索引位置
     */
    private int getIndexFromGap(float  gap) {

        if (gap <= 0.01f) return 0;
        else if (gap <= 0.02f) return 1;
        else if (gap <= 0.03f) return 2;
        else if (gap <= 0.04f) return 3;
        else if (gap <= 0.05f) return 4;
        else if (gap <= 0.06f) return 5;
        else if (gap <= 0.07f) return 6;
        else if (gap <= 0.08f) return 7;
        else if (gap <= 0.09f) return 8;
        else return 9;
    }

    public interface OnScanningGapSelectedListener {
        void onScanningGapSelected(String scanningGap);
    }
}