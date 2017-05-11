package com.moe.bean;
import android.graphics.drawable.Drawable;

public class MenuItem
{
	private int id;
	private Drawable icon;
	private String summory;


	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setIcon(Drawable icon)
	{
		this.icon = icon;
	}

	public Drawable getIcon()
	{
		return icon;
	}

	public void setSummory(String summory)
	{
		this.summory = summory;
	}

	public String getSummory()
	{
		return summory;
	}}
