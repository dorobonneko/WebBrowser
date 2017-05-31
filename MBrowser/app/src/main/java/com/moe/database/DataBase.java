package com.moe.database;
import android.database.sqlite.*;
import android.content.*;
import java.util.*;
import android.database.*;
import java.net.URL;
import java.net.MalformedURLException;
import com.moe.utils.Url;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import com.moe.entity.TaskInfo;
import com.moe.download.DownloadTask.State;
import com.moe.download.DownloadTask;
import com.moe.entity.DownloadInfo;
import de.greenrobot.event.EventBus;
import com.moe.bean.TaskBean;
import java.io.File;
import android.text.TextUtils;

class DataBase extends SQLiteOpenHelper implements SearchHistory,WebHistory,BlackList,HomePage
{

	@Override
	public String getJsonData()
	{
		JSONArray ja=new JSONArray();
		for (String[] str:getData())
		{
			try
			{
				JSONObject jo=new JSONObject();
				jo.put("url", str[0]);
				jo.put("title", str[1]);
				ja.put(jo);
			}
			catch (JSONException e)
			{}
		}
		return ja.toString();
	}


	@Override
	public synchronized List<String[]> getData()
	{
		List<String[]> list=new ArrayList<>();
		
		Cursor c=sql.query("homepage", null, null, null, null, null, "no asc");
		while (c.moveToNext())
		{
			list.add(new String[]{c.getString(c.getColumnIndex("url")),c.getString(c.getColumnIndex("title"))});
		}
		c.close();
		
		return list;
	}

	@Override
	public synchronized void insertItem(String url, String title)
	{
		Cursor c=sql.query("homepage", null, null, null, null, null, null);
		ContentValues cv=new ContentValues();
		cv.put("url", url);
		cv.put("title", title);
		cv.put("no", c.getCount() - 1);c.close();
		sql.insert("homepage", null, cv);
	}

	@Override
	public synchronized void deleteItem(String url)
	{
		Cursor c=sql.query("homepage", null, "url=?", new String[]{url}, null, null, null);
		if (c.moveToFirst())
		{
			int index=c.getInt(c.getColumnIndex("no"));
			c.close();
			sql.delete("homepage", "url=?", new String[]{url});
			c = sql.query("homepage", null, "no>? and no<?", new String[]{index + "",Integer.MAX_VALUE + ""}, null, null, null);
			while (c.moveToNext())
			{
				ContentValues cv=new ContentValues();
				cv.put("no", c.getInt(c.getColumnIndex("no")) - 1);
				sql.update("homepage", cv, "url=?", new String[]{c.getString(c.getColumnIndex("url"))});
			}
		}
		else{ c.close();
		
		}
	}


	@Override
	public int isBlackOrWhiteUrl(String url)
	{
		int flag=BlackList.UNKNOW;
		Cursor c=sql.query("blacklist", null, "url=?", new String[]{Url.getScheme(url)}, null, null, null);
		if (c.moveToFirst())
		{
			flag = c.getInt(c.getColumnIndex("flag"));
		}
		c.close();
		return flag;
	}


	@Override
	public void insertSite(String url, int state)
	{

		ContentValues cv=new ContentValues();
		cv.put("url", Url.getScheme(url));
		cv.put("flag", state);
		sql.insert("blacklist", null, cv);

	}


	@Override
	public synchronized void insertOrUpdateWebHistory(String url, String title)
	{
		if (url.length() != 0)
		{
			Cursor c=sql.query("webhistory", null, "url=?", new String[]{url}, null, null, null);
			ContentValues cv=new ContentValues();
			cv.put("time", System.currentTimeMillis());
			if (c.getCount() != 1)
			{
				cv.put("url", url);
				cv.put("title", title);
				sql.insert("webhistory", null, cv);
			}
			else sql.update("webhistory", cv, "url=?", new String[]{url});
			c.close();
		}
	}

	@Override
	public List getWebHistory()
	{
		ArrayList<String[]> as=new ArrayList<>();
		Cursor c=sql.query("webhistory", null, null, null, null, null, "time desc");
		while (c.moveToNext())
			as.add(new String[]{c.getString(c.getColumnIndex("url")),c.getString(c.getColumnIndex("title"))});
		c.close();
		return as;
	}

