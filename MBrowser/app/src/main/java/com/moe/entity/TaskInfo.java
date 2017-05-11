package com.moe.entity;
import java.util.List;

public class TaskInfo
{
	private String taskurl;
	private String taskname;
	private String dir;
	private String cookie;
	private List<DownloadInfo> downloadinfo;
	private boolean multiThread=false;
	private int support=Pause.UNKNOW;
	private boolean success=false;
	private String useragent;
	private String type;
	private String sourceUrl;
	private long length;

	public void setLength(long length)
	{
		this.length = length;
	}

	public long getLength()
	{
		return length;
	}
	public void setSourceUrl(String sourceUrl)
	{
		this.sourceUrl = sourceUrl;
	}

	public String getSourceUrl()
	{
		return sourceUrl;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}
	public void setUseragent(String useragent)
	{
		this.useragent = useragent;
	}

	public String getUseragent()
	{
		return useragent;
	}
	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public boolean isSuccess()
	{
		return success;
	}
	public void setMultiThread(boolean multiThread)
	{
		this.multiThread = multiThread;
	}

	public boolean isMultiThread()
	{
		return multiThread;
	}

	public void setSupport(int support)
	{
		this.support = support;
	}

	public int getSupport()
	{
		return support;
	}

	public void setTaskurl(String taskurl)
	{
		this.taskurl = taskurl;
	}

	public String getTaskurl()
	{
		return taskurl;
	}

	public void setTaskname(String taskname)
	{
		this.taskname = taskname;
	}

	public String getTaskname()
	{
		return taskname;
	}

	public void setDir(String dir)
	{
		this.dir = dir;
	}

	public String getDir()
	{
		return dir;
	}

	public void setCookie(String cookie)
	{
		this.cookie = cookie;
	}

	public String getCookie()
	{
		return cookie;
	}

	public void setDownloadinfo(List<DownloadInfo> downloadinfo)
	{
		this.downloadinfo = downloadinfo;
	}

	public List<DownloadInfo> getDownloadinfo()
	{
		return downloadinfo;
	}
	public class Pause{
		public final static int UNKNOW=0;
		public final static int SUPPORT=1;
		public final static int UNSUPPORT=2;
	}
	}
