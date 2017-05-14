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

public class DownloadService extends Service
{
	//下载队列
	public static LinkedHashMap<Integer,TaskInfo> downloadlist=new LinkedHashMap<>();
	//正在下载队列
	private LinkedHashMap<Integer,DownloadTask> downloadinglist=new LinkedHashMap<>();
	private SharedPreferences shared;
	private NetworkState network;
	private OkHttpClient okhttp;
	@Override
	public IBinder onBind(Intent p1)
	{

		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		shared=getSharedPreferences("download",0);
		EventBus.getDefault().register(this);
		SSLContext ssl=null;
		try
		{
			ssl = SSLContext.getInstance("TLS");
			ssl.init(null, new TrustManager[]{new CustomTrustManager()}, new SecureRandom());
		}
		catch (NoSuchAlgorithmException e)
		{}
		catch (KeyManagementException e)
		{}
		okhttp = new OkHttpClient.Builder().sslSocketFactory(ssl.getSocketFactory()).hostnameVerifier(new HostnameVerifier(){

				@Override
				public boolean verify(String p1, SSLSession p2)
				{
					// TODO: Implement this method
					return true;
				}
			}).build();
		startForeground(0, new Notification());
		EventBus.getDefault().post(this);
		
	}
	private void checkSize(){
		for(int i=downloadinglist.size();i<Integer.parseInt(getResources().getTextArray(R.array.size)[shared.getInt(Download.Setting.SIZE,Download.Setting.SIZE_DEFAULT)].toString());i++){
			for(Integer key:downloadlist.keySet()){
				if(!downloadinglist.containsKey(key))
			downloadinglist.put(key,new DownloadTask(this,downloadlist.get(key),okhttp));
			}
		}
	}
	public void taskItemFinish(int url,boolean isSuccess){
		downloadinglist.remove(url);
		downloadlist.remove(url);
		checkSize();
		EventBus.getDefault().post(Message.obitMessage(88888));
	}
	@Subscribe
	public void onTask(TaskBean tb)
	{
		switch(tb.getState()){
			case ADD:
				tb.getTaskInfo().setState(DownloadTask.State.WAITING);
				downloadlist.put(tb.getTaskInfo().getId(),tb.getTaskInfo());
				break;
			case UPDATE:
				if(downloadinglist.containsKey(tb.getTaskInfo().getId())){
					downloadinglist.remove(tb.getTaskInfo().getId()).pause();
					downloadlist.remove(tb.getTaskInfo().getId());
					}
				tb.getTaskInfo().setState(DownloadTask.State.WAITING);
				downloadlist.put(tb.getTaskInfo().getId(),tb.getTaskInfo());
				break;
			case PAUSE:
				if(downloadinglist.containsKey(tb.getTaskInfo().getId())){
					downloadinglist.remove(tb.getTaskInfo().getId()).pause();
					downloadlist.remove(tb.getTaskInfo().getId());
				}
				tb.getTaskInfo().setState(DownloadTask.State.PAUSE);
				EventBus.getDefault().post(tb.getTaskInfo());
				break;
			case DELETE:
				if(downloadinglist.containsKey(tb.getTaskInfo().getId())){
					downloadinglist.remove(tb.getTaskInfo().getId()).pause();
					downloadlist.remove(tb.getTaskInfo().getId());
				}
				break;
		}
		checkSize();
	}
	@Subscribe
	public void download(DownloadTask dt){
		switch(dt.getStateOfTask()){
			case DNS:
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
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public class CustomTrustManager implements X509TrustManager{

			@Override
			public void checkClientTrusted(X509Certificate[] p1, String p2) throws CertificateException
			{
				// TODO: Implement this method
			}

			@Override
			public void checkServerTrusted(X509Certificate[] p1, String p2) throws CertificateException
			{
				// TODO: Implement this method
			}

			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				// TODO: Implement this method
				return new X509Certificate[0];
			}
		}


}