	@Override
	public void clearWebHistory()
	{
		new Thread(){
			public void run()
			{
				sql.delete("webhistory", null, null);
			}
		}.start();

	}


	/**@Override
	public boolean isBookmark(String url)
	{
		if (TextUtils.isEmpty(url))return false;
		boolean flag=false;
		Cursor c=sql.rawQuery("select no from bookmarks where url=?",new String[]{url});
		flag = c.getCount() == 1;
		c.close();
		return flag;
	}
*/
	

	@Override
	public void insertSearchHistory(final String data)
	{
		if (TextUtils.isEmpty(data))return;
		new Thread(){
			public void run(){
				Cursor c=sql.rawQuery("select time from searchhistory where data=?",new String[]{data});
				sql.beginTransaction();
				if(c.getCount()==1){
					ContentValues cv=new ContentValues();
					cv.put("time", System.currentTimeMillis());
					sql.update("searchhistory", cv, "data=?", new String[]{data});
					sql.setTransactionSuccessful();
				}else{
					SQLiteStatement statement=sql.compileStatement("insert into searchhistory values (?,?);");
					//sql.insert("searchhistory", null, cv);
					statement.bindString(1,data);
					statement.bindLong(2,System.currentTimeMillis());
					statement.executeInsert();
					sql.setTransactionSuccessful();
				}
				c.close();
				sql.endTransaction();
			}
		}.start();
	}

	@Override
	public List getSearchHistoryList()
	{
		
		List<String> ls=new ArrayList<>();
		Cursor c=sql.query("searchhistory", null, null, null, null, null, "time");
		while (c.moveToNext())
		{
			ls.add(c.getString(c.getColumnIndex("data")));
		}
		c.close();
		
		return ls;
	}

	@Override
	public List queryWebHistory(String key)
	{
		
		if (key.isEmpty())return null;
//		StringBuffer sb=new StringBuffer();
//		//sb.append("'");
//		for (String str:key.split(""))
//			sb.append(str).append("%");
//		//sb.append("'");
		List<String[]> ls=new ArrayList<>();
		key = "%"+key+"%";
		Cursor c=sql.query(false,"webhistory", null, "url like ? OR title like ?", new String[]{key,key}, null, null, "time desc","0,10");
		while (c.moveToNext())
		{
			ls.add(new String[]{c.getString(c.getColumnIndex("url")),c.getString(c.getColumnIndex("title"))});
		}
		c.close();
		
		return ls;
	}


	@Override
	public List querySearchHistory(String key)
	{
		
		if (key == null)
			throw new NullPointerException("key is not null!");
		key = "%"+key+"%";
		List ls=new ArrayList<>();
		Cursor c=sql.query("searchhistory", null, "data like ?", new String[]{key}, null, null, "time desc");
		if(c.getCount()>0)
		while (c.moveToNext())
		{
			ls.add(c.getString(c.getColumnIndex("data")));
		}
		c.close();
		
		return ls;
		
	}

	@Override
	public void clearSearchHistory()
	{
		sql.delete("searchhistory", null, null);
	}

	private static DataBase db;
	private SQLiteDatabase sql;

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("CREATE TABLE searchhistory(data TEXT primary key,time INTEGER)");
		p1.execSQL("CREATE TABLE webhistory(url TEXT primary key,title TEXT,time INTEGER)");
		p1.execSQL("CREATE TABLE blacklist(url TEXT primary key,flag INTEGER)");
		p1.execSQL("CREATE TABLE homepage(url TEXT PRIMARY KEY,title TEXT,no INTEGER)");
		ContentValues cv = new ContentValues();
		cv.put("url", "moe://addBookmark");
		cv.put("title", "添加");
		cv.put("no", Integer.MAX_VALUE);
		p1.insert("homepage", null, cv);
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
			}

	private DataBase(Context context)
	{
		super(context.getApplicationContext(), "moedatabase", null, 3);
		sql = getReadableDatabase();
	}
	static DataBase getInstance(Context context)
	{
		if (db == null)
			db = new DataBase(context);
		return db;
	}
}
