package com.moe.widget;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.moe.utils.Theme;

public class ProgressBar extends View
{
    private Rect rect=new Rect();
    private int max=100;
    private int progress=0;
    private int color=0xff66ccff;
    private Paint paint=new Paint();
    public ProgressBar(Context context){
        this(context,null);
    }
    public ProgressBar(Context context,AttributeSet attrs){
        super(context,attrs);
        setWillNotDraw(false);
        paint.setColor(color);
    }
    public void setProgress(int progress){
        this.progress=progress;
        postInvalidate();
    }
    public void setMax(int max){
        this.max=max;
        postInvalidate();
    }
//    public void setColor(int color){
//        this.color=color;
//        paint.setColor(color);
//        postInvalidate();
//    }

    
    
    @Override
    protected void onDraw(Canvas canvas)
    {
		paint.setColor(Theme.backgroundColor);
        rect.set(0,0,(int)((progress/(float)max)*getWidth()),getHeight());
        canvas.drawRect(rect,paint);
        super.onDraw(canvas);
    }
    
}
