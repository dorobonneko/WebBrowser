package com.moe.bean;

public class Message
{
	
	public int what;
	public Object obj;
	public int data;
public Message(){}
	public Message(int what, Object obj)
	{
		this.what = what;
		this.obj = obj;
	}
	public static Message obitMessage(int what){
		Message m=new Message();
		m.what=what;
		return m;
	}
	public static Message obitMessage(int what,int data){
		Message m=new Message();
		m.what=what;
		m.data=data;
		return m;
	}
}
