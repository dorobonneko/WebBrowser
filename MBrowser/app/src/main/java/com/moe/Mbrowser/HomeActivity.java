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
import com.moe.fragment.DownloadFragment;
import com.moe.utils.DataUtils;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import android.os.Environment;
import com.moe.utils.Theme;
import com.moe.utils.ToolManager;

public class HomeActivity extends FragmentActivity
{
	private SharedPreferences shared;
	private Fragment current,main,bookmark,download;
	private SettingFragment setting=new SettingFragment();
	private DownloadDialog dd;
	private Message callback;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
		init();
	}
	private void init()
	{
		long i=System.currentTimeMillis();
		shared = getSharedPreferences("moe", 0);
		setContentView(R.layout.main);
		String path=getIntent().getStringExtra("activity");
		if ("download".equals(path))
		{
			download = new DownloadFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.main2, download).commit();
			current = download;
		}
		else
		{
			main = new MainFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
		}
		dd = new DownloadDialog(this);
		if (shared.getBoolean("full", false))
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			((MainFragment)main).setPadding(true);
		}
		if(shared.getBoolean("night",false))
			findViewById(R.id.main3).setBackgroundColor(0x50000000);
		startService(new Intent(this, ResourceService.class));
		Toast.makeText(this, "启动耗时：" + (System.currentTimeMillis() - i), 300).show();
		EventBus.getDefault().register(this);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 55);
		}

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
			case BOOKMARKS:
				if (bookmark == null)bookmark = new BookmarksFragment();
				if (bookmark.isAdded())
					getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).show(bookmark).commit();
				else
					getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).add(R.id.main2, bookmark).commit();
				current = bookmark;
				break;
			case DOWNLOAD:
				//startService(new Intent(this,DownloadService.class));
				if (download == null)download = new DownloadFragment();
				if (download.isAdded())
					getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).show(download).commit();
				else
					getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).add(R.id.main2, download).commit();
				current = download;
				break;
			case HOME:
				getSupportFragmentManager().beginTransaction().setCustomAnimations(0,R.anim.right_out).hide(current).commit();
					//current = main;
				break;
			case FULLSCREEN:
				if (!shared.getBoolean("full", false))
				{
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
					shared.edit().putBoolean("full", true).commit();
					((MainFragment)main).setPadding(true);
				}
				else
				{
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					shared.edit().putBoolean("full", false).commit();
					((MainFragment)main).setPadding(false);
				}

				break;
			case EXIT:
				super.onBackPressed();
				break;
			case SETTING:
				//if(current!=null&&!current.isHidden())
				//getSupportFragmentManager().beginTransaction().hide(current).commit();
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
		}
	}
	@Subscribe
	public void download(DownloadTask dt)
	{
		if (dt.getStateOfTask() == DownloadTask.State.DNS)
		{
			AlertDialog ad=new AlertDialog(this);
			ad.setMessage(dt.getTaskInfo().getTaskname() + "/n程序已终止下载");
			ad.setTitle("疑似网络劫持");
			ad.show();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case 233:
				if (resultCode == RESULT_OK)
					dd.setPath(data.getStringExtra("dir"));
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
		if (!setting.onBackPressed())
		{
			if (setting.isAdded() && !setting.isHidden())
			{
				getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).hide(setting).commit();
				return;
			}
		}else return;
		if (current != null && current.onBackPressed())return;
		else
		if(!current.isHidden())
		{
				getSupportFragmentManager().beginTransaction().setCustomAnimations(0, R.anim.right_out).hide(current).commit();
				if (main == null)
				{
					main = new MainFragment();
					getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
				}
		}else if (!main.onBackPressed())
			super.onBackPressed();

    }

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		String path=intent.getStringExtra("activity");
		getIntent().putExtra("activity", path);
		if ("download".equals(path))
		{
			if (download.isAdded())
				getSupportFragmentManager().beginTransaction().hide(current).show(download).commit();
			else
				getSupportFragmentManager().beginTransaction().add(R.id.main2, download).commit();
			current = download;
		}
		else
		{
			if (main.isAdded())
				getSupportFragmentManager().beginTransaction().hide(current).show(main).commit();
			else
				getSupportFragmentManager().beginTransaction().add(R.id.main1, main).commit();
		}
		//init();
	}

}
