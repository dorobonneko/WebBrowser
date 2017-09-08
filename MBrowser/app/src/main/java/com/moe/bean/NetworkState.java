package com.moe.bean;

public class NetworkState
{
	private boolean wifi;
	private boolean connected;
	private boolean canUse;

	

	public void setWifi(boolean wifi)
	{
		this.wifi = wifi;
	}

	public boolean isWifi()
	{
		return wifi;
	}

	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}

	public boolean isConnected()
	{
		return connected;
	}

	public void setCanUse(boolean canUse)
	{
		this.canUse = canUse;
	}

	public boolean isCanUse()
	{
		return canUse;
	}}
