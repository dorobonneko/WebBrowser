package com.moe.internal;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.text.TextUtils;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.content.Context;
import java.util.List;
import com.moe.database.Sqlite;
import com.moe.database.UrlBlockDatabase;
import java.util.regex.PatternSyntaxException;
import java.util.Collection;
import com.moe.utils.LinkedListMap;

public class UrlBlock
{
	private static UrlBlock vb;
	private LinkedListMap<String,Pattern> video=new LinkedListMap<>(); 
	private UrlBlock(Context context){
		reload(Sqlite.getInstance(context,UrlBlockDatabase.class).query());
	}
public boolean isExists(String url)
{
	List<Pattern> ls=video.values();
	for(int i=0;i<ls.size();i++)
	//while(s.matcher(url).lookingAt())return true;
	while(ls.get(i).matcher(url).find())
		return true;
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
		try{video.put(s,Pattern.compile(s));}catch(PatternSyntaxException e){}
	}
	public void delete(String s){
		video.removeKey(s);
	}
	public void clear(){
		video.clear();
	}
}
