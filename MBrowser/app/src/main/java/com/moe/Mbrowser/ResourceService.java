package com.moe.Mbrowser;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Environment;
import android.net.ConnectivityManager;
import android.content.Context;
import android.net.NetworkInfo;
import android.app.Notification;
import de.greenrobot.event.EventBus;
import com.moe.bean.TaskBean;
import de.greenrobot.event.Subscribe;
import android.app.ActivityManager;
import com.moe.bean.NetworkState;

public class ResourceService extends Service
{
	public static String dir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download";
	private Network network=new Network();
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		registerReceiver(network,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		startForeground(0,new Notification.Builder(this).setOngoing(true).build());
		EventBus.getDefault().register(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}
	@Subscribe
	public void task(TaskBean tb){
		if(!isRunning())
			startService(new Intent(this,DownloadService.class));
	}
	private boolean isRunning(){
		ActivityManager am=getSystemService(ActivityManager.class);
		for(ActivityManager.RunningServiceInfo ar:am.getRunningServices(Integer.MAX_VALUE)){
			if(ar.service.getClassName().equals("com.moe.MBrowser.DownloadService")){
				return true;
			}
		}
		return false;
	}
	@Override
	public void onDestroy()
	{
		stopForeground(true);
		unregisterReceiver(network);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
	
	class Network extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			NetworkInfo ni=(NetworkInfo)p2.getExtras().get("networkInfo");
				switch(ni.getType()){//判断网络类型，决定是否下载
					case ConnectivityManager.TYPE_WIFI:
						EventBus.getDefault().post(new NetworkState(true,ni.isConnected(),true));
						break;
					case ConnectivityManager.TYPE_MOBILE:
					case ConnectivityManager.TYPE_MOBILE_DUN:
					case ConnectivityManager.TYPE_MOBILE_HIPRI:
					case ConnectivityManager.TYPE_MOBILE_MMS:
					case ConnectivityManager.TYPE_MOBILE_SUPL:
						EventBus.getDefault().post(new NetworkState(false,ni.isConnected(),true));
						break;
					default:
						EventBus.getDefault().post(new NetworkState(false,ni.isConnected(),false));
						break;
				}
				//未联网，作相关处理
			}
		

	}
}
