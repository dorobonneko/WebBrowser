package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.moe.entity.TaskInfo;
import java.util.List;
import android.view.View;
import android.widget.TextView;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.content.res.TypedArray;
import java.text.DecimalFormat;
import android.util.TypedValue;
import android.widget.ProgressBar;
import android.widget.ImageView;
import com.moe.entity.DownloadInfo;
import com.moe.download.DownloadTask;
import java.math.BigDecimal;
import android.view.View.OnLongClickListener;

public class DownloadItemAdapter extends RecyclerView.Adapter
{
	private List<TaskInfo> l1,l2;
	private List<Integer> selected;
public DownloadItemAdapter(List<TaskInfo> l1,List<TaskInfo> l2,List<Integer> selected){
	this.l1=l1;
	this.l2=l2;
	this.selected=selected;
}
public TaskInfo getItem(int position){
	switch(getItemViewType(position)){
		case 0:
			return null;
			case 1:
			return l1.get(position-1);
			case 2:
			return l2.get(position-l1.size()-2);
	}
		return null;
		
}
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		switch(p2){
			case 0:
				return new Header(new TextView(p1.getContext()));
			case 1:
				return new ViewHolder1(LayoutInflater.from(p1.getContext()).inflate(R.layout.notification_view,p1,false));
			case 2:
				LinearLayout ll=new LinearLayout(p1.getContext());
				ll.setOrientation(ll.VERTICAL);
				ll.setGravity(Gravity.LEFT|Gravity.CENTER);
				ll.addView(new TextView(p1.getContext()));
				ll.addView(new TextView(p1.getContext()));
				return new ViewHolder2(ll);
		}
		return null;
	
}
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder p1, int p2)
	{
		if(p1 instanceof Header){
			switch(p2){
				case 0:
					((Header)p1).title.setText("当前任务");
					break;
				default:
					((Header)p1).title.setText("已完成任务");
				break;
			}
		}else if(p1 instanceof ViewHolder1){
			ViewHolder1 vh=(ViewHolder1)p1;
			TaskInfo ti=l1.get(p2-1);
			long size=0;
			long speed;
			if(ti.getDownloadinfo()!=null){
			for(DownloadInfo di:ti.getDownloadinfo()){
				size+=di.getCurrent()-di.getStart();
			}
			vh.pb.setProgress((int)(size/(double)ti.getLength()*100));
			}
			vh.title.setText(ti.getTaskname());
			vh.size.setText(new DecimalFormat("0.00").format(size/1024.0/1024)+"/"+new DecimalFormat("0.00").format(ti.getLength()/1024.0/1024)+"MB");
			if(ti.getTag()!=null){
				double time=(System.currentTimeMillis()-ti.getTag()[0])/1000.0;
				long s=size-ti.getTag()[1];
				speed=(long)(s/time);
				vh.speed.setVisibility(View.VISIBLE);
				vh.speed.setText(new DecimalFormat("0.00").format(speed/1024.0/1024)+"MB/s");
			}else
			vh.speed.setVisibility(View.INVISIBLE);
				if(ti.isDownloading())
				vh.state.setImageResource(R.drawable.ic_pause);
				else
				vh.state.setImageResource(R.drawable.ic_play);
			ti.setTag(System.currentTimeMillis(),size);
		}else if(p1 instanceof ViewHolder2){
			((ViewHolder2)p1).title.setText(l2.get(p2-l1.size()-2).getTaskname());
			((ViewHolder2)p1).summary.setText(new DecimalFormat("0.00").format(l2.get(p2-l1.size()-2).getLength()/1024.0/1024)+"MB");
		}
		if(selected.contains(p2))
			p1.itemView.setBackgroundResource(R.color.accent);
			else
			p1.itemView.setBackgroundColor(0x00000000);
	}

	@Override
	public int getItemCount()
	{
		return l1.size()+l2.size()+2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return position==0||position==l1.size()+1?0:position>0&&position<=l1.size()?1:2;
	}
	public class ViewHolder1 extends RecyclerView.ViewHolder{
		private TextView title,speed,size;
		private ProgressBar pb;
		private ImageView state;
		public ViewHolder1(View v){
			super(v);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeight,android.support.v7.appcompat.R.attr.selectableItemBackground});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			//v.setBackground(ta.getDrawable(1));
			v.setFocusable(true);
			v.setClickable(true);
			v.setForeground(ta.getDrawable(1));
			ta.recycle();
			v.setPaddingRelative((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0);
			title=(TextView)v.findViewById(R.id.notificationview_title);
			speed=(TextView)v.findViewById(R.id.notificationview_speed);
			size=(TextView)v.findViewById(R.id.notificationview_size);
			pb=(ProgressBar)v.findViewById(R.id.notificationview_progress);
			state=(ImageView)v.findViewById(R.id.notificationviewImage_state);
			View.OnClickListener vo=new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						if(ocl!=null)ocl.onClick(getPosition());
					}
				};
			itemView.setOnClickListener(vo);
			state.setOnClickListener(vo);
			pb.setMax(100);
			itemView.setOnLongClickListener(new View.OnLongClickListener(){

					@Override
					public boolean onLongClick(View p1)
					{
						if(olcl!=null)return olcl.onLongClick(getPosition());
						return false;
					}
				});
		}
	}
	public class ViewHolder2 extends RecyclerView.ViewHolder{
		TextView title,summary;
		public ViewHolder2(View v){
			super(v);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeight,android.support.v7.appcompat.R.attr.selectableItemBackground});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			//v.setBackground(ta.getDrawable(1));
			v.setFocusable(true);
			v.setClickable(true);
			v.setForeground(ta.getDrawable(1));
			title=(TextView)((ViewGroup)v).getChildAt(0);
			summary=(TextView)((ViewGroup)v).getChildAt(1);
			title.setTextAppearance(android.R.style.TextAppearance_Large);
			summary.setTextAppearance(android.R.style.TextAppearance_Small);
			title.setSingleLine();
			summary.setSingleLine();
			ta.recycle();
			v.setPaddingRelative((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,v.getResources().getDisplayMetrics()),0);
			itemView.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						if(ocl!=null)ocl.onClick(getPosition());
					}
				});
			itemView.setOnLongClickListener(new View.OnLongClickListener(){

					@Override
					public boolean onLongClick(View p1)
					{
						if(olcl!=null)return olcl.onLongClick(getPosition());
						return false;
					}
				});
		}
	}
	public class Header extends RecyclerView.ViewHolder{
		TextView title;
		public Header(View v){
			super(v);
			v.setBackgroundColor(0x30ffffff);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeight});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)/2));
			ta.recycle();
			title=(TextView)v;
			title.setGravity(Gravity.LEFT|Gravity.CENTER);
		}
	}
	public void setOnClickListener(OnClickListener o){
		this.ocl=o;
	}
	private OnClickListener ocl;
	public abstract interface OnClickListener{
		void onClick(int position);
	}
	public void setOnLongClickListener(OnLongClickListener o){
		this.olcl=o;
	}
	private OnLongClickListener olcl;
	public abstract interface OnLongClickListener{
		boolean onLongClick(int position);
	}
}
