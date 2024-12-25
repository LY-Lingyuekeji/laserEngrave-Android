package in.co.gorest.grblcontroller.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class AngledTextBottomLeft extends com.joanzapata.iconify.widget.IconButton {

    public AngledTextBottomLeft(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.save();
        canvas.rotate(45, this.getWidth()/2 - 10, this.getHeight()/2);
        super.onDraw(canvas);
        canvas.restore();
    }
}
