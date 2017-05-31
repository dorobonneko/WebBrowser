package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

class BookmarkImpl extends SQLiteOpenHelper implements BookMarks
{
	private static BookmarkImpl bi;
	private SQLiteDatabase sql;
	static BookmarkImpl getInstance(Context context){
		if(bi==null)bi=new BookmarkImpl(context);
		return bi;
	}
	private BookmarkImpl(Context context){
		super(context.getApplicationContext(),"bookmarks",null,3);
		sql=getReadableDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
				
	}

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("CREATE TABLE bookmarks(url TEXT primary key,title TEXT,no INTEGER,dir TEXT)");
		p1.execSQL("CREATE TABLE bookmarksgroup(name TEXT primary key,no INTEGER)");
		ContentValues cv=new ContentValues();
		cv.put("name", "默认");
		cv.put("no", 0);
		p1.insert("bookmarksgroup", null, cv);
		
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
	
	
}
