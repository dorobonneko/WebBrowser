package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import java.util.List;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.graphics.Bitmap;
import com.moe.utils.ImageDraw;
import android.content.res.TypedArray;
import com.moe.entity.Bookmark;
import com.moe.utils.Theme;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder>
{
	private Context context;
	private List list;
	private Type type;
	private List<Integer> selected;
	private LruCache<Character,Bitmap> lc=new LruCache<Character,Bitmap>((int)Runtime.getRuntime().totalMemory() / 8){

		@Override
		protected int sizeOf(Character key, Bitmap value)
		{
		
			return value.getByteCount();
		}

		@Override
		protected void entryRemoved(boolean evicted, Character key, Bitmap oldValue, Bitmap newValue)
		{
			super.entryRemoved(evicted, key, oldValue, newValue);
			oldValue.recycle();
		}

		
	};
	public BookmarksAdapter(Context context,List list,Type type,List<Integer> sel){
		this.selected=sel;
		this.context=context;
		this.list=list;
		this.type=type;
	}
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.bookmark_item,null));
	}

	@Override
	public void onBindViewHolder(ViewHolder vh, int p2)
	{
		switch(type){
			case BOOKMARK:
				Bookmark bookmark=(Bookmark)list.get(p2);
				vh.title.setText(bookmark.getTitle());
				vh.url.setText(bookmark.getSummary());
				switch(bookmark.getType()){
					case 0:
						vh.url.setVisibility(View.GONE);
						vh.more.setVisibility(View.VISIBLE);
						vh.icon.setImageResource(R.drawable.ic_folder_outline);
						break;
					case 1:
						vh.url.setVisibility(View.VISIBLE);
						vh.more.setVisibility(View.GONE);
						char c=bookmark.getTitle().charAt(0);
						Bitmap b=lc.get(c);
						if(b!=null)
							vh.icon.setImageBitmap(b);
						else{
							b=ImageDraw.TextImage(c,false);
							lc.put(c,b);
							vh.icon.setImageBitmap(b);
						}
						break;
				}
				if(selected.indexOf(p2)!=-1)
					vh.itemView.setBackgroundColor(Theme.backgroundColor);
					else
					vh.itemView.setBackgroundColor(0x00000000);
				break;
			case HISTORY:
				vh.title.setText(((String[])list.get(p2))[1]);
				vh.url.setText(((String[])list.get(p2))[0]);
				vh.url.setVisibility(View.VISIBLE);
				vh.more.setVisibility(View.GONE);
				vh.icon.setImageResource(R.drawable.ic_web);
				break;
		}
	}

	@Override
	public int getItemCount()
	{
		return list.size();
	}
	public class ViewHolder extends RecyclerView.ViewHolder{
		private ImageView icon;
		private TextView title,url;
		private View more;
		public ViewHolder(View v){
			super(v);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeightSmall,android.support.v7.appcompat.R.attr.selectableItemBackground});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			v.setClickable(true);
			v.setForeground(ta.getDrawable(1));
			ta.recycle();
			icon=(ImageView)v.findViewById(R.id.bookmarkitem_icon);
			title=(TextView)v.findViewById(R.id.bookmarkitem_title);
			url=(TextView)v.findViewById(R.id.bookmarkitem_url);
			more=v.findViewById(R.id.bookmarkitem_more);
			itemView.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						if(oicl!=null)
							oicl.OnItemClick(BookmarksAdapter.this,ViewHolder.this);
						
					}
				});
			itemView.setOnLongClickListener(new View.OnLongClickListener(){

					@Override
					public boolean onLongClick(View p1)
					{
						if(oicll!=null)return oicll.OnItemLongClick(BookmarksAdapter.this,ViewHolder.this);
						return false;
					}
				});
		}
	}
	public void setOnItemClickListener(OnItemClickListener o){
		oicl=o;
	}
	public abstract interface OnItemClickListener{
		void OnItemClick(BookmarksAdapter ba,ViewHolder vh);
	}
	public void setOnItemLongClickListener(OnItemLongClickListener o){
		oicll=o;
	}
	public abstract interface OnItemLongClickListener{
		boolean OnItemLongClick(BookmarksAdapter ba,ViewHolder vh);
	}
	private OnItemClickListener oicl;
	private OnItemLongClickListener oicll;
	public enum Type{
		HISTORY,BOOKMARK;
	}
}
