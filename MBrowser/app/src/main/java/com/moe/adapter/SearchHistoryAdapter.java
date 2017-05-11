package com.moe.adapter;
import android.support.v7.widget.*;
import android.view.*;
import java.util.*;
import android.content.*;
import com.moe.Mbrowser.*;
import android.widget.*;
import com.moe.database.*;
import android.util.*;
import android.support.v4.content.*;
import android.app.*;

public class SearchHistoryAdapter extends RecyclerView.Adapter
{
	private Context context;
	private SearchHistory sh;
	private EditText et;
	private Dialog d;
	public SearchHistoryAdapter(Context context,EditText et,Dialog d){
		this.context=context;
		this.et=et;
		this.d=d;
		sh=DataBase.getInstance(context);
		refresh();
	}
	private List<String> list=new ArrayList<>();

	public void query(String url)
	{
		list.clear();
		list.addAll(sh.querySearchHistory(url));
		notifyDataSetChanged();
	}
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.search_item,null));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder p1, int p2)
	{
		
		final ViewHolder vh=(ViewHolder)p1;
		if(p2==list.size()){
			vh.add.setVisibility(ImageButton.GONE);
			vh.title.setGravity(Gravity.CENTER);
			vh.title.setText(R.string.clear_search_history);
			vh.title.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						sh.clearSearchHistory();
						refresh();
					}
				});
		}else{
		vh.title.setGravity(Gravity.LEFT|Gravity.CENTER);
			vh.title.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(new Intent("com.moe.search").putExtra("text", ((TextView)p1).getText().toString()));
						d.dismiss();
						
					}
				});
		vh.add.setVisibility(ImageButton.VISIBLE);
		vh.title.setText(list.get(p2));
		vh.add.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					et.setText(vh.title.getText());
				}
			});
			}
	}

	@Override
	public int getItemCount()
	{
		return list.size()==0?0:list.size()+1;
	}
	public void refresh(){
		list.clear();
		list.addAll(sh.getSearchHistoryList());
		notifyDataSetChanged();
	}
	class ViewHolder extends RecyclerView.ViewHolder{
		TextView title;
		ImageButton add;
		public ViewHolder(View v){
			super(v);
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,45,context.getResources().getDisplayMetrics())));
			
			if(!(v instanceof TextView)){
			title=(TextView)v.findViewById(R.id.searchitem_title);
			add=(ImageButton)v.findViewById(R.id.searchitem_add);}
		}
	}
}
