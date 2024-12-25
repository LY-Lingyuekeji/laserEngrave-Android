
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.fragment.BtConnetModelFragment;
import in.co.gorest.grblcontroller.fragment.WiFiConnetModelFragment;

public class ConnectActivity extends AppCompatActivity {

    // 返回
    private ImageView ivBack;
    // 蓝牙模式
    private TextView tvBtModel;
    // WIFI模式
    private TextView tvWifiModel;
    // ViewPager 分页
    private ViewPager2 viewPager;
    // ViewPagerAdapter
    private ViewPagerAdapter adapter;
    // fragment数组
    private ArrayList<Fragment> fragments = new ArrayList<>();
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
        DataBindingUtil.setContentView(this, R.layout.activity_connect);

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
        // WIFI模式
        tvWifiModel = findViewById(R.id.tv_wifi_model);
        // 蓝牙模式
        tvBtModel = findViewById(R.id.tv_bt_model);
        // 分页
        viewPager = findViewById(R.id.view_pager_connect);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 添加fragment数据源
        fragments.add(new WiFiConnetModelFragment());
        fragments.add(new BtConnetModelFragment());

        adapter = new ViewPagerAdapter(this, fragments);

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(0);

        tvWifiModel.setSelected(true);
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

        // WIFI模式
        tvWifiModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                tvWifiModel.setSelected(true);
                tvBtModel.setSelected(false);
            }
        });

        // 蓝牙模式
        tvBtModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                tvWifiModel.setSelected(false);
                tvBtModel.setSelected(true);
            }
        });
    }

}
