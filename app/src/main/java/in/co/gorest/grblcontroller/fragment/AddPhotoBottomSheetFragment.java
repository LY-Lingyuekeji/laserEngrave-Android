package in.co.gorest.grblcontroller.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class AddPhotoBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = AddPhotoBottomSheetFragment.class.getSimpleName();

    private OnAddPhotoSelectedListener mListener;

    // 素材
    private RelativeLayout rlMaterial;


    public AddPhotoBottomSheetFragment() {
    }


    public static AddPhotoBottomSheetFragment newInstance() {
        return new AddPhotoBottomSheetFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAddPhotoSelectedListener) {
            mListener = (OnAddPhotoSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnAddPhotoSelectedListener");
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
        return inflater.inflate(R.layout.fragment_add_photo_bottom_sheet, container, false);
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
        // 素材
        rlMaterial = view.findViewById(R.id.rl_material);
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
        rlMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddPhotoSelected("素材");
            }
        });
    }

    public interface OnAddPhotoSelectedListener {
        void onAddPhotoSelected(String tool);
    }

}