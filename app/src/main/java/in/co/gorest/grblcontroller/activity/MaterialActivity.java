
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.MaterialAdapter;
import in.co.gorest.grblcontroller.model.PictureBean;

public class MaterialActivity extends AppCompatActivity {

    // TAG
    private final String TAG = MaterialActivity.class.getSimpleName();
    // 页面跳转Code
    private final static int ACTIVITY_CODE_FINISH = 5000;
    private final static int ACTIVITY_CODE_DATA = 5001;
    // 返回
    private ImageView ivBack;
    // 素材
    private RecyclerView mRecycleView;
    // 适配器
    private MaterialAdapter materialAdapter;
    // 素材列表
    private List<PictureBean> PictureList;

    // 业务模式
    private int businessType;

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
        DataBindingUtil.setContentView(this, R.layout.activity_material);

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
        // 素材
        mRecycleView = findViewById(R.id.material_recycle_view);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取业务模式
        businessType = getIntent().getIntExtra("businessType", 1);
        Log.d(TAG, "businessType=" + businessType);
        // 设置布局管理器
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(gridLayoutManager);
        // 添加素材
        PictureList = new ArrayList<>();
        PictureBean pictureBean1 = new PictureBean();
        pictureBean1.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_a));
        PictureList.add(pictureBean1);
        PictureBean pictureBean2 = new PictureBean();
        pictureBean2.setUrl(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.icon_b));
        PictureList.add(pictureBean2);
        // 实例化素材适配器
        materialAdapter = new MaterialAdapter(getApplicationContext(), PictureList);
        mRecycleView.setAdapter(materialAdapter);

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


        // 素材
        materialAdapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
                int position = viewHolder.getAbsoluteAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Intent intent = new Intent(MaterialActivity.this, EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", PictureList.get(position).getUrl());
                intent.putExtra("businessType", businessType);
                startActivityForResult(intent, businessType == 1 ? ACTIVITY_CODE_FINISH : ACTIVITY_CODE_DATA);
                Log.d(TAG, PictureList.get(position).getUrl().toString());
                finish();
            }
        });
    }

}
