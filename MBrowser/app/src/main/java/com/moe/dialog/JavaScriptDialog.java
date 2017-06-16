package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.content.DialogInterface;
import java.util.ArrayList;
import java.util.List;
import com.moe.adapter.JavaScriptAdapter;
import com.moe.database.JavaScript;
import com.moe.database.Sqlite;
import android.widget.Toast;
import com.moe.internal.ToolManager;
import com.moe.webkit.WebViewManagerView;

public class JavaScriptDialog implements JavaScriptAdapter.OnClickListener
{
	public final static int SHOW=0xff85;
	private RecyclerView rv;
	private AlertDialog ad;
	private JavaScriptAddDialog jsad;
	private List<Object[]> list=new ArrayList<>();
	private JavaScriptAdapter jsa;
	private JavaScript javascript;
	public JavaScriptDialog(final Context context){
		ad=new AlertDialog.Builder(context)
		.setTitle("JavaScript")
		.setView(rv=new RecyclerView(context))
		.setPositiveButton("取消",null)
			.setNegativeButton("新增", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					if(jsad==null)jsad=new JavaScriptAddDialog(context);
					jsad.show();
					p1.dismiss();
				}
			}).create();
		LinearLayoutManager llm=new LinearLayoutManager(context);
		llm.setAutoMeasureEnabled(true);
		rv.setLayoutManager(llm);
		rv.setAdapter(jsa=new JavaScriptAdapter(list));
		javascript=Sqlite.getInstance(context,JavaScript.class);
		jsa.setOnClickalistener(this);
		}
	public void show(){
		ad.show();
		list.clear();
		list.addAll(javascript.getAllScript());
		jsa.notifyDataSetChanged();
	}

	@Override
	public void onClick(int position, int type)
	{
		switch(type){
			case 0:
				if(jsad==null)jsad=new JavaScriptAddDialog(ad.getContext());
				jsad.show(list.get(position));
				break;
			case 1:
				((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).loadUrl(list.get(position)[2].toString());
				Toast.makeText(ad.getContext(),list.get(position)[1].toString(),Toast.LENGTH_SHORT).show();
				break;
		}
	}

	
	
}
