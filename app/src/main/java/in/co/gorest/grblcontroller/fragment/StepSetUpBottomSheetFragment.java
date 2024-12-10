package in.co.gorest.grblcontroller.fragment;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.MainActivity;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.WifiChooseBottomSheetAdapter;
import in.co.gorest.grblcontroller.events.ConnectStepSetupEvent;
import in.co.gorest.grblcontroller.events.StepSetupEvent;
import in.co.gorest.grblcontroller.events.WifiNameEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;

public class StepSetUpBottomSheetFragment extends BottomSheetDialogFragment {

    // 用于日志记录的标签
    private static final String TAG = StepSetUpBottomSheetFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 重置
    private TextView tvRest;
    // 步长（短）
    private RelativeLayout rlStepShort;
    // 步长（短） TextView
    private TextView tvStepShort;
    // 步长（短） Double
    private Double stepShort;
    // 步长（常规）
    private RelativeLayout rlStepGeneral;
    // 步长（常规） TextView
    private TextView tvStepGeneral;
    // 步长（常规） Double
    private Double stepGeneral;
    // 步长（中）
    private RelativeLayout rlStepMiddle;
    // 步长（中） TextView
    private TextView tvStepMiddle;
    // 步长（中） Double
    private Double stepMiddle;
    // 步长（长）
    private RelativeLayout rlStepLong;
    // 步长（长） TextView
    private TextView tvStepLong;
    // 步长（长） Double
    private Double stepLong;
    // 速度（慢）
    private RelativeLayout rlSpeedSlow;
    // 速度（慢） TextView
    private TextView tvSpeedSlow;
    // 速度（慢） Double
    private Double speedSlow;
    // 速度（中等）
    private RelativeLayout rlSpeedMiddle;
    // 速度（中等） TextView
    private TextView tvSpeedMiddle;
    // 速度（中等） Double
    private Double speedMiddle;
    // 速度（快）
    private RelativeLayout rlSpeedFast;
    // 速度（快） TextView
    private TextView tvSpeedFast;
    // 速度（快） Double
    private Double speedFast;
    // 速度（超快）
    private RelativeLayout rlSpeedPrestissimo;
    // 速度（超快） TextView
    private TextView tvSpeedPrestissimo;
    // 速度（超快） Double
    private Double speedPrestissimo;

    public StepSetUpBottomSheetFragment() {
    }


