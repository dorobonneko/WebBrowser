package com.moe.m3u.tag;
import java.util.Calendar;

public class M3uXProgramDataTimeTag extends M3uXTag
{
	private Calendar date;
public M3uXProgramDataTimeTag(){
	super("#EXT-X-PROGRAM-DATA-TIME");
}
	public void setDate(Calendar date)
	{
		this.date = date;
	}

	public Calendar getDate()
	{
		return date;
	}
	
}
