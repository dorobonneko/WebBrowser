package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.Map;
import android.widget.TextView;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.util.TypedValue;

public class AdBlockAdapter extends RecyclerView.Adapter<AdBlockAdapter.ViewHolder>
{
	private Type type=Type.HOST;
	private Map<String,String> mss;
	private String[] selector;
	private String[] key;
	private int pos;
	private TextView title;
	public AdBlockAdapter(Map<String,String> mss,TextView title){
		this.mss=mss;
		this.title=title;
	}
	public Type getType(){
		return type;
	}
	public void setType(Type type,int position){
		this.type=type;
		switch(type){
			case HOST:
				title.setText("广告拦截");
				key=mss.keySet().toArray(new String[0]);
				break;
			case SELECTOR:
				pos=position;
				title.setText(key[position]);
				selector=mss.get(key[position]).split(",");
				break;
		}
	}
	public int getKeyPosition(){
		return pos;
	}
	public String getKey(int position){
		return key[position];
	}
	public String getValue(int position){
		return selector[position];
	}
	public String[] getValue(){
		return selector;
	}
	@Override
	public AdBlockAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(new TextView(p1.getContext()));
	}

	@Override
	public void onBindViewHolder(AdBlockAdapter.ViewHolder p1, int p2)
	{
		switch(type){
			case HOST:
				p1.name.setText(key[p2]);
				break;
			case SELECTOR:
				p1.name.setText(selector[p2]);
				break;
		}
	}

	@Override
	public int getItemCount()
	{
		return type==Type.HOST?mss.size():selector.length;
	}
	
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
		TextView name;
		public ViewHolder(View v){
			super(v);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeightSmall,android.support.v7.appcompat.R.attr.selectableItemBackground});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			v.setForeground(ta.getDrawable(1));
			v.setClickable(true);
			ta.recycle();
			v.setPaddingRelative((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0,0,0);
			name=(TextView)v;
			name.setGravity(Gravity.CENTER_VERTICAL);
			name.setOnClickListener(this);
			name.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View p1)
		{
			if(ocl!=null)ocl.onClick(getType(),getPosition());
		}

		@Override
		public boolean onLongClick(View p1)
		{
			if(olcl!=null)return olcl.onLongClick(getType(),getPosition());
			return false;
		}
		
	}
	private OnClickListener ocl;
	public void setOnClickListener(OnClickListener o){
		ocl=o;
	}
	public abstract interface OnClickListener{
		void onClick(Type type,int position);
	}
	private OnLongClickListener olcl;
	public void setOnLongClickListener(OnLongClickListener o){
		olcl=o;
	}
	public abstract interface OnLongClickListener{
		boolean onLongClick(Type type,int position);
	}
	public enum Type{
		HOST,SELECTOR;
	}
}
