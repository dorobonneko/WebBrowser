package com.moe.m3u.tag;

public class M3uXMediaTag extends M3uXTag
{
	private String uri;
	private long groupId;
	private String language;
	private String name;
	private boolean _default;
	private boolean autuSelected;
	private Type type;
	public M3uXMediaTag(){
		super("#EXT-X-MEDIA");
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}
	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public String getUri()
	{
		return uri;
	}

	public void setGroupId(long groupId)
	{
		this.groupId = groupId;
	}

	public long getGroupId()
	{
		return groupId;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setDefault(boolean _default)
	{
		this._default = _default;
	}

	public boolean isDefault()
	{
		return _default;
	}

	public void setAutuSelected(boolean autuSelected)
	{
		this.autuSelected = autuSelected;
	}

	public boolean isAutuSelected()
	{
		return autuSelected;
	}
	public enum Type{
		AUDIO,VIDEO;
	}
}
