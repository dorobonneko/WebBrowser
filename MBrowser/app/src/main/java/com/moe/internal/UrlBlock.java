package com.moe.internal;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.text.TextUtils;
import java.io.IOException;
import android.content.Context;
import java.util.List;
import com.moe.database.Sqlite;
import com.moe.database.UrlBlockDatabase;
import java.util.regex.PatternSyntaxException;
import java.util.Collection;
import com.moe.utils.LinkedListMap;
import com.moe.regex.Matcher;

public class UrlBlock
{
	private static UrlBlock vb;
	private LinkedListMap<String,Matcher> video=new LinkedListMap<>(); 
	private UrlBlock(Context context){
		reload(Sqlite.getInstance(context,UrlBlockDatabase.class).query());
	}
public boolean matches(String host,String path,String url)
{
	List<Matcher> ls=video.values();
	Matcher matcher=null;
	for(int i=0;i<ls.size();i++){
		matcher=ls.get(i);
	switch(matcher.getMode()){
		case HOST:
			if(matcher.matches(host))
				return true;
				else break;
		case PATH:
			if(matcher.matches(path))
				return true;
			else break;
		default:
			if(matcher.matches(url))
				return true;
			else break;
	}
		}
	return false;
}
	
	public static UrlBlock getInstance(Context context){
			if (vb == null)
				vb = new UrlBlock(context);
		return vb;
	}
	public static UrlBlock getInstance(Context context,List<String> list){
		if (vb == null)
		vb=new UrlBlock(context);
		else
			vb.reload(list);
		return vb;
	}
	public void reload(List<String> list){
		video.clear();
		for(String s:list)
		insert(s);
	}
	public void insert(String s){
		try{video.put(s,Matcher.compile(s));}catch(PatternSyntaxException e){}
	}
	public void delete(String s){
		video.removeKey(s);
	}
	public void clear(){
		video.clear();
	}
}
