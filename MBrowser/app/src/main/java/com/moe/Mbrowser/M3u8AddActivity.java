package com.moe.Mbrowser;
import android.app.Activity;
import android.os.Bundle;
import java.io.InputStream;
import com.moe.m3u.M3uList;
import com.moe.m3u.tag.M3uTag;
import com.moe.m3u.M3uParser;
import com.moe.m3u.tag.M3uXStreamInfTag;
import com.moe.entity.DownloadInfo;
import com.moe.m3u.tag.M3uInfTag;
import java.util.ArrayList;
import java.io.IOException;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.ContentResolver;
import android.provider.MediaStore;
import android.database.Cursor;
import java.io.File;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import com.moe.entity.TaskInfo;
import com.moe.database.Download;
import com.moe.database.Sqlite;
import android.text.TextUtils;
import android.support.design.widget.TextInputLayout;
import java.io.FileInputStream;
import android.os.Handler;
import android.os.Message;
import android.net.Uri;
import android.content.Intent;
import android.support.v4.content.FileProvider;
import android.content.ContentProviderResult;
import android.content.ContentProvider;
import com.moe.database.Download.State;
import android.widget.Toast;

public class M3u8AddActivity extends Activity implements View.OnClickListener,Download.Callback
{

	@Override
	public void callback(TaskInfo ti, final Download.State state)
	{
		runOnUiThread(new Runnable(){

				@Override
				public void run()
				{
					switch(state){
						case SUCCESS:
							Toast.makeText(M3u8AddActivity.this,"下载中",Toast.LENGTH_SHORT).show();
							break;
						case FAIL:
							Toast.makeText(M3u8AddActivity.this,"添加失败",Toast.LENGTH_SHORT).show();
							break;
					}
				}
			});
		
	}

	private TextInputLayout til;
	private TextView name,path;
	private ArrayList<DownloadInfo> ad=new ArrayList<>();
	private TaskInfo ti;
	private boolean ready,error=false;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ResourceService.start(this);
		super.onCreate(savedInstanceState);
		setContentView(LayoutInflater.from(this).inflate(R.layout.m3u8add_view, null), new ViewGroup.LayoutParams(getWindowManager().getDefaultDisplay().getWidth(), ViewGroup.LayoutParams.MATCH_PARENT));
		int color=Color.parseColor(getResources().getTextArray(R.array.skin_color)[getSharedPreferences("moe", 0).getInt("color", 0)].toString());
		int red=Color.red(color);
		int green=Color.green(color);
		int blue=Color.blue(color);
		getWindow().getDecorView().getRootView().setBackgroundColor(Color.rgb(red - 30 < 0 ?0: red - 30, green - 30 < 0 ?0: green - 30, blue - 30 < 0 ?0: blue - 30));
		findViewById(R.id.m3u8add_view_cancel).setOnClickListener(this);
		findViewById(R.id.m3u8add_view_sure).setOnClickListener(this);
		name = (EditText)findViewById(R.id.m3u8add_view_name);
		til = (TextInputLayout)findViewById(R.id.m3u8add_view_hint);
		til.setErrorEnabled(true);
		path=((TextView)findViewById(R.id.m3u8add_view_path));
		ti=new TaskInfo();
		ti.setDir(this.getSharedPreferences("download", 0).getString(Download.Setting.DIR, Download.Setting.DIR_DEFAULT));
		Uri uri=getIntent().getData();
		String name=null;
		switch(uri.getScheme()){
			case "file":
				name=new File(uri.getPath()).getName();
				break;
			case "content":
				Cursor c=getContentResolver().query(getIntent().getData(),new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME},null,null,null);
				if(c.moveToFirst())
					name=c.getString(0);
				c.close();
				break;
		}
		this.name.setText(name);
		ti.setId((System.currentTimeMillis()+"").hashCode());
		parse();
	}
	private void parse()
	{
		new Thread(){
			public void run()
			{
				InputStream is = null;
				try
				{
					is=getContentResolver().openInputStream(getIntent().getData());
					M3uList ml=M3uParser.parse(is).getList();
					ad.clear();
					switch (ml.getType())
					{
						case MASTER:
							for (M3uTag mt:ml.getList())
							{
								if (mt instanceof M3uXStreamInfTag)
								{
									ti.setTaskurl(((M3uXStreamInfTag)mt).getUrl());
									return;
								}
							}
							break;
						case MEDIA:
							if (ml.isLive())throw new IOException("不支持直播m3u8");
							for (M3uTag mt:ml.getList())
							{
								if (mt instanceof M3uInfTag)
								{
									DownloadInfo di=new DownloadInfo();
									di.setTaskId(ti.getId());
									di.setNo(ad.size());
									di.setCurrent(0);
									di.setStart(0);
									di.setUrl(((M3uInfTag)mt).getUrl());
									ad.add(di);
								}
							}
							break;
					}
					ready=true;
				}
				catch (Exception i)
				{
					error=true;
					handler.obtainMessage(0).sendToTarget();
				}finally{
					try
					{
						if (is != null)is.close();
					}
					catch (IOException e)
					{}
				}
			}
		}.start();


	}

	@Override
	public void onClick(View p1)
	{
		switch (p1.getId())
		{
			case R.id.m3u8add_view_cancel:
				finish();
				break;
			case R.id.m3u8add_view_sure:
				if(error)return;
				if (TextUtils.isEmpty(name.getText().toString().trim()))
					til.setError("名称不能为空");
				else if(!ready) til.setError("数据正在加载，请稍等");
				else
				{
					ti.setTaskname(name.getText().toString().trim());
					ti.setDownloadinfo(ad);
					ti.setType("application/x-mpegURL");
					Sqlite.getInstance(this, Download.class).addTaskInfo(ti, this);
					finish();
				}
				break;
		}
	}

	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what){
				case 0:
					til.setError("此文件不是m3u8文件，或者不支持");
					break;
			}
		}
	
};

}
