package com.moe.utils;

public class Url
{
	public static String getScheme(String url){
		if(url==null||url.trim().length()==0)return null;
		int end=url.indexOf(":");
		return url.substring(0,end);
	}
}
