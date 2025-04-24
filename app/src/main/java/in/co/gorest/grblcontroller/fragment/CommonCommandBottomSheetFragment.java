package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.greenrobot.eventbus.EventBus;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.CommonCommandValueMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class CommonCommandBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = CommonCommandBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 指令名称
    private TextView tvCommonCommandName;
    // 指令内容
    private EditText etCommonCommandContent;
    // 发送
    private TextView tvCommonCommandSend;
    // 返回
    private TextView tvCommonCommandBack;

    // 传递的tag
    private String tag;
    // 指令内容文本
    private String common_command;

    public CommonCommandBottomSheetFragment() {
    }


    public static CommonCommandBottomSheetFragment newInstance() {
        return new CommonCommandBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_common_command_bottom_sheet, container, false);
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
        // 指令名称
        tvCommonCommandName = view.findViewById(R.id.tv_common_command_name);
        // 指令内容
        etCommonCommandContent = view.findViewById(R.id.et_common_command_content);
        // 发送
        tvCommonCommandSend = view.findViewById(R.id.tv_common_command_send);
        // 返回
        tvCommonCommandBack = view.findViewById(R.id.tv_common_command_back);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取传递的tag
        tag = getTag();
        Log.d(TAG, "Tag: " + tag);
        // 根据tag设置内容
        if (tag.equals("common_command_one")) {
            // 设置标题
            tvCommonCommandName.setText("指令1");
            // 获取保存的指令实例，并设置指令内容填充到输入框
            common_command = sharedPref.getString(getString(R.string.ll_cnc_funcations_common_command_one_content), "");
            Log.d(TAG, "common_command=" + common_command);
            etCommonCommandContent.setText(common_command);
        } else if (tag.equals("common_command_two")) {
            // 设置标题
            tvCommonCommandName.setText("指令2");
            // 获取保存的指令实例，并设置指令内容填充到输入框
            common_command = sharedPref.getString(getString(R.string.ll_cnc_funcations_common_command_two_content), "");
            Log.d(TAG, "common_command=" + common_command);
            etCommonCommandContent.setText(common_command);
        }
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {

        // 指令内容
        etCommonCommandContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "指令内容变更：" + s.toString());
                if (tag.equals("common_command_one")) {
                    sharedPref.edit().putString(getString(R.string.ll_cnc_funcations_common_command_one_content), s.toString()).apply();
                } else if (tag.equals("common_command_two")) {
                    sharedPref.edit().putString(getString(R.string.ll_cnc_funcations_common_command_two_content), s.toString()).apply();
                }
            }
        });

        // 发送
        tvCommonCommandSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 消息传递
                EventBus.getDefault().post(new CommonCommandValueMessageEvent(etCommonCommandContent.getText().toString()));
            }
        });

        // 返回
        tvCommonCommandBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏弹窗
                dismiss();
            }
        });
    }

}