package com.moe.adapter;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.File;
import android.widget.TextView;

public class FolderAdapter extends BaseAdapter
{
	private ArrayList<String> list;
	private Context context;
public FolderAdapter(Context context,List list){
	this.list=(ArrayList)list;
	this.context=context;
}
	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return list.size();
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return list.get(p1);
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return p1;
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		TextView tv=new TextView(context);
		tv.setText(list.get(p1));
		tv.setPadding(20,15,20,15);
		tv.setTextSize(18);
		return tv;
	}

}
