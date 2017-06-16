package com.moe.Mbrowser;
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
import com.moe.download.DownloadTask;
import com.moe.fragment.SettingFragment;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import com.moe.fragment.DownloadFragment;
import com.moe.utils.DataUtils;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import android.os.Environment;
import com.moe.fragment.SkinFragment;
import android.view.View;
import android.view.ViewGroup;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import com.moe.dialog.BottomDialog;
import android.content.DialogInterface;
import com.moe.bean.WindowEvent;
import java.util.ArrayList;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import com.moe.dialog.ToolboxDialog;
import com.moe.entity.TaskInfo;
import com.moe.database.Download;
import android.os.Looper;
import com.moe.dialog.AlertDialog;
import com.moe.database.Sqlite;
import com.moe.dialog.JavaScriptDialog;
import com.moe.fragment.BitImageFragment;
import android.app.SearchManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.FragmentActivity;
import com.moe.internal.Theme;
import com.moe.internal.ToolManager;
import android.os.Build;
import android.content.ClipboardManager;
import android.widget.Button;
import com.moe.fragment.NetworkLogFragment;
import com.moe.webkit.WebViewManagerView;
import com.moe.fragment.InputFragment;

public class HomeActivity extends FragmentActivity implements Download.Callback
{
	private SharedPreferences shared;
	private Fragment current,main,bookmark,download,skin,bit,network,search;
	private SettingFragment setting;
	private DownloadDialog dd;
	private Message callback;
	private boolean exit=false;
	private ToolboxDialog toolbox;
	private android.support.v7.app.AlertDialog ad;
	private JavaScriptDialog jsd;
	private ClipboardManager cm;
	@Override
	public void callback(final TaskInfo ti, Download.State state)
	{
		switch(state){
			case SUCCESS:
				break;
			case FAIL:
				runOnUiThread(new Runnable(){

						@Override
						public void run()
						{
							if(ad==null)
								ad=new android.support.v7.app.AlertDialog.Builder(HomeActivity.this).setMessage("请选择以下操作").setTitle("当前任务已存在")
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
											Sqlite.getInstance(HomeActivity.this,Download.class).updateTaskInfo(ti);
											p1.dismiss();
										}
									})
									.setPositiveButton("覆盖", new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface p1, int p2)
										{
											Sqlite.getInstance(HomeActivity.this,Download.class).deleteTaskInfoWithId(ti.getId());
											Sqlite.getInstance(HomeActivity.this,Download.class).addTaskInfo(ti,null);
											p1.dismiss();
										}
									}).create();
							ad.show();
						}
					});
				break;
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		shared = getSharedPreferences("moe", 0);
		setContentView(R.layout.main);
		Theme.registerBackground(findViewById(R.id.main));
		dd = new DownloadDialog(this);
		if (shared.getBoolean("full", false))
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			//((MainFragment)main).setPadding(true);
		}
		if (shared.getBoolean("night", false))
			findViewById(R.id.main3).setBackgroundColor(0x50000000);
		startService(new Intent(this, ResourceService.class));
		EventBus.getDefault().register(this);
		if(Build.VERSION.SDK_INT>19)
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 55);
		}
		loadURL(getIntent());
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		switch (requestCode)
		{
			case 49:
				if (callback != null)
				{
					for (int i:grantResults)
					{
						if (i != PackageManager.PERMISSION_GRANTED)
						{
							((GeolocationPermissions.Callback)((Object[])callback.obj)[1]).invoke(((Object[])callback.obj)[0].toString(), false, false);
							callback = null;
							return;}
					}
					((GeolocationPermissions.Callback)((Object[])callback.obj)[1]).invoke(((Object[])callback.obj)[0].toString(), true, false);
					callback = null;
				}
				break;
			case 55:
				for (int i:grantResults)
				{
					if (i != PackageManager.PERMISSION_GRANTED)
					{
						Toast.makeText(this, "无存储器读写权限，软件部分功能将无法使用", Toast.LENGTH_LONG).show();
						return;
					}
				}
				break;

		}
		if(current!=null)current.onRequestPermissionsResult(requestCode,permissions,grantResults);
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
	}

	
	@Subscribe
	public void onFullScreen(View v){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		((ViewGroup)findViewById(R.id.main3)).addView(v);
	}
	@Subscribe
	public void onHide(String str){
		if(str.equals("hide")){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if (shared.getBoolean("full", false))
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			else
			{
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			((ViewGroup)findViewById(R.id.main3)).removeAllViews();
		}
	}
	@Subscribe
	public void permission(Message callback)
	{
		switch (callback.what)
		{
			case 49:
				this.callback = callback;
				break;
			case 2888:
				this.callback = callback;
				WebChromeClient.FileChooserParams wf=(WebChromeClient.FileChooserParams)((Object[])callback.obj)[1];
				Intent intent=null;
				if (wf.isCaptureEnabled())
				{
					intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File externalDataDir = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
					File cameraDataDir = new File(externalDataDir.getAbsolutePath()
												  + File.separator + "temp");
					cameraDataDir.mkdirs();
					String mCameraFilePath = cameraDataDir.getAbsolutePath()
						+ File.separator + System.currentTimeMillis() + ".jpg";
					intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
					intent.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(new File(mCameraFilePath)));
				}
				else
				{
					intent = new Intent(Intent.ACTION_GET_CONTENT);

					if (wf.getAcceptTypes() != null)
					{
						if (wf.getAcceptTypes().length > 1)
						{
							Intent[] in=new Intent[wf.getAcceptTypes().length];
							for (int i=0;i < wf.getAcceptTypes().length;i++)
							{
								in[i].setType(wf.getAcceptTypes()[i]);
							}
							intent.setAction(Intent.ACTION_CHOOSER);
							intent.putExtra(Intent.EXTRA_ALTERNATE_INTENTS, in);
						}
						else
							intent.setType(wf.getAcceptTypes()[0]);
					}

				}
				if (intent.getType().trim().isEmpty())
					intent.setType("*/*");
				try
				{
					startActivityForResult(intent, 2888);
				}
				catch (Exception e)
				{
					ValueCallback<Uri[]> vu=(ValueCallback<Uri[]>)((Object[])callback.obj)[0];
					vu.onReceiveValue(null);
					this.callback = null;
				}
				break;
				case 6:
					//Toast.makeText(this,callback.obj.toString(),400).show();
				/**final String[] data=callback.obj.toString().split(";");
				final ArrayList<String> as=new ArrayList<String>();
				for(String str:data){
					if(str.trim().isEmpty())continue;
					as.add(str);
				}*/
				final ArrayList<String> as=(ArrayList<String>)callback.obj;
				if(as.size()==0)
					Toast.makeText(this,"没有结果！",Toast.LENGTH_SHORT).show();
					else
				new BottomDialog.Builder(this).addArrayItem(as.toArray(new String[0]), new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
						  EventBus.getDefault().post(new WindowEvent( WindowEvent.WHAT_URL_WINDOW,as.get(p2)));
						  p1.dismiss();
						}
						}, new View.OnLongClickListener(){

							@Override
							public boolean onLongClick(View p1)
							{
								cm.setText(((Button)p1).getText());
								Toast.makeText(HomeActivity.this,"已复制",Toast.LENGTH_SHORT).show();
								return true;
							}
						}).show();
					break;
		}
	}
	@Subscribe
	public void onDownload(DownloadItem di)
	{
		dd.show(di);
	}
	@Subscribe
	public void onEvent(MenuOptions mo)
	{
		switch (mo)
		{
			case HISTORY:
				if (bookmark == null||bookmark.isDetached())bookmark = new BookmarksFragment();
				((BookmarksFragment)bookmark).setCurrent(1);
				openFragment(bookmark);
				break;
			case BOOKMARKS:
				if (bookmark == null||bookmark.isDetached())bookmark = new BookmarksFragment();
				openFragment(bookmark);
				break;
			case DOWNLOAD:
				//startService(new Intent(this,DownloadService.class));
				if (download == null||download.isDetached())download = new DownloadFragment();
				openFragment(download);
				break;
			case HOME:
				getSupportFragmentManager().beginTransaction().setCustomAnimations(0, R.anim.right_out).hide(current).commit();
				//current = main;
				break;
			case FULLSCREEN:
				if (!shared.getBoolean("full", false))
				{
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
					shared.edit().putBoolean("full", true).commit();
					//((MainFragment)main).setPadding(true);
				}
				else
				{
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					shared.edit().putBoolean("full", false).commit();
					//((MainFragment)main).setPadding(false);
				}

				break;
			case EXIT:
				super.onBackPressed();
				break;
			case SETTING:
				if(setting==null||setting.isDetached())setting=new SettingFragment();
				if (setting.isAdded())
				{
					getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(setting).commit();
				}
				else
				{
					getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).add(R.id.main2, setting).commit();
				}
				break;
			case NIGHTMODE:
				if (!shared.getBoolean("night", false))
				{
					shared.edit().putBoolean("night", true).commit();
					findViewById(R.id.main3).setBackgroundColor(0x50000000);
				}
				else
				{
					shared.edit().putBoolean("night", false).commit();
					findViewById(R.id.main3).setBackgroundColor(0x00000000);
				}

				break;
			case SKIN:
				if (skin == null||skin.isDetached())skin = new SkinFragment();
				openFragment(skin);
				break;
			case BITIMAGESCANNER:
				if (bit == null||bit.isDetached())bit = new BitImageFragment();
				openFragment(bit);
				break;
			case NETWORKLOG:
				if (network == null||network.isDetached())network = new NetworkLogFragment();
				((NetworkLogFragment)network).setArguments(((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).getCurrent().getNetworkLog());
				openFragment(network);
				break;
			case SEARCH:
				if (search == null||search.isDetached())search = new InputFragment();
				((InputFragment)search).setArguments(((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).getUrl());
				openFragment(search);
				break;
		}
	}
	/**@Subscribe
	public void download(DownloadTask dt)
	{
		if (dt.getStateOfTask() == DownloadTask.State.DNS)
		{
			AlertDialog ad=new AlertDialog(this);
			ad.setMessage(dt.getTaskInfo().getTaskname() + "/n程序已终止下载");
			ad.setTitle("疑似网络劫持");
			ad.show();
		}
	}*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case 256:
				if(resultCode==RESULT_OK)
					dd.setPath(data.getDataString());
				break;
			case 2888:
				if (callback != null)
				{
					ValueCallback<Uri[]> vu=(ValueCallback<Uri[]>)((Object[])callback.obj)[0];
					if (resultCode == RESULT_OK)
						vu.onReceiveValue(new Uri[]{data.getData()});
					else
						vu.onReceiveValue(null);
					callback = null;
				}
				break;
				default:
				if(setting!=null)setting.onActivityResult(requestCode,resultCode,data);
				if(current!=null)current.onActivityResult(requestCode,resultCode,data);
				break;

		}
	}
@Subscribe
public void onEvent(Integer event){
	switch(event){
		case ToolboxDialog.SHOW:
			if(toolbox==null)
				toolbox=new ToolboxDialog(this);
				toolbox.show();
			
			break;
		case JavaScriptDialog.SHOW:
			if(jsd==null)jsd=new JavaScriptDialog(this);
			jsd.show();
			break;
	}
}
	@Override
	public void finish()
	{
		if (shared.getBoolean(R.id.datamanager_view_autoclear + "", false))
			DataUtils.clearData(this);
		super.finish();
	}

	@Override
	protected void onDestroy()
	{

		EventBus.getDefault().unregister(this);
		stopService(new Intent(this, ResourceService.class));
		super.onDestroy();
	}

    @Override
    public void onBackPressed()
    {
		if(ToolManager.getInstance().isShowFind()){
			ToolManager.getInstance().findToggle(false);
			return;
		}
		if(setting!=null){
		if (!setting.onBackPressed())
		{
			if (setting.isAdded() && !setting.isHidden())
			{
				getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).detach(setting).commit();
				return;
			}
		}else return;}
		if (current != null && current.onBackPressed())return;
		else
		if (current != null && !current.isHidden())
		{
			if(current==search)
			getSupportFragmentManager().beginTransaction().setCustomAnimations(0,R.anim.right_out).hide(current).commit();
			else
			getSupportFragmentManager().beginTransaction().setCustomAnimations(0, R.anim.right_out).detach(current).remove(current).commit();
			current=null;
			if (main == null)
			{
				main = new MainFragment();
				getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
			}
			System.gc();
		}
		else if (!main.onBackPressed())
		{
			if (exit)
				super.onBackPressed();
			else
			{
				exit = true;
				Toast.makeText(this, "再点一次退出！", 1000).show();
				new Thread(){
					public void run()
					{
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{}exit = false;
					}
				}.start();
			}
		}

    }
private void loadURL(Intent intent){
	switch(intent.getAction()){
		case Intent.ACTION_VIEW:
			if(main==null){
				main = new MainFragment();
				Bundle b=new Bundle();
				b.putString("url",intent.getDataString());
				main.setArguments(b);
			}else{
				((MainFragment)main).openNewWindow(intent.getDataString());
			}
			if(main.isAdded())
				getSupportFragmentManager().beginTransaction().show(main).commit();
			else
				getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
			break;
			case Intent.ACTION_WEB_SEARCH:
			case Intent.ACTION_SEARCH:
			if(main==null){
				main = new MainFragment();
				Bundle b=new Bundle();
				b.putString(SearchManager.QUERY,intent.getStringExtra(SearchManager.QUERY));
				main.setArguments(b);
			}else
			LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("com.moe.search").putExtra(SearchManager.QUERY,intent.getStringExtra(SearchManager.QUERY)));
			if(main.isAdded())
				getSupportFragmentManager().beginTransaction().show(main).commit();
			else
				getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
			break;
			case Intent.ACTION_MAIN:
				main();
				break;
			case "download":
			onEvent(MenuOptions.DOWNLOAD);
			main();
				break;
	}
	
	
}
private void openFragment(Fragment fragment){
	current=fragment;
	if (fragment.isAdded())
	{
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).show(fragment).commit();
	}else
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).add(R.id.main2, fragment).commit();
	
}
private void main(){
	if(main==null)
		main = new MainFragment();
	if(main.isAdded())
		getSupportFragmentManager().beginTransaction().show(main).commit();
	else
		getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
}
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		loadURL(intent);
	}

}
