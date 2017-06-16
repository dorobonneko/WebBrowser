package com.moe.widget;
import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;

public class RotateImageView extends ImageView implements Runnable
{
	private boolean isRotating=false;
	private int degrees=0;
	private Thread thread;
	public RotateImageView(Context context){
		super(context);
	}
	public RotateImageView(Context context,AttributeSet attrs){
		super(context,attrs);
	}
	public RotateImageView(Context context,AttributeSet attrs,int defStyle){
		super(context,attrs,defStyle);
	}
	public void setState(boolean state)
	{
		if(thread==null)thread=new Thread(this);
		if(state==true&&isRotating==false){
			isRotating=true;
			try{
			thread.start();
			}catch(Exception e){}
		}else{
			isRotating=false;
			degrees=0;
			invalidate();
			}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.rotate(degrees,getWidth()/2,getHeight()/2);
		super.onDraw(canvas);
	}

	@Override
	public void run()
	{
		while(isRotating){
			degrees++;
			postInvalidate();
			try
			{
				thread.sleep(16);
			}
			catch (InterruptedException e)
			{}
		}
	}

	
}
