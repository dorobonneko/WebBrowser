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
import com.moe.bean.NetworkState;
import com.moe.entity.TaskInfo;
import java.util.List;
import java.util.ArrayList;
import com.moe.entity.DownloadInfo;
import com.moe.download.DownloadTask;
import android.content.SharedPreferences;
import com.moe.database.Download;
import java.util.LinkedHashMap;
import okhttp3.OkHttpClient;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import com.moe.bean.Message;
import com.moe.database.Sqlite;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import com.moe.utils.DataUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.moe.internal.NotificationManager;
import com.moe.utils.LinkedListMap;
import java.util.TimerTask;
import java.util.Timer;
import com.moe.net.OkHttp;

public class DownloadService extends Service
{
	//下载队列
	public static LinkedListMap<Integer,TaskInfo> downloadlist=new LinkedListMap<>();
	//正在下载队列
	private LinkedListMap<Integer,DownloadTask> downloadinglist=new LinkedListMap<>();
	private SharedPreferences shared;
	private NetworkState network;
	private OkHttpClient okhttp;
	private NotificationManager nm;
	private LinkedListMap<Integer,Timer> timer;
	@Override
	public IBinder onBind(Intent p1)
	{

		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		timer=new LinkedListMap<>();
		nm=NotificationManager.getInstance(this);
		shared=getSharedPreferences("download",0);
		EventBus.getDefault().register(this);
		okhttp=OkHttp.getOkHttp();
		startForeground(0, new Notification());
		registerReceiver(new stateChange(),new IntentFilter("com.moe.notification.state"));
		EventBus.getDefault().post(this);
	}
	private void checkSize(){
		if(downloadlist.size()==0)stopSelf();
		else
		for(int i=downloadinglist.size();i<Integer.parseInt(getResources().getTextArray(R.array.size)[shared.getInt(Download.Setting.SIZE,Download.Setting.SIZE_DEFAULT)].toString());i++){
			for(final Integer key:downloadlist.keys()){
				if(!downloadinglist.containsKey(key)){
					downloadinglist.put(key,new DownloadTask(this,downloadlist.getKey(key),okhttp));
					Timer t=new Timer();
					TimerTask tt=new TimerTask(){

						@Override
						public void run()
						{
							try{EventBus.getDefault().post(downloadlist.getKey(key));}catch(Exception e){this.cancel();}
						}
					};
					timer.put(key,t);
					t.schedule(tt,0,1000);
					new Thread(downloadinglist.getKey(key)).start();
					break;
			}
			}
		}
	}
	public void taskItemFinish(int id,boolean isSuccess){
		DownloadTask dt=downloadinglist.remove(id);
		if(dt!=null)
		EventBus.getDefault().unregister(dt);
		TaskInfo ti=downloadlist.remove(id);
		Timer t=timer.remove(id);
		if(t!=null)t.cancel();
		if(ti!=null)
		EventBus.getDefault().post(ti);
		checkSize();
		}
	@Subscribe
	public void onTask(TaskBean tb)
	{
		DownloadTask dt;
		switch(tb.getState()){
			case ADD:
				tb.getTaskInfo().setState(DownloadTask.State.WAITING);
				downloadlist.put(tb.getTaskInfo().getId(),tb.getTaskInfo());
				EventBus.getDefault().post(tb.getTaskInfo());
				break;
			case UPDATE:
				dt=downloadinglist.remove(tb.getTaskInfo().getId());
				if(dt!=null)dt.pause();
					downloadlist.remove(tb.getTaskInfo().getId());
				tb.getTaskInfo().setState(DownloadTask.State.WAITING);
				downloadlist.put(tb.getTaskInfo().getId(),tb.getTaskInfo());
				EventBus.getDefault().post(tb.getTaskInfo());
				break;
			case PAUSE:
				dt=downloadinglist.remove(tb.getTaskInfo().getId());
				if(dt!=null)dt.pause();
				downloadlist.remove(tb.getTaskInfo().getId());
				Timer t=timer.remove(tb.getTaskInfo().getId());
				if(t!=null)t.cancel();
				tb.getTaskInfo().setState(DownloadTask.State.PAUSE);
				EventBus.getDefault().post(tb.getTaskInfo());
				break;
			case DELETE:
				dt=downloadinglist.remove(tb.getTaskInfo().getId());
				if(dt!=null){dt.pause();}
				TaskInfo ti=downloadlist.remove(tb.getTaskInfo().getId());
				Timer tt=timer.remove(tb.getTaskInfo().getId());
				if(tt!=null)tt.cancel();
				if(ti!=null){
					ti.setState(DownloadTask.State.DELETE);
					}
				nm.cancel(tb.getTaskInfo().getId());
				break;
		}
		checkSize();
	}
	@Subscribe
	public void download(DownloadTask dt){
		switch(dt.getStateOfTask()){
			case FAIL:
			case DISKLESS:
			case NOPERMISSION:
			case INVALIDE:
				downloadinglist.remove(dt.getTaskInfo().getId());
				downloadlist.remove(dt.getTaskInfo().getId());
				break;
		}
	}
	@Subscribe
	public void onNetworkChange(NetworkState ns){
		network=ns;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		stopForeground(true);
		downloadlist.clear();
		for(DownloadTask dt:downloadinglist.values())
		dt.pause();
		downloadinglist.clear();
		//nm.destory();
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	
	class stateChange extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			int id=p2.getIntExtra("id",-1);
			if(id!=-1){
				TaskInfo ti= downloadlist.getKey(id);
				if(ti!=null)
					EventBus.getDefault().post(new TaskBean(ti,TaskBean.State.PAUSE));
				else
					EventBus.getDefault().post(new TaskBean(Sqlite.getInstance(p1,Download.class).queryTaskInfoWithId(id),TaskBean.State.ADD));
				
				
			}
		}

	
}
}
