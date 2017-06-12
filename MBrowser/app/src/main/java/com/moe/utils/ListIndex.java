package com.moe.utils;
import java.util.List;
import java.util.ArrayList;
import android.os.Handler;
import android.os.Looper;

public class ListIndex
{
	private static ListIndex li;
	private ArrayList<Integer> ai=new ArrayList<>();
	private Handler handler;
	private ListIndex(){
		handler=new Handler();
	}
	public static ListIndex getInstance(){
		if(li==null)li=new ListIndex();
		return li;
	}
	public void query(final List<String> ls,final String key){
		new Thread(){
			public void run(){
				ai.clear();
				for(int i=0;i<ls.size();i++){
					if(ls.get(i).indexOf(key)!=-1)
						ai.add((Integer)i);
				}
				handler.post(new Runnable(){

						@Override
						public void run()
						{
							if(call!=null)call.finded(ai);
						}
					});
			}
		}.start();
	}
	
	public void setCallback(Callback call){
		this.call=call;
	}
	private Callback call;
	public abstract interface Callback{
		void finded(List<Integer> li);
	}
}
