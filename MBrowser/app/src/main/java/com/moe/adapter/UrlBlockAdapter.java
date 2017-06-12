package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.TypedArray;
import android.util.TypedValue;
import java.util.List;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.ViewFlipper;
import com.moe.internal.Theme;
import android.os.Build;

public class UrlBlockAdapter extends RecyclerView.Adapter<UrlBlockAdapter.ViewHolder>
{
	private List<String> list;
	private ViewFlipper toolbar;
	public UrlBlockAdapter(List<String> list,ViewFlipper vf){
		this.list=list;
		this.toolbar=vf;
	}
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(new TextView(p1.getContext()));
	}
	@Override
	public void onBindViewHolder(ViewHolder p1, int p2)
	{
		if(p2<2){
			if(p2==0)
				p1.title.setText("导入规则");
				else
				p1.title.setText("导出规则");
		}else{
			p1.title.setText(list.get(p2-2));
		}
		if(toolbar.getDisplayedChild()==1&&((Integer)toolbar.getTag()).intValue()==p2)
			p1.itemView.setBackgroundColor(Theme.color);
			else
			p1.itemView.setBackgroundColor(0x00000000);
	}

	@Override
	public int getItemCount()
	{
		return list.size()+2;
	}
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		TextView title;
		public ViewHolder(TextView v){
			super(v);
			title=v;
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeightSmall,android.support.v7.appcompat.R.attr.selectableItemBackground,android.R.attr.textColorSecondary});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			v.setForeground(ta.getDrawable(1));
			v.setClickable(true);
			v.setTextColor(ta.getColor(2,0));
			ta.recycle();
			if(Build.VERSION.SDK_INT>16)
			v.setPaddingRelative((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0,0,0);
			else
			v.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0,0,0);
			v.setGravity(Gravity.CENTER_VERTICAL);
			v.setOnClickListener(this);
		}

		@Override
		public void onClick(View p1)
		{
			if(oicl!=null)oicl.onItemClick(getAdapterPosition());
		}

		
	}
	public void setOnItemClickListener(OnItemClickListener o){
		oicl=o;
	}
	private OnItemClickListener oicl;
	public abstract interface OnItemClickListener{
		void onItemClick(int pos);
	;
	}
}
