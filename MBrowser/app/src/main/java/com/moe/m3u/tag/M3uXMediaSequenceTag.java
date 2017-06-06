package com.moe.m3u.tag;

public class M3uXMediaSequenceTag extends M3uXTag
{
	private long sequence;

public M3uXMediaSequenceTag(){
	super("#EXT-X-MEDIA-SEQUENCE");
}
	public void setSequence(long sequence)
	{
		this.sequence = sequence;
	}

	public long getSequence()
	{
		return sequence;
	}

	
	
	}
