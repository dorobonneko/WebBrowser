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
import java.util.ArrayList;
import java.util.List;
import android.os.Build;
import com.moe.internal.NotificationManager;
import java.util.Collection;
import com.moe.entity.TaskInfo;
import android.os.Handler;
import android.os.Message;
import com.moe.download.DownloadTask;

public class ResourceService extends Service
{
	private Network network=new Network();
	private NetworkState ns;
	private List<TaskBean> lt=new ArrayList<>();
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	public static void start(Context context){
		context.startService(new Intent(context,ResourceService.class));
	}
	public static void stop(Context context){
		context.stopService(new Intent(context,ResourceService.class));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		registerReceiver(network,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		if(!EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
		startForeground(0,Build.VERSION.SDK_INT>15?new Notification.Builder(this).setOngoing(true).build():new Notification.Builder(this).setOngoing(true).getNotification());
		return START_STICKY;
	}
	@Subscribe
	public void task(TaskBean tb){
		
		if(!isRunning(this)){
			if(!lt.contains(tb))lt.add(tb);
			startService(new Intent(this,DownloadService.class));
			}
	}
	@Subscribe
	public void cannBack(DownloadService ds){
		int size=lt.size();
		for(int i=0;i<size;i++){
			ds.onTask(lt.remove(0));
		}
	}
	public static boolean isRunning(Context context){
		ActivityManager am=(ActivityManager)context.getSystemService(Service.ACTIVITY_SERVICE);
		for(ActivityManager.RunningServiceInfo ar:am.getRunningServices(Integer.MAX_VALUE)){
			if(ar.service.getClassName().equals("com.moe.Mbrowser.DownloadService")){
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
		if(!isRunning(this))
		NotificationManager.getInstance(this).destory();
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			if(ns.isConnected()&&ns.isCanUse()){
				if(lt.size()>0)
					startService(new Intent(ResourceService.this,DownloadService.class));
			}else{
				ArrayList<TaskInfo> ct=DownloadService.downloadlist.values();
				for(int i=0;i<ct.size();i++){
					TaskInfo ti=ct.get(i);
					ti.setState(DownloadTask.State.NETERROR);
					EventBus.getDefault().post(ti);
					lt.add(new TaskBean(ti,TaskBean.State.ADD));
				}
				stopService(new Intent(ResourceService.this,DownloadService.class));

			}
		}
		
	};
	class Network extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			if(ns==null)ns=new NetworkState();
			NetworkInfo ni=(NetworkInfo)p2.getExtras().get("networkInfo");
			if(ni.isConnected()){
			ns.setConnected(ni.isConnected());
			ns.setCanUse(true);
				switch(ni.getType()){//判断网络类型，决定是否下载
					case ConnectivityManager.TYPE_WIFI:
						ns.setWifi(true);
						break;
					case ConnectivityManager.TYPE_MOBILE:
					case ConnectivityManager.TYPE_MOBILE_DUN:
					case ConnectivityManager.TYPE_MOBILE_HIPRI:
					case ConnectivityManager.TYPE_MOBILE_MMS:
					case ConnectivityManager.TYPE_MOBILE_SUPL:
						ns.setWifi(false);
						break;
					default:
					ns.setCanUse(false);
						break;
				}
				}else{
					ns.setConnected(false);
					ns.setCanUse(false);
					}
				handler.sendEmptyMessage(0);
			}
		

	}
}
