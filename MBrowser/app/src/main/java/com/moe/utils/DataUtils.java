package com.moe.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebView;
import com.moe.Mbrowser.R;
import com.moe.database.DataBase;
import android.webkit.CookieManager;
import com.moe.database.Sqlite;
import com.moe.database.SearchHistory;
import com.moe.database.WebHistory;
import com.moe.database.BlackList;
import java.io.File;

public class DataUtils
{
	public static void clearData(Context context){
		SharedPreferences shared=context.getSharedPreferences("moe",0);
		WebView wv=new WebView(context);
		if(shared.getBoolean(R.id.datamanager_view_searchhistory+"",false))
			Sqlite.getInstance(context,SearchHistory.class).clearSearchHistory();
		if(shared.getBoolean(R.id.datamanager_view_webhistory+"",false))
			Sqlite.getInstance(context,WebHistory.class).clearWebHistory();
		if(shared.getBoolean(R.id.datamanager_view_cache+"",false))
		{wv.clearCache(true);
		deleteDir(context.getCacheDir());}
		if(shared.getBoolean(R.id.datamanager_view_cookies+"",false))
			CookieManager.getInstance().removeAllCookie();
		if(shared.getBoolean(R.id.datamanager_view_form+"",false))
			wv.clearFormData();
			wv.destroy();
		if(shared.getBoolean(R.id.datamanager_view_outprograme+"",false))
			Sqlite.getInstance(context,BlackList.class).clear();
		}
	public static void deleteDir(File f){
		if(f.isDirectory()){
			for(File ff:f.listFiles())
			deleteDir(ff);
			f.delete();
		}else f.delete();
	}
}
