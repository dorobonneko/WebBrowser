package com.moe.entity;

public class DomElement
{
	private String tag;
	private String _class;
	private String src;
	private String href;
	private String value;
	private String id;


	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getTag()
	{
		return tag;
	}

	public void set_Class(String _class)
	{
		this._class = _class;
	}

	public String get_Class()
	{
		return this._class;
	}

	public void setSrc(String src)
	{
		this.src = src;
	}

	public String getSrc()
	{
		return src;
	}

	public void setHref(String href)
	{
		this.href = href;
	}

	public String getHref()
	{
		return href;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return tag+";"+id+";"+_class+";"+value+";"+src+";"+href;
	}
	
	}
