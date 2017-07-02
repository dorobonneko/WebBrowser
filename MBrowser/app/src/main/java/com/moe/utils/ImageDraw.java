package com.moe.utils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.util.*;

public class ImageDraw
{
	private static LruCache<Character,Bitmap> lc=new LruCache<Character,Bitmap>((int)Runtime.getRuntime().totalMemory() / 8){

		@Override
		protected int sizeOf(Character key, Bitmap value)
		{
			return value.getByteCount();
		}

		@Override
		protected void entryRemoved(boolean evicted, Character key, Bitmap oldValue, Bitmap newValue)
		{
			super.entryRemoved(evicted, key, oldValue, newValue);
			oldValue.recycle();
		}
	};
	private static LruCache<Character,Bitmap> lco=new LruCache<Character,Bitmap>((int)Runtime.getRuntime().totalMemory() / 8){

		@Override
		protected int sizeOf(Character key, Bitmap value)
		{
			return value.getByteCount();
		}

		@Override
		protected void entryRemoved(boolean evicted, Character key, Bitmap oldValue, Bitmap newValue)
		{
			super.entryRemoved(evicted, key, oldValue, newValue);
			oldValue.recycle();
		}


	};
	public static Bitmap TextImage(Character text,boolean outline){
		Bitmap b=null;
		if(outline)b=lco.get(text);
		else
			lc.get(text);
		if(b!=null)return b;
		int size=outline?120:96;
		b=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
		Canvas c=new Canvas(b);
		Paint p=new Paint();
		p.setAntiAlias(true);
		p.setDither(true);
		p.setColor(Color.WHITE);
		c.drawCircle(b.getWidth()/2,b.getHeight()/2,b.getWidth()/3,p);
		p.setTextAlign(Paint.Align.CENTER);
		//Rect r=new Rect();
		//p.getTextBounds(text.toString(),0,1,r);
		p.setColor(RandomColor.getColor());
		p.setTextSize(b.getWidth()/2);
		c.drawText(text.toString(),b.getWidth()/2 ,b.getHeight()/2-(p.descent()+p.ascent())/2,p);
		if(outline){
			p.setStrokeWidth(2f);
			p.setStyle(Paint.Style.STROKE);
			//p.setStrokeCap(Paint.Cap.ROUND);
			RectF rectf=new RectF();
			rectf.set(5,5,b.getWidth()-5,b.getHeight()-5);
			c.drawArc(rectf,0,360,false,p);
			lco.put(text,b);
			}
			else
		lc.put(text,b);
		return b;
	}
	public static Bitmap squareImage(int size){
		Bitmap b=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);
		Canvas c=new Canvas(b);
		Paint p=new Paint();
		p.setColor(Color.GRAY);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(3f);
		c.drawRect(b.getWidth()/2-25,b.getHeight()/2-25,b.getWidth()/2+25,b.getHeight()/2+25,p);
		p.setTextAlign(Paint.Align.CENTER);
		p.setStrokeWidth(2f);
		p.setTextSize(24f);
		c.drawText(size+"",b.getWidth()/2,b.getHeight()/2-(p.descent()+p.ascent())/2,p);
		return b;
	}
}
