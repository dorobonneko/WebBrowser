package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.view.ViewGroup;
import com.moe.entity.Bookmark;
import java.util.List;
import android.content.res.TypedArray;
import android.view.View;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ImageView;
import com.moe.Mbrowser.R;
import android.os.Build;

public class FolderSelectAdapter extends RecyclerView.Adapter<FolderSelectAdapter.ViewHolder>
{
	private List<Bookmark> list;
	public FolderSelectAdapter(List<Bookmark> list){
		this.list=list;
	}
	@Override
	public FolderSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		LinearLayout ll=new LinearLayout(p1.getContext());
		ll.setOrientation(ll.HORIZONTAL);
		ll.addView(new ImageView(p1.getContext()));
		ll.addView(new TextView(p1.getContext()));
		return new ViewHolder(ll);
	}

	@Override
	public void onBindViewHolder(FolderSelectAdapter.ViewHolder p1, int p2)
	{
		if(Build.VERSION.SDK_INT>19)
		p1.itemView.setPaddingRelative(list.get(p2).getLevel()*p1.iv.getDrawable().getIntrinsicWidth(),0,0,0);
		else
		p1.itemView.setPadding(list.get(p2).getLevel()*p1.iv.getDrawable().getIntrinsicWidth(),0,0,0);
		p1.title.setText(list.get(p2).getTitle());
	}

	@Override
	public int getItemCount()
	{
		return list.size();
	}
	
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		TextView title;
		ImageView iv;
		public ViewHolder(LinearLayout v){
			super(v);
			iv=(ImageView)v.getChildAt(0);
			title=(TextView)v.getChildAt(1);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeightSmall,android.support.v7.appcompat.R.attr.selectableItemBackground,android.R.attr.textColorSecondary});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			v.setClickable(true);
			v.setForeground(ta.getDrawable(1));
			title.setTextColor(ta.getColor(2,0));
			ta.recycle();
			v.setGravity(Gravity.CENTER_VERTICAL);
			v.setOnClickListener(this);
			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			iv.setImageResource(R.drawable.ic_folder_outline);
		}

		@Override
		public void onClick(View p1)
		{
			if(oicl!=null)oicl.onItemClick(FolderSelectAdapter.this,getAdapterPosition());
		}

		
	}
	public void setOnItemClickListener(OnItemClickListener o){
		oicl=o;
	}
	private OnItemClickListener oicl;
	public abstract interface OnItemClickListener{
		void onItemClick(RecyclerView.Adapter ra,int pos);
	}
}
