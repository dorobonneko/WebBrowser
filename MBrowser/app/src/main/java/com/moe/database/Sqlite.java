package com.moe.database;
import android.content.Context;

public class Sqlite
{
	public static <T extends Object> T getInstance(Context context,Class<T> c){
		if(c == Download.class)
		return c.cast(DownloadImpl.getInstance(context));
		else if(c == AdBlockDatabase.class)
		return c.cast(AdBlockDatabase.getInstance(context));
		else if(c == BookMarks.class)
		return c.cast(BookmarkImpl.getInstance(context));
		else if(c == JavaScript.class)
		return c.cast(JavaScriptImlp.getInstance(context));
		else if(c== UrlBlockDatabase.class)
		return c.cast(UrlBlockDatabase.getInstance(context));
		else return c.cast(DataBase.getInstance(context));
	}
}
