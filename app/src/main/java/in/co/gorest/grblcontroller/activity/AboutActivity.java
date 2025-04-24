package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import com.hailong.appupdate.AppUpdateManager;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import in.co.gorest.grblcontroller.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = AboutActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 当前版本
    private TextView tvAboutVersion;
    // 检查更新
    private TextView tvAboutUpdate;

    // 更新内容
    private static String[] arrayContent = new String[]{""};
    // 服务器版本号
    private int serverVersionCode;
    // 服务器版本名
    private String serverVersionName;
    // 当前版本
    private int currentVersionCode;


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
        DataBindingUtil.setContentView(this, R.layout.activity_about);

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
        // 当前版本
        tvAboutVersion = findViewById(R.id.tv_about_version);
        // 检查更新
        tvAboutUpdate = findViewById(R.id.tv_about_update);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置当前版本
        tvAboutVersion.setText("当前版本：" + getVersion());
        // 获取服务器版本信息
        fetchVersionInfo();
        // 获取当前版本
        getCurrentAppVersion();
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

        // 检查更新
        tvAboutUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前日期并转换为yyyyMMdd格式
                String formattedDate = getCurrentDate();
                Log.d(TAG, "CurrentDate:" + formattedDate);  // 输出当前日期

                if (serverVersionCode > currentVersionCode) {
                    AppUpdateManager.Builder builder = new AppUpdateManager.Builder(AboutActivity.this);
                    builder.apkUrl("http://47.243.173.178/apk/iklestar-" + serverVersionName + "-" + formattedDate + ".apk")
                            .updateContent(arrayContent)
                            .updateForce(false)
                            .build();
                } else {
                    Toast.makeText(AboutActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
                }

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
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return versionName;
    }


    /**
     * 读取 versionInfo 模拟接口
     */
    public void fetchVersionInfo() {
        OkHttpClient client = new OkHttpClient();

        String url = "http://47.243.173.178/version-info.txt"; // 接口地址
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // 使用 UTF-8 编码读取文件内容
                        String versionInfo = new String(response.body().bytes(), StandardCharsets.UTF_8);
                        // 处理 versionInfo
                        parseVersionInfo(versionInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 处理 versionInfo
     * @param content 内容
     */
    private void parseVersionInfo(String content) {
        String[] lines = content.split("\n");
        String versionCode = lines[0].split("=")[1].trim();
        String versionName = lines[1].split("=")[1].trim();
        String versionContent = lines[2].split("=")[1].trim();
        // 1. 去掉首尾的中括号（即删除第一个和最后一个字符）
        versionContent = versionContent.substring(1, versionContent.length() - 1).trim();

        // 2. 按照逗号分割字符串
        String[] contentArray = versionContent.split(",");

        // 3. 去掉引号并将处理后的内容添加到一个 ArrayList 中
        ArrayList<String> arrayList = new ArrayList<>();
        for (String item : contentArray) {
            // 去掉每个元素的引号并添加到 ArrayList
            arrayList.add(item.replace("\"", "").trim());
        }


        // 在这里根据 versionCode, versionName 和 versionContent 进行版本检查等逻辑
        Log.d(TAG,"Version Code: " + versionCode);
        Log.d(TAG,"Version Name: " + versionName);
        Log.d(TAG,"Content: " + versionContent);

        serverVersionCode = Integer.parseInt(versionCode);
        serverVersionName = versionName;
        // 4. 将 ArrayList 转换为 String[] 并赋值给 arrayContent
        arrayContent = arrayList.toArray(new String[0]);
    }

    /**
     * 获取当前应用的 versionCode 和 versionName
     */
    private void getCurrentAppVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
             currentVersionCode = packageInfo.versionCode;
            String currentVersionName = packageInfo.versionName;

            // 打印当前应用的 versionCode 和 versionName
            Log.d(TAG, "Current versionCode: " + currentVersionCode);
            Log.d(TAG,"Current versionName: " + currentVersionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前日期并格式化为yyyyMMdd
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();  // 获取当前日期
        return sdf.format(date);  // 返回格式化后的日期
    }

}
