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
import android.text.TextUtils;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import com.moe.utils.M3u8Utils;
import java.util.Arrays;
import java.util.Comparator;
import java.io.FileInputStream;
import com.moe.utils.DataUtils;

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
	private int errorsize=0;
	public DownloadTask(DownloadService ds, TaskInfo ti, OkHttpClient ok)
	{
		this.ti = ti;
		this.ds = ds;
		this.download = Sqlite.getInstance(ds, Download.class);
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
		nm=null;
	}

	public void itemFinish(DownloadBlock p0)
	{
		if (p0.isSuccess())
			for (DownloadBlock db:ldb)
			{
				if (!db.isSuccess())
					return;
			}
		else
		{
			if(!ti.getM3u8()){
			if(errorsize++<Integer.parseInt(ds.getResources().getTextArray(R.array.size)[shared.getInt(Download.Setting.RELOADSIZE,Download.Setting.RELOADSIZE_DEFAULT)].toString())){
				p0.start();
				return;
			}}else{
				switch(shared.getInt(Download.Setting.M3U8ERROR,Download.Setting.M3U8ERROR_DEFAULT)){
					case 0:break;
					case 1:return;
					case 2:
						if(p0.getErrorsize()<Integer.parseInt(ds.getResources().getTextArray(R.array.size)[shared.getInt(Download.Setting.RELOADSIZE,Download.Setting.RELOADSIZE_DEFAULT)].toString()))
						{p0.start();
						p0.setErrorsize(p0.getErrorsize()+1);
						return;}
						else break;
					
				}
			}
			pause();
			ti.setState(State.FAIL);
			EventBus.getDefault().post(ti);
			ds.taskItemFinish(ti.getId(), false);
			return;
		}
		if(ti.isSuccess())return;
		else{
		ti.setSuccess(true);
		if(ti.getM3u8()){
			//合并文件
			File file=new File(ti.getDir(),ti.getTaskname());
			File[] lf=file.listFiles();
			Arrays.sort(lf,new Comparator<File>(){

					@Override
					public int compare(File p1, File p2)
					{
						return Integer.compare(Integer.parseInt(p1.getName()),Integer.parseInt(p2.getName()));
					}
				});
			File tmp=new File(ti.getDir(),System.currentTimeMillis()+"");
			try
			{
				int len=0;
				byte[] b=new byte[10240];
				FileOutputStream fos=new FileOutputStream(tmp);
				for(File l:lf){
					FileInputStream fis=new FileInputStream(l);
					while((len=fis.read(b))!=-1)
						fos.write(b,0,len);
					fis.close();
				}
				fos.flush();
				fos.close();
			}
			catch (Exception e)
			{}
			DataUtils.deleteDir(file);
			tmp.renameTo(file);
		}
		download.updateTaskInfoData(ti);
		EventBus.getDefault().post(ti);
		ds.taskItemFinish(ti.getId(), true);
		}
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
	private void startTask(){
		for(DownloadBlock db:ldb)
			if(ti.getState()==State.DOWNLOADING){
				db.start();
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
			
			for (DownloadInfo di:ldi)
			{
				if(!di.isSuccess())//避免创建已完成块，以免浪费资源
				ldb.add(new DownloadBlock(this, di));
			}
			ti.setState(State.DOWNLOADING);
			sendMessage();
			startTask();
			return;
		}
		ti.setState(State.QUERY);
		sendMessage();
		//构造请求
		Request.Builder request=new Request.Builder();
		if (!TextUtils.isEmpty(ti.getCookie()))
			request.addHeader("Cookie", ti.getCookie());
		if (!TextUtils.isEmpty(ti.getUserAgent()))
			request.addHeader("User-Agent", ti.getUserAgent());
		//if(ti.getSourceUrl()!=null)
		//request.addHeader("Referer",ti.getSourceUrl());
		request.addHeader("Accept", "*/*");
		request.addHeader("Connection", "close");
		request.addHeader("Icy-MetaData", "1");
		request.addHeader("Range", "bytes=0-");
		try
		{
			response = okhttp.newCall(request.url(ti.getTaskurl()).build()).execute();
			switch (response.code())
			{
				case 200:
					ti.setSupport(TaskInfo.Pause.UNSUPPORT);
					ti.setMultiThread(false);
					break;
				case 206:
					ti.setSupport(TaskInfo.Pause.SUPPORT);
					ti.setMultiThread(shared.getBoolean(Download.Setting.MULTITHREAD, Download.Setting.MULTITHREAD_DEFAULT));
					break;
				default:
					//链接无效
					ti.setState(State.INVALIDE);
					sendMessage();
					ds.taskItemFinish(ti.getId(), false);
					return;
			}
			long length=ti.getLength();
			try
			{
				length = Long.parseLong(response.header("Content-Length"));
			}
			catch (Exception e)
			{length=response.body().contentLength();}
			ti.setLength(length);
			if (!TextUtils.isEmpty(response.header("Content-Type")))
				ti.setType(response.header("Content-Type"));
			ArrayList<DownloadInfo> ad=new ArrayList<>();
			if (ti.getM3u8())
			{
				//m3u8
				InputStream is=response.body().byteStream();
				ByteArrayOutputStream baos=new ByteArrayOutputStream();
				int len=0;
				byte[] b=new byte[8072];
				while ((len = is.read(b)) != -1)
				{
					baos.write(b, 0, len);
				}
				baos.flush();
				is.close();
				List<String> ls=M3u8Utils.pauser(new String(baos.toByteArray()));
				baos.close();
				for (int i=0;i < ls.size();i++)
				{
					DownloadInfo di=new DownloadInfo();
					di.setTaskId(ti.getId());
					di.setNo(i);
					di.setCurrent(0);
					di.setStart(0);
					di.setUrl(ls.get(i));
					ad.add(di);
					ldb.add(new DownloadBlock(this, di));
				}
			}
			else
			{
				File f=new File(ti.getDir());
				if (!f.exists())f.mkdirs();
				if (!f.canWrite())
				{
					//无权限读写
					ti.setState(State.NOPERMISSION);
					sendMessage();
					ds.taskItemFinish(ti.getId(), false);
					return;
				}
				if (f.getFreeSpace() < length)
				{
					//空间不足
					ti.setState(State.DISKLESS);
					sendMessage();
					ds.taskItemFinish(ti.getId(), false);

					return;
				}
				raf = new RandomAccessFile(new File(f, ti.getTaskname()), "rw");
				ti.setState(State.TEMPFILE);
				sendMessage();
				raf.close();
				int size=0;//多线程数量
				if (ti.isMultiThread())
					size = shared.getInt(Download.Setting.THREADSIZE, Download.Setting.THREADSIZE_DEFAULE);
				else size = 1;
				long blocksize=length / size;
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
					di.setUrl(ti.getTaskurl());
					//download.insertDownloadInfo(di);
					ad.add(di);
					ldb.add(new DownloadBlock(this, di));
				}//循环
			}
			ti.setDownloadinfo(ad);
			download.updateTaskInfoData(ti);
			ti.setState(State.DOWNLOADING);
			startTask();
		}
		catch (Exception e)
		{
			ti.setState(State.FAIL);
			sendMessage();
			ds.taskItemFinish(ti.getId(), false);
		}
		finally
		{
			if (response != null)
				response.close();
		}
	}
	@Subscribe
	public void stop(Message msg)
	{
		if (msg.what == 88888)
		{
			if (msg.data == ti.getId())
			{
				if (ti.isSuccess())
				{
					nm.cancel(ti.getId());
					nb.setContent(null);
					nb.setContentTitle(ti.getTaskname());
					nb.setContentInfo("下载成功");
					nb.setOngoing(false);
					nb.setAutoCancel(true);
					Intent intent = new Intent(Intent.ACTION_VIEW);
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
		if(nm==null)return;
		if (ti != this.ti)return;
		if (!ti.isDownloading() || System.currentTimeMillis() - time > 1000)
		{
			long size=0;
			long speed;
			long length=0;
			if (ti.getDownloadinfo() != null)
			{
				for (DownloadInfo di:ti.getDownloadinfo())
				{
					size += di.getCurrent() - di.getStart();
					length+=di.getEnd();
				}
				if(!ti.getM3u8())length=ti.getLength();
				remoteviews.setProgressBar(R.id.notificationview_progress, 100, (int)(size / (double)length * 100), false);
			}
			remoteviews.setTextViewText(R.id.notificationview_size, new DecimalFormat("0.00").format(size / 1024.0 / 1024) + "/" + new DecimalFormat("0.00").format(length/ 1024.0 / 1024) + "MB");
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
		intent.setAction("download");
		intent.setClassName(ds.getPackageName(), ds.getPackageName() + ".HomeActivity");
		PendingIntent pi=PendingIntent.getActivity(ds, 233, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		nb = new Notification.Builder(ds)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContent(remoteviews)
			.setTicker("下载")
			//.setOngoing(true)
			.setPriority(Notification.PRIORITY_MAX)
			.setContentIntent(pi);
		remoteviews.setOnClickPendingIntent(R.id.notificationviewImage_state, PendingIntent.getBroadcast(ds, ti.getId(), new Intent("com.moe.notification.state").putExtra("id", ti.getId()), PendingIntent.FLAG_UPDATE_CURRENT));
	}
	public enum State
	{
		QUERY,DOWNLOADING,PAUSE,STOP,FAIL,INVALIDE,DISKLESS,WAITING,TEMPFILE,SUCCESS,NOPERMISSION,DNS;
		//查询信息，下载中，暂停，停止，失败，地址过期，磁盘空间不足，等待，创建临时文件，完成，无权限;
	}
}
