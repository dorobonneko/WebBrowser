package com.moe.widget;
import android.view.View;
import android.graphics.Canvas;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Paint;
import com.moe.utils.Theme;
import android.graphics.Bitmap;
import android.os.Parcelable;

public class CameraBorder extends View
{
	//取景框大小
	private int width=300,height=300;
	private int color=0xffffffff;
	private Rect rect;
	private Region region;
	private Paint paint=new Paint();
	//短长和长长
	private int len=5,length=25;
	//蒙层颜色
	private int bgColor=0x50000000;
	private Bitmap background;
	private boolean loop;
	private Rect line;
	private int y=0;
public CameraBorder(Context context,AttributeSet attrs){
	super(context,attrs);
	setWillNotDraw(false);
	rect=new Rect();
	region=new Region();
	paint.setAntiAlias(true);
	paint.setDither(true);
	line=new Rect();
}
	@Override
	protected void onDraw(Canvas canvas)
	{
		if(background==null)background=drawBackground();
		canvas.drawBitmap(background,0,0,null);
		canvas.drawRect(line,paint);
	}
	public void setSize(int width,int height){
		this.width=width;
		this.height=height;
		background=drawBackground();
		invalidate();
	}
	//与主题系统对接
	@Override
	public void setBackgroundColor(int color)
	{
		this.color=color;
		background=drawBackground();
		invalidate();
	}

	//绘制背景，避免重复绘制浪费资源
	private Bitmap drawBackground(){
		if(background!=null)background.recycle();
		try{
		Bitmap bitmap=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(bitmap);
		int size=getWidth()<getHeight()?getWidth():getHeight();
		rect.left=(size-width)/2;
		rect.right=size-rect.left;
		rect.top=(size-height)/2;
		rect.bottom=size-rect.top;
		line.left=rect.left+len;
		line.right=rect.right-len;
		region.set(0,0,getWidth(),getHeight());
		region.op(rect,Region.Op.DIFFERENCE);//将两者未重叠部分显示出来
		canvas.save();
		canvas.clipRegion(region);
		canvas.drawColor(bgColor);
		canvas.restore();
		canvas.save();
		canvas.clipRect(rect);
		paint.setColor(color);
		float[] data=new float[]{
			rect.left,rect.top,rect.left+length,rect.top+len,
			rect.right-length,rect.top,rect.right,rect.top+len,
			rect.left,rect.top,rect.left+len,rect.top+length,
			rect.right-len,rect.top,rect.right,rect.top+length,
			rect.left,rect.bottom-len,rect.left+length,rect.bottom,
			rect.right-length,rect.bottom-len,rect.right,rect.bottom,
			rect.left,rect.bottom-length,rect.left+len,rect.bottom,
			rect.right-len,rect.bottom-length,rect.right,rect.bottom
		};
		for(int i=0;i<data.length;i+=4)
			canvas.drawRect(data[i],data[i+1],data[i+2],data[i+3],paint);
		return bitmap;
		}catch(Exception e){}
		return null;
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		start();
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		loop=false;
		return super.onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		start();
		super.onRestoreInstanceState(state);
	}
	private void start(){
		loop=true;
		new Thread(){
			public void run(){
				while(loop){

					line.top=rect.top+length+y;
					line.bottom=line.top+len;
					y++;
					postInvalidate();
					if(line.bottom>=rect.bottom-length)y=0;
					try
					{
						sleep(16);//60帧
					}
					catch (InterruptedException e)
					{}
				}
			}
		}.start();
	}
	public Rect getFocusArea(){
		return rect;
	}
}
