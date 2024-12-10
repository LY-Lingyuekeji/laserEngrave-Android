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
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import com.joanzapata.iconify.widget.IconButton;
import com.joanzapata.iconify.widget.IconTextView;
import com.joanzapata.iconify.widget.IconToggleButton;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.databinding.FragmentJoggingTabBinding;
import in.co.gorest.grblcontroller.events.GrblOkEvent;
import in.co.gorest.grblcontroller.events.JogCommandEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.helpers.RepeatListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.CommandHistory;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.GcodeCommand;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class JoggingTabFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = JoggingTabFragment.class.getSimpleName();
    private MachineStatusListener machineStatus;
    private EnhancedSharedPreferences sharedPref;
    private BlockingQueue<Integer> completedCommands;
    private CustomCommandsAsyncTask customCommandsAsyncTask;
    private String pointsCoords;

    private final float[] stepValuesXY = {0.1f, 0.5f, 1f, 5f, 10f, 50f};
    private final float[] stepValuesZ = {0.01f, 0.05f, 0.1f, 0.5f, 1f, 5f};
    private final float[] stepValuesFeed = {500f, 1000f, 1500f, 2000f, 2400f};

    private IconButton laser;

    private Toast lastToast;

    public JoggingTabFragment() {
    }

    public static JoggingTabFragment newInstance() {
        return new JoggingTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        machineStatus = MachineStatusListener.getInstance();
        sharedPref = EnhancedSharedPreferences.getInstance(requireActivity().getApplicationContext(), getString(R.string.shared_preference_key));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        SetCustomButtons(requireView());

        String joggingPadRotateAngle = sharedPref.getString(getString(R.string.preference_xy_jog_pad_rotation), "0");
        String[] joggingPadTags = rotateJogPad(Integer.parseInt(joggingPadRotateAngle));
        int jogPadIndex = 0;
        for (int resourceId : new Integer[]{R.id.jog_xy_top_left, R.id.jog_y_positive, R.id.jog_xy_top_right, R.id.jog_x_negative, R.id.jog_x_positive, R.id.jog_xy_bottom_left, R.id.jog_y_negative, R.id.jog_xy_bottom_right}) {
            final IconButton jogButton = requireView().findViewById(resourceId);
            jogButton.setTag(joggingPadTags[jogPadIndex]);
            jogPadIndex++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentJoggingTabBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_jogging_tab, container, false);
        binding.setMachineStatus(machineStatus);
        View view = binding.getRoot();


        CardView viewLastToast = view.findViewById(R.id.view_last_toast);
        viewLastToast.setOnLongClickListener(v -> {
            if (lastToast != null) lastToast.show();
            return true;
        });

        for (int resourceId : new Integer[]{R.id.wpos_edit_x, R.id.wpos_edit_y, R.id.wpos_edit_z}) {
            IconTextView positionTextView = view.findViewById(resourceId);
            positionTextView.setOnClickListener(v -> setWorkPosition(v.getTag().toString()));
        }


        RelativeLayout joggingStepFeedView = view.findViewById(R.id.jogging_step_feed_view);
        joggingStepFeedView.setOnClickListener(this);

        // 机械原点
        IconButton mpos_zero = view.findViewById(R.id.mpos_zero);
        mpos_zero.setOnClickListener(this);
        // 冷却开关
        IconButton cool = view.findViewById(R.id.cool);
        cool.setOnClickListener(this);
        // 激光
        laser = view.findViewById(R.id.laser);
        laser.setOnClickListener(this);
        laser.setOnLongClickListener(this);

        for (int resourceId : new Integer[]{R.id.jog_y_positive, R.id.jog_x_positive, R.id.jog_z_positive,
                R.id.jog_xy_top_left, R.id.jog_xy_top_right, R.id.jog_xy_bottom_left, R.id.jog_xy_bottom_right,
                R.id.jog_y_negative, R.id.jog_x_negative, R.id.jog_z_negative}) {

            final IconButton iconButton = view.findViewById(resourceId);
            iconButton.setOnTouchListener(new RepeatListener(false, 300, 35));
            iconButton.setOnClickListener(view1 -> {
                if (isAdded()) {
                    sendJogCommand(iconButton.getTag().toString());
                }
            });

        }

        for (int resourceId : new Integer[]{R.id.jog_cancel, R.id.run_homing_cycle, R.id.goto_x_zero, R.id.goto_y_zero, R.id.goto_z_zero}) {
            IconButton iconButton = view.findViewById(resourceId);
            iconButton.setOnLongClickListener(this);
        }

        for (int resourceId : new Integer[]{R.id.run_homing_cycle, R.id.jog_cancel}) {
            IconButton iconButton = view.findViewById(resourceId);
            iconButton.setOnClickListener(this);
        }

        TableRow resetZeroLayout = view.findViewById(R.id.reset_zero_layout);
        for (int i = 0; i < resetZeroLayout.getChildCount(); i++) {
            View resetZeroLayoutView = resetZeroLayout.getChildAt(i);
            if (resetZeroLayoutView instanceof IconButton || resetZeroLayoutView instanceof IconToggleButton) {
                resetZeroLayoutView.setOnClickListener(view12 -> {
                    final String tag = view12.getTag().toString();

                    if (tag.equals(GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND)) {
                        if (!machineStatus.getState().equals(Constants.MACHINE_STATUS_RUN)) {
                            fragmentInteractionListener.onGcodeCommandReceived(tag);
                        }
                        return;
                    }

                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.text_zero_selected_axis))
                            .setMessage(getString(R.string.text_set_axis_location_in_current_wpos) + tag)
                            .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> sendCommandIfIdle(tag))
                            .setNegativeButton(getString(R.string.text_no_confirm), null)
                            .show();
                });
            }
        }

        TableRow wposLayout = view.findViewById(R.id.wpos_layout);
        for (int i = 0; i < wposLayout.getChildCount(); i++) {
            View wposLayoutView = wposLayout.getChildAt(i);
            if (wposLayoutView instanceof Button) {
                wposLayoutView.setOnClickListener(view13 -> {
                    if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)) {
                        sendCommandIfIdle(view13.getTag().toString());
                        sendCommandIfIdle(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND);
                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_selected_coordinate_system) + view13.getTag().toString()));
                    } else {
                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
                    }
                });
                wposLayoutView.setOnLongClickListener(this);
            }
        }

        return view;
    }

    private void setWorkPosition(final String axisLabel) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.dialog_input_decimal_signed, null, false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(v);
        alertDialogBuilder.setTitle(getString(R.string.test_set_cordinate_system, axisLabel));
        alertDialogBuilder.setMessage(getString(R.string.test_set_cordinate_system_description, axisLabel));

        final EditText editText = v.findViewById(R.id.dialog_input_decimal_signed);
        if (axisLabel.equalsIgnoreCase("X"))
            editText.setText(String.valueOf(machineStatus.getWorkPosition().getCordX()));
        if (axisLabel.equalsIgnoreCase("Y"))
            editText.setText(String.valueOf(machineStatus.getWorkPosition().getCordY()));
        if (axisLabel.equalsIgnoreCase("Z"))
            editText.setText(String.valueOf(machineStatus.getWorkPosition().getCordZ()));
        editText.setSelection(editText.getText().length());

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton(getString(R.string.text_ok), (dialog, id) -> {
                    String axisValue = editText.getText().toString();
                    if (axisValue.length() > 0) {
                        sendCommandIfIdle("G10L20P0" + axisLabel + axisValue);
                    }
                })
                .setNeutralButton("/2",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Double val = Double.parseDouble(editText.getText().toString());
                                val = val / 2;
                                String axisValue = val.toString();
                                if (axisValue.length() > 0) {
                                    sendCommandIfIdle("G10L20P0" + axisLabel + axisValue);
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.text_cancel), (dialog, id) -> dialog.cancel());

        AlertDialog dialog = alertDialogBuilder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void SetCustomButtons(View view) {
        TableRow customButtonLayout = view.findViewById(R.id.custom_button_layout);
        if (customButtonLayout == null) return;

        if (sharedPref.getBoolean(getString(R.string.preference_enable_custom_buttons), false)) {
            customButtonLayout.setVisibility(View.VISIBLE);

            for (int resourceId : new Integer[]{R.id.custom_button_1, R.id.custom_button_2, R.id.custom_button_3, R.id.custom_button_4}) {
                IconButton iconButton = view.findViewById(resourceId);

                if (resourceId == R.id.custom_button_1)
                    iconButton.setText(sharedPref.getString(getString(R.string.preference_custom_button_one), getString(R.string.text_value_na)));
                if (resourceId == R.id.custom_button_2)
                    iconButton.setText(sharedPref.getString(getString(R.string.preference_custom_button_two), getString(R.string.text_value_na)));
                if (resourceId == R.id.custom_button_3)
                    iconButton.setText(sharedPref.getString(getString(R.string.preference_custom_button_three), getString(R.string.text_value_na)));
                if (resourceId == R.id.custom_button_4)
                    iconButton.setText(sharedPref.getString(getString(R.string.preference_custom_button_four), getString(R.string.text_value_na)));

                iconButton.setOnLongClickListener(this);
                iconButton.setOnClickListener(this);
            }
        } else {
            customButtonLayout.setVisibility(View.GONE);
        }
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.jogging_step_feed_view:
                this.setJoggingStepAndFeed();
                return;

            case R.id.run_homing_cycle:

                if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_ALARM)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.text_homing_cycle))
                            .setMessage(getString(R.string.text_do_homing_cycle))
                            .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> fragmentInteractionListener.onGcodeCommandReceived(GrblUtils.GRBL_RUN_HOMING_CYCLE))
                            .setNegativeButton(getString(R.string.text_no_confirm), null)
                            .show();
                } else {
                    EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
                }
                break;

            case R.id.jog_cancel:
                if (machineStatus.getState().equals(Constants.MACHINE_STATUS_JOG)) {
                    fragmentInteractionListener.onGrblRealTimeCommandReceived(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
                }

                if (customCommandsAsyncTask != null && customCommandsAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                    customCommandsAsyncTask.cancel(true);
                    fragmentInteractionListener.onGrblRealTimeCommandReceived(GrblUtils.GRBL_RESET_COMMAND);
                }

                break;

            case R.id.custom_button_1:

                pointsCoords += String.valueOf(machineStatus.getWorkPosition().getCordX()) + ',' +
                        String.valueOf(machineStatus.getWorkPosition().getCordY()) + ',' +
                        String.valueOf(machineStatus.getWorkPosition().getCordZ()) + "\n";

                //System.out.println(pointsCoords);

                File pointsFile;

                pointsFile = new File(getActivity().getExternalFilesDir(null), "points.txt");

                try {
                    FileOutputStream fos = new FileOutputStream(pointsFile);
                    fos.write(pointsCoords.getBytes());
                    fos.flush();
                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                EventBus.getDefault().post(new UiToastEvent("new points file at " + getActivity().getExternalFilesDir(null), true, true));

                break;

            case R.id.custom_button_2:
            case R.id.custom_button_3:
            case R.id.custom_button_4:
                customButton(id, false);
                break;
            case R.id.mpos_zero:
                GcodeCommand gcodeCommand_mpos_zero = new GcodeCommand("G28");
                fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand_mpos_zero.getCommandString());
                CommandHistory.saveToHistory("G28", gcodeCommand_mpos_zero.getCommandString());
                break;
            case R.id.cool:
                // 开启气泵
                if (view.getTag().equals("opencool")) {
                    GcodeCommand gcodeCommand_open_cool = new GcodeCommand("M8");
                    fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand_open_cool.getCommandString());
                    CommandHistory.saveToHistory("M8", gcodeCommand_open_cool.getCommandString());

                    laser.setText("关闭气泵");
                    laser.setTag("closecool");
                } else {
                    // 关闭气泵
                    GcodeCommand gcodeCommand_close_cool = new GcodeCommand("M9");
                    fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand_close_cool.getCommandString());
                    CommandHistory.saveToHistory("M9", gcodeCommand_close_cool.getCommandString());
                    laser.setText("开启气泵");
                    laser.setTag("opencool");
                }
                break;
            case R.id.laser:
                if (view.getTag().equals("openlaser")) {
                    GcodeCommand gcodeCommand_laser = new GcodeCommand("M3 S10");
                    fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand_laser.getCommandString());
                    CommandHistory.saveToHistory("M3 S10", gcodeCommand_laser.getCommandString());

                    GcodeCommand gcodeCommand_open_laser = new GcodeCommand("G1 F1000");
                    fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand_open_laser.getCommandString());
                    CommandHistory.saveToHistory("G1 F1000", gcodeCommand_open_laser.getCommandString());

                    laser.setText("关闭激光");
                    laser.setTag("closelaser");
                } else {
                    // 关闭激光
                    GcodeCommand gcodeCommand_close_laser = new GcodeCommand("M5");
                    fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand_close_laser.getCommandString());
                    CommandHistory.saveToHistory("M5", gcodeCommand_close_laser.getCommandString());
                    laser.setText("开启激光");
                    laser.setTag("openlaser");
                }

                break;
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();

        switch (id) {

            case R.id.run_homing_cycle:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.text_set_coordinate_system))
                        .setMessage(getString(R.string.text_set_all_axes_zero))
                        .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> sendCommandIfIdle(GrblUtils.GCODE_RESET_COORDINATES_TO_ZERO))
                        .setNegativeButton(getString(R.string.text_no_confirm), null)
                        .show();
                return true;

            case R.id.jog_cancel:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.text_return_to_zero_position))
                        .setMessage(getString(R.string.text_go_to_zero_position_in_current_wpos))
                        .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
                            for (String gCommand : GrblUtils.getReturnToHomeCommands()) {
                                sendCommandIfIdle(gCommand);
                            }
                        })
                        .setNegativeButton(getString(R.string.text_no_confirm), null)
                        .show();
                return true;

            case R.id.wpos_g54:
            case R.id.wpos_g55:
            case R.id.wpos_g56:
            case R.id.wpos_g57:
                saveWPos((Button) view);
                return true;

            case R.id.goto_x_zero:
                gotoAxisZero("X");
                return true;

            case R.id.goto_y_zero:
                gotoAxisZero("Y");
                return true;

            case R.id.goto_z_zero:
                gotoAxisZero("Z");
                return true;

            case R.id.custom_button_1:
            case R.id.custom_button_2:
            case R.id.custom_button_3:
            case R.id.custom_button_4:
                customButton(id, true);
                return true;

            case R.id.laser:
                if (view.getTag().equals("openlaser")) {
                    setLaserOpneOrClose();
                } else {
                    // 关闭激光
                    String commandText = "M5";
                    if (commandText.length() > 0) {
                        GcodeCommand gcodeCommand = new GcodeCommand(commandText);
                        fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand.getCommandString());
                        CommandHistory.saveToHistory(commandText, gcodeCommand.getCommandString());
                    }
                    laser.setText("开启激光");
                    laser.setTag("openlaser");
                }
                return true;
        }
        return false;
    }

    private void customButton(int resourceId, boolean isLongClick) {

        if (!machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)) {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
            return;
        }

        String title = "";
        String commands = "";
        boolean confirmFirst = true;

        if (resourceId == R.id.custom_button_1) {
            title = sharedPref.getString(getString(R.string.preference_custom_button_one), getString(R.string.text_value_na));
            commands = isLongClick ? sharedPref.getString(getString(R.string.preference_custom_button_one_long_click), "") : sharedPref.getString(getString(R.string.preference_custom_button_one_short_click), "");
            confirmFirst = sharedPref.getBoolean(getString(R.string.preference_custom_button_one_confirm), true);
        }

        if (resourceId == R.id.custom_button_2) {
            title = sharedPref.getString(getString(R.string.preference_custom_button_two), getString(R.string.text_value_na));
            commands = isLongClick ? sharedPref.getString(getString(R.string.preference_custom_button_two_long_click), "") : sharedPref.getString(getString(R.string.preference_custom_button_two_short_click), "");
            confirmFirst = sharedPref.getBoolean(getString(R.string.preference_custom_button_two_confirm), true);
        }

        if (resourceId == R.id.custom_button_3) {
            title = sharedPref.getString(getString(R.string.preference_custom_button_three), getString(R.string.text_value_na));
            commands = isLongClick ? sharedPref.getString(getString(R.string.preference_custom_button_three_long_click), "") : sharedPref.getString(getString(R.string.preference_custom_button_three_short_click), "");
            confirmFirst = sharedPref.getBoolean(getString(R.string.preference_custom_button_three_confirm), true);
        }

        if (resourceId == R.id.custom_button_4) {
            title = sharedPref.getString(getString(R.string.preference_custom_button_four), getString(R.string.text_value_na));
            commands = isLongClick ? sharedPref.getString(getString(R.string.preference_custom_button_four_long_click), "") : sharedPref.getString(getString(R.string.preference_custom_button_four_short_click), "");
            confirmFirst = sharedPref.getBoolean(getString(R.string.preference_custom_button_four_confirm), true);
        }

        if (commands.trim().length() <= 0) {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_empty_command), true, true));
            return;
        }

        final String finalCommands = commands;

        if (confirmFirst) {
            String alertSummary = isLongClick ? getString(R.string.text_long_click) : getString(R.string.text_short_click);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.text_custom_action) + title)
                    .setMessage(getString(R.string.text_send_custom_command) + alertSummary + getString(R.string.text_on_button) + title)
                    .setPositiveButton(getString(R.string.text_send), (dialog, which) -> {
                        customCommandsAsyncTask = new CustomCommandsAsyncTask();
                        customCommandsAsyncTask.execute(finalCommands);
                    })
                    .setNegativeButton(getString(R.string.text_cancel), null)
                    .show();
        } else {
            customCommandsAsyncTask = new CustomCommandsAsyncTask();
            customCommandsAsyncTask.execute(finalCommands);
        }

    }

    private class CustomCommandsAsyncTask extends AsyncTask<String, Integer, Integer> {

        private int MAX_RX_SERIAL_BUFFER = Constants.DEFAULT_SERIAL_RX_BUFFER - 3;
        private int CURRENT_RX_SERIAL_BUFFER;
        private LinkedList<Integer> activeCommandSizes;

        protected void onPreExecute() {
            MachineStatusListener.CompileTimeOptions compileTimeOptions = MachineStatusListener.getInstance().getCompileTimeOptions();
            if (compileTimeOptions.serialRxBuffer > 0)
                MAX_RX_SERIAL_BUFFER = compileTimeOptions.serialRxBuffer - 3;

            completedCommands = new ArrayBlockingQueue<>(Constants.DEFAULT_SERIAL_RX_BUFFER);
            activeCommandSizes = new LinkedList<>();
            CURRENT_RX_SERIAL_BUFFER = 0;
        }

        protected Integer doInBackground(String... commands) {

            String[] lines = commands[0].split("[\r\n]+");
            for (String command : lines) {
                if (isCancelled()) break;
                streamLine(command);
            }

            return 1;
        }

        private void streamLine(String gcodeCommand) {

            int commandSize = gcodeCommand.length() + 1;

            // Wait until there is room, if necessary.
            while (MAX_RX_SERIAL_BUFFER < (CURRENT_RX_SERIAL_BUFFER + commandSize)) {
                try {
                    completedCommands.take();
                    if (activeCommandSizes.size() > 0)
                        CURRENT_RX_SERIAL_BUFFER -= activeCommandSizes.removeFirst();
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage(), e);
                    return;
                }
            }

            activeCommandSizes.offer(commandSize);
            CURRENT_RX_SERIAL_BUFFER += commandSize;
            fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand);
        }

    }

    private void gotoAxisZero(final String axis) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.text_move) + axis + getString(R.string.text_axis_to_zero_position))
                .setMessage(getString(R.string.text_go_to_zero_position) + axis + "0")
                .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> sendCommandIfIdle("G0 " + axis + "0"))
                .setNegativeButton(getString(R.string.text_no_confirm), null)
                .show();
    }

    private void saveWPos(Button button) {
        String wpos = button.getTag().toString();
        final String slot;

        switch (wpos) {
            case "G54":
                slot = "P1";
                break;
            case "G56":
                slot = "P3";
                break;
            case "G57":
                slot = "P4";
                break;
            default:
//              //G55
                slot = "P2";
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.text_save_coordinate_system)
                .setMessage(getString(R.string.text_save_coordinate_system_desc) + " " + wpos + "?")
                .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> sendCommandIfIdle(String.format("G10 L20 %s X0Y0Z0", slot)))
                .setNegativeButton(getString(R.string.text_no_confirm), null)
                .show();
    }

    private void sendJogCommand(String tag) {
        Log.d(TAG, "tag=" + tag);
        if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_JOG)) {

            String units = "G21";
            double jogFeed = machineStatus.getJogging().feed;

            if (machineStatus.getJogging().inches) {
                units = "G20";
                jogFeed = jogFeed / 25.4;
            }

            Double stepSize;
            if (tag.toUpperCase().contains("Z")) {
                stepSize = machineStatus.getJogging().stepZ;
            } else {
                stepSize = machineStatus.getJogging().stepXY;
            }

            String jog = String.format(tag, units, stepSize, jogFeed);
            EventBus.getDefault().post(new JogCommandEvent(jog));
        } else {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setJoggingStepAndFeed() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_step_and_feed, null, false);

        /****************************************************** XY ******************************************************/
        SeekBar seekBarXY = view.findViewById(R.id.jog_xy_step_seek_bar);
        TextView seekBarXY_Value = view.findViewById(R.id.jog_xy_step_seek_value);
        // 设置 SeekBar 为6个挡位
        seekBarXY.setMax(5);
        // 初始化显示第一个值
        seekBarXY_Value.setText("XY_Step_Value：" + machineStatus.getJogging().stepXY.floatValue());
        switch (String.valueOf(machineStatus.getJogging().stepXY.floatValue())) {
            case "0.1":
                seekBarXY.setProgress(0);
                break;
            case "0.5":
                seekBarXY.setProgress(1);
                break;
            case "1.0":
                seekBarXY.setProgress(2);
                break;
            case "5.0":
                seekBarXY.setProgress(3);
                break;
            case "10.0":
                seekBarXY.setProgress(4);
                break;
            case "50.0":
                seekBarXY.setProgress(5);
                break;
        }
        // seekBarXY
        seekBarXY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 映射进度值到对应的挡位值
                float currentValue = stepValuesXY[progress];
                seekBarXY_Value.setText("XY_Step_Value：" + currentValue);


                machineStatus.setJogging(Double.parseDouble(Float.toString(stepValuesXY[seekBarXY.getProgress()])),
                        machineStatus.getJogging().stepZ,
                        machineStatus.getJogging().feed,
                        sharedPref.getBoolean(getString(R.string.preference_jogging_in_inches), false)
                );


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                editor.putDouble(getString(R.string.preference_jogging_step_size), Double.parseDouble(Float.toString(stepValuesXY[seekBarXY.getProgress()]))).commit();
            }
        });
        // 按钮
        for (final int resourceId : new Integer[]{R.id.jog_xy_step_0_1, R.id.jog_xy_step_0_5, R.id.jog_xy_step_1,
                R.id.jog_xy_step_5, R.id.jog_xy_step_10, R.id.jog_xy_step_50}) {
            final IconButton iconButton = view.findViewById(resourceId);

            iconButton.setOnLongClickListener(v -> {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Save Quick Button Value")
                        .setMessage("do you want to save the quick button value as " + stepValuesXY[seekBarXY.getProgress()])
                        .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
                            EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(iconButton.getTag().toString(), Float.toString(stepValuesXY[seekBarXY.getProgress()])).commit();
                        })
                        .setNegativeButton(getString(R.string.text_no_confirm), null)
                        .show();

                return true;
            });

            iconButton.setOnClickListener(v -> {
                if (isAdded()) {
                    String stepValue = sharedPref.getString(iconButton.getTag().toString(), "0");
                    if (stepValue.equals("0")) {
                        switch (resourceId) {
                            case R.id.jog_xy_step_0_1:
                                stepValue = "0.1";
                                break;

                            case R.id.jog_xy_step_0_5:
                                stepValue = "0.5";
                                break;

                            case R.id.jog_xy_step_1:
                                stepValue = "1";
                                break;
                            case R.id.jog_xy_step_5:
                                stepValue = "5";
                                break;

                            case R.id.jog_xy_step_10:
                                stepValue = "10";
                                break;

                            case R.id.jog_xy_step_50:
                                stepValue = "50";
                                break;
                        }
                    }

                    float step_value = Float.parseFloat(stepValue);
                    switch (stepValue) {
                        case "0.1":
                            seekBarXY.setProgress(0);
                            break;
                        case "0.5":
                            seekBarXY.setProgress(1);
                            break;
                        case "1":
                            seekBarXY.setProgress(2);
                            break;
                        case "5":
                            seekBarXY.setProgress(3);
                            break;
                        case "10":
                            seekBarXY.setProgress(4);
                            break;
                        case "50":
                            seekBarXY.setProgress(5);
                            break;
                    }
                    EventBus.getDefault().post(new UiToastEvent("XY Axis step value is set to " + step_value));
                    EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                    editor.putDouble(getString(R.string.preference_jogging_step_size), Double.parseDouble(Float.toString(step_value))).commit();
                }
            });
        }

        /****************************************************** Z ******************************************************/
        SeekBar seekBarZ = view.findViewById(R.id.jog_z_step_seek_bar);
        TextView seekBarZ_Value = view.findViewById(R.id.jog_z_step_seek_value);
        // 设置 SeekBar 为6个挡位
        seekBarZ.setMax(5);
        // 初始化显示第一个值
        seekBarZ_Value.setText("Z_Step_Value：" + machineStatus.getJogging().stepZ.floatValue());
        switch (String.valueOf(machineStatus.getJogging().stepZ.floatValue())) {
            case "0.01":
                seekBarZ.setProgress(0);
                break;
            case "0.05":
                seekBarZ.setProgress(1);
                break;
            case "0.1":
                seekBarZ.setProgress(2);
                break;
            case "0.5":
                seekBarZ.setProgress(3);
                break;
            case "1.0":
                seekBarZ.setProgress(4);
                break;
            case "5.0":
                seekBarZ.setProgress(5);
                break;
        }
        // seekBarZ
        seekBarZ.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 映射进度值到对应的挡位值
                float currentValue = stepValuesZ[progress];
                seekBarZ_Value.setText("Z_Step_Value：" + currentValue);


                machineStatus.setJogging(machineStatus.getJogging().stepXY,
                        Double.parseDouble(Float.toString(stepValuesZ[seekBarZ.getProgress()])),
                        machineStatus.getJogging().feed,
                        sharedPref.getBoolean(getString(R.string.preference_jogging_in_inches), false)
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                editor.putDouble(getString(R.string.preference_jogging_step_size_z), Double.parseDouble(Float.toString(stepValuesZ[seekBarZ.getProgress()]))).commit();
            }
        });
        // 按钮
        for (final int resourceId : new Integer[]{R.id.jog_z_step_0_01, R.id.jog_z_step_0_05, R.id.jog_z_step_0_1,
                R.id.jog_z_step_0_1, R.id.jog_z_step_1, R.id.jog_z_step_5}) {
            final IconButton iconButton = view.findViewById(resourceId);

            iconButton.setOnLongClickListener(v -> {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Save Quick Button Value")
                        .setMessage("do you want to save the quick button value as " + stepValuesZ[seekBarZ.getProgress()])
                        .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
                            EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(iconButton.getTag().toString(), Float.toString(stepValuesZ[seekBarZ.getProgress()])).commit();
                        })
                        .setNegativeButton(getString(R.string.text_no_confirm), null)
                        .show();

                return true;
            });

            iconButton.setOnClickListener(v -> {
                if (isAdded()) {
                    String stepValue = sharedPref.getString(iconButton.getTag().toString(), "0");
                    if (stepValue.equals("0")) {
                        switch (resourceId) {
                            case R.id.jog_z_step_0_01:
                                stepValue = "0.01";
                                break;

                            case R.id.jog_z_step_0_05:
                                stepValue = "0.05";
                                break;

                            case R.id.jog_z_step_0_1:
                                stepValue = "0.1";
                                break;
                            case R.id.jog_z_step_0_5:
                                stepValue = "0.5";
                                break;

                            case R.id.jog_z_step_1:
                                stepValue = "1";
                                break;

                            case R.id.jog_z_step_5:
                                stepValue = "5";
                                break;
                        }
                    }

                    float step_value = Float.parseFloat(stepValue);
                    switch (stepValue) {
                        case "0.01":
                            seekBarZ.setProgress(0);
                            break;
                        case "0.05":
                            seekBarZ.setProgress(1);
                            break;
                        case "0.1":
                            seekBarZ.setProgress(2);
                            break;
                        case "0.5":
                            seekBarZ.setProgress(3);
                            break;
                        case "1":
                            seekBarZ.setProgress(4);
                            break;
                        case "5":
                            seekBarZ.setProgress(5);
                            break;
                    }
                    EventBus.getDefault().post(new UiToastEvent("Z Axis step value is set to " + step_value));
                    EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                    editor.putDouble(getString(R.string.preference_jogging_step_size_z), Double.parseDouble(Float.toString(step_value))).commit();
                }
            });
        }

        /****************************************************** feed ******************************************************/
        SeekBar seekBarFeed = view.findViewById(R.id.jog_feed_seek_bar);
        TextView seekBarFeed_Value = view.findViewById(R.id.jog_feed_seek_value);
        // 设置 SeekBar 为6个挡位
        seekBarFeed.setMax(4);
        // 初始化显示第一个值
        seekBarFeed_Value.setText("Feed_Step_Value：" + machineStatus.getJogging().feed.floatValue());
        switch (String.valueOf(machineStatus.getJogging().feed.floatValue())) {
            case "500.0":
                seekBarFeed.setProgress(0);
                break;
            case "1000.0":
                seekBarFeed.setProgress(1);
                break;
            case "1500.0":
                seekBarFeed.setProgress(2);
                break;
            case "2000.0":
                seekBarFeed.setProgress(3);
                break;
            case "2400.0":
                seekBarFeed.setProgress(4);
                break;

        }
        // seekBarFeed
        seekBarFeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 映射进度值到对应的挡位值
                float currentValue = stepValuesFeed[progress];
                seekBarFeed_Value.setText("Feed_Step_Value：" + currentValue);


                machineStatus.setJogging(machineStatus.getJogging().stepXY,
                        machineStatus.getJogging().stepZ,
                        Double.parseDouble(Float.toString(stepValuesFeed[seekBarFeed.getProgress()])),
                        sharedPref.getBoolean(getString(R.string.preference_jogging_in_inches), false)
                );


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                EnhancedSharedPreferences.Editor editor = sharedPref.edit();
                editor.putDouble(getString(R.string.preference_jogging_feed_rate), Double.parseDouble(Float.toString(stepValuesFeed[seekBarFeed.getProgress()]))).commit();
            }
        });

        SwitchCompat jogInches = view.findViewById(R.id.jog_inches);
        jogInches.setChecked(sharedPref.getBoolean(getString(R.string.preference_jogging_in_inches), false));
        jogInches.setOnCheckedChangeListener((compoundButton, b) -> {
            machineStatus.setJogging(machineStatus.getJogging().stepXY, machineStatus.getJogging().stepZ, machineStatus.getJogging().feed, b);
            EnhancedSharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.preference_jogging_in_inches), b).commit();
        });


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton(getString(R.string.text_ok), (dialog, id) -> {

                });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private String[] rotateJogPad(int angle) {

        switch (angle) {

            case 90:
                return new String[]{"$J=%1$sG91X-%2$sY-%2$sF%3$s", "$J=%sG91X-%sF%s", "$J=%1$sG91X-%2$sY%2$sF%3$s", "$J=%sG91Y-%sF%s", "$J=%sG91Y%sF%s", "$J=%1$sG91X%2$sY-%2$sF%3$s", "$J=%sG91X%sF%s", "$J=%1$sG91X%2$sY%2$sF%3$s"};

            case 180:
                return new String[]{"$J=%1$sG91X%2$sY-%2$sF%3$s", "$J=%sG91Y-%sF%s", "$J=%1$sG91X-%2$sY-%2$sF%3$s", "$J=%sG91X%sF%s", "$J=%sG91X-%sF%s", "$J=%1$sG91X%2$sY%2$sF%3$s", "$J=%sG91Y%sF%s", "$J=%1$sG91X-%2$sY%2$sF%3$s"};

            case 270:
                return new String[]{"$J=%1$sG91X%2$sY%2$sF%3$s", "$J=%sG91X%sF%s", "$J=%1$sG91X%2$sY-%2$sF%3$s", "$J=%sG91Y%sF%s", "$J=%sG91Y-%sF%s", "$J=%1$sG91X-%2$sY%2$sF%3$s", "$J=%sG91X-%sF%s", "$J=%1$sG91X-%2$sY-%2$sF%3$s"};

            default:
                return new String[]{"$J=%1$sG91X-%2$sY%2$sF%3$s", "$J=%sG91Y%sF%s", "$J=%1$sG91X%2$sY%2$sF%3$s", "$J=%sG91X-%sF%s", "$J=%sG91X%sF%s", "$J=%1$sG91X-%2$sY-%2$sF%3$s", "$J=%sG91Y-%sF%s", "$J=%1$sG91X%2$sY-%2$sF%3$s"};
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setLaserOpneOrClose() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_laser_open_or_close, null, false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(true);

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.setCancelable(true);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) RadioGroup radioGroup = view.findViewById(R.id.laser_level);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = view.findViewById(checkedId);
            String selectedText = selectedRadioButton.getText().toString();
            Log.d(TAG, "selectedText=" + selectedText);
            // TODO  打开激光
            String commandText = "M3 S" + Integer.valueOf(selectedText);
            if (commandText.length() > 0) {
                GcodeCommand gcodeCommand = new GcodeCommand(commandText);
                fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand.getCommandString());
                CommandHistory.saveToHistory(commandText, gcodeCommand.getCommandString());
            }

            String commandText1 = "G1 F1000";
            if (commandText1.length() > 0) {
                GcodeCommand gcodeCommand = new GcodeCommand(commandText1);
                fragmentInteractionListener.onGcodeCommandReceived(gcodeCommand.getCommandString());
                CommandHistory.saveToHistory(commandText1, gcodeCommand.getCommandString());
            }
            laser.setText("关闭激光");
            laser.setTag("closelaser");
            dialog.dismiss();  // Dismiss the dialog after selection
        });

        dialog.show();
    }


    private void sendCommandIfIdle(String command) {
        if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)) {
            fragmentInteractionListener.onGcodeCommandReceived(command);
        } else {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onGrblOkEvent(GrblOkEvent event) {
        if (customCommandsAsyncTask != null && customCommandsAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            completedCommands.offer(1);
        }
    }
}
