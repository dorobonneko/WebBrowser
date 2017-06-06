package com.moe.m3u.tag;

public class M3uXTagGetDurationTag extends M3uXTag
{
	private long time;

public M3uXTagGetDurationTag(){
	super("#EXT-X-TAGGETDURATION");
}
	public void setTime(long time)
	{
		this.time = time;
	}

	public long getTime()
	{
		return time;
	}

	

	
	}
