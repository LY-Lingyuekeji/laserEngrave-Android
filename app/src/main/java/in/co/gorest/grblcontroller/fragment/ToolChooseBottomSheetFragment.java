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
import in.co.gorest.grblcontroller.events.StepSetupEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class ToolChooseBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = ToolChooseBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 工具
    private String strToolName;
    // RadioGroup
    private RadioGroup rgTool;
    // RadioButton 画笔
    private RadioButton rbToolPen;
    // RadioButton 直线
    private RadioButton rbToolLine;
    // RadioButton 三角形
    private RadioButton rbToolTriangle;
    // RadioButton 矩形
    private RadioButton rbToolRectangle;
    // RadioButton 圆形
    private RadioButton rbToolCircle;
    // RadioButton 橡皮擦
    private RadioButton rbToolEraser;

    private OnToolSelectedListener mListener;


    public ToolChooseBottomSheetFragment() {
    }


    public static ToolChooseBottomSheetFragment newInstance() {
        return new ToolChooseBottomSheetFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnToolSelectedListener) {
            mListener = (OnToolSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnToolSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        return inflater.inflate(R.layout.fragment_tool_choose_bottom_sheet, container, false);
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
        rgTool = view.findViewById(R.id.rg_tool);
        // RadioButton 画笔
        rbToolPen = view.findViewById(R.id.rb_tool_pen);
        // RadioButton 直线
        rbToolLine = view.findViewById(R.id.rb_tool_line);
        // RadioButton 三角形
        rbToolTriangle = view.findViewById(R.id.rb_tool_triangle);
        // RadioButton 矩形
        rbToolRectangle = view.findViewById(R.id.rb_tool_rectangle);
        // RadioButton 圆形
        rbToolCircle = view.findViewById(R.id.rb_tool_circle);
        // RadioButton 橡皮擦
        rbToolEraser = view.findViewById(R.id.rb_tool_eraser);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例
        strToolName = sharedPref.getString(getString(R.string.preference_draw_board_tool_name), "pen");
        // 设置选中项
        if ("pen".equals(strToolName)) {
            rbToolPen.setChecked(true);
        } else if ("line".equals(strToolName)) {
            rbToolLine.setChecked(true);
        } else if ("triangle".equals(strToolName)) {
            rbToolTriangle.setChecked(true);
        } else if ("rectangle".equals(strToolName)) {
            rbToolRectangle.setChecked(true);
        } else if ("circle".equals(strToolName)) {
            rbToolCircle.setChecked(true);
        } else if ("eraser".equals(strToolName)) {
            rbToolEraser.setChecked(true);
        } else {
            rbToolPen.setChecked(false);
            rbToolLine.setChecked(false);
            rbToolTriangle.setChecked(false);
            rbToolRectangle.setChecked(false);
            rbToolCircle.setChecked(false);
            rbToolEraser.setChecked(false);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // RadioGroup
        rgTool.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据被选中的 RadioButton 的 ID 执行相应操作
                switch (checkedId) {
                    case R.id.rb_tool_pen:
                        // 设置共享偏好设置保存的工具实例为 画笔
                        sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "pen").apply();
                        // 设置选中
                        rbToolPen.setChecked(true);
                        // 传递toolName给Activity
                        mListener.onToolSelected("pen");
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_tool_line:
                        // 设置共享偏好设置保存的工具实例为 直线
                        sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "line").apply();
                        // 设置选中
                        rbToolLine.setChecked(true);
                        // 传递toolName给Activity
                        mListener.onToolSelected("line");
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_tool_triangle:
                        // 设置共享偏好设置保存的工具实例为 三角形
                        sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "triangle").apply();
                        // 设置选中
                        rbToolTriangle.setChecked(true);
                        // 传递toolName给Activity
                        mListener.onToolSelected("triangle");
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_tool_rectangle:
                        // 设置共享偏好设置保存的工具实例为 矩形
                        sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "rectangle").apply();
                        // 设置选中
                        rbToolRectangle.setChecked(true);
                        // 传递toolName给Activity
                        mListener.onToolSelected("rectangle");
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_tool_circle:
                        // 设置共享偏好设置保存的工具实例为 圆形
                        sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "circle").apply();
                        // 设置选中
                        rbToolCircle.setChecked(true);
                        // 传递toolName给Activity
                        mListener.onToolSelected("circle");
                        // 隐藏弹窗
                        dismiss();
                        break;
                    case R.id.rb_tool_eraser:
                        // 设置共享偏好设置保存的工具实例为 圆形
                        sharedPref.edit().putString(getString(R.string.preference_draw_board_tool_name), "eraser").apply();
                        // 设置选中
                        rbToolEraser.setChecked(true);
                        // 传递toolName给Activity
                        mListener.onToolSelected("eraser");
                        // 隐藏弹窗
                        dismiss();
                        break;
                }
            }
        });
    }

    public interface OnToolSelectedListener {
        void onToolSelected(String tool);
    }

}