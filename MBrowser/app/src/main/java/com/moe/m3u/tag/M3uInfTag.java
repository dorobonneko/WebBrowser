package com.moe.m3u.tag;

public class M3uInfTag extends M3uTag
{
	private double duration;
	private String title;
	private String url;
	public M3uInfTag(){
		super("#EXTINF");
	}

	public void setDuration(double duration)
	{
		this.duration = duration;
	}

	public double getDuration()
	{
		return duration;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	@Override
	public boolean hasData()
	{
		return true;
	}

	
	}
