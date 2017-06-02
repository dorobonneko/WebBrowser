package com.moe.download;
import com.moe.entity.TaskInfo;
import com.moe.Mbrowser.DownloadService;
import android.app.Notification;
import android.widget.RemoteViews;
import com.moe.Mbrowser.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import okhttp3.Headers;
import de.greenrobot.event.EventBus;
import android.content.SharedPreferences;
import com.moe.database.Download;
import java.io.RandomAccessFile;
import java.io.File;
import com.moe.entity.DownloadInfo;
import java.util.ArrayList;
import java.util.List;
import com.moe.database.DataBase;
import android.content.Context;
import java.util.Iterator;
import java.text.DecimalFormat;
import android.app.NotificationManager;
import de.greenrobot.event.Subscribe;
import android.content.Intent;
import android.app.PendingIntent;
import android.support.v4.content.FileProvider;
import android.os.Build;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.moe.bean.Message;
import com.moe.database.Sqlite;

public class DownloadTask extends Thread
{
	private List<DownloadBlock> ldb=new ArrayList<>();
	private TaskInfo ti;
	private DownloadService ds;
	private OkHttpClient okhttp;
	private SharedPreferences shared;
	private Download download;
	private Response response;
	private RandomAccessFile raf;
	private Notification.Builder nb;
	private RemoteViews remoteviews;
	private long time;
	private NotificationManager nm;
	public DownloadTask(DownloadService ds, TaskInfo ti, OkHttpClient ok)
	{
		this.ti = ti;
		this.ds = ds;
		this.download = Sqlite.getInstance(ds,Download.class);
		nm = ds.getSystemService(NotificationManager.class);
		okhttp = ok;
		shared = ds.getSharedPreferences("download", 0);
		setPriority(Thread.MIN_PRIORITY);
		getNotificafion();
		EventBus.getDefault().register(this);
		start();
	}

	public void cancelNotifycation()
	{
		nm.cancel(ti.getId());
	}

	public void itemFinish(DownloadBlock p0)
	{
		if (p0.isSuccess() && ldb.size() > 0)
			for (DownloadBlock db:ldb)
			{
				if (!db.isSuccess())
					return;
			}
		else
		{
			pause();
			ti.setState(State.FAIL);
			EventBus.getDefault().post(ti);
			ds.taskItemFinish(ti.getId(), false);
			return;
		}
		ti.setSuccess(true);
		download.updateTaskInfoData(ti);
		EventBus.getDefault().post(ti);
		ds.taskItemFinish(ti.getId(), true);
	}
	public Context getContext()
	{
		return ds;
	}
	public OkHttpClient getOkHttpCliebt()
	{
		return okhttp;
	}
	public SharedPreferences getSharedPreferences()
	{
		return shared;
	}
	public Download getDownload()
	{
		return download;
	}
	public State getStateOfTask()
	{
		return ti.getState();
	}
	public TaskInfo getTaskInfo()
	{
		return ti;
	}
	public void pause()
	{
		//暂停任务
		switch (ti.getState())
		{
			case QUERY:
				if (response != null)
				{
					response.close();
				}
				break;
			case TEMPFILE:
				try
				{
					raf.close();
					new File(ti.getDir(), ti.getTaskname()).delete();
				}
				catch (IOException e)
				{}
				response.close();
				break;
			case DOWNLOADING:
				Iterator<DownloadBlock> i=ldb.iterator();
				while (i.hasNext())
				{
					DownloadBlock db=i.next();
					if (db != null)
						db.pause();}
				ldb.clear();
				break;
		}

	}

