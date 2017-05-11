package com.moe.entity;

public class DownloadInfo
{
	private int id;
	private String taskurl;
	private long start,current,end;
	 

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setTaskurl(String taskurl)
	{
		this.taskurl = taskurl;
	}

	public String getTaskurl()
	{
		return taskurl;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public long getStart()
	{
		return start;
	}

	public void setCurrent(long current)
	{
		this.current = current;
	}

	public long getCurrent()
	{
		return current;
	}

	public void setEnd(long end)
	{
		this.end = end;
	}

	public long getEnd()
	{
		return end;
	}

	
}
