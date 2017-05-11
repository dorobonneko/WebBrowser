package com.moe.widget;
import android.support.v4.view.ViewPager;
import android.content.Context;
import android.view.View;
import java.util.List;

public class MenuViewPager extends ViewPager
{
	private List<View> lv;
	public MenuViewPager(Context context,List<View> lv)
	{
		super(context);
		this.lv=lv;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int height=0;

// 下面遍历所有child的高度

		for (int i=0;i < lv.size(); i++)
		{

			View child=lv.get(i);

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
