package in.co.gorest.grblcontroller.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.AboutActivity;
import in.co.gorest.grblcontroller.activity.AgreementActivity;
import in.co.gorest.grblcontroller.activity.GuideActivity;
import in.co.gorest.grblcontroller.activity.LanguageActivity;
import in.co.gorest.grblcontroller.activity.ModelActivity;
import in.co.gorest.grblcontroller.activity.QuestionActivity;
import in.co.gorest.grblcontroller.activity.SettingsActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.LanguageChangeEvent;
import in.co.gorest.grblcontroller.events.ModelChangeEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.util.DataCleanManager;

public class SettingFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = SettingFragment.class.getSimpleName();
    // 使用指南
    private LinearLayout llSettingGuide;
    // 常见问题
    private LinearLayout llSettingQuestion;
    // 操作模式
    private RelativeLayout rlSettingModel;
    // 模式
    private TextView tvSettingModel;
    // 语言
    private RelativeLayout rlSettingLanguage;
    // 语言
    private TextView tvLanguage;
    // 用户协议
    private RelativeLayout rlSettingAgreement;
    // 清除缓存
    private RelativeLayout rlSettingClean;
    // 缓存大小
    private TextView tvMemorySize;
    // 设置
    private RelativeLayout rlSettingSettings;
    // 关于
    private RelativeLayout rlSettingAbout;
    // 版本
    private TextView tvVersionName;

    public SettingFragment() {}

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
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
        // 使用指南
        llSettingGuide = view.findViewById(R.id.ll_setting_guide);
        // 常见问题
        llSettingQuestion = view.findViewById(R.id.ll_setting_question);
        // 操作模式
        rlSettingModel = view.findViewById(R.id.rl_setting_model);
        // 模式
        tvSettingModel = view.findViewById(R.id.tv_setting_model);
        // 语言
        rlSettingLanguage = view.findViewById(R.id.rl_setting_language);
        // 语言
        tvLanguage = view.findViewById(R.id.tv_language);
        // 用户协议
        rlSettingAgreement = view.findViewById(R.id.rl_setting_agreement);
        // 清除缓存
        rlSettingClean = view.findViewById(R.id.rl_setting_clean);
        // 缓存大小
        tvMemorySize = view.findViewById(R.id.tv_memory_size);
        // 设置
        rlSettingSettings = view.findViewById(R.id.rl_setting_settings);
        // 关于
        rlSettingAbout = view.findViewById(R.id.rl_setting_about);
        // 版本
        tvVersionName = view.findViewById(R.id.tv_version_name);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置模式
        EnhancedSharedPreferences sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        String operationMode = sharedPref.getString(getString(R.string.preference_operation_mode), "simple");
        Log.d(TAG, "operationMode=" + operationMode);
        if ("simple".equals(operationMode)) {
            tvSettingModel.setText("简易模式");
        } else if ("pro".equals(operationMode)) {
            tvSettingModel.setText("专业模式");
        }

        // 获取缓存并设置
        try {
            tvMemorySize.setText(DataCleanManager.getTotalCacheSize(getContext()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 设置版本号
        tvVersionName.setText("V" + getVersion());
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 使用指南
        llSettingGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), GuideActivity.class));
            }
        });

        // 常见问题
        llSettingQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), QuestionActivity.class));
            }
        });

        // 操作模式
        rlSettingModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ModelActivity.class));
            }
        });

        // 语言
        rlSettingLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LanguageActivity.class));
            }
        });

        // 用户协议
        rlSettingAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AgreementActivity.class));
            }
        });

        // 清除缓存
        rlSettingClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseDialog.showCustomDialog(getContext(), "温馨提示", "确认清除应用缓存吗"
                        , "确定", "取消",
                        v1 -> {
                            // 处理确定按钮点击事件
                            DataCleanManager.clearAllCache(getContext());
                            try {
                                tvMemorySize.setText(DataCleanManager.getTotalCacheSize(getContext()));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        },
                        v1 -> {
                            // 处理取消按钮点击事件
                            try {
                                tvMemorySize.setText(DataCleanManager.getTotalCacheSize(getContext()));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        });

        // 设置
        rlSettingSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });

        // 关于
        rlSettingAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });
    }

    /**
     * 获取版本号
     *
     * @return versionName 版本号
     */
    private String getVersion() {
        String versionName;
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return versionName;
    }


    /**
     * 模式切换
     *
     * @param event {@link ModelChangeEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnModelChangeEvent(ModelChangeEvent event) {
        String model = event.getMessage();
        if (!model.isEmpty()) {
            if ("simple".equals(model)) {
                tvSettingModel.setText("简易模式");
            } else if ("pro".equals(model)) {
                tvSettingModel.setText("专业模式");
            }

        }
    }

    /**
     * 语言
     *
     * @param event {@link LanguageChangeEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnLanguageChangeEvent(LanguageChangeEvent event) {
        String language = event.getMessage();
        if (!language.isEmpty()) {
            if ("0".equals(language)) {
                tvLanguage.setText("跟随系统");
            } else if ("1".equals(language)) {
                tvLanguage.setText("中文");
            }else if ("2".equals(language)) {
                tvLanguage.setText("英语");
            }else if ("3".equals(language)) {
                tvLanguage.setText("俄语");
            }else if ("4".equals(language)) {
                tvLanguage.setText("西班牙语");
            }else if ("5".equals(language)) {
                tvLanguage.setText("德语");
            }else if ("6".equals(language)) {
                tvLanguage.setText("日语");
            }

        }
    }
}