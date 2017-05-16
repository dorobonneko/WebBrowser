package com.moe.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebView;
import com.moe.Mbrowser.R;
import com.moe.database.DataBase;
import android.webkit.CookieManager;

public class DataUtils
{
	public static void clearData(Context context){
		SharedPreferences shared=context.getSharedPreferences("moe",0);
		WebView wv=new WebView(context);
		if(shared.getBoolean(R.id.datamanager_view_searchhistory+"",false))
			DataBase.getInstance(context).clearSearchHistory();
		if(shared.getBoolean(R.id.datamanager_view_webhistory+"",false))
			DataBase.getInstance(context).clearWebHistory();
		if(shared.getBoolean(R.id.datamanager_view_cache+"",false))
			wv.clearCache(true);
		if(shared.getBoolean(R.id.datamanager_view_cookies+"",false))
			CookieManager.getInstance().removeAllCookie();
		if(shared.getBoolean(R.id.datamanager_view_form+"",false))
			wv.clearFormData();
			wv.destroy();
	}
}
