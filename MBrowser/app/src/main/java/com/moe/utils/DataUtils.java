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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
		{
		deleteDir(new File(context.getCacheDir().getParentFile(),"app_webview/IndexedDB"));
			deleteDir(new File(context.getCacheDir().getParentFile(),"app_webview/Local Storage"));
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
	public static void copyFile(File oldFile,File newFile) throws FileNotFoundException{
		if(!oldFile.exists())throw new FileNotFoundException("文件不存在");
		if(oldFile.isDirectory())
		{
			if(!newFile.exists())newFile.mkdirs();
			for(File file:oldFile.listFiles())
			copyFile(file,new File(newFile,file.getName()));
		}else{
			if(newFile.getParentFile().exists())newFile.getParentFile().mkdirs();
			byte[] buffer=new byte[409600];
			int len=0;
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try
			{
				fis=new FileInputStream(oldFile);
				fos=new FileOutputStream(newFile);
				while((len =fis.read(buffer))!=-1)
					fos.write(buffer,0,len);
					fos.flush();
			}
			catch (IOException e)
			{}finally{
				try
				{
					if (fos != null)fos.close();
				}
				catch (IOException e)
				{}
				try
				{
					if (fis != null)fis.close();
				}
				catch (IOException e)
				{}
			}
		}
	}
}
