package com.moe.entity;

public class Bookmark implements Cloneable
{
	private int type,no,level;
	private String parent="",title="",summary="";

	public Bookmark clone()
	{
		try
		{
			return (Bookmark)super.clone();
		}
		catch (CloneNotSupportedException e)
		{}
		return null;
	}

	public String getPath()
	{
		if(parent.length()>0){
		if(parent.charAt(parent.length()-1)=='/')
			return parent+title;
			}
		return parent+"/"+title;
	}

	
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


	

	public void setParent(String parent)
	{
		this.parent = parent;
	}

	public String getParent()
	{
		return parent;
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
