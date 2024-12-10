/*
 *  /**
 *  * Copyright (C) 2017  Grbl Controller Contributors
 *  *
 *  * This program is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation; either version 2 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, write to the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  * <http://www.gnu.org/licenses/>
 *
 */

package in.co.gorest.grblcontroller.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joanzapata.iconify.widget.IconButton;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.CommandHistoryAdapter;
import in.co.gorest.grblcontroller.databinding.FragmentConsoleTabBinding;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.listeners.ConsoleLoggerListener;
import in.co.gorest.grblcontroller.listeners.EndlessRecyclerViewScrollListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.CommandHistory;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.GcodeCommand;
import in.co.gorest.grblcontroller.util.FileUploader;
import in.co.gorest.grblcontroller.util.GrblUtils;
import in.co.gorest.grblcontroller.util.TelnetHelper;

public class ConsoleTabFragment extends BaseFragment {

    private MachineStatusListener machineStatus;
    private ConsoleLoggerListener consoleLogger;
    private EnhancedSharedPreferences sharedPref;
    private ViewSwitcher viewSwitcher;
    private List<CommandHistory> dataSet;
    private CommandHistoryAdapter commandHistoryAdapter;
    private EditText commandInput;

    public ConsoleTabFragment() {}

    public static ConsoleTabFragment newInstance() {
        return new ConsoleTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = EnhancedSharedPreferences.getInstance(getActivity(), getString(R.string.shared_preference_key));
        consoleLogger = ConsoleLoggerListener.getInstance();
        machineStatus = MachineStatusListener.getInstance();
    }

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentConsoleTabBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_console_tab, container, false);
        View view = binding.getRoot();
        binding.setConsole(consoleLogger);
        binding.setMachineStatus(machineStatus);

        Log.d("ConsoleTabFragment", "state="+ machineStatus.getState());


        viewSwitcher = view.findViewById(R.id.console_view_switcher);
        final TextView consoleLogView = view.findViewById(R.id.console_logger);
        consoleLogView.setMovementMethod(new ScrollingMovementMethod());
        commandInput = view.findViewById(R.id.command_input);

        final EditText commandInput = view.findViewById(R.id.command_input);

        consoleLogView.setOnTouchListener((v, event) -> {
            v.getParent().getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                v.getParent().getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        IconButton sendCommand = view.findViewById(R.id.send_command);
        sendCommand.setOnClickListener(view12 -> {
            String commandText = commandInput.getText().toString();
            if(commandText.length() > 0){
                GcodeCommand gcodeCommand = new GcodeCommand(commandText);
                fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand.getCommandString());
                CommandHistory.saveToHistory(commandText, gcodeCommand.getCommandString());
                if(gcodeCommand.getHasRomAccess()){
                    fragmentInteractionListener.onGcodeCommandReceived(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND);
                    fragmentInteractionListener.onGcodeCommandReceived(GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND);
                }

                if(gcodeCommand.getCommandString().toUpperCase().contains("G43.1Z")){
                    fragmentInteractionListener.onGcodeCommandReceived(GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND);
                }

                if(gcodeCommand.getCommandString().equals("$32=1")) machineStatus.setLaserModeEnabled(true);
                if(gcodeCommand.getCommandString().equals("$32=0")) machineStatus.setLaserModeEnabled(false);
                commandInput.setText(null);
                viewSwitcher.setDisplayedChild(0);
            }
        });
        sendCommand.setOnLongClickListener(view1 -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.text_clear_console))
                    .setMessage(getString(R.string.text_clear_console_description))
                    .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> consoleLogger.clearMessages())
                    .setNegativeButton(getString(R.string.text_no_confirm), null)
                    .show();

            return true;
        });

        // $$配置
        IconButton sendCommandConfig = view.findViewById(R.id.send_command_config);
        sendCommandConfig.setOnClickListener(view12 -> {
            GcodeCommand gcodeCommandConfig = new GcodeCommand("$$");
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandConfig.getCommandString());
            CommandHistory.saveToHistory("$$", gcodeCommandConfig.getCommandString());
        });

        // $#参数
        IconButton sendCommandParam = view.findViewById(R.id.send_command_param);
        sendCommandParam.setOnClickListener(view12 -> {
            GcodeCommand gcodeCommandParam = new GcodeCommand("$#");
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandParam.getCommandString());
            CommandHistory.saveToHistory("$#", gcodeCommandParam.getCommandString());
        });

        // $G状态
        IconButton sendCommandState = view.findViewById(R.id.send_command_state);
        sendCommandState.setOnClickListener(view12 -> {
            GcodeCommand gcodeCommandState = new GcodeCommand("$G");
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandState.getCommandString());
            CommandHistory.saveToHistory("$G", gcodeCommandState.getCommandString());
        });

        // $I版本
        IconButton sendCommandVersion = view.findViewById(R.id.send_command_version);
        sendCommandVersion.setOnClickListener(view12 -> {
            GcodeCommand gcodeCommandVersion = new GcodeCommand("$I");
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandVersion.getCommandString());
            CommandHistory.saveToHistory("$I", gcodeCommandVersion.getCommandString());
        });

        // $+参数
        IconButton sendCommandHConfig = view.findViewById(R.id.send_command_h_config);
        sendCommandHConfig.setOnClickListener(view12 -> {
            GcodeCommand gcodeCommandHConfig = new GcodeCommand("$+");
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandHConfig.getCommandString());
            CommandHistory.saveToHistory("$+", gcodeCommandHConfig.getCommandString());
        });

        // 恢复出厂设置
        IconButton sendCommandReset = view.findViewById(R.id.send_command_reset);
        sendCommandReset.setOnClickListener(view12 -> {
            GcodeCommand gcodeCommandReset = new GcodeCommand("$RST=*");
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandReset.getCommandString());
            CommandHistory.saveToHistory("$RST=*+", gcodeCommandReset.getCommandString());

        });

        // 蓝牙模式
        IconButton sendCommandUploadFile = view.findViewById(R.id.send_command_bt);
        sendCommandUploadFile.setOnClickListener(view12 -> {
            String connectType = sharedPref.getString(getString(R.string.connect_type), "AP");
            if ("BT".equals(connectType)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("当前已经是蓝牙模式")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("点击OK后，模式即将切换为蓝牙模式，请在5秒后重启机器，避免错误发生，如有需要请重启APP")
                        .setPositiveButton("OK", (dialog, which) -> {
                            GcodeCommand gcodeCommandBTModel = new GcodeCommand("$Radio/Mode=BT");
                            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandBTModel.getCommandString());
                            CommandHistory.saveToHistory("$Radio/Mode=BT", gcodeCommandBTModel.getCommandString());
                            dialog.dismiss();
                        }).show();

            }

        });

        // 离线雕刻
        IconButton sendCommandSDRun = view.findViewById(R.id.send_command_ap);
        sendCommandSDRun.setOnClickListener(view12 -> {
            String connectType = sharedPref.getString(getString(R.string.connect_type), "AP");
            if ("AP".equals(connectType)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("当前已经是WIFI模式")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("点击OK后，模式即将切换为AP模式，请在5秒后重启机器，避免错误发生，如有需要请重启APP")
                        .setPositiveButton("OK", (dialog, which) -> {
                            GcodeCommand gcodeCommandAPModel = new GcodeCommand("$Radio/Mode=AP");
                            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommandAPModel.getCommandString());
                            CommandHistory.saveToHistory("$Radio/Mode=AP", gcodeCommandAPModel.getCommandString());
                            dialog.dismiss();
                        }).show();


            }
        });

        final SwitchCompat consoleVerboseOutput = view.findViewById(R.id.console_verbose_output);
        consoleVerboseOutput.setChecked(sharedPref.getBoolean(getString(R.string.preference_console_verbose_mode), false));
        consoleVerboseOutput.setOnCheckedChangeListener((compoundButton, b) -> {
            MachineStatusListener.getInstance().setVerboseOutput(b);
            sharedPref.edit().putBoolean(getString(R.string.preference_console_verbose_mode), b).apply();
        });

        IconButton consoleHistory = view.findViewById(R.id.console_history);
        consoleHistory.setOnClickListener(v -> viewSwitcher.showNext());

        dataSet = CommandHistory.getHistory("0", "15");
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        commandHistoryAdapter = new CommandHistoryAdapter(dataSet);
        commandHistoryAdapter.setItemClickListener(onItemClickListener);
        commandHistoryAdapter.setItemLongClickListener(onItemLongClickListener);
        recyclerView.setAdapter(commandHistoryAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                String offset = String.valueOf(page * 15);
                List<CommandHistory> moreItems = CommandHistory.getHistory(offset, "15");
                dataSet.addAll(moreItems);
                commandHistoryAdapter.notifyItemRangeInserted(commandHistoryAdapter.getItemCount(), dataSet.size() - 1);
            }
        });

        return view;
    }

    private final View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAbsoluteAdapterPosition();
            if(position == RecyclerView.NO_POSITION) return;
            CommandHistory commandHistory = dataSet.get(position);
            commandInput.append(commandHistory.getCommand());
        }
    };

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
