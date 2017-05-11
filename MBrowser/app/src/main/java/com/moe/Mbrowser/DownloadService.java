package com.moe.Mbrowser;
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import java.util.Set;
import android.net.NetworkInfo;
import android.app.Notification;
import android.widget.RemoteViews;
import android.os.Environment;
import com.moe.bean.TaskBean;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.EventBus;

public class DownloadService extends Service
{
	@Override
	public IBinder onBind(Intent p1)
	{

		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		EventBus.getDefault().register(this);
	}

	@Subscribe
	public void onTask(TaskBean tb)
	{

	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		startForeground(0, new Notification());

		return START_STICKY;
	}
	private Notification.Builder getNotificafion()
	{
		RemoteViews rv=new RemoteViews(getPackageName(), R.layout.notification_view);
		return new Notification.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContent(rv)
			.setTicker("下载")
			.setOngoing(true)
			.setPriority(Notification.PRIORITY_MAX);
	}
	@Override
	public void onDestroy()
	{
		stopForeground(true);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}




}