    public static StepSetUpBottomSheetFragment newInstance() {
        return new StepSetUpBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_setup_bottom_sheet, container, false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // 返回
        ivBack = view.findViewById(R.id.iv_back);
        // 重置
        tvRest = view.findViewById(R.id.tv_rest);

        // 步长（短）
        rlStepShort = view.findViewById(R.id.rl_step_short);
        // 步长（常规）
        rlStepGeneral = view.findViewById(R.id.rl_step_general);
        // 步长（中）
        rlStepMiddle = view.findViewById(R.id.rl_step_middle);
        // 步长（长）
        rlStepLong = view.findViewById(R.id.rl_step_long);
        // 步长（短） TextView
        tvStepShort = view.findViewById(R.id.tv_step_short);
        // 步长（常规） TextView
        tvStepGeneral = view.findViewById(R.id.tv_step_general);
        // 步长（中） TextView
        tvStepMiddle = view.findViewById(R.id.tv_step_middle);
        // 步长（长） TextView
        tvStepLong = view.findViewById(R.id.tv_step_long);

        // 速度（慢）
        rlSpeedSlow = view.findViewById(R.id.rl_speed_slow);
        // 速度（中等）
        rlSpeedMiddle = view.findViewById(R.id.rl_speed_middle);
        // 速度（快）
        rlSpeedFast = view.findViewById(R.id.rl_speed_fast);
        // 速度（极快）
        rlSpeedPrestissimo = view.findViewById(R.id.rl_speed_prestissimo);
        // 速度（慢） TextView
        tvSpeedSlow = view.findViewById(R.id.tv_speed_slow);
        // 速度（中等） TextView
        tvSpeedMiddle = view.findViewById(R.id.tv_speed_middle);
        // 速度（快） TextView
        tvSpeedFast = view.findViewById(R.id.tv_speed_fast);
        // 速度（极快） TextView
        tvSpeedPrestissimo = view.findViewById(R.id.tv_speed_prestissimo);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取共享偏好设置保存的运动参数实例

        // 步长（短）
        stepShort = sharedPref.getDouble(getString(R.string.preference_step_short), 0.1);
        tvStepShort.setText(stepShort + "mm");
        // 步长（常规）
        stepGeneral = sharedPref.getDouble(getString(R.string.preference_step_general), 1.0);
        tvStepGeneral.setText(stepGeneral + "mm");
        // 步长（中）
        stepMiddle = sharedPref.getDouble(getString(R.string.preference_step_middle), 5.0);
        tvStepMiddle.setText(stepMiddle + "mm");
        // 步长（长）
        stepLong = sharedPref.getDouble(getString(R.string.preference_step_long), 10.0);
        tvStepLong.setText(stepLong + "mm");

        // 速度（慢）
        speedSlow = sharedPref.getDouble(getString(R.string.preference_speed_slow), 2500.0);
        tvSpeedSlow.setText(speedSlow + "mm/min");
        // 速度（中等）
        speedMiddle = sharedPref.getDouble(getString(R.string.preference_speed_middle), 5000.0);
        tvSpeedMiddle.setText(speedMiddle + "mm/min");
        // 速度（快）
        speedFast = sharedPref.getDouble(getString(R.string.preference_speed_fast), 7500.0);
        tvSpeedFast.setText(speedFast + "mm/min");
        // 速度（极快）
        speedPrestissimo = sharedPref.getDouble(getString(R.string.preference_speed_prestissimo), 10000.0);
        tvSpeedPrestissimo.setText(speedPrestissimo + "mm/min");

    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 返回
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 重置
        tvRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 步长（短）
                sharedPref.edit().putDouble(getString(R.string.preference_step_short), 0.1).apply();
                // 步长（常规）
                sharedPref.edit().putDouble(getString(R.string.preference_step_general), 1.0).apply();
                // 步长（中）
                sharedPref.edit().putDouble(getString(R.string.preference_step_middle), 5.0).apply();
                // 步长（长）
                sharedPref.edit().putDouble(getString(R.string.preference_step_long), 10.0).apply();

                // 速度（慢）
                sharedPref.edit().putDouble(getString(R.string.preference_speed_slow), 2500.0).apply();
                // 速度（中等）
                sharedPref.edit().putDouble(getString(R.string.preference_speed_middle), 5000.0).apply();
                // 速度（快）
                sharedPref.edit().putDouble(getString(R.string.preference_speed_fast), 7500.0).apply();
                // 速度（极快）
                sharedPref.edit().putDouble(getString(R.string.preference_speed_prestissimo), 10000.0).apply();
            }
        });

        // 步长（短）
        rlStepShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepShortBottomSheetFragment stepShortBottomSheetFragment = new StepShortBottomSheetFragment();
                stepShortBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        // 步长（常规）
        rlStepGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepGeneralBottomSheetFragment stepGeneralBottomSheetFragment = new StepGeneralBottomSheetFragment();
                stepGeneralBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        // 步长（中）
        rlStepMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepMiddleBottomSheetFragment stepMiddleBottomSheetFragment = new StepMiddleBottomSheetFragment();
                stepMiddleBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        // 步长（长）
        rlStepLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepLongBottomSheetFragment stepLongBottomSheetFragment = new StepLongBottomSheetFragment();
                stepLongBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });


        // 速度（慢）
        rlSpeedSlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedSlowBottomSheetFragment speedSlowBottomSheetFragment = new SpeedSlowBottomSheetFragment();
                speedSlowBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        // 速度（中等）
        rlSpeedMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedMiddleBottomSheetFragment speedMiddleBottomSheetFragment = new SpeedMiddleBottomSheetFragment();
                speedMiddleBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        // 速度（快）
        rlSpeedFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedFastBottomSheetFragment speedFastBottomSheetFragment = new SpeedFastBottomSheetFragment();
                speedFastBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        // 速度（极快）
        rlSpeedPrestissimo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedPrestissimoBottomSheetFragment speedPrestissimoBottomSheetFragment = new SpeedPrestissimoBottomSheetFragment();
                speedPrestissimoBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });
    }


    /**
     * 步长数据更新
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStepSetupEvent(StepSetupEvent event) {
        if (!event.getMessage().isEmpty()) {
            initData();
            EventBus.getDefault().post(new ConnectStepSetupEvent("update"));
        }
    }
}