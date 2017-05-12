package com.moe.Mbrowser;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.moe.fragment.MainFragment;
import com.moe.fragment.Fragment;
import android.widget.Toast;
import android.view.Gravity;
import android.view.Menu;
import com.moe.bean.Message;
import de.greenrobot.event.Subscribe;
import com.moe.bean.MenuOptions;
import com.moe.fragment.BookmarksFragment;
import de.greenrobot.event.EventBus;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import android.view.Window;
import android.content.SharedPreferences;
import android.content.Intent;
import com.moe.bean.DownloadItem;
import com.moe.dialog.DownloadDialog;
import com.moe.dialog.AlertDialog;
import com.moe.download.DownloadTask;
import com.moe.fragment.SettingFragment;
import android.webkit.GeolocationPermissions;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;

public class HomeActivity extends FragmentActivity
{
private SharedPreferences shared;
private Fragment current,main=new MainFragment(),bookmark=new BookmarksFragment();
private SettingFragment setting=new SettingFragment();
private DownloadDialog dd;
	private Message callback;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        long i=System.currentTimeMillis();
		shared=getSharedPreferences("moe",0);
        setContentView(R.layout.main);
        getSupportFragmentManager().beginTransaction().add(R.id.main,main).commit();
		current=main;
		dd=new DownloadDialog(this);
		if(shared.getBoolean("full",false))
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
			((MainFragment)main).setPadding(true);
		}
		startService(new Intent(this,ResourceService.class));
        Toast.makeText(this,"启动耗时："+(System.currentTimeMillis()-i),300).show();
		EventBus.getDefault().register(this);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 55);
			}
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(requestCode==49&&callback!=null){
			for(int i:grantResults){
				if(i!=PackageManager.PERMISSION_GRANTED)
				{
					((GeolocationPermissions.Callback)((Object[])callback.obj)[1]).invoke(((Object[])callback.obj)[0].toString(),false,false);
					callback=null;
					return;}
			}
			((GeolocationPermissions.Callback)((Object[])callback.obj)[1]).invoke(((Object[])callback.obj)[0].toString(),true,false);
			callback=null;
		}else if(requestCode==55){
			for(int i:grantResults){
				if(i!=PackageManager.PERMISSION_GRANTED){
					Toast.makeText(this,"无存储器读写权限，软件部分功能将无法使用",Toast.LENGTH_LONG).show();
					return;
					}
				}
		}
		
	}
	
	@Subscribe
	public void permission(Message callback){
		if(callback.what==49)
		this.callback=callback;
	}
	@Subscribe
	public void onDownload(DownloadItem di){
		dd.show(di);
	}
	@Subscribe
	public void onEvent(MenuOptions mo){
		switch(mo){
			case BOOKMARKS:
				if(bookmark.isAdded())
					getSupportFragmentManager().beginTransaction().hide(current).show(bookmark).commit();
					else
					getSupportFragmentManager().beginTransaction().hide(current).add(R.id.main,bookmark).commit();
					current=bookmark;
				break;
			case DOWNLOAD:
				//startService(new Intent(this,DownloadService.class));
				break;
			case HOME:
				if(current!=main){
					getSupportFragmentManager().beginTransaction().hide(current).show(main).commit();
					current=main;
				}
				break;
			case FULLSCREEN:
				if(!shared.getBoolean("full",false))
				{
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
					shared.edit().putBoolean("full",true).commit();
					((MainFragment)main).setPadding(true);
				}else{
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				shared.edit().putBoolean("full",false).commit();
					((MainFragment)main).setPadding(false);
				}
				
				break;
			case EXIT:
				super.onBackPressed();
				break;
			case SETTING:
				getSupportFragmentManager().beginTransaction().hide(current).commit();
				if(setting.isAdded()){
					getFragmentManager().beginTransaction().show(setting).commit();
				}else{
					getFragmentManager().beginTransaction().add(R.id.main,setting).commit();
					}
				break;
		}
	}
	@Subscribe
	public void download(DownloadTask dt){
		if(dt.getStateOfTask()==DownloadTask.State.DNS){
		AlertDialog ad=new AlertDialog(this);
		ad.setMessage(dt.getTaskInfo().getTaskname()+"/n程序已终止下载");
		ad.setTitle("疑似网络劫持");
		ad.show();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode==RESULT_OK){
			switch(requestCode){
				case 233:
					dd.setPath(data.getStringExtra("dir"));
					break;
			}
		}
	}

	
	@Override
	protected void onDestroy()
	{
		
		EventBus.getDefault().unregister(this);
		stopService(new Intent(this,ResourceService.class));
		super.onDestroy();
	}

    @Override
    public void onBackPressed()
    {
		if(setting.onBackPressed()){
			return;
		}
		else{
			if(setting.isAdded()&&!setting.isHidden()){
				getFragmentManager().beginTransaction().hide(setting).commit();
				getSupportFragmentManager().beginTransaction().show(current).commit();
			return;
				}
		}
		if(!current.onBackPressed()){
		if(current!=main){getSupportFragmentManager().beginTransaction().hide(current).show(main).commit();current=main;}
		else if(!main.onBackPressed())
        super.onBackPressed();
		}
    }
        
}
