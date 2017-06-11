package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import com.moe.entity.Bookmark;
import android.database.sqlite.SQLiteStatement;
import java.io.File;

class BookmarkImpl extends SQLiteOpenHelper implements BookMarks
{

	@Override
	public Bookmark getRoot()
	{
		return root;
	}


	private Bookmark root;
	private static BookmarkImpl bi;
	private SQLiteDatabase sql;
	static BookmarkImpl getInstance(Context context){
		if(bi==null)bi=new BookmarkImpl(context);
		return bi;
	}
	private BookmarkImpl(Context context){
		super(context.getApplicationContext(),"bookmarks",null,3);
		sql=getReadableDatabase();
		root=new Bookmark();
		root.setLevel(0);
		root.setTitle("根目录");
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
				
	}

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("create table bookmarks(parent TEXT,title TEXT,summary TEXT,type INTEGER,no INTEGER,path TEXT primary key)");
		
	}

	@Override
	public void importData(File file)
	{
		SQLiteDatabase sqlite=sql.openDatabase(file.getAbsolutePath(),null,3);
		Cursor c=sqlite.rawQuery("select * from bookmarks",null);
		while(c.moveToNext()){
			Bookmark b=new Bookmark();
			b.setParent(c.getString(0));
			b.setTitle(c.getString(1));
			b.setSummary(c.getString(2));
			b.setType(c.getInt(3));
			insert(b);
		}
		c.close();
		sqlite.close();
	}


	@Override
	public void trimNo(Bookmark b)
	{
		//改变顺序专用
		SQLiteStatement state=sql.compileStatement("update bookmarks set no=? where path=?");
		state.acquireReference();
		state.bindLong(1,b.getNo());
		state.bindString(2,b.getPath());
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
	}

	@Override
	public List<Bookmark> loop(Bookmark bookmark)
	{
		ArrayList<Bookmark> ab=new ArrayList<>();
		ab.add(bookmark);
		Cursor c=sql.rawQuery("select * from bookmarks where parent=? and type=0 order by no desc",new String[]{bookmark.getPath()});
		while(c.moveToNext()){
			Bookmark b=new Bookmark();
			b.setParent(c.getString(0));
			b.setTitle(c.getString(1));
			b.setSummary(c.getString(2));
			b.setType(c.getInt(3));
			b.setNo(c.getInt(4));
			b.setLevel(bookmark.getLevel()+1);
			ab.addAll(loop(b));
		}
		c.close();
		return ab;
	}

	@Override
	public void update(Bookmark old,Bookmark b)
	{
		Bookmark bookmark=queryWithPath(old.getPath());
		if(b.getParent()==bookmark.getParent()){
			//(未改文件夹)
			SQLiteStatement state=sql.compileStatement("update bookmarks set title=?,summary=? where path=?");
			state.acquireReference();
			state.bindString(1,b.getTitle());
			state.bindString(2,b.getSummary());
			state.bindString(3,old.getPath());
			state.executeUpdateDelete();
			state.close();
			state.releaseReference();
			}else{
			//更改了文件夹
			delete(bookmark);//从原文件夹删除
			insert(b);//插入新数据
		}
		
	}

	@Override
	public void insert(Bookmark b)
	{
		Cursor c=sql.rawQuery("select count(path) from bookmarks where parent=?",new String[]{b.getParent()+""});
		SQLiteStatement state=sql.compileStatement("insert into bookmarks(parent,title,summary,type,no,path) values(?,?,?,?,?,?)");
		state.acquireReference();
		state.bindString(1,b.getParent());
		state.bindString(2,b.getTitle());
		state.bindString(3,b.getSummary());
		state.bindLong(4,b.getType());
		if(c.moveToFirst())
		state.bindLong(5,c.getInt(0));
		c.close();
		state.bindString(6,b.getPath());
		try{state.executeInsert();}catch(Exception e){}
		state.close();
		state.releaseReference();
	}

	@Override
	public void delete(Bookmark b)
	{
		if(b.getType()==1){
			deleteThrow(b);
		}
		else{
			for(Bookmark bb:query(b))
			delete(bb);
			deleteThrow(b);
		}
	}
	private void deleteThrow(Bookmark b){
		sql.beginTransaction();
		SQLiteStatement state=sql.compileStatement("delete from bookmarks where path=?");
		state.acquireReference();
		state.bindString(1,b.getPath());
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
		Cursor c=sql.rawQuery("select no,path from bookmarks where parent=? and no>?",new String[]{b.getParent()+"",b.getNo()+""});
		while(c.moveToNext()){
			state=sql.compileStatement("update bookmarks set no=? where path=?");
			state.acquireReference();
			state.bindLong(1,c.getInt(0)-1);
			state.bindString(2,c.getString(1));
			state.executeUpdateDelete();
			state.close();
			state.releaseReference();
		}
		c.close();
		sql.setTransactionSuccessful();
		sql.endTransaction();
	}
	@Override
	public List<Bookmark> query(Bookmark bookmark)
	{
		ArrayList<Bookmark> ab=new ArrayList<Bookmark>();
		Cursor c=sql.rawQuery("select * from bookmarks where parent=? order by no desc",new String[]{bookmark.getPath()});
		while(c.moveToNext()){
			Bookmark b=new Bookmark();
			b.setParent(c.getString(0));
			b.setTitle(c.getString(1));
			b.setSummary(c.getString(2));
			b.setType(c.getInt(3));
			b.setNo(c.getInt(4));
			b.setLevel(bookmark.getLevel()+1);
			ab.add(b);
		}
		c.close();
		return ab;
	}
	@Override
	public Bookmark queryWithPath(String path){
		Bookmark b=null;
		Cursor c=sql.rawQuery("select * from bookmarks where path=?",new String[]{path});
		if(c.moveToFirst()){
			b=new Bookmark();
			b.setParent(c.getString(0));
			b.setTitle(c.getString(1));
			b.setSummary(c.getString(2));
			b.setType(c.getInt(3));
			b.setNo(c.getInt(4));
		}
		c.close();
		return b;
	}
	

	
}
