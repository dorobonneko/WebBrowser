package com.moe.m3u.tag;

public class M3uXKeyTag extends M3uXTag
{
	private Method method;
	private String uri;
	private String key;
public M3uXKeyTag(){
	super("#EXT-X-KEY");
}
	public void setKey(String key)
	{
		this.key = key;
	}

	public String getKey()
	{
		return key;
	}
	public void setMethod(Method method)
	{
		this.method = method;
	}

	public Method getMethod()
	{
		return method;
	}

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public String getUri()
	{
		return uri;
	}
	

	public enum Method{
		NONE,AES128;
	}
}
