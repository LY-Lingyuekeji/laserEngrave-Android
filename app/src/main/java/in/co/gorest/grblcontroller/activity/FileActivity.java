
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import java.util.ArrayList;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.fragment.LocalFileFragment;
import in.co.gorest.grblcontroller.fragment.RemoteFileFragment;
import in.co.gorest.grblcontroller.model.Constants;

public class FileActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = FileActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 本地文件
    private TextView tvLocalFile;
    // 设备SD卡文件
    private TextView tvRemoteFile;
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
        DataBindingUtil.setContentView(this, R.layout.activity_file);

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
        // 本地文件
        tvLocalFile = findViewById(R.id.tv_local_file);
        // 设备SD卡文件
        tvRemoteFile = findViewById(R.id.tv_remote_file);
        // 分页
        viewPager = findViewById(R.id.view_pager_connect);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 添加fragment数据源
        fragments.add(new LocalFileFragment());
        fragments.add(new RemoteFileFragment());

        adapter = new ViewPagerAdapter(this, fragments);

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(0);

        tvLocalFile.setSelected(true);
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

        // 本地文件
        tvLocalFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                tvLocalFile.setSelected(true);
                tvRemoteFile.setSelected(false);
            }
        });

        // 设备SD卡文件
        tvRemoteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                tvLocalFile.setSelected(false);
                tvRemoteFile.setSelected(true);
            }
        });
    }

    /**
     * 请求结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.FILE_PICKER_REQUEST_CODE) {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                if (filePath != null) {
                    Log.d(TAG, "filePath=" + filePath);

                }
            }
        }

    }
}
