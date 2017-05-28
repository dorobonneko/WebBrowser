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

public class DataBase extends SQLiteOpenHelper implements SearchHistory,WebHistory,BookMarks,BlackList,HomePage,Download
{

	@Override
	public void clearAllTask(final Object[] id, final boolean file)
	{
		//new Thread(){
		//	public void run(){
				
					for(Object tii:id){
						TaskInfo ti=(TaskInfo)tii;
						deleteTaskInfoWithId(ti.getId());
						if(file)
							new File(ti.getDir(),ti.getTaskname()).delete();
					}
				
		//	}
		//}.start();
	}


	@Override
	public void clearAllSuccessTask(final boolean file)
	{
		new Thread(){
			public void run(){
				
				Cursor c=sql.query("download",new String[]{"id","dir","name"},"success=?",new String[]{1+""},null,null,null);
				while(c.moveToNext()){
					deleteTaskInfoWithId(c.getInt(c.getColumnIndex("id")));
					if(file)
						new File(c.getString(c.getColumnIndex("dir")),c.getString(c.getColumnIndex("name"))).delete();
				}
				c.close();
				
				
			}
		}.start();
	}


	@Override
	public void updataBookmark(String url, String name, String dir, String currenturl)
	{
		
		Cursor c=sql.query("bookmarks",null,"url=?",new String[]{currenturl},null,null,null);
		if(c.moveToFirst()){
			String folder=c.getString(c.getColumnIndex("dir"));
			c.close();
			
			if(folder.equals(dir)){
				ContentValues cv=new ContentValues();
				cv.put("url",url);
				cv.put("title",name);
				sql.update("bookmarks",cv,"url=?",new String[]{currenturl});
			}else{
				deleteBookmark(currenturl);
				insertBookmark(url,name,dir);
			}
		}else{ 
		c.close();
		}
	}


	@Override
	public String[] getBookmark(String url)
	{
		
		Cursor c=sql.query("bookmarks",null,"url=?",new String[]{url},null,null,null);
		if(c.moveToFirst()){
			String[] str=new String[3];
			str[0]=url;
			str[1]=c.getString(c.getColumnIndex("title"));
			str[2]=c.getString(c.getColumnIndex("dir"));
			c.close();
			
			return str;
		}else{
			c.close();
			
			}
		return null;
	}


	@Override
	public void updateTaskInfoData(TaskInfo ti)
	{
		ContentValues cv=new ContentValues();
		cv.put("url", ti.getTaskurl());
		//cv.put("dir",ti.getDir());
		//cv.put("name",ti.getTaskname());
		cv.put("pause", ti.getSupport());
		cv.put("multithread", ti.isMultiThread());
		cv.put("cookie", ti.getCookie());
		cv.put("success", ti.isSuccess());
		cv.put("useragent", ti.getUserAgent());
		cv.put("mime", ti.getType());
		cv.put("sourceurl", ti.getSourceUrl());
		cv.put("length", ti.getLength());
		//cv.put("time",System.currentTimeMillis());
		sql.update("download", cv, "id=?", new String[]{ti.getId() + ""});
		deleteDownloadInfoWithId(ti.getId());
		insertDownloadInfo(ti);
	}


	@Override
	public void updateTaskInfo(TaskInfo ti)
	{
		Cursor c=sql.query("download", null, "id=?", new String[]{ti.getId() + ""}, null, null, null);
		if (c.moveToFirst() && c.getCount() == 1)
		{
			int id=c.getInt(c.getColumnIndex("id"));
			c.close();
			ContentValues cv=new ContentValues();
			cv.put("url", ti.getTaskurl());
			//cv.put("dir",ti.getDir());
			//cv.put("name",ti.getTaskname());
			//cv.put("pause",ti.getSupport());
			//cv.put("multithread",ti.isMultiThread());
			cv.put("cookie", ti.getCookie());
			cv.put("success",ti.isSuccess());
			cv.put("useragent", ti.getUserAgent());
			cv.put("mime", ti.getType());
			//cv.put("sourceurl",ti.getSourceUrl());
			//cv.put("length",ti.getLength());
			cv.put("time", System.currentTimeMillis());
			sql.update("download", cv, "id=?", new String[]{id + ""});
//		List<DownloadInfo> ldi=getDownloadInfoWithId(id);
//		if (ldi.size() > 0){
//			deleteDownloadInfoWithId(ti.getId());
//			for (DownloadInfo di:ldi)
//				di.setTaskId(id);
//			insertDownloadInfo(ldi);
//		}
			EventBus.getDefault().post(new TaskBean(queryTaskInfoWithId(ti.getId()), TaskBean.State.UPDATE));
		}
		else
		{
			c.close();
			
		}
	}


