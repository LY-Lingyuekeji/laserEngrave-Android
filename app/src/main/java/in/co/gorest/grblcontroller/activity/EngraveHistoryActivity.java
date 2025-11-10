
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.AddDeviceAdapter;
import in.co.gorest.grblcontroller.adapters.HistoryAdapter;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.fragment.LocalFileFragment;
import in.co.gorest.grblcontroller.fragment.RemoteFileFragment;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.EngraveHistoryRecord;
import in.co.gorest.grblcontroller.util.FileUtil;
import okhttp3.Call;

public class EngraveHistoryActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = EngraveHistoryActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 历史记录
    private RecyclerView rvEngraveHistory;
    // Laser列表适配器
    private HistoryAdapter adapter;

    private List<EngraveHistoryRecord> recordList = new ArrayList<>();


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
        DataBindingUtil.setContentView(this, R.layout.activity_engrave_history);

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
        // 历史记录
        rvEngraveHistory = findViewById(R.id.rv_engrave_history);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置LayoutManager
        rvEngraveHistory.setLayoutManager(new GridLayoutManager(this, 2));
        // 加载历史记录
        loadHistory();


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
    }

    private void loadHistory() {
        File file = new File(GrblController.getInstance().getExternalFilesDir(null) + "/history/", "engrave_history.json");
        if (!file.exists()) {
            Log.d(TAG, "文件不存在");
            return;
        }

        try {
            String jsonStr = FileUtil.readFile(file);
            Log.d(TAG, "读取到的文件内容: " + jsonStr); // 输出文件内容进行调试
            List<EngraveHistoryRecord> jsonList = new Gson().fromJson(jsonStr, new TypeToken<List<EngraveHistoryRecord>>(){}.getType());
            for (EngraveHistoryRecord record : jsonList) {
                recordList.add(record);
            }
            // 创建适配器，将设备数据传入适配器
            adapter = new HistoryAdapter(this, recordList);
            // 设置Adpter
            rvEngraveHistory.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
