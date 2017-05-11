package com.moe.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import java.util.List;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;

public class ViewPagerAdapter extends PagerAdapter
{
	private List<View> lv;
public ViewPagerAdapter(List list){
	this.lv=list;
}
	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return lv.size();
	}

	@Override
	public boolean isViewFromObject(View p1, Object p2)
	{
		// TODO: Implement this method
		return p1==p2;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		container.addView(lv.get(position));
		return lv.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView(lv.get(position));
		super.destroyItem(container, position, object);
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return lv.get(position).getTag().toString();
	}
	
}
