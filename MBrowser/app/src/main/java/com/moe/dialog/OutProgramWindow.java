package com.moe.dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import com.moe.database.BlackList;
import com.moe.database.DataBase;
import com.moe.database.Sqlite;

public class OutProgramWindow extends Dialog implements View.OnClickListener
{
	private String url;
	private static OutProgramWindow opw;
	private BlackList bl;
	private OutProgramWindow(Context context){
		super(context,R.style.searchDialog);
		bl=Sqlite.getInstance(context,BlackList.class);
	}

	public void show(String url)
	{
		show();
		this.url=url;
	}
	public static OutProgramWindow getInstance(Context context){
		if(opw==null)opw=new OutProgramWindow(context);
		return opw;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.PopupWindowAnim);
		super.onCreate(savedInstanceState);
		setContentView(LayoutInflater.from(getContext()).inflate(R.layout.out_program_view,null),new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT));
		findViewById(R.id.outprogram_allow).setOnClickListener(this);
		findViewById(R.id.outprogram_allow_once).setOnClickListener(this);
		findViewById(R.id.outprogram_reject).setOnClickListener(this);
		findViewById(R.id.outprogram_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View p1)
	{
		Intent intent=null;
		switch(p1.getId()){
			case R.id.outprogram_allow:
				bl.insertSite(url,BlackList.WHITE);
				intent=new Intent(intent.ACTION_VIEW,Uri.parse(url));
				try{
				getContext().startActivity(intent);
				}catch(Exception e){}
				break;
			case R.id.outprogram_allow_once:
				intent=new Intent(intent.ACTION_VIEW,Uri.parse(url));
				try{
				getContext().startActivity(intent);
				}catch(Exception e){}
				break;
			case R.id.outprogram_reject:
				bl.insertSite(url,BlackList.BLACK);
				break;
			case R.id.outprogram_cancel:
				break;
		}
		dismiss();
	}

	
}
