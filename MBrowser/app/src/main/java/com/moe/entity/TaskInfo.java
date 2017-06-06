package com.moe.entity;
import java.util.List;
import com.moe.download.DownloadTask;

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
	private String userAgent;
	private String type;
	private String sourceUrl="";
	private long length=0;
	private int id;
	private DownloadTask.State state=DownloadTask.State.PAUSE;
	private long time;
	private long[] tag=null;
	private boolean forbidden=false;

	public void setForbidden(boolean forbidden)
	{
		this.forbidden = forbidden;
	}

	public boolean isForbidden()
	{
		return forbidden;
	}
	public boolean getM3u8()
	{
		return "application/x-mpegURL".equalsIgnoreCase(type)||"application/vnd.apple.mpegurl".equalsIgnoreCase(type);
	}
	public void setTag(long time,long size)
	{
		if(tag==null)tag=new long[2];
		tag[0]=time;
		tag[1]=size;
	}

	public long[] getTag()
	{
		return tag;
	}
	public void setState(DownloadTask.State state)
	{
		this.state = state;
	}

	public DownloadTask.State getState()
	{
		return state;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public long getTime()
	{
		return time;
	}
	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		if(id==0)
			return taskname.hashCode();
		return id;
	}
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
	public void setUserAgent(String useragent)
	{
		this.userAgent = useragent;
	}

	public String getUserAgent()
	{
		return userAgent;
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
	public boolean isDownloading(){
		switch(state){
			case WAITING:
			case QUERY:
			case TEMPFILE:
			case DOWNLOADING:
				return true;
			default:
			return false;
		}
	}
	public class Pause{
		public final static int UNKNOW=0;
		public final static int SUPPORT=1;
		public final static int UNSUPPORT=2;
	}
	}
