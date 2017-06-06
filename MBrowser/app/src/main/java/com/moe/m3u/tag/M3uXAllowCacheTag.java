package com.moe.m3u.tag;

public class M3uXAllowCacheTag extends M3uXTag
{
	private boolean allow;
public M3uXAllowCacheTag(){
	super("#EXT-X-ALLOW-CACHE");
}

	public void setAllow(boolean allow)
	{
		this.allow = allow;
	}

	public boolean isAllow()
	{
		return allow;
	}

	
	
	}
