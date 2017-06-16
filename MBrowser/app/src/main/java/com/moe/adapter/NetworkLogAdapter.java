package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.content.res.TypedArray;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View;
import android.view.ViewGroup;
import com.moe.utils.LinkedListMap;
import com.moe.fragment.NetworkLogFragment;
import java.util.List;
import android.view.Gravity;

public class NetworkLogAdapter extends RecyclerView.Adapter<NetworkLogAdapter.ViewHolder>
{
	private LinkedListMap<NetworkLogFragment.Type,List<String>> llm;
	private NetworkLogFragment.Type type;
	public NetworkLogAdapter(LinkedListMap<NetworkLogFragment.Type,List<String>> llm,NetworkLogFragment.Type type,NetworkLogFragment nlf){
		this.llm=llm;
		this.type=type;
		setOnItemClickListener(nlf);
		setOnItemLongClickListener(nlf);
	}

	public void setOnItemLongClickListener(OnItemLongClickListener oilcl)
	{
		this.oilcl = oilcl;
	}

	public void setOnItemClickListener(OnItemClickListener oicl)
	{
		this.oicl = oicl;
	}

	
	@Override
	public NetworkLogAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(new TextView(p1.getContext()));
	}

	@Override
	public void onBindViewHolder(NetworkLogAdapter.ViewHolder p1, int p2)
	{
		p1.title.setText(llm.getKey(type).get(p2));
	}

	@Override
	public int getItemCount()
	{
		try{
		return llm.getKey(type).size();
		}catch(Exception e){}
		return 0;
	}
	public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener,OnLongClickListener{
		TextView title;
		public ViewHolder(TextView v){
			super(v);
			this.title=v;
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeight,android.support.v7.appcompat.R.attr.selectableItemBackground,android.R.attr.textColorSecondary});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			v.setForeground(ta.getDrawable(1));
			v.setClickable(true);
			v.setTextColor(ta.getColor(2,0));
			ta.recycle();
			v.setGravity(Gravity.CENTER_VERTICAL);
			v.setOnClickListener(this);
			v.setOnLongClickListener(this);
			v.setEms(100);
		}

		@Override
		public void onClick(View p1)
		{
			if(oicl!=null)oicl.onItemClick(type,this);
		}

		@Override
		public boolean onLongClick(View p1)
		{
			if(oilcl!=null)return oilcl.onItemLongClick(type,this);
			return false;
		}


		
	}
	private OnItemLongClickListener oilcl;
	private OnItemClickListener oicl;
	public abstract interface OnItemClickListener{
		void onItemClick(NetworkLogFragment.Type type,RecyclerView.ViewHolder vh);
	}
	public abstract interface OnItemLongClickListener{
		boolean onItemLongClick(NetworkLogFragment.Type type,RecyclerView.ViewHolder vh);
	}
}
