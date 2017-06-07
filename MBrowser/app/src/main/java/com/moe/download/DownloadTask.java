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
import java.util.Arrays;
import java.util.Comparator;
import java.io.FileInputStream;
import com.moe.utils.DataUtils;
import com.moe.m3u.M3uParser;
import java.net.URI;
import com.moe.m3u.M3uList;
import java.util.zip.GZIPInputStream;
import com.moe.m3u.tag.M3uTag;
import com.moe.m3u.tag.M3uXStreamInfTag;
import com.moe.m3u.tag.M3uInfTag;

public class DownloadTask extends Thread
{
	private List<DownloadBlock> ldb;
	private TaskInfo ti;
	private DownloadService ds;
	private OkHttpClient okhttp;
	private SharedPreferences shared;
	private Download download;
	private Response response;
	private RandomAccessFile raf;
	private int errorsize=0;
	public DownloadTask(DownloadService ds, TaskInfo ti, OkHttpClient ok)
	{
		this.ti = ti;
		this.ds = ds;
		ldb = new ArrayList<>();
		this.download = Sqlite.getInstance(ds, Download.class);
		okhttp = ok;
		shared = ds.getSharedPreferences("download", 0);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	public void itemFinish(DownloadBlock p0)
	{
		if (p0.isSuccess())
		{
			for (DownloadBlock db:ldb)
			{
				if (!db.isSuccess())
					return;
			}
			if (ti.isSuccess())return;//任务全部成功
			if (ti.getM3u8())
			{
				ti.setSuccess(true);
				//合并文件
				File file=new File(ti.getDir(), ti.getTaskname());
				File[] lf=file.listFiles();
				Arrays.sort(lf, new Comparator<File>(){

						@Override
						public int compare(File p1, File p2)
						{
							return Integer.compare(Integer.parseInt(p1.getName()), Integer.parseInt(p2.getName()));
						}
					});
				File tmp=new File(ti.getDir(), System.currentTimeMillis() + "");
				try
				{
					int len=0;
					byte[] b=new byte[10240];
					FileOutputStream fos=new FileOutputStream(tmp);
					for (File l:lf)
					{
						FileInputStream fis=new FileInputStream(l);
						while ((len = fis.read(b)) != -1)
							fos.write(b, 0, len);
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
			ti.setSuccess(true);
			download.updateTaskInfoData(ti);
			ds.taskItemFinish(ti.getId(), true);
		}
		else
		{
			if (ti.getM3u8())
			{
				switch (shared.getInt(Download.Setting.M3U8ERROR, Download.Setting.M3U8ERROR_DEFAULT))
				{
					case 0:break;
					case 1:return;
					case 2://重试当前出错任务
						if (p0.getErrorsize() < Integer.parseInt(ds.getResources().getTextArray(R.array.size)[shared.getInt(Download.Setting.RELOADSIZE, Download.Setting.RELOADSIZE_DEFAULT)].toString()))
						{
							p0.start();
							p0.setErrorsize(p0.getErrorsize() + 1);
							return;}
						break;
				}

			}
			else
			{
				if (errorsize++ < Integer.parseInt(ds.getResources().getTextArray(R.array.size)[shared.getInt(Download.Setting.RELOADSIZE, Download.Setting.RELOADSIZE_DEFAULT)].toString()))
				{
					p0.start();
					return;
				}
			}
			pause();
			ti.setState(State.FAIL);
			ds.taskItemFinish(ti.getId(), false);
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
					DataUtils.deleteDir(new File(ti.getDir(), ti.getTaskname()));
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
	private void startTask() throws Exception
	{
		for (DownloadBlock db:ldb)
			if (ti.getState() == State.DOWNLOADING)
				try
				{db.start();}
				catch (Exception e)
				{throw new Exception();}
	}

	@Override
	public void run()
	{
		try
		{
			//判断任务是否已经存在，如果不存在则创建查询
			List<DownloadInfo> ldi=ti.getDownloadinfo();
			if (ldi != null && ldi.size() > 0)
			{
				for (DownloadInfo di:ldi)
				{
					if (!di.isSuccess())//避免创建已完成块，以免浪费资源
						ldb.add(new DownloadBlock(this, di));
				}
				if (ldb.size() == 0)
				{
					ti.setSuccess(true);
					download.updateTaskInfoData(ti);
					ds.taskItemFinish(ti.getId(), true);
				}
				else
				{
					ti.setState(State.DOWNLOADING);
					startTask();}
				return;
			}
			ti.setState(State.QUERY);
			//构造请求
			Request.Builder request=new Request.Builder();
			if (!TextUtils.isEmpty(ti.getCookie()))
				request.addHeader("Cookie", ti.getCookie());
			if (!TextUtils.isEmpty(ti.getUserAgent()))
				request.addHeader("User-Agent", ti.getUserAgent());
			//if(ti.getSourceUrl()!=null)
			if (!ti.isForbidden())
				request.addHeader("Referer", ti.getTaskurl());
			request.addHeader("Accept", "*/*");
			request.addHeader("Connection", "Keep-Alive");
			//request.addHeader("Icy-MetaData", "1");
			request.addHeader("Range", "bytes=0-");
			request.addHeader("Accept-Encoding", "gzip");
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
				case 403://有防盗链
					if (ti.isForbidden())throw new IOException("资源禁止访问");
					ti.setForbidden(true);
					start();
					return;
				default:
					//链接无效
					ti.setState(State.INVALIDE);
					ds.taskItemFinish(ti.getId(), false);
					return;
			}
			long length=ti.getLength();
			try
			{
				length = Long.parseLong(response.header("Content-Length"));
			}
			catch (Exception e)
			{length = response.body().contentLength();}
			ti.setLength(length);
			if (!TextUtils.isEmpty(response.header("Content-Type")))
				ti.setType(response.header("Content-Type"));
			ArrayList<DownloadInfo> ad=new ArrayList<>();
			if (ti.getM3u8())
			{
				//m3u8
				InputStream is=response.body().byteStream();
				if ("gzip".equalsIgnoreCase(response.header("Content-Encoding")))
					is = new GZIPInputStream(is);
				M3uList ml=M3uParser.parse(is, URI.create(ti.getTaskurl())).getList();
				is.close();
				switch (ml.getType())
				{
					case MASTER:
						for (M3uTag mt:ml.getList())
						{
							if (mt instanceof M3uXStreamInfTag)
							{
								ti.setTaskurl(((M3uXStreamInfTag)mt).getUrl());
								start();
								return;
							}
						}
						break;
					case MEDIA:
						if (ml.isLive())throw new IOException("不支持直播m3u8");
						for (M3uTag mt:ml.getList())
						{
							if (mt instanceof M3uInfTag)
							{
								DownloadInfo di=new DownloadInfo();
								di.setTaskId(ti.getId());
								di.setNo(ad.size());
								di.setCurrent(0);
								di.setStart(0);
								di.setUrl(((M3uInfTag)mt).getUrl());
								ad.add(di);
								ldb.add(new DownloadBlock(this, di));
							}
						}
						break;
				}

			}
			else
			{
				File f=new File(ti.getDir());
				if (!f.exists())
					f.mkdirs();
				if (!f.canWrite())
				{
					//无权限读写
					ti.setState(State.NOPERMISSION);
					ds.taskItemFinish(ti.getId(), false);
					return;
				}
				if (f.getFreeSpace() < length)
				{
					//空间不足
					ti.setState(State.DISKLESS);
					ds.taskItemFinish(ti.getId(), false);
					return;
				}
				File file=new File(f, ti.getTaskname());
				if (file.exists())DataUtils.deleteDir(file);
				raf = new RandomAccessFile(file, "rw");
				ti.setState(State.TEMPFILE);
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
			ds.taskItemFinish(ti.getId(), false);
		}
		finally
		{
			if (response != null)
				response.close();
		}
	}
	
	
	public enum State
	{
		QUERY,DOWNLOADING,PAUSE,FAIL,INVALIDE,DISKLESS,WAITING,TEMPFILE,SUCCESS,NOPERMISSION,DELETE;
		//查询信息，下载中，暂停，失败，地址过期，磁盘空间不足，等待，创建临时文件，完成，无权限;
	}
}