	@Override
	public void run()
	{
		//判断任务是否已经存在，如果不存在则创建查询
		List<DownloadInfo> ldi=ti.getDownloadinfo();
		if (ldi != null && ldi.size() > 0)
		{
			File f=new File(ti.getDir(), ti.getTaskname());
			if (!f.exists())
			{
				download.deleteDownloadInfoWithId(ti.getId());ti.setDownloadinfo(null); start();return;//下载的文件已被删除，删除下载信息并重启任务
			}
			//循环遍历并加载任务
			ti.setState(State.DOWNLOADING);
			sendMessage();
			for (DownloadInfo di:ldi)
			{
				ldb.add(new DownloadBlock(this, di));
			}
		}
		else
		{
			ti.setState(State.QUERY);
			sendMessage();
			Request.Builder request=new Request.Builder();
			if (ti.getCookie() != null)
				request.addHeader("Cookie", ti.getCookie());
			if (ti.getUserAgent() != null)
				request.addHeader("User-Agent", ti.getUserAgent());
			if(ti.getSourceUrl()!=null)
				request.addHeader("Referer",ti.getSourceUrl());
			//request.addHeader("Range","bytes=0-100");
			try
			{
				response = okhttp.newCall(request.url(ti.getTaskurl()).build()).execute();
				if (response.code() == 200)
				{
					long length=ti.getLength();
					try{
					length=Long.parseLong(response.header("Content-Length"));
					}catch(Exception e){}
//				if (length != ti.getLength())
//				{
//					state=State.DNS;
//					EventBus.getDefault().post(this);
//					return;
//				}
//				else
//				{
					//是否支持断点
					if (response.header("Accept-Ranges") == null)
					{
						ti.setSupport(TaskInfo.Pause.UNSUPPORT);
						ti.setMultiThread(false);
					}
					else
					{
						ti.setSupport(TaskInfo.Pause.SUPPORT);
						ti.setMultiThread("close".equalsIgnoreCase(response.header("Connection")) ?false: true && shared.getBoolean(Download.Setting.MULTITHREAD, Download.Setting.MULTITHREAD_DEFAULT));
					}
					File f=new File(ti.getDir());
					if (!f.exists())f.mkdirs();
					if (!f.canWrite())
					{
						ti.setState(State.NOPERMISSION);
						sendMessage();
						return;
					}
					else
					{
						if (f.getFreeSpace() > length)
						{
							raf = new RandomAccessFile(new File(f, ti.getTaskname()), "rw");
							ti.setState(State.TEMPFILE);
							sendMessage();
							raf.close();
							ti.setState(State.DOWNLOADING);
							sendMessage();
							int size=0;
							if (ti.isMultiThread())
								size = shared.getInt(Download.Setting.THREADSIZE, Download.Setting.THREADSIZE_DEFAULE);
							else size = 1;
							long blocksize=length / size;
							ArrayList<DownloadInfo> ad=new ArrayList<>();
							for (int i=0;i < size;i++)
							{
								DownloadInfo di=new DownloadInfo();
								di.setStart(i * blocksize);
								di.setCurrent(di.getStart());
								if (i == size - 1)
									di.setEnd(length);
								else
									di.setEnd((i + 1) * blocksize);
								di.setTaskId(ti.getId());
								di.setNo(i);
								//download.insertDownloadInfo(di);
								ad.add(di);
								ldb.add(new DownloadBlock(this, di));
							}//循环
							ti.setDownloadinfo(ad);
							if (response.header("Content-Type") != null)
								ti.setType(response.header("Content-Type"));
							ti.setLength(length);
							download.updateTaskInfoData(ti);
						}//剩余空间
						else
						{
							//空间不足
							ti.setState(State.DISKLESS);
							sendMessage();
							return;
						}
					}//是否可以读写
					//}//是否网络劫持
				}//链接返回正常
				else
				{
					//链接无效
					ti.setState(State.INVALIDE);
				}
			}
			catch (Exception e)
			{
				ti.setState(State.FAIL);
				sendMessage();
				ds.taskItemFinish(ti.getId(), false);
			}
			finally
			{
				if(response!=null)
				response.close();
			}
		}
	}
	@Subscribe
	public void stop(Message msg){
		if(msg.what==88888){
			if(msg.data==ti.getId()){
				if (ti.isSuccess())
				{
				nm.cancel(ti.getId());
				nb.setContent(null);
				nb.setContentTitle(ti.getTaskname());
				nb.setContentInfo("下载成功");
				nb.setOngoing(false);
				nb.setAutoCancel(true);
				Intent intent = new Intent(Intent.ACTION_VIEW);
//判断是否是AndroidN以及更高的版本
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				Uri contentUri = FileProvider.getUriForFile(ds, getContext().getPackageName() + ".fileProvider", new File(ti.getDir(), ti.getTaskname()));
				intent.setDataAndType(contentUri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl((intent.getDataString()))));
				}
				else
				{
				intent.setDataAndType(Uri.fromFile(new File(ti.getDir(), ti.getTaskname())), MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(intent.getDataString())));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}

				PendingIntent pi=PendingIntent.getActivity(ds, 233, intent, PendingIntent.FLAG_ONE_SHOT);
				nb.setContentIntent(pi);
				nm.notify(ti.getId(), nb.build());
				}
				EventBus.getDefault().unregister(this);
			}
		}
	}
	@Subscribe
	public void refresh(TaskInfo ti)
	{
		if (ti != this.ti)return;
		if (!ti.isDownloading() || System.currentTimeMillis() - time > 1000)
		{
			long size=0;
			long speed;
			if (ti.getDownloadinfo() != null)
			{
				for (DownloadInfo di:ti.getDownloadinfo())
				{
					size += di.getCurrent() - di.getStart();
				}
				remoteviews.setProgressBar(R.id.notificationview_progress, 100, (int)(size / (double)ti.getLength() * 100), false);
			}
			remoteviews.setTextViewText(R.id.notificationview_size, new DecimalFormat("0.00").format(size / 1024.0 / 1024) + "/" + new DecimalFormat("0.00").format(ti.getLength() / 1024.0 / 1024) + "MB");
			if (ti.getTag() != null)
			{
				double time=(System.currentTimeMillis() - ti.getTag()[0]) / 1000.0;
				long s=size - ti.getTag()[1];
				speed = (long)(s / time);
				remoteviews.setTextViewText(R.id.notificationview_speed, new DecimalFormat("0.00").format(speed / 1024.0 / 1024) + "MB/s");
			}
			if (ti.isDownloading())
				remoteviews.setImageViewResource(R.id.notificationviewImage_state, R.drawable.ic_pause);
			else
				remoteviews.setImageViewResource(R.id.notificationviewImage_state, R.drawable.ic_play);
			time = System.currentTimeMillis();
			nm.notify(ti.getId(), nb.build());
		}
	}
	private void sendMessage()
	{
		EventBus.getDefault().post(ti);
	}
	private void getNotificafion()
	{
		remoteviews = new RemoteViews(ds.getPackageName(), R.layout.notification_view);
		remoteviews.setTextViewText(R.id.notificationview_title, ti.getTaskname());
		Intent intent=new Intent();
		intent.setClassName(ds.getPackageName(),ds.getPackageName()+".HomeActivity");
		intent.putExtra("activity","download");
		PendingIntent pi=PendingIntent.getActivity(ds,233,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		nb = new Notification.Builder(ds)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContent(remoteviews)
			.setTicker("下载")
			//.setOngoing(true)
			.setPriority(Notification.PRIORITY_MAX)
			.setContentIntent(pi);
		remoteviews.setOnClickPendingIntent(R.id.notificationviewImage_state,PendingIntent.getBroadcast(ds,ti.getId(),new Intent("com.moe.notification.state").putExtra("id",ti.getId()),PendingIntent.FLAG_UPDATE_CURRENT));
	}
	public enum State
	{
		QUERY,DOWNLOADING,PAUSE,STOP,FAIL,INVALIDE,DISKLESS,WAITING,TEMPFILE,SUCCESS,NOPERMISSION,DNS;
		//查询信息，下载中，暂停，停止，失败，地址过期，磁盘空间不足，等待，创建临时文件，完成，无权限;
	}
}
