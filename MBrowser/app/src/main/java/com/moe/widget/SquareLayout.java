package com.moe.widget;
import android.widget.*;
import android.content.*;
import android.util.*;

public class SquareLayout extends LinearLayout
{
	public SquareLayout(Context context,AttributeSet attrs){
		super(context,attrs);
		setOrientation(VERTICAL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
	
}
