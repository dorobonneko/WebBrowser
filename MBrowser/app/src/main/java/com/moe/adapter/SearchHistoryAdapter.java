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
import android.os.Handler;
import android.os.Message;

public class SearchHistoryAdapter extends RecyclerView.Adapter
{
	private Context context;
	private SearchHistory sh;
	private WebHistory wh;
	private EditText et;
	private Dialog d;
	private List list=new ArrayList<>();

	public SearchHistoryAdapter(Context context, EditText et, Dialog d)
	{
		this.context = context;
		this.et = et;
		this.d = d;
		sh = DataBase.getInstance(context);
		wh = DataBase.getInstance(context);
		refresh();
	}
	
	public void query(final String url)
	{
		if (url != null)
		{
			handler.removeMessages(1);
			handler.sendMessageDelayed(handler.obtainMessage(1, url), 100);}
	}
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{

		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.search_item, null));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder p1, int p2)
	{

		final ViewHolder vh=(ViewHolder)p1;
		if (p2 == list.size())
		{
			vh.add.setVisibility(ImageButton.GONE);
			vh.title.setGravity(Gravity.CENTER);
			vh.title.setText(R.string.clear_search_history);
			vh.url.setVisibility(View.GONE);
		}
		else
		{
			vh.title.setGravity(Gravity.LEFT | Gravity.CENTER);
			vh.add.setVisibility(ImageButton.VISIBLE);
			if (list.get(p2) instanceof String)
			{
				vh.title.setText(list.get(p2).toString());
				vh.url.setVisibility(View.GONE);
				vh.url.setText(list.get(p2).toString());
			}
			else
			{
				String[] data=(String[])list.get(p2);
				vh.title.setText(data[1]);
				vh.url.setVisibility(View.VISIBLE);
				vh.url.setText(data[0]);
			}
		}
	}

	@Override
	public int getItemCount()
	{
		return list.size() == 0 ?0: list.size() + 1;
	}
	public void refresh()
	{
		list.clear();
		list.addAll(sh.getSearchHistoryList());
		notifyDataSetChanged();
	}
	final Handler handler=new Handler(){

		@Override
		public void handleMessage(final Message msg)
		{
			switch(msg.what){
				case 0:
					list.addAll((List)msg.obj);
					notifyDataSetChanged();
					break;
				case 1:
					final String key=msg.obj.toString();
					new Thread(){
						public void run(){
							list.clear();
							List list=sh.querySearchHistory(key);
							List tmp=wh.queryWebHistory(key);
							if(tmp!=null)
								list.addAll(tmp);
							handler.sendMessage(handler.obtainMessage(0,list));
						}
					}.start();
					break;
			}
		}

	};
	class ViewHolder extends RecyclerView.ViewHolder
	{
		TextView title,url;
		ImageButton add;
		public ViewHolder(View v)
		{
			super(v);
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, context.getResources().getDisplayMetrics())));

			if (!(v instanceof TextView))
			{
				title = (TextView)v.findViewById(R.id.searchitem_title);
				add = (ImageButton)v.findViewById(R.id.searchitem_add);
				url = (TextView)v.findViewById(R.id.searchitem_url);
				add.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							et.setText(title.getText());
						}
					});
				itemView.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							if (getPosition() == list.size())
							{
								sh.clearSearchHistory();
								refresh();
							}
							else
							{
								LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(new Intent("com.moe.search").putExtra("text", url.getText().toString()));
								d.dismiss();}

						}
					});	
			}
		}
	}
}
