package com.moe.bean;
import android.webkit.URLUtil;
import java.net.URL;
import android.net.Uri;

public class DownloadItem
{
	private String url;
	private String contentDisposition;
	private String userAgent;
	private String cookie;
	private long length;
	private String mime;
	private String sourceUrl;
	private String fileName;


	public String getFileName()
	{
		if(fileName==null)
			fileName=Uri.decode(URLUtil.guessFileName(url,contentDisposition,mime));
		return fileName;
	}
	public void setSourceUrl(String sourceUrl)
	{
		this.sourceUrl = sourceUrl;
	}

	public String getSourceUrl()
	{
		return sourceUrl;
	}
	public void setMime(String mime)
	{
		this.mime = mime;
	}

	public String getMime()
	{
		return mime;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public void setContentDisposition(String contentDisposition)
	{
		this.contentDisposition = contentDisposition;
	}

	public String getContentDisposition()
	{
		return contentDisposition;
	}

	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	public String getUserAgent()
	{
		return userAgent;
	}

	public void setCookie(String cookie)
	{
		this.cookie = cookie;
	}

	public String getCookie()
	{
		return cookie;
	}

	public void setLength(long length)
	{
		this.length = length;
	}

	public long getLength()
	{
		return length;
	}}
