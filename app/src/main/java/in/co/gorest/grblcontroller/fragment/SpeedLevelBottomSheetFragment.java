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

import java.util.ArrayList;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class SpeedLevelBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = SpeedLevelBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 记录选中项的索引
    private int selectedPosition = -1;  // 默认没有选中任何项
    // 自定义雕刻速度
    private EditText etCustomizeSpeedLevel;
    // 确定
    private TextView tvConfirmCustomizeSpeedLevel;

    // ListView
    private ListView listView;
    // 选项
    private String[] options;

    // TAG 标签 用于区分切割还是雕刻
    private String tag;

    private OnSpeedLevelSelectedListener listener;

    public SpeedLevelBottomSheetFragment() {
    }

    public static SpeedLevelBottomSheetFragment newInstance() {
        return new SpeedLevelBottomSheetFragment();
    }

    public void setOnSpeedLevelSelectedListener(OnSpeedLevelSelectedListener listener) {
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
        return inflater.inflate(R.layout.fragment_speed_level_bottom_sheet, container, false);
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
        int savedSpeedLevel = sharedPref.getInt(getString(R.string.preference_recommended_speed), 1000);  // 默认为1000

        // 恢复上次选中的项
        selectedPosition = getIndexFromSpeed(savedSpeedLevel);

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
        listView = view.findViewById(R.id.lv_speed_level_options);
        // 自定义雕刻速度
        etCustomizeSpeedLevel = view.findViewById(R.id.et_customize_speed_level);
        // 确定
        tvConfirmCustomizeSpeedLevel = view.findViewById(R.id.tv_confirm_customize_speed_level);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取传递的 tag
        tag = getTag();
        if ("cutting".equals(tag)) {
            // 处理切割模式
            // 动态生成速度选项
            ArrayList<String> speedList = new ArrayList<>();
            for (int i = 10; i <= 100; i += 10) {
                speedList.add(i + "mm/min");
            }
            for (int i = 200; i <= 1000; i += 100) {
                speedList.add(i + "mm/min");
            }
            options = speedList.toArray(new String[0]);
        } else if ("engraving".equals(tag)) {
            // 处理雕刻模式
            // 动态生成速度选项
            ArrayList<String> speedList = new ArrayList<>();
            for (int i = 100; i <= 1000; i += 100) {
                speedList.add(i + "mm/min");
            }
            for (int i = 2000; i <= 30000; i += 1000) {
                speedList.add(i + "mm/min");
            }
            options = speedList.toArray(new String[0]);
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
        tvConfirmCustomizeSpeedLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CustomizeSpeedLevel=" + etCustomizeSpeedLevel.getText().toString());

                if (TextUtils.isEmpty(etCustomizeSpeedLevel.getText().toString())) {
                    Toast.makeText(requireContext(), "请先输入自定义的速度", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.valueOf(etCustomizeSpeedLevel.getText().toString()) < 10) {
                    Toast.makeText(requireContext(), "最小速度仅支持10mm/min", Toast.LENGTH_SHORT).show();
                    etCustomizeSpeedLevel.setText("10");
                    return;
                }

                if ("cutting".equals(tag)) {
                    if (Integer.valueOf(etCustomizeSpeedLevel.getText().toString()) > 1000) {
                        Toast.makeText(requireContext(), "最大速度仅支持1000mm/min", Toast.LENGTH_SHORT).show();
                        etCustomizeSpeedLevel.setText("1000");
                        return;
                    }
                } else if ("engraving".equals(tag)) {
                    if (Integer.valueOf(etCustomizeSpeedLevel.getText().toString()) > 30000) {
                        Toast.makeText(requireContext(), "最大速度仅支持30000mm/min", Toast.LENGTH_SHORT).show();
                        etCustomizeSpeedLevel.setText("30000");
                        return;
                    }
                }


                listener.OnSpeedLevelSelectedListener(etCustomizeSpeedLevel.getText().toString() + "mm/min");
                sharedPref.edit().putInt(getString(R.string.preference_recommended_speed), Integer.valueOf(etCustomizeSpeedLevel.getText().toString())).apply();
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
                Log.d(TAG, "Calling listener onSpeedLevelSelected");
                listener.OnSpeedLevelSelectedListener(options[position]);
                sharedPref.edit().putInt(getString(R.string.preference_recommended_speed), Integer.parseInt(options[position].toString().replace("mm/min", ""))).apply();
            }
            dismiss();  // 选择后关闭底部弹窗
        });
    }

    /**
     * 根据雕刻速度值确定选中的位置
     *
     * @param speedLevel 保存的雕刻速度
     * @return 索引位置
     */
    private int getIndexFromSpeed(int speedLevel) {
        if ("cutting".equals(tag)) {
            if (speedLevel >= 10 && speedLevel <= 100 && speedLevel % 10 == 0) {
                return (speedLevel / 10) - 1;
            } else if (speedLevel >= 200 && speedLevel <= 1000 && speedLevel % 100 == 0) {
                return 10 + ((speedLevel - 200) / 100);
            } else {
                return 0;  // 默认返回第一个项
            }
        } else if ("engraving".equals(tag)) {
            if (speedLevel >= 100 && speedLevel <= 1000 && speedLevel % 100 == 0) {
                return (speedLevel / 100) - 1;
            } else if (speedLevel >= 2000 && speedLevel <= 30000 && speedLevel % 1000 == 0) {
                return 10 + ((speedLevel - 2000) / 1000);
            } else {
                return 0;  // 默认返回第一个项
            }
        } else {
            return 0;
        }
    }


    public interface OnSpeedLevelSelectedListener {
        void OnSpeedLevelSelectedListener(String speedLevel);
    }

}