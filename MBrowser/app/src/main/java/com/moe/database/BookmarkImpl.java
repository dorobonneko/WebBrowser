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
		root.setParent(-1);
		root.setSon(0);
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
		p1.execSQL("create table bookmarks(son INTEGER primary key AUTOINCREMENT,parent INTEGER,title TEXT,summary TEXT,type INTEGER,no INTEGER)");
		
	}

	@Override
	public void trimNo(Bookmark b)
	{
		//改变顺序专用
		SQLiteStatement state=sql.compileStatement("update bookmarks set no=? where son=?");
		state.acquireReference();
		state.bindLong(1,b.getNo());
		state.bindLong(2,b.getSon());
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
	}

	@Override
	public List<Bookmark> loop(Bookmark bookmark)
	{
		ArrayList<Bookmark> ab=new ArrayList<>();
		ab.add(bookmark);
		Cursor c=sql.rawQuery("select * from bookmarks where parent=? and type=0 order by no desc",new String[]{bookmark.getSon()+""});
		while(c.moveToNext()){
			Bookmark b=new Bookmark();
			b.setSon(c.getInt(0));
			b.setParent(c.getInt(1));
			b.setTitle(c.getString(2));
			b.setSummary(c.getString(3));
			b.setType(c.getInt(4));
			b.setNo(c.getInt(5));
			b.setLevel(bookmark.getLevel()+1);
			ab.addAll(loop(b));
		}
		c.close();
		return ab;
	}

	@Override
	public void update(Bookmark b)
	{
		Bookmark bookmark=queryWithSon(b.getSon());
		if(b.getParent()==bookmark.getParent()){
			//(未改文件夹)
			SQLiteStatement state=sql.compileStatement("update bookmarks set title=?,summary=? where son=?");
			state.acquireReference();
			state.bindString(1,b.getTitle());
			state.bindString(2,b.getSummary());
			state.bindLong(3,b.getSon());
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
		Cursor c=sql.rawQuery("select count(son) from bookmarks where parent=?",new String[]{b.getParent()+""});
		SQLiteStatement state=sql.compileStatement("insert into bookmarks(parent,title,summary,type,no) values(?,?,?,?,?)");
		state.bindLong(1,b.getParent());
		state.bindString(2,b.getTitle());
		state.bindString(3,b.getSummary());
		state.bindLong(4,b.getType());
		if(c.moveToFirst())
		state.bindLong(5,c.getInt(0));
		c.close();
		state.executeInsert();
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
		SQLiteStatement state=sql.compileStatement("delete from bookmarks where son=?");
		state.acquireReference();
		state.bindLong(1,b.getSon());
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
		Cursor c=sql.rawQuery("select son,no from bookmarks where parent=? and no>?",new String[]{b.getParent()+"",b.getNo()+""});
		while(c.moveToNext()){
			state=sql.compileStatement("update bookmarks set no=? where son=?");
			state.acquireReference();
			state.bindLong(1,c.getInt(1)-1);
			state.bindLong(2,c.getInt(0));
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
		Cursor c=sql.rawQuery("select * from bookmarks where parent=? order by no desc",new String[]{bookmark.getSon()+""});
		while(c.moveToNext()){
			Bookmark b=new Bookmark();
			b.setSon(c.getInt(0));
			b.setParent(c.getInt(1));
			b.setTitle(c.getString(2));
			b.setSummary(c.getString(3));
			b.setType(c.getInt(4));
			b.setNo(c.getInt(5));
			b.setLevel(bookmark.getLevel()+1);
			ab.add(b);
		}
		c.close();
		return ab;
	}
	@Override
	public Bookmark queryWithSon(int son){
		Bookmark b=null;
		Cursor c=sql.rawQuery("select * from bookmarks where son=?",new String[]{son+""});
		if(c.moveToFirst()){
			b=new Bookmark();
			b.setSon(c.getInt(0));
			b.setParent(c.getInt(1));
			b.setTitle(c.getString(2));
			b.setSummary(c.getString(3));
			b.setType(c.getInt(4));
			b.setNo(c.getInt(5));
		}
		c.close();
		return b;
	}
	

	
}
