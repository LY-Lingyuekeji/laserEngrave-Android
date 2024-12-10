
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.oned.Code128Writer;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.events.ScanResultMessageEvent;
import in.co.gorest.grblcontroller.fragment.HomeFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeAudioModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeBusinessCardModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeCopyModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeTextModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeVideoModelFragment;
import in.co.gorest.grblcontroller.fragment.QrCodeWiFiModelFragment;
import in.co.gorest.grblcontroller.fragment.SettingFragment;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class QrCodeActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private static final String TAG = QrCodeActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 文本
    private TextView tvTextModel;
    // WiFi
    private TextView tvWifiModel;
    // 名片
    private TextView tvBusinessCardModel;
    // 复制
    private TextView tvCopyModel;
    // 音频
    private TextView tvAudioModel;
    // 视频
    private TextView tvVideoModel;
    // 分页
    private ViewPager2 viewPagerQrcode;
    //fragment数组
    private ArrayList<Fragment> fragments = new ArrayList<>();
    // PagerAdapter
    private ViewPagerAdapter adapter;



    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 绑定视图
        DataBindingUtil.setContentView(this, R.layout.activity_qrcode);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);
        // 文本
        tvTextModel = findViewById(R.id.tv_text_model);
        // Wi-Fi
        tvWifiModel = findViewById(R.id.tv_wifi_model);
        // 名片
        tvBusinessCardModel = findViewById(R.id.tv_business_card_model);
        // 复制
        tvCopyModel = findViewById(R.id.tv_copy_model);
        // 音频
        tvAudioModel = findViewById(R.id.tv_audio_model);
        // 输入框
        tvVideoModel = findViewById(R.id.tv_video_model);
        // 分页
        viewPagerQrcode = findViewById(R.id.view_pager_qrcode);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 添加数据源
        fragments.add(new QrCodeTextModelFragment());
        fragments.add(new QrCodeWiFiModelFragment());
        fragments.add(new QrCodeBusinessCardModelFragment());
        fragments.add(new QrCodeCopyModelFragment());
        fragments.add(new QrCodeAudioModelFragment());
        fragments.add(new QrCodeVideoModelFragment());

        adapter = new ViewPagerAdapter(this, fragments);

        viewPagerQrcode.setAdapter(adapter);
        viewPagerQrcode.setUserInputEnabled(false);
        viewPagerQrcode.setOffscreenPageLimit(3);
        viewPagerQrcode.setCurrentItem(0);

        tvTextModel.setSelected(true);
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        // 返回
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 文本
        tvTextModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(0);
                tvTextModel.setSelected(true);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // Wi-Fi
        tvWifiModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(1);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(true);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // 名片
        tvBusinessCardModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(2);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(true);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // 复制
        tvCopyModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(3);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(true);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(false);
            }
        });

        // 音频
        tvAudioModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(4);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(true);
                tvVideoModel.setSelected(false);
            }
        });

        // 视频
        tvVideoModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerQrcode.setCurrentItem(5);
                tvTextModel.setSelected(false);
                tvWifiModel.setSelected(false);
                tvBusinessCardModel.setSelected(false);
                tvCopyModel.setSelected(false);
                tvAudioModel.setSelected(false);
                tvVideoModel.setSelected(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // 用户取消扫描
                Toast.makeText(this, "用户取消扫描", Toast.LENGTH_SHORT).show();
            } else {
                // 处理扫描结果
                String scanResult = result.getContents();
                Log.d(TAG, "scanResult=" + scanResult);
                // TODO: 在这里处理扫描结果
                EventBus.getDefault().post(new ScanResultMessageEvent(scanResult));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
