package com.moe.widget;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import com.moe.Mbrowser.R;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class CircleImageView extends View
{
	private int color=0xffffffff;
	private boolean selected=false;
	private Paint paint=new Paint();
	public CircleImageView(Context context){
		this(context,null);
	}
	public CircleImageView(Context context,AttributeSet attrs){
		this(context,attrs,0);
	}
	public CircleImageView(Context context,AttributeSet attrs,int defStyle){
		super(context,attrs,defStyle);
		setWillNotDraw(false);
		paint.setColor(0xffffffff);
		paint.setAntiAlias(true);
		paint.setDither(true);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		//canvas.drawColor(0x00000000);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		canvas.drawCircle(getWidth()/2,getWidth()/2,getWidth()/3,paint);
		if(selected)
		{
			paint.setColor(0xff000000);
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			RectF rectf=new RectF();
			rectf.left=(getWidth()/2-getWidth()/3);
			rectf.top=rectf.left;
			rectf.right=getWidth()/3*2+rectf.left;
			rectf.bottom=rectf.right;
			canvas.drawArc(rectf,0,360,false,paint);
		}
		//super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}

	@Override
	public void setBackgroundColor(int color)
	{
		this.color=color;
		invalidate();
	}

	@Override
	public void setSelected(boolean selected)
	{
		this.selected=selected;
		invalidate();
	}
	
}
