package in.co.gorest.grblcontroller.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.shixia.colorpickerview.ColorPickerView;
import com.shixia.colorpickerview.OnColorChangeListener;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.MySeekBar;

public class SizeChooseBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private final static String TAG = SizeChooseBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // seekBar
    private MySeekBar seekbarSize;
    // 粗细
    private TextView tvSize;
    // 取消
    private TextView tvCancel;
    private TextView tvConfirm;

    // 初始颜色
    private int defaultSize = 5;

    private OnSizeSelectedListener mListener;

    public SizeChooseBottomSheetFragment() {
    }


    public static SizeChooseBottomSheetFragment newInstance() {
        return new SizeChooseBottomSheetFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSizeSelectedListener) {
            mListener = (OnSizeSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSizeSelectedListener");
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
        return inflater.inflate(R.layout.fragment_size_choose_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));

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
        // seekBar
        seekbarSize = view.findViewById(R.id.seekbar_size);
        // 粗细
        tvSize = view.findViewById(R.id.tv_size);
        // 取消
        tvCancel = view.findViewById(R.id.tv_cancel);
        // 确定
        tvConfirm = view.findViewById(R.id.tv_confirm);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        seekbarSize.setProgressMin(1);
        seekbarSize.setProgressMax(100);
        defaultSize =  sharedPref.getInt(getString(R.string.preference_draw_board_pen_size), 5);
        seekbarSize.setProgressDefault(defaultSize);
        tvSize.setText(String.valueOf(seekbarSize.getProgressDefault()));
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {

        // 颜色选择器
        seekbarSize.setProgressChanged(new MySeekBar.onProgressChanged() {
            @Override
            public void onProgress(int Progress) {
                defaultSize = Progress;
                tvSize.setText(String.valueOf(Progress));
            }

            @Override
            public void onStop(int Progress) {

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
                // 传递defaultSize给Activity
                if (mListener != null) {
                    if (defaultSize != -1) {
                        mListener.onSizeSelected(defaultSize);
                        dismiss();  // 关闭底部弹窗
                    } else {
                        Toast.makeText(getActivity(), "请设置粗细", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public interface OnSizeSelectedListener {
        void onSizeSelected(int size);
    }
}