package in.co.gorest.grblcontroller.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.CommandHistoryAdapter;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.databinding.FragmentCommandBottomSheetBinding;
import in.co.gorest.grblcontroller.events.FragmentCommandEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.listeners.ConsoleLoggerListener;
import in.co.gorest.grblcontroller.listeners.EndlessRecyclerViewScrollListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.CommandHistory;
import in.co.gorest.grblcontroller.model.GcodeCommand;
import in.co.gorest.grblcontroller.ui.BaseFragment;
import in.co.gorest.grblcontroller.util.DataCleanManager;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class CommandBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于管理和访问增强的共享偏好设置实例
    private EnhancedSharedPreferences sharedPref;
    // 用于监听和管理机器状态的监听器
    private MachineStatusListener machineStatus;
    // 用于记录控制台日志的监听器
    private ConsoleLoggerListener consoleLogger;
    // ViewSwitcher
    private ViewSwitcher viewSwitcher;
    // consoleLogView
    private TextView consoleLogView;
    // 开关 verboseOutputSwitch
    private Switch verboseOutputSwitch;
    // G-code输入框
    private EditText etCommandInput;
    // 发送
    private RelativeLayout rlSendCommand;
    // 历史记录
    private RelativeLayout rlConsoleHistory;
    // $$配置
    private TextView tvCommandConfig;
    // $#参数
    private TextView tvCommandParam;
    // $G状态
    private TextView tvCommandState;
    // $I版本
    private TextView tvCommandVersion;
    // RecyclerView
    private RecyclerView recyclerView;
    // 历史数据源
    private List<CommandHistory> dataSet;
    // 历史数据 Adapter
    private CommandHistoryAdapter commandHistoryAdapter;

    public CommandBottomSheetFragment() {
    }

    public static CommandBottomSheetFragment newInstance() {
        return new CommandBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = EnhancedSharedPreferences.getInstance(getActivity(), getString(R.string.shared_preference_key));
        consoleLogger = ConsoleLoggerListener.getInstance();
        machineStatus = MachineStatusListener.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentCommandBottomSheetBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_command_bottom_sheet, container, false);
        View view = binding.getRoot();
        binding.setConsole(consoleLogger);
        binding.setMachineStatus(machineStatus);
        return view;
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
        // ViewSwitcher
        viewSwitcher = view.findViewById(R.id.view_switcher);
        // consoleLogView
        consoleLogView = view.findViewById(R.id.tv_logger);
        // 开关 verboseOutputSwitch
        verboseOutputSwitch = view.findViewById(R.id.verbose_output_switch);
        // G-code输入框
        etCommandInput = view.findViewById(R.id.et_command_input);
        // 发送
        rlSendCommand = view.findViewById(R.id.rl_send_command);
        // 历史记录
        rlConsoleHistory = view.findViewById(R.id.rl_console_history);
        // $$配置
        tvCommandConfig = view.findViewById(R.id.tv_command_config);
        // $#参数
        tvCommandParam = view.findViewById(R.id.tv_command_param);
        // $G状态
        tvCommandState = view.findViewById(R.id.tv_command_state);
        // $I版本
        tvCommandVersion = view.findViewById(R.id.tv_command_version);
        // RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 开关 verboseOutputSwitch
        verboseOutputSwitch.setChecked(sharedPref.getBoolean(getString(R.string.preference_console_verbose_mode), false));
        // 历史数据
        dataSet = CommandHistory.getHistory("0", "15");
        commandHistoryAdapter = new CommandHistoryAdapter(dataSet);
        commandHistoryAdapter.setItemClickListener(onItemClickListener);
        commandHistoryAdapter.setItemLongClickListener(onItemLongClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(commandHistoryAdapter);
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        //  开关 verboseOutputSwitch
        verboseOutputSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            MachineStatusListener.getInstance().setVerboseOutput(b);
            sharedPref.edit().putBoolean(getString(R.string.preference_console_verbose_mode), b).apply();
        });

        // consoleLogView
        consoleLogView.setOnTouchListener((v, event) -> {
            v.getParent().getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                v.getParent().getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        // 发送
        rlSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commandText = etCommandInput.getText().toString();
                if (commandText.length() > 0) {
                    GcodeCommand gcodeCommand = new GcodeCommand(commandText);
                    EventBus.getDefault().post(new FragmentCommandEvent(gcodeCommand.getCommandString()));
                    CommandHistory.saveToHistory(commandText, gcodeCommand.getCommandString());

                    if (gcodeCommand.getHasRomAccess()) {
                        EventBus.getDefault().post(new FragmentCommandEvent(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND));
                        EventBus.getDefault().post(new FragmentCommandEvent(GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND));
                    }

                    if (gcodeCommand.getCommandString().toUpperCase().contains("G43.1Z")) {
                        EventBus.getDefault().post(new FragmentCommandEvent(GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND));
                    }

                    if (gcodeCommand.getCommandString().equals("$32=1"))
                        machineStatus.setLaserModeEnabled(true);
                    if (gcodeCommand.getCommandString().equals("$32=0"))
                        machineStatus.setLaserModeEnabled(false);
                    etCommandInput.setText(null);
                    viewSwitcher.setDisplayedChild(0);
                }
            }
        });

        // 发送 长按
        rlSendCommand.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BaseDialog.showCustomDialog(getContext(), "温馨提示", "确认清除命令消息？"
                        , "确定", "取消",
                        v1 -> {
                            consoleLogger.clearMessages();
                        },
                        v1 -> {

                        });
                return true;
            }
        });

        // 历史记录
        rlConsoleHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewSwitcher.showNext();
            }
        });

        // $$配置
        tvCommandConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GcodeCommand gcodeCommand = new GcodeCommand("$$");
                EventBus.getDefault().post(new FragmentCommandEvent(gcodeCommand.getCommandString()));
                CommandHistory.saveToHistory("$$", gcodeCommand.getCommandString());
            }
        });

        // $#参数
        tvCommandParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GcodeCommand gcodeCommand = new GcodeCommand("$#");
                EventBus.getDefault().post(new FragmentCommandEvent(gcodeCommand.getCommandString()));
                CommandHistory.saveToHistory("$#", gcodeCommand.getCommandString());
            }
        });

        // $G状态
        tvCommandState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GcodeCommand gcodeCommand = new GcodeCommand("$G");
                EventBus.getDefault().post(new FragmentCommandEvent(gcodeCommand.getCommandString()));
                CommandHistory.saveToHistory("$G", gcodeCommand.getCommandString());
            }
        });

        // $I版本
        tvCommandVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GcodeCommand gcodeCommand = new GcodeCommand("$I");
                EventBus.getDefault().post(new FragmentCommandEvent(gcodeCommand.getCommandString()));
                CommandHistory.saveToHistory("$I", gcodeCommand.getCommandString());
            }
        });

        // RecycleView
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(new LinearLayoutManager(getContext())) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                String offset = String.valueOf(page * 15);
                List<CommandHistory> moreItems = CommandHistory.getHistory(offset, "15");
                dataSet.addAll(moreItems);
                commandHistoryAdapter.notifyItemRangeInserted(commandHistoryAdapter.getItemCount(), dataSet.size() - 1);
            }
        });
    }

    /**
     * item项点击
     */
    private final View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAbsoluteAdapterPosition();
            if(position == RecyclerView.NO_POSITION) return;
            CommandHistory commandHistory = dataSet.get(position);
            etCommandInput.append(commandHistory.getCommand());
        }
    };

    /**
     * 长按
     */
    private final View.OnLongClickListener onItemLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            final RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            final int position = viewHolder.getAbsoluteAdapterPosition();
            if(position == RecyclerView.NO_POSITION) return false;
            final CommandHistory commandHistory = dataSet.get(position);

            new AlertDialog.Builder(getActivity())
                    .setTitle(commandHistory.getCommand())
                    .setMessage(getString(R.string.text_delete_command_history_confirm))
                    .setPositiveButton(requireActivity().getString(R.string.text_yes_confirm), (dialog, which) -> {
                        commandHistory.delete();
                        dataSet.remove(position);
                        commandHistoryAdapter.notifyItemRemoved(position);
                        commandHistoryAdapter.notifyItemRangeChanged(position, dataSet.size());
                    }).setNegativeButton(requireActivity().getString(R.string.text_cancel), null).setCancelable(true).show();

            return true;
        }
    };

}