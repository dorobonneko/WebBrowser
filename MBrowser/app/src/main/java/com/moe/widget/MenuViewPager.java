package com.moe.widget;
import android.support.v4.view.ViewPager;
import android.content.Context;
import android.view.View;
import java.util.List;
import android.widget.Adapter;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import com.moe.adapter.MenuAdapter;
import com.moe.adapter.ViewPagerAdapter;

public class MenuViewPager extends ViewPager
{
	public MenuViewPager(Context context,AttributeSet attrs)
	{
		super(context,attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int height=0;

// 下面遍历所有child的高度
		ViewPagerAdapter adapter=(ViewPagerAdapter)getAdapter();
		if(adapter!=null)
		for (int i=0;i < adapter.getCount(); i++)
		{

			View child=adapter.get(i);

			child.measure(widthMeasureSpec,

						  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			int h=child.getMeasuredHeight();

			if (h > height)// 采用最大的view的高度。
				height = h;
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
															MeasureSpec.EXACTLY);

		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);


		
	}

}
