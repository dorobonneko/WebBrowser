package com.moe.dialog;
import android.view.*;
import android.widget.*;

import android.content.Context;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import com.moe.entity.TaskInfo;
import android.content.ClipboardManager;
import android.content.res.TypedArray;
import android.text.method.ScrollingMovementMethod;
import android.app.Service;
import android.os.Build;

public class TaskInfoDialog extends android.app.Dialog implements View.OnLongClickListener
{
	private TextView name,url,source,pause,thread,dir;
	private ClipboardManager cm;
	public TaskInfoDialog(Context context){
		super(context,R.style.Dialog);
		cm=(ClipboardManager)context.getSystemService(Service.CLIPBOARD_SERVICE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().setGravity(Gravity.BOTTOM);
		super.onCreate(savedInstanceState);
		LinearLayout ll=new LinearLayout(getContext());
		setContentView(ll,new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT));
		ll.setOrientation(ll.VERTICAL);
		name=new TextView(getContext());
		url=new TextView(getContext());
		source=new TextView(getContext());
		pause=new TextView(getContext());
		thread=new TextView(getContext());
		dir=new TextView(getContext());
		url.setMovementMethod(ScrollingMovementMethod.getInstance());
		source.setMovementMethod(ScrollingMovementMethod.getInstance());
		/**dir.setOnClickListener(this);
		name.setOnClickListener(this);
		url.setOnClickListener(this);
		source.setOnClickListener(this);
		pause.setOnClickListener(this);
		thread.setOnClickListener(this);
		name.setOnLongClickListener(this);
		url.setOnLongClickListener(this);
		source.setOnLongClickListener(this);
		pause.setOnLongClickListener(this);
		thread.setOnLongClickListener(this);
		dir.setOnLongClickListener(this);*/
		ll.addView(name);
		ll.addView(dir);
		ll.addView(url);
		ll.addView(source);
		ll.addView(pause);
		ll.addView(thread);
		ll.setBackgroundColor(getContext().getResources().getColor(R.color.window_background));
		TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeightSmall,android.support.v7.appcompat.R.attr.selectableItemBackground});
		for(int i=0;i<ll.getChildCount();i++){
		View v=ll.getChildAt(i);
		v.setOnLongClickListener(this);
		v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
		if(Build.VERSION.SDK_INT>16)
		v.setBackground(ta.getDrawable(1));
			else
		v.setBackgroundDrawable(ta.getDrawable(1));
		v.setClickable(true);
		}
		ta.recycle();
	}

	public void show(TaskInfo ti)
	{
		super.show();
		name.setText("名称：\n"+ti.getTaskname());
		url.setText("下载地址：\n"+ti.getTaskurl());
		source.setText("来源：\n"+ti.getSourceUrl());
		pause.setText("断点续传："+(ti.getSupport()==0?"未知":ti.getSupport()==1?"支持":"不支持"));
		thread.setText("多线程："+ti.isMultiThread());
		dir.setText("保存路径：\n"+ti.getDir());
	}

	@Override
	public boolean onLongClick(View p1)
	{
		if(p1==name){
			cm.setText(name.getText().toString().substring(4));
		}else if(p1==dir){
			cm.setText(dir.getText().toString().substring(6));
			
		}else if(p1==url){
			cm.setText(url.getText().toString().substring(6));
			
		}else if(p1==source){
			cm.setText(source.getText().toString().substring(4));
			
		}else
		return true;
		Toast.makeText(getContext(),"已复制",Toast.LENGTH_SHORT).show();
		return true;
	}


	
	
}
