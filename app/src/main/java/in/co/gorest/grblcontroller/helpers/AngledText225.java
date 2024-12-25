package in.co.gorest.grblcontroller.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class AngledText225 extends com.joanzapata.iconify.widget.IconButton {

    public AngledText225(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.save();
        canvas.rotate(-45, this.getWidth()/2 + 10, this.getHeight()/2);
        super.onDraw(canvas);
        canvas.restore();
    }
}
