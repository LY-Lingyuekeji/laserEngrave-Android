
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.EngraveListItemAdapter;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.EngraveListItem;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class BeginEngraveActivity extends AppCompatActivity implements EngraveListItemAdapter.OnItemClickListener{

    // 用于日志记录的标签
    private final static String TAG = BeginEngraveActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 管理
    private TextView tvManager;
    // 列表
    private RecyclerView recyclerView;
    // 默认数据
    List<EngraveListItem> items = new ArrayList<>();
    // Adapter
    private EngraveListItemAdapter adapter;
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
        DataBindingUtil.setContentView(this, R.layout.activity_begin_engrave);

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
        // 管理
        tvManager = findViewById(R.id.tv_manager);
        // 列表
        recyclerView = findViewById(R.id.recycler_view);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取保存的列表数据
        String itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);

        if (itemsJsonArray == null) {
            // 默认数据
            items.add(new EngraveListItem(R.drawable.ic_star, "素材库", true));
//            items.add(new EngraveListItem(R.drawable.ic_file, "文件", true));
            items.add(new EngraveListItem(R.drawable.ic_photo, "相册", true));
            items.add(new EngraveListItem(R.drawable.ic_camera, "相机", true));
//            items.add(new EngraveListItem(R.drawable.ic_create, "画图", true));
//            items.add(new EngraveListItem(R.drawable.ic_text, "文字", true));
//            items.add(new EngraveListItem(R.drawable.ic_calendar, "条形码", true));
//            items.add(new EngraveListItem(R.drawable.ic_qr_code, "二维码", true));

            // 转换为JSONArray
            JSONArray jsonArray = new JSONArray();
            for (EngraveListItem item : items) {
                jsonArray.put(item.toJSON());
            }
            sharedPref.edit().putString(getString(R.string.preference_engrave_list_item), jsonArray.toString()).apply();
            itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        }

        // 从 Prefs 获取数据源
        List<EngraveListItem> itemList = loadListFromPrefs(itemsJsonArray);
        // 设置LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 初始化adapter
        adapter = new EngraveListItemAdapter(getApplicationContext(), itemList, this);
        // 设置Adapter适配器
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 从 Prefs 获取数据源
        String itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);
        // 数据源
        List<EngraveListItem> itemList = loadListFromPrefs(itemsJsonArray);
        // 设置LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 初始化adapter
        adapter = new EngraveListItemAdapter(getApplicationContext(), itemList, this);
        // 设置Adapter适配器
        recyclerView.setAdapter(adapter);
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
        // 管理
        tvManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BeginEngraveActivity.this, CardManagerActivity.class));
            }
        });
    }

    /**
     * 从 Prefs 获取数据源
     * @return itemList
     */
    public List<EngraveListItem> loadListFromPrefs(String itemsJsonArray) {

        // 数据源
        List<EngraveListItem> itemList = new ArrayList<>();
        if (itemsJsonArray != null) {
            Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);
            try {
                JSONArray array = new JSONArray(itemsJsonArray);
                for (int i = 0; i < array.length(); i++) {
                    EngraveListItem item = EngraveListItem.fromJSON(array.getString(i));
                    if (item != null && item.isVisible()) {
                        itemList.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return itemList;
    }

    /**
     * item点击事件
     * @param item 子项
     */
    @Override
    public void onItemClick(EngraveListItem item) {
        switch (item.getText()) {
            case "素材库":
                startActivity(new Intent(this, MaterialActivity.class));
                break;
            case "文件":
                startActivity(new Intent(this, FileActivity.class));
                break;
            case "相册":
                ImgUtil.openAlbum(this);
                break;
            case "相机":
                ImgUtil.openCamera(this);
                break;
            case "画图":
                startActivity(new Intent(this, DrawBoardActivity.class));
                break;
            case "文字":
                startActivity(new Intent(this, TextCreateActivity.class));
                break;
            case "条形码":
                startActivity(new Intent(this, BarCodeActivity.class));
                break;
            case "二维码":
                startActivity(new Intent(this, QrCodeActivity.class));
                break;
        }
    }


    /**
     * 请求结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ImgUtil.CHOOSE_PHOTO) {
                Uri selectedImageUri = data.getData();
                Intent intent = new Intent(BeginEngraveActivity.this, EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", selectedImageUri);
                intent.putExtra("businessType", 1);
                startActivity(intent);
            } else if (requestCode == ImgUtil.TAKE_PHOTO) {
                Intent intent = new Intent(BeginEngraveActivity.this, EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", ImgUtil.imageUri);
                intent.putExtra("businessType", 1);
                startActivity(intent);
            }

        }
    }
}
