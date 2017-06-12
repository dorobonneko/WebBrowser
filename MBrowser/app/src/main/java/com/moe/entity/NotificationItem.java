package com.moe.entity;
import android.app.Notification;
import android.widget.RemoteViews;
import android.os.Build;

public class NotificationItem
{
	private long size;
	private Notification notification;
	private RemoteViews remoteViews;
	private Notification.Builder builder;

	public void setSize(long size)
	{
		this.size = size;
	}

	public long getSize()
	{
		return size;
	}

	public void setBuilder(Notification.Builder builder)
	{
		this.builder = builder;
	}

	public Notification.Builder getBuilder()
	{
		return builder;
	}
	

	public Notification getNotification()
	{
		if(notification==null)
			notification=Build.VERSION.SDK_INT>15?builder.build():builder.getNotification();
		return notification;
	}


	public void setRemoteViews(RemoteViews remoteViews)
	{
		this.remoteViews = remoteViews;
	}

	public RemoteViews getRemoteViews()
	{
		return remoteViews;
	}
	
	}
