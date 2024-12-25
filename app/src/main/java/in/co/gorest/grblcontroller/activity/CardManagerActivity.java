
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.CardManagerListItemAdapter;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.EngraveListItem;

public class CardManagerActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = CardManagerActivity.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例
    protected EnhancedSharedPreferences sharedPref;
    // 返回
    private ImageView ivBack;
    // 显示的项目
    private RecyclerView recyclerViewVisible;
    // 隐藏的项目
    private RecyclerView recyclerViewHidden;
    // 没有隐藏的项目
    private TextView tvHiddenEmpty;
    // 显示的项目 Adapter
    private CardManagerListItemAdapter adapterVisible;
    // 隐藏的项目 Adapter
    private CardManagerListItemAdapter adapterHidden;
    // 显示的项目 数据源
    private List<EngraveListItem> visibleItems = new ArrayList<>();
    // 隐藏的项目 数据源
    private List<EngraveListItem> hiddenItems = new ArrayList<>();


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
        DataBindingUtil.setContentView(this, R.layout.activity_card_manager);

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
        // 显示的项目
        recyclerViewVisible = findViewById(R.id.recycler_view_visible);
        // 隐藏的项目
        recyclerViewHidden = findViewById(R.id.recycler_view_hidden);
        // 没有隐藏的项目
        tvHiddenEmpty = findViewById(R.id.tv_hidden_empty);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 从 Prefs 获取数据源
        loadListFromPrefs();
        // 设置LayoutManager
        recyclerViewVisible.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHidden.setLayoutManager(new LinearLayoutManager(this));
        // 初始化adapter
        adapterVisible = new CardManagerListItemAdapter(getApplicationContext(), visibleItems);
        adapterHidden = new CardManagerListItemAdapter(getApplicationContext(), hiddenItems);
        // 设置Adapter适配器
        recyclerViewVisible.setAdapter(adapterVisible);
        recyclerViewHidden.setAdapter(adapterHidden);
        // 初始化 ItemTouchHelper
        ItemTouchHelper touchHelperVisible = new ItemTouchHelper(itemTouchCallback);
        ItemTouchHelper touchHelperHidden = new ItemTouchHelper(itemTouchCallback);
        // 将 touchHelper 附加到 RecyclerView
        touchHelperVisible.attachToRecyclerView(recyclerViewVisible);
        touchHelperHidden.attachToRecyclerView(recyclerViewHidden);
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
     * Prefs 保存数据源
     */
    public void saveListToPrefs() {
        List<EngraveListItem> allItems = new ArrayList<>();
        allItems.addAll(visibleItems);
        allItems.addAll(hiddenItems);

        // 转换为JSON
        JSONArray jsonArray = new JSONArray();
        for (EngraveListItem item : allItems) {
            jsonArray.put(item.toJSON());
        }

        // 保存到 SharedPreferences
        sharedPref.edit().putString(getString(R.string.preference_engrave_list_item), jsonArray.toString()).apply();
    }


    /**
     * 从 Prefs 获取数据源
     *
     * @return itemList
     */
    public List<EngraveListItem> loadListFromPrefs() {
        // 获取保存的列表数据
        String itemsJsonArray = sharedPref.getString(getString(R.string.preference_engrave_list_item), null);
        // 数据源
        List<EngraveListItem> itemList = new ArrayList<>();
        if (itemsJsonArray != null) {
            Log.d(TAG, "itemsJsonArray=" + itemsJsonArray);
            try {
                JSONArray array = new JSONArray(itemsJsonArray);
                for (int i = 0; i < array.length(); i++) {
                    EngraveListItem item = EngraveListItem.fromJSON(array.getString(i));
                    if (item != null) {
                        itemList.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        // 根据可见性将项目添加到不同的列表中
        visibleItems.clear();
        hiddenItems.clear();
        for (EngraveListItem item : itemList) {
            if (item.isVisible()) {
                visibleItems.add(item);
            } else {
                hiddenItems.add(item);
            }
        }


        return itemList;
    }


    /**
     * 实现 ItemTouchHelper.Callback 用于处理拖动
     */
    private final ItemTouchHelper.Callback itemTouchCallback = new ItemTouchHelper.Callback() {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if (recyclerView == recyclerViewVisible) {
                Collections.swap(visibleItems, fromPosition, toPosition);
                adapterVisible.notifyItemMoved(fromPosition, toPosition);
            } else {
                Collections.swap(hiddenItems, fromPosition, toPosition);
                adapterHidden.notifyItemMoved(fromPosition, toPosition);
            }

            saveListToPrefs();
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // 处理从一个 RecyclerView 拖动到另一个 RecyclerView
            int position = viewHolder.getAdapterPosition();
            EngraveListItem item;

            if (viewHolder.itemView.getParent() == recyclerViewVisible) {
                // 从可见列表拖动到隐藏列表
                item = visibleItems.remove(position);
                item.setVisible(false);
                hiddenItems.add(item);
                adapterVisible.notifyItemRemoved(position);
                adapterHidden.notifyItemInserted(hiddenItems.size() - 1);
            } else {
                // 从隐藏列表拖动到可见列表
                item = hiddenItems.remove(position);
                item.setVisible(true);
                visibleItems.add(item);
                adapterHidden.notifyItemRemoved(position);
                adapterVisible.notifyItemInserted(visibleItems.size() - 1);
            }

            saveListToPrefs();
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    };
}
