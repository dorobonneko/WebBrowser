package com.moe.m3u.tag;

public abstract class M3uTag
{
	private String tagName;
	public M3uTag(String tagName){
		setTagName(tagName);
	}
	public void setTagName(String tagName)
	{
		this.tagName = tagName;
	}

	public String getTagName()
	{
		return tagName;
	}
	public abstract boolean hasData();
	}
