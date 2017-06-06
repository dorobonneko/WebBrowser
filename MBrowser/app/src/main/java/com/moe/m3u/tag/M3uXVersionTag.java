package com.moe.m3u.tag;

public class M3uXVersionTag extends M3uXTag
{
	private int version;
	public M3uXVersionTag(){
		super("#EXT-X-VERSION");
	}
	public void setVersion(int version)
	{
		this.version = version;
	}

	public int getVersion()
	{
		return version;
	}
	

	
}
