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

public class EngraveCountBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = EngraveCountBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 记录选中项的索引
    private int selectedPosition = -1;  // 默认没有选中任何项
    // 自定义激光功率
    private EditText etCustomizeEngraveCount;
    // 确定
    private TextView tvConfirmCustomizeEngraveCount;
    // ListView
    private ListView listView;
    // 选项
    private String[] options = new String[100];

    private OnEngraveCountSelectedListener listener;

    public EngraveCountBottomSheetFragment() {
    }

    public static EngraveCountBottomSheetFragment newInstance() {
        return new EngraveCountBottomSheetFragment();
    }

    public void setOnEngraveCountSelectedListener(OnEngraveCountSelectedListener listener) {
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
        return inflater.inflate(R.layout.fragment_engrave_count_bottom_sheet, container, false);
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
        int savedEngraveCount = sharedPref.getInt(getString(R.string.preference_recommended_engrave_count), 1);  // 默认为10%

        // 恢复上次选中的项
        selectedPosition = getIndexFromEngraveCount(savedEngraveCount);

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
        listView = view.findViewById(R.id.lv_engrave_count_options);
        // 自定义雕刻次数
        etCustomizeEngraveCount = view.findViewById(R.id.et_customize_engrave_count);
        // 确定
        tvConfirmCustomizeEngraveCount = view.findViewById(R.id.tv_confirm_customize_engrave_count);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 动态生成options数组
        for (int i = 0; i < 100; i++) {
            options[i] = String.valueOf(i + 1);
        }

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
        tvConfirmCustomizeEngraveCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CustomizeEngraveCount=" + etCustomizeEngraveCount.getText().toString());

                if (TextUtils.isEmpty(etCustomizeEngraveCount.getText().toString())) {
                    Toast.makeText(requireContext(), "请先输入要自定义的雕刻次数", Toast.LENGTH_SHORT).show();
                    return;
                }

                int inputEngraveCount = Integer.valueOf(etCustomizeEngraveCount.getText().toString());

                if (inputEngraveCount <= 0) {
                    Toast.makeText(requireContext(), "请输入非0的雕刻次数", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (inputEngraveCount > 100) {
                    Toast.makeText(requireContext(), "最大雕刻次数仅支持100", Toast.LENGTH_SHORT).show();
                    etCustomizeEngraveCount.setText("100");
                    return;
                }

                listener.onEngraveCountSelected(inputEngraveCount + "");
                sharedPref.edit().putInt(getString(R.string.preference_recommended_engrave_count), inputEngraveCount).apply();
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
                listener.onEngraveCountSelected(options[position]);
                sharedPref.edit().putInt(getString(R.string.preference_recommended_engrave_count), Integer.valueOf(options[position])).apply();
            }
            dismiss();  // 选择后关闭底部弹窗
        });
    }


    /**
     * 根据雕刻次数值确定选中的位置
     *
     * @param engraveCount 雕刻次数
     * @return 索引位置
     */
    private int getIndexFromEngraveCount(int engraveCount) {
        if (engraveCount >= 1 && engraveCount <= 100) {
            return engraveCount - 1; // 数组从0开始
        }
        return -1;
    }

    public interface OnEngraveCountSelectedListener {
        void onEngraveCountSelected(String engraveCount);
    }
}