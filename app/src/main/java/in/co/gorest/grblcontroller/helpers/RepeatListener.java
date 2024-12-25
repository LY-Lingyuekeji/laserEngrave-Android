package in.co.gorest.grblcontroller.helpers;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class RepeatListener implements OnTouchListener {

    private final Handler handler = new Handler();

    private final boolean immediateClick;
    private final int initialInterval;
    private final int normalInterval;
    private boolean haveClicked;

    private View downView;

    private final Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            haveClicked = true;
            if(downView != null) downView.performClick();
            handler.postDelayed(this, normalInterval);
        }
    };

    public RepeatListener(boolean immediateClick, int initialInterval, int normalInterval){
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.immediateClick = immediateClick;
        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
    }

    public RepeatListener(){
        immediateClick = false;
        initialInterval = android.view.ViewConfiguration.getLongPressTimeout();
        normalInterval = initialInterval;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initialInterval);
                downView = view;
                if (immediateClick && downView != null) downView.performClick();
                haveClicked = immediateClick;
                return true;
            case MotionEvent.ACTION_UP:
                // If we haven't clicked yet, click now
                if (!haveClicked && downView != null) downView.performClick();

            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(handlerRunnable);
                downView = null;
                return true;
        }

        return false;
    }
}