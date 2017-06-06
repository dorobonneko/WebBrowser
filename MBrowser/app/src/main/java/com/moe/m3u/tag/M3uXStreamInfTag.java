package com.moe.m3u.tag;

public class M3uXStreamInfTag extends M3uTag
{
	//id
	private int id;
	//码率
	private int bandwidth;
	//分辨率
	private int width;
	private int height;
	private String codecs;
	private String url;
public M3uXStreamInfTag(){
	super("#EXT-X-STREAM-INF");
}

public void setUrl(String url)
{
	this.url = url;
}

public String getUrl()
{
	return url;
}

public void setCodecs(String codecs)
{
	this.codecs = codecs;
}

public String getCodecs()
{
	return codecs;
}
	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return width;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getHeight()
	{
		return height;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setBandwidth(int bandwidth)
	{
		this.bandwidth = bandwidth;
	}

	public int getBandwidth()
	{
		return bandwidth;
	}

	@Override
	public boolean hasData()
	{
		return true;
	}

	
	}
