package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class LaserLevelBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = LaserLevelBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 记录选中项的索引
    private int selectedPosition = -1;  // 默认没有选中任何项
    // 自定义激光功率
    private EditText etCustomizeLaserPower;
    // 确定
    private TextView tvConfirmCustomizeLaserPower;
    // ListView
    private ListView listView;
    // 选项
    private String[] options = new String[100];

    private OnLaserPowerSelectedListener listener;

    public LaserLevelBottomSheetFragment() {
    }

    public static LaserLevelBottomSheetFragment newInstance() {
        return new LaserLevelBottomSheetFragment();
    }

    public void setOnLaserPowerSelectedListener(OnLaserPowerSelectedListener listener) {
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
        return inflater.inflate(R.layout.fragment_laser_level_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 禁用 BottomSheetDialogFragment 的滑动
        if (getDialog() != null && getDialog().getWindow() != null) {
            View bottomSheet = getDialog().getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setDraggable(false); // 禁止底部弹窗滑动
        }

        // 获取保存的激光功率（假设保存的是一个百分比值，范围从 0 到 100）
        int savedLaserLevel = sharedPref.getInt(getString(R.string.preference_recommended_power), 10);  // 默认为10%

        // 恢复上次选中的项
        selectedPosition = getIndexFromPower(savedLaserLevel);

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
        listView = view.findViewById(R.id.lv_laser_power_options);
        // 自定义激光功率
        etCustomizeLaserPower = view.findViewById(R.id.et_customize_laser_power);
        // 确定
        tvConfirmCustomizeLaserPower = view.findViewById(R.id.tv_confirm_customize_laser_power);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 动态生成options数组
        for (int i = 0; i < 100; i++) {
            options[i] = String.valueOf(i + 1) + "%";
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
        tvConfirmCustomizeLaserPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CustomizeLaserPower=" + etCustomizeLaserPower.getText().toString());

                if (TextUtils.isEmpty(etCustomizeLaserPower.getText().toString())) {
                    Toast.makeText(requireContext(), "请先输入要自定义的激光功率", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.valueOf(etCustomizeLaserPower.getText().toString()) < 1) {
                    Toast.makeText(requireContext(), "最小功率仅支持1%", Toast.LENGTH_SHORT).show();
                    etCustomizeLaserPower.setText("1");
                    return;
                }

                if (Integer.valueOf(etCustomizeLaserPower.getText().toString()) > 100) {
                    Toast.makeText(requireContext(), "最大功率仅支持100%", Toast.LENGTH_SHORT).show();
                    etCustomizeLaserPower.setText("100");
                    return;
                }

                listener.onLaserPowerSelected(etCustomizeLaserPower.getText().toString() + "%");
                sharedPref.edit().putInt(getString(R.string.preference_recommended_power), Integer.valueOf(etCustomizeLaserPower.getText().toString())).apply();
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
                Log.d(TAG, "Calling listener onLaserPowerSelected");
                listener.onLaserPowerSelected(options[position]);
                sharedPref.edit().putInt(getString(R.string.preference_recommended_power), Integer.valueOf(options[position].toString().replace("%", ""))).apply();
            }
            dismiss();  // 选择后关闭底部弹窗
        });
    }


    /**
     * 根据激光功率值确定选中的位置
     *
     * @param laserLevel 保存的激光功率
     * @return 索引位置
     */
    private int getIndexFromPower(int laserLevel) {

        if (laserLevel >= 1 && laserLevel <= 100) {
            return laserLevel - 1; // 数组从0开始
        }
        return -1;
    }

    public interface OnLaserPowerSelectedListener {
        void onLaserPowerSelected(String laserPower);
    }
}