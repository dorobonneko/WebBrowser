package com.moe.utils;
import android.support.v7.widget.*;
import android.graphics.*;
import android.view.*;
import android.support.v7.widget.RecyclerView.State;

public class CustomDecoration extends RecyclerView.ItemDecoration
{
	private int width;
	private Paint p=new Paint();
	public CustomDecoration(int width){
		this(width,Color.GRAY);
		p.setAlpha(0xaa);
	}
	public CustomDecoration(int width,int color){
		this.width=width;
		p.setColor(color);
		p.setAntiAlias(true);
		p.setDither(true);
	}

	@Override
	public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent)
	{
//		if(itemPosition!=parent.getChildCount()-1){
//		outRect.set(0,0,0,width);
//		parent.getChildAt(itemPosition).setPadding(0,0,0,width);}
		super.getItemOffsets(outRect, itemPosition, parent);
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
	{
		// TODO: Implement this method
		//super.getItemOffsets(outRect, view, parent, state);
		if(parent.getChildAdapterPosition(view)!=parent.getAdapter().getItemCount()-1)
		outRect.bottom=width;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent)
	{
		//左右偏移
		final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
		
		for(int i=0;i<parent.getChildCount()-1;i++){
			final View child = parent.getChildAt(i);
            android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
				.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + width;
            c.drawRect(left,top,right,bottom,p);
		}
	}
	
}
