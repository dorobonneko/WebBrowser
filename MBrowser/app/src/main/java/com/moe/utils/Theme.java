package com.moe.utils;
import java.util.ArrayList;
import android.view.View;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParserException;
import android.graphics.drawable.Drawable;
import java.io.IOException;
import android.content.res.Resources;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import java.io.ByteArrayInputStream;
import com.moe.Mbrowser.R;
import android.graphics.Color;

public class Theme
{
    public static int color=0xffffffff;
    private static ArrayList<View> av=new ArrayList<>();
	private static ArrayList<View> background=new ArrayList<>();
	public static int backgroundColor=-7829368;

	public static void registerBackground(View view)
	{
		view.setBackgroundColor(backgroundColor);
		background.add(view);
	}

	public static void clear()
	{
		av.clear();
	}
    public static void registerForeGround(View v)
	{
		v.setBackgroundColor(color);
		av.add(v);
	}
	public static void unRegisterBackground(View v){
		v.setBackground(null);
		background.remove(v);
	}
	public static void updateTheme(int color)
	{
		Theme.color = color;
		int red=Color.red(color);
		int green=Color.green(color);
		int blue=Color.blue(color);
		Theme.backgroundColor=Color.rgb(red-30<0?0:red-30,green-30<0?0:green-30,blue-30<0?0:blue-30);
		Iterator<View> i=av.iterator();
		while (i.hasNext())
		{
			View v=i.next();
			if (v == null)continue;
			v.setBackgroundColor(color);
		}
		i=background.iterator();
		while (i.hasNext())
		{
			View v=i.next();
			if (v == null)continue;
			v.setBackgroundColor(backgroundColor);
		}
	}
	/**
	 public static Drawable getDrawable(Resources res,String name,int color){
	 Drawable d=null;
	 XmlPullParser xpp=null;
	 try
	 {
	 xpp=XmlPullParserFactory.newInstance().newPullParser();
	 String xml=StringUtils.newString( res.getAssets().open(name));
	 ByteArrayInputStream bais=new ByteArrayInputStream(xml.replace("$color",color+"").getBytes());
	 xpp.setInput(bais,"utf-8");
	 d=Drawable.createFromXml(res, xpp);
	 }
	 catch (XmlPullParserException e)
	 {}
	 catch (IOException e)
	 {}
	 return d;
	 }*/
}
