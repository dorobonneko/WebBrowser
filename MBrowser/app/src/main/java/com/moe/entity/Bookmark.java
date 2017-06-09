package com.moe.entity;

public class Bookmark
{
	private int parent,son,type,no,level;
	private String title,summary="";

	
	public void setLevel(int level)
	{
		this.level = level;
	}

	public int getLevel()
	{
		return level;
	}

	
	public void setNo(int no)
	{
		this.no = no;
	}

	public int getNo()
	{
		return no;
	}


	

	public void setParent(int parent)
	{
		this.parent = parent;
	}

	public int getParent()
	{
		return parent;
	}

	public void setSon(int son)
	{
		this.son = son;
	}

	public int getSon()
	{
		return son;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public int getType()
	{
		return type;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setSummary(String summary)
	{
		this.summary = summary;
	}

	public String getSummary()
	{
		return summary;
	}}
