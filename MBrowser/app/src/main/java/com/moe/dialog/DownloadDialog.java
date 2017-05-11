package com.moe.dialog;
import android.content.Context;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import android.widget.EditText;
import android.support.design.widget.TextInputLayout;
import com.moe.entity.DownloadInfo;
import com.moe.bean.DownloadItem;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import android.webkit.URLUtil;
import android.content.SharedPreferences;
import com.moe.Mbrowser.DownloadService;
import android.view.View;
import com.moe.database.Download;
import com.moe.database.DataBase;
import com.moe.entity.TaskInfo;
import com.moe.Mbrowser.FileExplorer;
import android.content.Intent;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import com.moe.Mbrowser.ResourceService;

public class DownloadDialog extends Dialog implements View.OnClickListener
{
	private EditText name,dir;
	private TextInputLayout namelayout;
	private SharedPreferences shared;
	private Download download;
	private DownloadItem di;
	private Context context;
	private AlertDialog ad;
	public DownloadDialog(Context context){
		super(context);
		this.context=context;
		download=DataBase.getInstance(context);
		shared=context.getSharedPreferences("download",0);
		ad=new AlertDialog.Builder(context).setMessage("请选择以下操作").setTitle("当前任务已存在")
		.setNeutralButton("取消", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
				}
			})
		.setNegativeButton("更新", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
				}
			})
		.setPositiveButton("覆盖", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
				}
			}).create();

	}

	public void setPath(String stringExtra)
	{
		dir.setText(stringExtra);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_item_view);
		name=(EditText)findViewById(R.id.download_item_view_name);
		namelayout=(TextInputLayout)findViewById(R.id.download_item_view_name_layout);
		dir=(EditText)findViewById(R.id.download_item_view_path);
		findViewById(R.id.download_item_view_sure).setOnClickListener(this);
		findViewById(R.id.download_item_view_cancel).setOnClickListener(this);
		dir.setOnClickListener(this);
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.download_item_view_sure:
				String taskname=name.getText().toString().trim().replaceAll("[*|/\\\"<>?]*","");
				if(taskname.length()>1){
				TaskInfo ti=new TaskInfo();
				ti.setTaskname(taskname);
				ti.setTaskurl(di.getUrl());
				ti.setCookie(di.getCookie());
				ti.setDir(dir.getText().toString());
				ti.setLength(di.getLength());
				ti.setSourceUrl(di.getSourceUrl());
				ti.setType(di.getMime());
				ti.setUseragent(di.getUserAgent());
				switch(download.addTaskInfo(ti)){
					case SUCCESS:
						dismiss();
						break;
					case UPDATE:
					case SAMEURL:
						ad.show();
						dismiss();
						break;
					case SAMENAME:
						namelayout.setError("当前任务名已存在，请更换名称！");
						break;
				}
				}else namelayout.setError("文件名不能为空！");
				break;
			case R.id.download_item_view_cancel:
				dismiss();
				break;
			case R.id.download_item_view_path:
				((Activity)context).startActivityForResult(new Intent(getContext(),FileExplorer.class).putExtra("path",dir.getText().toString()),233);
				break;
		}
	}


	
	public void show(DownloadItem di)
	{
		this.di=di;
		super.show();
		namelayout.setErrorEnabled(false);
		name.setText(di.getFileName());
		setTitle("文件大小"+new DecimalFormat("#.00").format(di.getLength()/1024.0/1024)+"MB");
		dir.setText(shared.getString("dir",ResourceService.dir));
	}
	
	/*public String getName(DownloadItem di){
		if(di.getContentDisposition()!=null)
		return di.getContentDisposition().substring(di.getContentDisposition().indexOf("=")+1).replaceAll("\"","");
		else{
		String name=di.getUrl().substring(di.getUrl().lastIndexOf("/"));
		int end=name.indexOf("?");
		if(end!=-1)
			name=name.substring(0,end);
			return name;
		}
	}*/
}
