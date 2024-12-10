
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.QuestionAdapter;
import in.co.gorest.grblcontroller.model.FAQ;

public class QuestionActivity extends AppCompatActivity {
    // 返回
    private ImageView ivBack;
    // 常见问题
    private RecyclerView recyclerViewQuestion;
    // 适配器
    private QuestionAdapter adapter;

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
        DataBindingUtil.setContentView(this, R.layout.activity_question);

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
        // 常见问题
        recyclerViewQuestion = findViewById(R.id.recycler_view_question);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        recyclerViewQuestion.setLayoutManager(new LinearLayoutManager(this));

        String faqContent = readTxtFile("faq.txt");  // 读取FAQ数据
        List<FAQ> faqList = processFAQData(faqContent);  // 处理成FAQ列表

        adapter = new QuestionAdapter(faqList);  // 创建适配器
        recyclerViewQuestion.setAdapter(adapter);  // 设置适配器

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

    /**
     * 读取TXT文件
     * @param filename 文件名
     * @return 文件内容
     */
    private String readTxtFile(String filename) {
        StringBuilder text = new StringBuilder();
        try {
            InputStream is = getAssets().open(filename); // 如果文件在assets文件夹中
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    /**
     * 处理读取的文件内容，将其分割成Q&A（问题和答案）的形式
     * @param content 内容
     * @return
     */
    private List<FAQ> processFAQData(String content) {
        List<FAQ> faqList = new ArrayList<>();
        String[] entries = content.split("\n\n"); // 假设每个问题和答案之间有一个空行分隔

        for (String entry : entries) {
            String[] qa = entry.split("\n");
            if (qa.length == 2) {  // 保证有一个问题和对应的答案
                faqList.add(new FAQ(qa[0], qa[1]));  // 将问题和答案添加到列表中
            }
        }
        return faqList;
    }
}
