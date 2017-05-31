package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import java.util.List;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatatypeMismatchException;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;

class JavaScriptImlp extends SQLiteOpenHelper implements JavaScript
{

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("create table javascript(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,content TEXT,autorun INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
		// TODO: Implement this method
	}

	@Override
	public List<String> getAllScript(String host)
	{
		ArrayList<String> as=new ArrayList<>();
		Cursor c=sql.query("javascript",new String[]{"content"},"name in (?,'all') and autorun=1",new String[]{host},null,null,null);
		while(c.moveToNext()){
			as.add(c.getString(0));
		}
		c.close();
		return as;
	}


	@Override
	public void updateScriptState(int id, boolean state)
	{
		ContentValues cv=new ContentValues();
		cv.put("autorun",state);
		sql.update("javascript",cv,"id=?",new String[]{id+""});
		
	}


	@Override
	public void addScript(String name, String content)
	{
		ContentValues cv=new ContentValues();
		cv.put("name",name);
		cv.put("content",content);
		sql.insert("javascript",null,cv);
	}

	@Override
	public void updateScript(int id, String name, String content)
	{
		ContentValues cv=new ContentValues();
		cv.put("name",name);
		cv.put("content",content);
		sql.update("javascript",cv,"id=?",new String[]{id+""});
	}

	@Override
	public void deleteScript(int id)
	{
		sql.delete("javascript","id=?",new String[]{id+""});
	}

	@Override
	public List<Object[]> getAllScript()
	{
		ArrayList<Object[]> al=new ArrayList<>();
		Cursor c=sql.query("javascript",null,null,null,null,null,null);
		while(c.moveToNext())
		{
			Object[] o=new Object[4];
			o[0]=c.getInt(c.getColumnIndex("id"));
			o[1]=c.getString(c.getColumnIndex("name"));
			o[2]=c.getString(c.getColumnIndex("content"));
			o[3]=c.getInt(c.getColumnIndex("autorun"))==1;
			al.add(o);
		}
		c.close();
		return al;
	}
	
	private JavaScriptImlp(Context content){
		super(content.getApplicationContext(),"javascript",null,3);
		sql=getReadableDatabase();
	}
	private SQLiteDatabase sql;
	private static JavaScriptImlp jsi;
	static JavaScript getInstance(Context content){
		if(jsi==null)jsi=new JavaScriptImlp(content);
		return jsi;
	}
}
