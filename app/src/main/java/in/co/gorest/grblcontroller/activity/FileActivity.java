
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.ViewPagerAdapter;
import in.co.gorest.grblcontroller.fragment.LocalFileFragment;
import in.co.gorest.grblcontroller.fragment.RemoteFileFragment;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.WebSocketManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class FileActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = FileActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
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
    // 最大重试次数
    private int MAX_RETRY_NUM = 5;
    // 上传弹窗
    private AlertDialog dialogUpload;

    // 当前模式
    private String connectType;

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

        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));


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

        // 获取当前模式
        connectType = sharedPref.getString(getString(R.string.preference_connect_type), "AP");
        Log.d(TAG, "connectType=" + connectType);
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
                    File file = new File(filePath);
                    // 文件上传
                    uploadFile(file, MAX_RETRY_NUM);
                }
            }
        }
    }


    /**
     * 上传文件
     *
     * @param file 文件
     * @param num  重试次数
     */
    private void uploadFile(File file, final int num) {
        Log.d(TAG, "upload num=" + num);
        if (num > 0) {
            // 使用自定义布局创建 AlertDialog
            LayoutInflater inflater = LayoutInflater.from(FileActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_upload, null);
            // 获取 ProgressBar 和 TextView
            ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
            TextView progressText = dialogView.findViewById(R.id.progressText);
            // 创建弹窗
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FileActivity.this);
            alertDialogBuilder.setTitle("温馨提示");
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setCancelable(false);
            // UI线程
            runOnUiThread(() -> {
                dialogUpload = alertDialogBuilder.create();
                // 显示弹窗
                dialogUpload.show();
            });

            WebSocketManager webSocketManager = WebSocketManager.getInstance();
            webSocketManager.disconnect();

            // 初始化
            OkHttpUtils.getInstance();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)  // 连接超时
                    .readTimeout(120, TimeUnit.SECONDS)     // 读取超时
                    .writeTimeout(120, TimeUnit.SECONDS)    // 写入超时
                    .build();

            OkHttpUtils.initClient(client);

            if (connectType.equals("AP") || connectType.equals("STA")) {
                String ipAddress = sharedPref.getString(getString(R.string.preference_sta_type_ipaddress), "");
                if (!TextUtils.isEmpty(ipAddress)) {
                    Log.d(TAG, "ipAddress = " + ipAddress);
                    // 文件上传
                    OkHttpUtils.post().addFile("myfile[]", file.getName(), file)
                            .url("http://" + ipAddress + "/upload")
                            .addParams("path", "/")
                            .addParams("/" + file.getName(), String.valueOf(file.length()))
                            .tag(this).build().execute(new StringCallback() {

                                @Override
                                public void inProgress(float f, long j, int i) {
                                    super.inProgress(f, j, i);
                                    Log.e(TAG, "onResponse  inProgress=" + f + "---" + j + "---" + i);
                                    runOnUiThread(() -> {
                                        progressBar.setProgress((int) (f * 100.0f));
                                        progressText.setText(((int) (f * 100.0f)) + "%");
                                    });

                                }

                                @Override
                                public void onError(Call call, Exception exc, int i) {
                                    // 隐藏上传弹窗
                                    runOnUiThread(() -> {
                                        dialogUpload.dismiss();
                                    });
                                    Log.d(TAG, "e=" + exc.getMessage().toString());
                                    exc.printStackTrace();
                                    Toast.makeText(FileActivity.this, "上传失败，请检查并重试", Toast.LENGTH_SHORT).show();
                                    uploadFile(file, MAX_RETRY_NUM--);
                                }

                                @Override
                                public void onResponse(String str3, int i) {
                                    Log.e(TAG, "onResponse=" + str3);
                                    Toast.makeText(FileActivity.this, "上传完成", Toast.LENGTH_SHORT).show();
                                    // 隐藏上传弹窗
                                    runOnUiThread(() -> {
                                        dialogUpload.dismiss();
                                    });
                                    // 重新连接
                                    webSocketManager.connect(ipAddress);

                                    // 跳转雕刻页面
                                    Intent intent = new Intent(FileActivity.this, EngraveActivity.class);
                                    intent.putExtra("imagePath", "");
                                    intent.putExtra("filePath", file.getPath());
                                    startActivity(intent);
                                    finish();

                                }
                            });
                } else {
                    Toast.makeText(FileActivity.this, "上传地址为空，请联系客服！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(FileActivity.this, "蓝牙模式暂不支持TF上传，敬请期待下一版本", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(FileActivity.this, "上传失败，请检查并重试", Toast.LENGTH_SHORT).show();
        }

    }

}
