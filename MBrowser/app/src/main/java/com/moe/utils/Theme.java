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

public class Theme
{
    public static int color=0xffffffff;
    private static ArrayList<View> av=new ArrayList<>();

	public static void clear()
	{
		av.clear();
	}
    public static void registerTheme(View v)
{
    av.add(v);
}
public static void unRegisterTheme(View v){
    av.remove(v);
}
public static void updateTheme(int color){
    Theme.color=color;
    Iterator<View> i=av.iterator();
    while(i.hasNext()){
        View v=i.next();
        if(v==null)continue;
        switch(v.getId()){
            
            default:
            v.setBackgroundColor(color);
            break;
        }
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
