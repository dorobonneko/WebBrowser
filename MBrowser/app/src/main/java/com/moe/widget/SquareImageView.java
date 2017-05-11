package com.moe.widget;
import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class SquareImageView extends ImageButton
{
	public SquareImageView(Context context){
		super(context);
		setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		
	}
	public SquareImageView(Context context,AttributeSet attrs){
		super(context,attrs);
		setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		
	}
	public SquareImageView(Context context,AttributeSet attrs,int style){
		super(context,attrs,style);
		setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// TODO: Implement this method
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
	
}
