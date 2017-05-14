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
	public DownloadTask(DownloadService ds, TaskInfo ti, OkHttpClient ok)
	{
		this.ti = ti;
		this.ds = ds;
		this.download = DataBase.getInstance(ds);
		okhttp = ok;
		shared = ds.getSharedPreferences("download", 0);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	public void itemFinish(DownloadBlock p0)
	{
		if(p0.isSuccess()&&ldb.size()>0)
		for(DownloadBlock db:ldb){
			if(!db.isSuccess())
				return;
		}
		else{
			pause();
			ti.setState(State.FAIL);
			EventBus.getDefault().post(ti);
			ds.taskItemFinish(ti.getId(),false);
			return;
		}
		ti.setSuccess(true);
		download.updateTaskInfoData(ti);
		EventBus.getDefault().post(ti);
		ds.taskItemFinish(ti.getId(),true);
	}
	public Context getContext(){
		return ds;
	}
	public OkHttpClient getOkHttpCliebt(){
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
		switch(ti.getState()){
			case QUERY:
				if(response!=null){
					response.close();
				}
				break;
			case TEMPFILE:
				try
				{
					raf.close();
					new File(ti.getDir(),ti.getTaskname()).delete();
				}
				catch (IOException e)
				{}
				response.close();
				break;
			case DOWNLOADING:
				Iterator<DownloadBlock> i=ldb.iterator();
				while(i.hasNext()){
					DownloadBlock db=i.next();
					if(db!=null)
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
		if(ldi!=null&&ldi.size()>0){
			File f=new File(ti.getDir(), ti.getTaskname());
			if(!f.exists()){
				download.deleteDownloadInfoWithId(ti.getId());ti.setDownloadinfo(null); start();return;//下载的文件已被删除，删除下载信息并重启任务
			}
			//循环遍历并加载任务
			ti.setState(State.DOWNLOADING);
			EventBus.getDefault().post(ti);
			for(DownloadInfo di:ldi){
				ldb.add(new DownloadBlock(this,di));
			}
		}else{
		ti.setState(State.QUERY);
		EventBus.getDefault().post(ti);
		Request.Builder request=new Request.Builder();
		if (ti.getCookie() != null)
			request.addHeader("Cookie", ti.getCookie());
		if (ti.getUserAgent() != null)
			request.addHeader("User-Agent", ti.getUserAgent());
		//request.addHeader("Range","bytes=0-100");
		request.url(ti.getTaskurl());
		try
		{
			response=okhttp.newCall(request.build()).execute();
			if (response.code() == 200)
			{
				long length=Long.parseLong(response.header("Content-Length"));
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
					ti.setMultiThread("close".equalsIgnoreCase(response.header("Connection"))?false:true&& shared.getBoolean(Download.Setting.MULTITHREAD, Download.Setting.MULTITHREAD_DEFAULT));
					}
					File f=new File(ti.getDir());
					if (!f.exists())f.mkdirs();
					if (!f.canWrite())
					{
						ti.setState(State.NOPERMISSION);
						EventBus.getDefault().post(ti);
						return;
					}
					else
					{
						if(f.getFreeSpace()>length){
						raf=new RandomAccessFile(new File(f, ti.getTaskname()), "rw");
						ti.setState(State.TEMPFILE);
						EventBus.getDefault().post(ti);
						raf.setLength(length);
						raf.close();
						ti.setState(State.DOWNLOADING);
						EventBus.getDefault().post(ti);
						int size=0;
						if (ti.isMultiThread())
							size=shared.getInt(Download.Setting.THREADSIZE, Download.Setting.THREADSIZE_DEFAULE);
							else size=1;
							long blocksize=length/size;
							ArrayList<DownloadInfo> ad=new ArrayList<>();
							for (int i=0;i < size;i++)
							{
								DownloadInfo di=new DownloadInfo();
								di.setStart(i*blocksize);
								di.setCurrent(di.getStart());
								if(i==size-1)
									di.setEnd(length);
									else
								di.setEnd((i+1)*blocksize);
								di.setTaskId(ti.getId());
								di.setNo(i);
								//download.insertDownloadInfo(di);
								ad.add(di);
								ldb.add(new DownloadBlock(this, di));
							}//循环
							ti.setDownloadinfo(ad);
							if(response.header("Content-Type")!=null)
							ti.setType(response.header("Content-Type"));
							ti.setLength(length);
							download.updateTaskInfoData(ti);
					}//剩余空间
					else{
						//空间不足
						ti.setState(State.DISKLESS);
						EventBus.getDefault().post(ti);
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
		catch (IOException e)
		{
			ti.setState(State.FAIL);
			EventBus.getDefault().post(ti);
			ds.taskItemFinish(ti.getId(),false);
		}finally{
			response.close();
		}
		}
	}
	private Notification.Builder getNotificafion()
	{
		RemoteViews rv=new RemoteViews(ds.getPackageName(), R.layout.notification_view);
		return new Notification.Builder(ds)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContent(rv)
			.setTicker("下载")
			.setOngoing(true)
			.setPriority(Notification.PRIORITY_MAX);
	}
	public enum State
	{
		QUERY,DOWNLOADING,PAUSE,STOP,FAIL,INVALIDE,DISKLESS,WAITING,TEMPFILE,SUCCESS,NOPERMISSION,DNS;
		//查询信息，下载中，暂停，停止，失败，地址过期，磁盘空间不足，等待，创建临时文件，完成，无权限;
	}
}
