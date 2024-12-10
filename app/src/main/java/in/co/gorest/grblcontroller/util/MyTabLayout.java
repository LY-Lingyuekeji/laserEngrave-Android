package in.co.gorest.grblcontroller.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;

/**
 * 作者: liuhuaqian on 2020-12-17.
 */
public class MyTabLayout extends TabLayout {
    private List<String> titles;
    private int itemPressed, itemNormal;

    public MyTabLayout(Context context) {
        super(context);
        init();
    }

    public MyTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void reTitles() {
        if (titles != null)
            titles.clear();
        this.removeAllTabs();
    }

    private void init() {
        titles = new ArrayList<>();
        itemPressed = R.drawable.tablayout_item_pressed;
        itemNormal = R.drawable.tablayout_item_normal;
        this.addOnTabSelectedListener(new OnTabSelectedListener() {

            @Override
            public void onTabSelected(Tab tab) {
                /**
                 * 设置当前选中的Tab为特殊高亮样式。
                 */
                if (tab != null && tab.getCustomView() != null) {
                    TextView tab_layout_text = tab.getCustomView().findViewById(R.id.tab_layout_text);

                    tab_layout_text.setTextColor(getResources().getColor(R.color.colorBlack));
                    tab_layout_text.setBackgroundResource(itemPressed);
                }
            }

            @Override
            public void onTabUnselected(Tab tab) {
                /**
                 * 重置所有未选中的Tab颜色、字体、背景恢复常态(未选中状态)。
                 */
                if (tab != null && tab.getCustomView() != null) {
                    TextView tab_layout_text = tab.getCustomView().findViewById(R.id.tab_layout_text);

                    tab_layout_text.setTextColor(getResources().getColor(android.R.color.white));
                    tab_layout_text.setBackgroundResource(itemNormal);
                }
            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });
    }

    public void setTablayoutItemBg(int pressed, int normal) {
        itemPressed = pressed;
        itemNormal = normal;
    }

    public void setTitle(List<String> titles) {
        this.titles = titles;

        /**
         * 开始添加切换的Tab。
         */
        for (String title : this.titles) {
            Tab tab = newTab();
            tab.setCustomView(R.layout.tablayout_item);

            if (tab.getCustomView() != null) {
                TextView text = tab.getCustomView().findViewById(R.id.tab_layout_text);
                text.setText(title);
            }

            this.addTab(tab);

        }
    }

    public void setTitle(String titles) {
        if (this.titles == null)
            this.titles = new ArrayList<>();

        this.titles.add(titles);
        /**
         * 开始添加切换的Tab。
         */

        Tab tab = newTab();
        tab.setCustomView(R.layout.tablayout_item);

        if (tab.getCustomView() != null) {
            TextView text = tab.getCustomView().findViewById(R.id.tab_layout_text);
            text.setText(titles);
        }

        this.addTab(tab);

    }
}
