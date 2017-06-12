package com.moe.utils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

public class ImageDraw
{
	public static Bitmap TextImage(Character text,boolean outline){
		Bitmap b=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);
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
			}
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
