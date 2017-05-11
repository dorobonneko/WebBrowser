package com.moe.widget;
import android.support.v7.widget.GridLayoutManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import com.moe.Mbrowser.R;

public class MenuGridLayoutManager extends GridLayoutManager
{
	public MenuGridLayoutManager(Context context,int size){
		super(context,size);
	}

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec)
	{
		/*try{
		View view = recycler.getViewForPosition(0);
		if(view != null){
			measureChild(view, widthSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightSpec),MeasureSpec.getMode(heightSpec)));
			int measuredWidth = MeasureSpec.getSize(widthSpec);
			int measuredHeight = view.getMeasuredHeight();
			int mode=MeasureSpec.getMode(measuredHeight);
			int size=MeasureSpec.getSize(measuredHeight)*2+view.getResources().getDimensionPixelSize(view.getResources().getIdentifier("status_bar_height", "dimen", "android"))+(int)view.getResources().getDimension(R.dimen.actionBarSize);
			setMeasuredDimension(measuredWidth, MeasureSpec.makeMeasureSpec(size,mode));
			}
			}catch(Exception e){}*/
			super.onMeasure(recycler,state,widthSpec,heightSpec);
	}
}
