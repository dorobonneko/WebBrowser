package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import android.database.Cursor;
import java.util.ArrayList;
import android.database.sqlite.SQLiteStatement;

public class UrlBlockDatabase extends SQLiteOpenHelper
{
	private SQLiteDatabase sql;
	private  static UrlBlockDatabase ubd;
	private UrlBlockDatabase(Context context){
		super(context.getApplicationContext(),"urlblock",null,3);
		sql=getReadableDatabase();
	}

	public void clear()
	{
		SQLiteStatement state=sql.compileStatement("delete from urlblock");
		state.acquireReference();
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
	}
	static UrlBlockDatabase getInstance(Context context){
		if(ubd==null)ubd=new UrlBlockDatabase(context);
		return ubd;
	}
	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
		// TODO: Implement this method
	}

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("create table urlblock(url TEXT primary key)");
	}
	public List<String> query(){
		ArrayList<String> as=new ArrayList<>();
		Cursor c=sql.query("urlblock",null,null,null,null,null,null);
		while(c.moveToNext())
			as.add(c.getString(0));
			c.close();
		return as;
	}
	public boolean insert(String url){
		 boolean flag=true;
		SQLiteStatement state=sql.compileStatement("insert into urlblock values(?)");
		state.acquireReference();
		state.bindString(1,url);
		try{
		state.executeInsert();}
		catch(Exception e){flag=false;}
		state.close();
		state.releaseReference();
		return flag;
	}
	public boolean update(String old,String new_){
		boolean flag=true;
		SQLiteStatement state=sql.compileStatement("update urlblock set url=? where url=?");
		state.acquireReference();
		state.bindString(1,new_);
		state.bindString(2,old);
		try{state.executeUpdateDelete();}catch(Exception e){flag=false;}
		state.close();
		state.releaseReference();
		return flag;
	}
	public void delete(String url){
		SQLiteStatement state=sql.compileStatement("delete from urlblock where url=?");
		state.acquireReference();
		state.bindString(1,url);
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
	}
}
