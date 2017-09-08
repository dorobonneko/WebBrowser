package com.moe.internal;
import android.content.Context;
import de.greenrobot.event.EventBus;
import com.moe.bean.Message;
import android.os.Build;
import android.content.Intent;
import com.moe.entity.TaskInfo;
import android.app.PendingIntent;
import android.app.Notification;
import com.moe.Mbrowser.R;
import android.widget.RemoteViews;
import android.support.v4.content.FileProvider;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import java.io.File;
import de.greenrobot.event.Subscribe;
import com.moe.entity.DownloadInfo;
import java.text.DecimalFormat;
import com.moe.download.DownloadTask;
import de.greenrobot.event.ThreadMode;
import com.moe.utils.LinkedListMap;
import com.moe.entity.NotificationItem;
import android.app.Service;

public class NotificationManager
{
	private static NotificationManager manger;
	private android.app.NotificationManager nm;
	private Context context;
	private LinkedListMap<Integer,NotificationItem> llm;
	private NotificationManager(Context context){
		this.context=context;
		llm=new LinkedListMap<>();
		nm=(android.app.NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
		EventBus.getDefault().register(this);
	}
	public static NotificationManager getInstance(Context context){
		if(manger==null)manger=new NotificationManager(context);
		return manger;
	}
	public void cancel(int id)
	{
		nm.cancel(id);
	}
	@Subscribe(priority=1,threadMode=ThreadMode.BackgroundThread)
	public void update(TaskInfo ti){
		if(ti.getState()==DownloadTask.State.DELETE)return;
		if(ti.isSuccess()){
			success(ti);
			return;
		}
		NotificationItem ni=llm.getKey(ti.getId());
		if(ni==null){//创建
		ni=new NotificationItem();
			ni.setRemoteViews(new RemoteViews(context.getPackageName(), R.layout.notification_view));
			Intent intent=new Intent();
			intent.setAction("download");
			intent.setClassName(context.getPackageName(), context.getPackageName() + ".HomeActivity");
			PendingIntent pi=PendingIntent.getActivity(context, 233, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			ni.setBuilder(new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_weather_cloudy)
				.setContent(ni.getRemoteViews())
				.setTicker("下载")
				.setContentIntent(pi));
			ni.getRemoteViews().setOnClickPendingIntent(R.id.notificationviewImage_state, PendingIntent.getBroadcast(context, 0, new Intent("com.moe.notification.state").putExtra("id", ti.getId()), PendingIntent.FLAG_UPDATE_CURRENT));
			ni.getRemoteViews().setTextViewText(R.id.notificationview_title, ti.getTaskname());
			llm.put(ti.getId(),ni);
		}
		if(ti.isDownloading()){
			long size=0;
				long length=0;
				if (ti.getDownloadinfo() != null)
				{
					for (DownloadInfo di:ti.getDownloadinfo())
					{
						size += di.getCurrent() - di.getStart();
						length += di.getEnd();
					}
					if (!ti.getM3u8())length = ti.getLength();
					ni.getRemoteViews().setProgressBar(R.id.notificationview_progress, 100, (int)(size / (double)length * 100), false);
				}
				ni.getRemoteViews().setTextViewText(R.id.notificationview_size, new DecimalFormat("0.00").format(size / 1024.0 / 1024) + "/" + new DecimalFormat("0.00").format(length / 1024.0 / 1024) + "MB");
			long speed=(size-ni.getSize());
				ti.setSpeed(new DecimalFormat("0.00").format(speed / 1024.0f/ 1024) + "MB/s");
				ni.getRemoteViews().setTextViewText(R.id.notificationview_speed,ti.getSpeed() );
				ni.setSize(size);
				ni.getRemoteViews().setImageViewResource(R.id.notificationviewImage_state, R.drawable.ic_pause);
							
		}else{
			ni.getRemoteViews().setImageViewResource(R.id.notificationviewImage_state, R.drawable.ic_play);
			}
		if(ti.getState()!=DownloadTask.State.WAITING)
		nm.notify(ti.getId(),ni.getNotification());
			
	}
	public void success(TaskInfo ti){
		NotificationItem ni=llm.getKey(ti.getId());
		llm.removeKey(ti.getId());
		nm.cancel(ti.getId());
		if(ni==null)return;
			Notification.Builder nb=ni.getBuilder();
			nb.setContent(null);
			nb.setContentTitle(ti.getTaskname());
			nb.setContentInfo("下载成功");
			nb.setOngoing(false);
			nb.setAutoCancel(true);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			if (Build.VERSION.SDK_INT >19)
			{
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", new File(ti.getDir(), ti.getTaskname()));
				intent.setDataAndType(contentUri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl((intent.getDataString()))));
			}
			else
			{
				intent.setDataAndType(Uri.fromFile(new File(ti.getDir(), ti.getTaskname())), MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(intent.getDataString())));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			PendingIntent pi=PendingIntent.getActivity(context, 233, intent, PendingIntent.FLAG_ONE_SHOT);
			nb.setContentIntent(pi);
		nm.notify(ti.getId(),Build.VERSION.SDK_INT >15?nb.build():nb.getNotification());
		if(ti.getTaskname().matches(".*?.apk"))
			context.startActivity(intent);
	}
	public void destory(){
		manger=null;
		nm.cancelAll();
		EventBus.getDefault().unregister(this);
	}
}
