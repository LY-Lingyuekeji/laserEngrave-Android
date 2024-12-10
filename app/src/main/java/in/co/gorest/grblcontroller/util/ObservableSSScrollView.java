package in.co.gorest.grblcontroller.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class ObservableSSScrollView extends HorizontalScrollView {
    private ScrollViewListener scrollViewListener = null;
    public ObservableSSScrollView(Context context) {
        super(context);
    }

    public ObservableSSScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableSSScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public interface ScrollViewListener {
        void onScrollChanged(ObservableSSScrollView scrollView, int x, int y, int oldx, int oldy);
    }
    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }
}
