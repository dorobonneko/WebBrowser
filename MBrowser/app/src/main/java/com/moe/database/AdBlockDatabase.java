package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.LinkedHashMap;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

public class AdBlockDatabase extends SQLiteOpenHelper
{
	private static AdBlockDatabase abd;
	private SQLiteDatabase sql;

	public String getData(String host)
	{
		String data="";
		Cursor c=sql.query("adblock",new String[]{"selector"},"host=?",new String[]{host},null,null,null);
		if(c.moveToFirst()){
			data=c.getString(0);
		}
		c.close();
		return data;
	}

	public void changeHost(String src, String trim)
	{
		SQLiteStatement s=sql.compileStatement("update adblock set host=? where host = ?");
		s.bindAllArgsAsStrings(new String[]{trim,src});
		s.executeUpdateDelete();
	}
	static AdBlockDatabase getInstance(Context context){
		if(abd==null)abd=new AdBlockDatabase(context);
		return abd;
	}
	private AdBlockDatabase(Context context){
		super(context.getApplicationContext(),"adblock",null,3);
		sql=getReadableDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
	}

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("create table adblock(host TEXT PRIMARY KEY,selector TEXT)");
		
	}
	public void add(String host,String selector){
		Cursor c=sql.query("adblock",null,"host=?",new String[]{host},null,null,null);
		if(c.moveToFirst()){
			if(c.getString(1).indexOf(selector+",")==-1){
				SQLiteStatement s=sql.compileStatement("update adblock set selector=? where host = ?");
				s.bindAllArgsAsStrings(new String[]{c.getString(1)+selector+",",host});
				s.executeUpdateDelete();
			}
		}else{
		ContentValues cv=new ContentValues();
		cv.put("host",host);
		cv.put("selector",selector==null?null:selector+",");
		sql.insert("adblock",null,cv);
		}
		c.close();
	}
	public void update(String host,String... value){
		StringBuffer sb=new StringBuffer();
		for(String v:value){
			if(TextUtils.isEmpty(v))
				continue;
				sb.append(v).append(",");
		}
		update(host,sb.toString());
	}
	public void update(String host,String selector){
		ContentValues cv=new ContentValues();
		cv.put("selector",selector);
		sql.update("adblock",cv,"host=?",new String[]{host});
	}
	public void delete(String host){
		sql.delete("adblock","host=?",new String[]{host});
	}
	public Map<String,String> getAllData(){
		Map<String,String> map=new LinkedHashMap<>();
		Cursor c=sql.query("adblock",null,null,null,null,null,null);
		while(c.moveToNext()){
			map.put(c.getString(0),c.getString(1));
			
		}
		c.close();
		return map;
	}
}
