package com.moe.utils;
import android.app.Dialog;
import java.lang.reflect.Field;

public class DialogUtils
{
	public static void changeState(Dialog d,boolean flag){
		try {
			Field field = d.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(d, flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
