package in.co.gorest.grblcontroller.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.shixia.colorpickerview.ColorPickerView;
import com.shixia.colorpickerview.OnColorChangeListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.DrawBoardActivity;
import in.co.gorest.grblcontroller.adapters.WifiChooseBottomSheetAdapter;
import in.co.gorest.grblcontroller.events.WifiNameEvent;

public class ColorChooseBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private final static String TAG = ColorChooseBottomSheetFragment.class.getSimpleName();
    // 颜色选择器
    private ColorPickerView cpvColorPicker;
    // 取消
    private TextView tvCancel;
    private TextView tvConfirm;

    // 初始颜色
    private int defaultColor = -1;

    private OnColorSelectedListener mListener;

    public ColorChooseBottomSheetFragment() {
    }


    public static ColorChooseBottomSheetFragment newInstance() {
        return new ColorChooseBottomSheetFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnColorSelectedListener) {
            mListener = (OnColorSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnColorSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_color_choose_bottom_sheet, container, false);
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
        // 颜色选择器
        cpvColorPicker = view.findViewById(R.id.cpv_color_picker);
        // 取消
        tvCancel = view.findViewById(R.id.tv_cancel);
        // 确定
        tvConfirm = view.findViewById(R.id.tv_confirm);

    }

    /**
     * 初始化数据
     */
    private void initData() {

    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {

        // 颜色选择器
        cpvColorPicker.setOnColorChangeListener(new OnColorChangeListener() {
            @Override
            public void colorChanged(int color) {
                Log.d(TAG, "color=" + color);
                defaultColor = color;
            }
        });


        // 取消
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        // 确定
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 传递defaultColor给Activity
                if (mListener != null) {
                    if (defaultColor != -1) {
                        mListener.onColorSelected(defaultColor);
                        dismiss();  // 关闭底部弹窗
                    } else {
                        Toast.makeText(getActivity(), "请选取颜色", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}