package com.moe.utils;
import java.util.Random;
import android.graphics.Color;

public class RandomColor
{
	public static int getColor(){
		Random r=new Random();
		return Color.rgb(r.nextInt(256),r.nextInt(256),r.nextInt(256));
	}
}
