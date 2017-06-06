package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.moe.entity.TaskInfo;
import java.io.File;
import android.database.Cursor;
import android.content.ContentValues;
import de.greenrobot.event.EventBus;
import com.moe.bean.TaskBean;
import java.util.ArrayList;
import java.util.List;
import com.moe.entity.DownloadInfo;
import android.database.sqlite.SQLiteStatement;
import com.moe.utils.DataUtils;

class DownloadImpl extends SQLiteOpenHelper implements Download
{
	private static DownloadImpl di;
	private SQLiteDatabase sql;
	static Download getInstance(Context context){
		if(di==null)di=new DownloadImpl(context);
		return di;
	}
	public DownloadImpl(Context context){
		super(context.getApplicationContext(),"download",null,3);
		sql=getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("CREATE TABLE download(id INTEGER PRIMARY KEY,url TEXT UNIQUE,name TEXT UNIQUE,dir TEXT,cookie TEXT,multithread INTEGER,pause INTEGER,success INTEGER,useragent TEXT,mime TEXT,sourceurl TEXT,length INTEGER,time INTEGER,fail INTEGER,forbidden INTEGER)");
		p1.execSQL("CREATE TABLE downloadinfo(id INTEGER,no INTEGER,start INTEGER,current INTEGER,end INTEGER,success INTEGER,url TEXT)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
	}

	@Override
	public void renameTask(int id, String name)
	{
		SQLiteStatement state=sql.compileStatement("update download set name=? where id=?");
		state.bindString(1,name);
		state.bindLong(2,id);
		state.executeUpdateDelete();
	}


	@Override
	public void clearAllTask(final TaskInfo[] id, final boolean file)
	{
		//new Thread(){
		//	public void run(){

		for(Object tii:id){
			final TaskInfo ti=(TaskInfo)tii;
			deleteTaskInfoWithId(ti.getId());
			if(file)
			{new Thread(){
				public void run(){
					DataUtils.deleteDir(new File(ti.getDir(), ti.getTaskname()));
				}
			}.start();}
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
		cv.put("forbidden",ti.isForbidden());
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
				try{sql.insertOrThrow("download", null, cv);
				insertDownloadInfo(ti);
					EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.ADD));
					if(call!=null)call.callback(ti,Download.State.SUCCESS);
				}catch(Exception e){
					if(call!=null)call.callback(ti,Download.State.FAIL);
				}
			}
		}.start();
	}

	@Override
	public void deleteTaskInfoWithId(int url)
	{
		TaskInfo ti=new TaskInfo();
		ti.setId(url);
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
			ti.setForbidden(cursor.getInt(cursor.getColumnIndex("forbidden"))==1);
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
			ti.setForbidden(cursor.getInt(cursor.getColumnIndex("forbidden"))==1);
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
			di.setUrl(c.getString(c.getColumnIndex("url")));
			di.setSuccess(c.getInt(c.getColumnIndex("success"))==1);
			ad.add(di);
		}
		c.close();

		return ad;
	}
	@Override
	public void insertDownloadInfo(TaskInfo ti)
	{
		if(ti.getDownloadinfo()!=null)
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
		cv.put("url",di.getUrl());
		cv.put("success",di.isSuccess());
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
	public void updateDownloadInfoWithData(DownloadInfo di)
	{
		ContentValues cv=new ContentValues();
		cv.put("start", di.getStart());
		cv.put("current", di.getCurrent());
		cv.put("end", di.getEnd());
		cv.put("url",di.getUrl());
		cv.put("success",di.isSuccess());
		sql.update("downloadinfo", cv, "id=? and no=?", new String[]{di.getTaskId() + "",di.getNo() + ""});
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
	
}
