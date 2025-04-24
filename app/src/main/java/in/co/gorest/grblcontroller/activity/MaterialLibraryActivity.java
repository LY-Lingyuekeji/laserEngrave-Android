
package in.co.gorest.grblcontroller.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.LaserMaterialAdapter;
import in.co.gorest.grblcontroller.adapters.MaterialLibraryAdapter;
import in.co.gorest.grblcontroller.base.BaseAlertDialog;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.model.Material;

public class MaterialLibraryActivity extends AppCompatActivity {

    // 用于日志记录的标签
    private final static String TAG = MaterialLibraryActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;
    // 素材库列表
    private RecyclerView materialLibraryRecyclerVie;



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
        DataBindingUtil.setContentView(this, R.layout.activity_material_library);

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
        // 素材库列表
        materialLibraryRecyclerVie = findViewById(R.id.recycler_view_material_library);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化 RecyclerView
        materialLibraryRecyclerVie.setLayoutManager(new GridLayoutManager(this, 2));

        // 创建数据源
        List<Material> materialList = new ArrayList<>();
        materialList.add(new Material("胶合板(2mm)", R.mipmap.ic_sc_muban));
        materialList.add(new Material("胶合板(5mm)", R.mipmap.ic_sc_muban));
        materialList.add(new Material("胶合板(8mm)", R.mipmap.ic_sc_muban));
        materialList.add(new Material("纸板(2mm)", R.mipmap.ic_sc_zhiban));
        materialList.add(new Material("牛皮纸(250g)", R.mipmap.ic_sc_niupizhi));
        materialList.add(new Material("平安树叶", R.mipmap.ic_sc_pinganye));
        materialList.add(new Material("不锈钢", R.mipmap.ic_sc_buxiugang));
        materialList.add(new Material("金属漆面", R.mipmap.ic_sc_jinshuqimian));
        materialList.add(new Material("皮革（1mm）", R.mipmap.ic_sc_pige));
        materialList.add(new Material("PVC/塑料", R.mipmap.ic_sc_suliao));
        materialList.add(new Material("黑色亚克力", R.mipmap.ic_sc_yakeli));
        materialList.add(new Material("橡胶印章", R.mipmap.ic_sc_xiangjiaoyinzhang));
        materialList.add(new Material("MDF板", R.mipmap.ic_sc_miduban));
        materialList.add(new Material("竹子", R.mipmap.ic_sc_zhuzi));
        materialList.add(new Material("软磁贴片", R.mipmap.ic_sc_ruancitiepian));
        materialList.add(new Material("食物", R.mipmap.ic_sc_shiwu));
        materialList.add(new Material("玻璃", R.mipmap.ic_sc_boli));
        materialList.add(new Material("布料", R.mipmap.ic_sc_buliao));
        materialList.add(new Material("陶瓷", R.mipmap.ic_sc_taoci));
        materialList.add(new Material("黄铜", R.mipmap.ic_sc_huangtong));
        materialList.add(new Material("纯铝", R.mipmap.ic_sc_chunlv));
        materialList.add(new Material("电路铜板", R.mipmap.ic_sc_dianlutongban));
        materialList.add(new Material("板岩", R.mipmap.ic_sc_banyan));
        // 设置适配器
        MaterialLibraryAdapter adapter = new MaterialLibraryAdapter(this, materialList);
        materialLibraryRecyclerVie.setAdapter(adapter);
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


}