	@Override
	public void addTaskInfo(final TaskInfo ti,final Download.Callback call)
	{
		new Thread(){
			public void run(){
				ContentValues cv=new ContentValues();
				cv.put("id", ti.getId());
				cv.put("url", ti.getTaskurl());
				cv.put("dir", ti.getDir());
				cv.put("name", ti.getTaskname());
				cv.put("pause", ti.getSupport());
				cv.put("multithread", ti.isMultiThread());
				cv.put("cookie", ti.getCookie());
				cv.put("success", ti.isSuccess());
				cv.put("useragent", ti.getUserAgent());
				cv.put("mime", ti.getType());
				cv.put("sourceurl", ti.getSourceUrl());
				cv.put("length", ti.getLength());
				cv.put("time", System.currentTimeMillis());
				long i=sql.insert("download", null, cv);
				if(i>0){
					EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.ADD));
					if(call!=null)call.callback(ti,Download.State.SUCCESS);
				}
				if(call!=null)call.callback(ti,Download.State.FAIL);
				
			}
		}.start();
		}

	@Override
	public void deleteTaskInfoWithId(int url)
	{
		TaskInfo ti=queryTaskInfoWithId(url);
		sql.delete("download", "id=?", new String[]{url + ""});
		deleteDownloadInfoWithId(url);
		EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.DELETE));
	}

	@Override
	public List<TaskInfo> getAllTaskInfo()
	{
		ArrayList<TaskInfo> at=new ArrayList<>();
		
		Cursor cursor=sql.query("download", null, null, null, null, null, null);
		while (cursor.moveToNext())
		{
			TaskInfo ti=new TaskInfo();
			ti.setId(cursor.getInt(cursor.getColumnIndex("id")));
			ti.setTaskurl(cursor.getString(cursor.getColumnIndex("url")));
			ti.setTaskname(cursor.getString(cursor.getColumnIndex("name")));
			ti.setCookie(cursor.getString(cursor.getColumnIndex("cookie")));
			ti.setDir(cursor.getString(cursor.getColumnIndex("dir")));
			ti.setSupport(cursor.getInt(cursor.getColumnIndex("pause")));
			ti.setMultiThread(cursor.getInt(cursor.getColumnIndex("multithread")) == 1);
			ti.setSuccess(cursor.getInt(cursor.getColumnIndex("success")) == 1);
			ti.setUserAgent(cursor.getString(cursor.getColumnIndex("useragent")));
			ti.setType(cursor.getString(cursor.getColumnIndex("mime")));
			ti.setSourceUrl(cursor.getString(cursor.getColumnIndex("sourceurl")));
			ti.setLength(cursor.getLong(cursor.getColumnIndex("length")));
			if (!ti.isSuccess())
			{
				ti.setDownloadinfo(getDownloadInfoWithId(ti.getId()));

			}
			at.add(ti);
		}
		cursor.close();
		
		return at;
	}


	@Override
	public List<TaskInfo> getAllTaskInfoWithState(boolean state)
	{

		ArrayList<TaskInfo> at=new ArrayList<>();
		
		Cursor cursor=sql.query("download", null, "success=?", new String[]{(state == true ?1: 0) + ""}, null, null, state == true ?"time desc": "time asc");
		while (cursor.moveToNext())
		{
			TaskInfo ti=new TaskInfo();
			ti.setId(cursor.getInt(cursor.getColumnIndex("id")));
			ti.setTaskurl(cursor.getString(cursor.getColumnIndex("url")));
			ti.setTaskname(cursor.getString(cursor.getColumnIndex("name")));
			ti.setCookie(cursor.getString(cursor.getColumnIndex("cookie")));
			ti.setDir(cursor.getString(cursor.getColumnIndex("dir")));
			ti.setSupport(cursor.getInt(cursor.getColumnIndex("pause")));
			ti.setMultiThread(cursor.getInt(cursor.getColumnIndex("multithread")) == 1);
			ti.setSuccess(cursor.getInt(cursor.getColumnIndex("success")) == 1);
			ti.setUserAgent(cursor.getString(cursor.getColumnIndex("useragent")));
			ti.setType(cursor.getString(cursor.getColumnIndex("mime")));
			ti.setSourceUrl(cursor.getString(cursor.getColumnIndex("sourceurl")));
			ti.setLength(cursor.getLong(cursor.getColumnIndex("length")));
			if (!ti.isSuccess())
			{
				ti.setDownloadinfo(getDownloadInfoWithId(ti.getId()));
			}
			at.add(ti);
		}
		cursor.close();
		
		return at;
	}

	@Override
	public TaskInfo queryTaskInfoWithId(int url)
	{
		TaskInfo ti=new TaskInfo();
		
		Cursor cursor=sql.query("download", null, "id=?", new String[]{url + ""}, null, null, null);
		if (cursor.moveToFirst())
		{
			ti.setId(cursor.getInt(cursor.getColumnIndex("id")));
			ti.setTaskurl(cursor.getString(cursor.getColumnIndex("url")));
			ti.setTaskname(cursor.getString(cursor.getColumnIndex("name")));
			ti.setCookie(cursor.getString(cursor.getColumnIndex("cookie")));
			ti.setDir(cursor.getString(cursor.getColumnIndex("dir")));
			ti.setSupport(cursor.getInt(cursor.getColumnIndex("pause")));
			ti.setMultiThread(cursor.getInt(cursor.getColumnIndex("multithread")) == 1);
			ti.setSuccess(cursor.getInt(cursor.getColumnIndex("success")) == 1);
			ti.setUserAgent(cursor.getString(cursor.getColumnIndex("useragent")));
			ti.setSourceUrl(cursor.getString(cursor.getColumnIndex("sourceurl")));
			ti.setType(cursor.getString(cursor.getColumnIndex("mime")));
			ti.setLength(cursor.getLong(cursor.getColumnIndex("length")));
			if (!ti.isSuccess())
			{
				ti.setDownloadinfo(getDownloadInfoWithId(ti.getId()));

			}
		}
		cursor.close();
		
		return ti;
	}

	@Override
	public List<DownloadInfo> getDownloadInfoWithId(int url)
	{
		ArrayList<DownloadInfo> ad=new ArrayList<>();
		
		Cursor c=sql.query("downloadinfo", null, "id=?", new String[]{url + ""}, null, null, null);
		while (c.moveToNext())
		{
			DownloadInfo di=new DownloadInfo();
			di.setTaskId(url);
			di.setNo(c.getInt(c.getColumnIndex("no")));
			di.setStart(c.getLong(c.getColumnIndex("start")));
			di.setCurrent(c.getLong(c.getColumnIndex("current")));
			di.setEnd(c.getLong(c.getColumnIndex("end")));
			ad.add(di);
		}
		c.close();
		
		return ad;
	}
	@Override
	public void insertDownloadInfo(TaskInfo ti)
	{
		insertDownloadInfo(ti.getDownloadinfo());
	}

	@Override
	public void insertDownloadInfo(List<DownloadInfo> ld)
	{
		for (DownloadInfo di:ld)
		{
			insertDownloadInfo(di);
		}
	}

	@Override
	public void insertDownloadInfo(DownloadInfo di)
	{
		ContentValues cv=new ContentValues();
		cv.put("id", di.getTaskId());
		cv.put("no", di.getNo());
		cv.put("start", di.getStart());
		cv.put("current", di.getCurrent());
		cv.put("end", di.getEnd());
		sql.insert("downloadinfo", null, cv);

	}



	@Override
	public void deleteDownloadInfoWithId(int id)
	{
		sql.delete("downloadinfo", "id=?", new String[]{id + ""});
	}

	@Override
	public void updateDownloadInfo(List<DownloadInfo> ld)
	{
		for (DownloadInfo di:ld)
		{
			updateDownloadInfo(di);
		}
	}

	@Override
	public void updateDownloadInfo(DownloadInfo di)
	{
		ContentValues cv=new ContentValues();
		cv.put("current", di.getCurrent());
		sql.update("downloadinfo", cv, "id=? and no=?", new String[]{di.getTaskId() + "",di.getNo() + ""});
	}



	@Override
	public void updateDownloadInfo(TaskInfo ti)
	{
		updateDownloadInfo(ti.getDownloadinfo());
	}


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
	public void createFolder(String name)
	{
		Cursor c=sql.query("bookmarksgroup", null, null, null, null, null, null);
		ContentValues cv=new ContentValues();
		cv.put("name", name);
		cv.put("no", c.getCount());
		c.close();
		sql.insert("bookmarksgroup", null, cv);
	}

	@Override
	public void deleteFolder(String name)
	{
		Cursor tmp=sql.query("bookmarksgroup", null, "name=?", new String[]{name}, null, null, null);
		if (tmp.moveToFirst())
		{
			int index=tmp.getInt(tmp.getColumnIndex("no"));tmp.close();
			sql.delete("bookmarksgroup", "name=?", new String[]{name});
			Cursor c=sql.query("bookmarksgroup", null, "no>?", new String[]{index + ""}, null, null, "no desc");
			while (c.moveToNext())
			{
				ContentValues cv=new ContentValues();
				cv.put("no", c.getInt(c.getColumnIndex("no")) - 1);
				sql.update("bookmarksgroup", cv, "name=?", new String[]{c.getString(c.getColumnIndex("name"))});

			}
			c.close();
			c = sql.query("bookmarks", null, "dir=?", new String[]{name}, null, null, null);
			while (c.moveToNext())
			{
				moveToDirectory(c.getString(c.getColumnIndex("url")),"默认");
				}
			c.close();
		}
		else tmp.close();
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
	public void insertBookmark(String url, String title)
	{
		insertBookmark(url, title, "默认");
	}

	@Override
	public void changeFolder(String str, String dir)
	{
		ContentValues cv=new ContentValues();
		cv.put("name", str);
		sql.update("bookmarksgroup", cv, "name=?", new String[]{dir});
	}




	@Override
	public void insertBookmark(String url, String title, String dir)
	{
		if (url == null || url.trim().length() == 0)throw new NullPointerException("url is must not null");
		Cursor c=sql.query("bookmarks", null, "dir=?", new String[]{dir}, null, null, null);
		ContentValues cv=new ContentValues();
		cv.put("url", url);
		cv.put("title", title);
		cv.put("no", c.getCount());c.close();
		cv.put("dir", dir);
		sql.insert("bookmarks", null, cv);
	}

	@Override
	public void moveToDirectory(String url, String dir)
	{
		Cursor c = sql.query("bookmarks", null, "dir=?", new String[]{"默认"}, null, null, null);
		int count=c.getCount();c.close();
		ContentValues cv=new ContentValues();
		cv.put("dir", dir);
		cv.put("no", count++);
		sql.update("bookmarks", cv, "url=?", new String[]{url});
		
		
	}

	@Override
	public void deleteBookmark(String url)
	{
		Cursor c=sql.query("bookmarks",null,"url=?",new String[]{url},null,null,null);
		if(c.moveToFirst()){
		int index=c.getInt(c.getColumnIndex("no"));
		String dir=c.getString(c.getColumnIndex("dir"));
		c.close();
		sql.delete("bookmarks","url=?",new String[]{url});
		c=sql.query("bookmarks",null,"no>? and dir=?",new String[]{index+"",dir},null,null,"no asc");
		while(c.moveToNext()){
			ContentValues cv=new ContentValues();
			cv.put("no",index--);
			sql.update("bookmarks",cv,"url=?",new String[]{c.getString(c.getColumnIndex("url"))});
		}
		c.close();
		}else c.close();
	}

	@Override
	public synchronized void moveToIndex(final String url, final int index)
	{
		new Thread(){
			public void run(){
				Cursor src=sql.query("bookmarks", null, "url=?", new String[]{url}, null, null, null);
				if (src.moveToFirst())
				{
					int seat=src.getInt(src.getColumnIndex("no"));
					String dir=src.getString(src.getColumnIndex("dir"));
					src.close();
					int tmp=0;
					int max=index;
					//seat最小
					if (seat > index)
					{
						tmp = seat;
						seat = index;
						max = tmp;
					}
					//从小到大更改序号
					Cursor c=sql.query("bookmarks", null, "dir=? and no>=? and no<=?", new String[]{dir,seat + "",max + ""}, null, null, "no asc");
					while (c.moveToNext())
					{
						ContentValues cv=new ContentValues();
						cv.put("no", max--);//从最大逐渐递减
						sql.update("bookmarks", cv, "url=?", new String[]{c.getString(c.getColumnIndex("url"))});
					}
					c.close();
				}
				else src.close();
			}}.start();

	}

	@Override
	public synchronized void moveGroupToIndex(final String dir, final int index)
	{
		new Thread(){
			public void run(){
				Cursor src=sql.query("bookmarksgroup", null, "name=?", new String[]{dir}, null, null, null);
				if (src.moveToFirst())
				{
					int seat=src.getInt(src.getColumnIndex("no"));
					src.close();
					int tmp=0;
					int max=index;
					//seat最小
					if (seat > index)
					{
						tmp = seat;
						seat = index;
						max = tmp;
					}
					//从小到大更改序号
					Cursor c=sql.query("bookmarksgroup", null, "no>=? and no<=?", new String[]{seat + "",max + ""}, null, null, "no asc");
					while (c.moveToNext())
					{
						ContentValues cv=new ContentValues();
						cv.put("no", max--);//从最大逐渐递减
						sql.update("bookmarksgroup", cv, "name=?", new String[]{c.getString(c.getColumnIndex("name"))});
					}
					c.close();
				}
				if (!src.isClosed())src.close();
			}}.start();

	}


	@Override
	public List getAllBookmarkGroup()
	{
		ArrayList<String> ao=new ArrayList<>();
		
		Cursor c=sql.query("bookmarksgroup", null, null, null, null, null, "no asc");
		while (c.moveToNext())
		{
			ao.add(c.getString(c.getColumnIndex("name")));
		}
		c.close();
		
		return ao;
	}

	@Override
	public List queryBookmark(String name)
	{
		if (name == null)throw new NullPointerException("dir is must not null");
		ArrayList<Object[]> ao=new ArrayList<>();
		
		Cursor c=sql.query("bookmarks", null, "dir=?", new String[]{name}, null, null, "no asc");
		while (c.moveToNext())
		{
			String[] o=new String[2];
			o[0] = c.getString(c.getColumnIndex("url"));
			o[1] = c.getString(c.getColumnIndex("title"));
			ao.add(o);
		}
		c.close();
		
		return ao;
	}


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
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
		p1.execSQL("CREATE TABLE searchhistory(data TEXT primary key,time INTEGER)");
		p1.execSQL("CREATE TABLE webhistory(url TEXT primary key,title TEXT,time INTEGER)");
		p1.execSQL("CREATE TABLE bookmarks(url TEXT primary key,title TEXT,no INTEGER,dir TEXT)");
		p1.execSQL("CREATE TABLE bookmarksgroup(name TEXT primary key,no INTEGER)");
		p1.execSQL("CREATE TABLE blacklist(url TEXT primary key,flag INTEGER)");
		p1.execSQL("CREATE TABLE homepage(url TEXT PRIMARY KEY,title TEXT,no INTEGER)");
		p1.execSQL("CREATE TABLE download(id INTEGER PRIMARY KEY,url TEXT UNIQUE,name TEXT UNIQUE,dir TEXT,cookie TEXT,multithread INTEGER,pause INTEGER,success INTEGER,useragent TEXT,mime TEXT,sourceurl TEXT,length INTEGER,time INTEGER,fail INTEGER)");
		p1.execSQL("CREATE TABLE downloadinfo(id INTEGER,no INTEGER,start INTEGER,current INTEGER,end INTEGER)");
		ContentValues cv=new ContentValues();
		cv.put("name", "默认");
		cv.put("no", 0);
		p1.insert("bookmarksgroup", null, cv);
		cv = new ContentValues();
		cv.put("url", "moe://addBookmark");
		cv.put("title", "添加");
		cv.put("no", Integer.MAX_VALUE);
		p1.insert("homepage", null, cv);
	}

	private DataBase(Context context)
	{
		super(context, "moedatabase", null, 3);
		sql = getReadableDatabase();
	}
	public static DataBase getInstance(Context context)
	{
		if (db == null)
			db = new DataBase(context.getApplicationContext());
		return db;
	}
}
