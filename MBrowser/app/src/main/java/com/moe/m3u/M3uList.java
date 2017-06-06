package com.moe.m3u;
import com.moe.m3u.tag.M3uTag;
import java.util.ArrayList;
import java.util.List;

public class M3uList
{
	private boolean live=true;
	private Type type=Type.MEDIA;
	private List<M3uTag> lmt=new ArrayList<>();

	public void setLive(boolean live)
	{
		this.live = live;
	}

	public boolean isLive()
	{
		return live;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}
	public List<M3uTag> getList(){
		return lmt;
	}
	public enum Type{
		MASTER,MEDIA;
	}
}
