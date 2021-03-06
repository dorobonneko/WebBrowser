package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import java.util.ArrayList;
import com.moe.bean.MenuItem;
import android.view.View;
import android.content.Context;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.List;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewTreeObserver;
import android.util.*;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder>
{
	private List<MenuItem> lm;
	
	public MenuAdapter(List<MenuItem> lm){
		this.lm=lm;
	}

	public MenuItem get(int p0)
	{
		return lm.get(p0);
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(LayoutInflater.from(p1.getContext()).inflate(R.layout.menu_item,null));
	}

	@Override
	public void onBindViewHolder(ViewHolder vh, int p2)
	{
		vh.itemView.setId(lm.get(p2).getId());
		vh.summary.setText(lm.get(p2).getSummory());
		vh.icon.setImageDrawable(lm.get(p2).getIcon());
		vh.summary.setTextColor(vh.summary.getResources().getColor(lm.get(p2).getColor()));
	}
	
	@Override
	public int getItemCount()
	{
		return lm.size();
	}
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		ImageView icon;
		TextView summary;
		public ViewHolder(View v){
			super(v);
			RecyclerView.LayoutParams rl=new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT);
			rl.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,v.getResources().getDisplayMetrics()),(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,v.getResources().getDisplayMetrics()),(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,v.getResources().getDisplayMetrics()),0);
			v.setLayoutParams(rl);
			icon=(ImageView)v.findViewById(R.id.menuitem_icon);
			summary=(TextView)v.findViewById(R.id.menuitem_summary);
			v.setOnClickListener(this);
		}

		@Override
		public void onClick(View p1)
		{
			if(oicl!=null)oicl.onItemClick(p1);
		}

		
	}
	public void setOnItemClickListener(OnItemClickListener o){
		this.oicl=o;
	}
	private OnItemClickListener oicl;
	public abstract interface OnItemClickListener{
		void onItemClick(View v);
	}
}
