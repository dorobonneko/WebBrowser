package com.moe.bean;

public class WindowEvent
{
	
    public final static int WHAT_NEW_WINDOW=0;
    public final static int WHAT_JS_NEW_WINDOW=1;
    public final static int WHAT_URL_NEW_WINDOW=2;
	public final static int WHAT_TOGGLE_WINDOW=3;
	public final static int WHAT_URL_WINDOW=4;
	public final static int WHAT_URL_NEW_WINDOW_BACKGROUND=5;
	public final static int WHAT_JS_CLOSE_WINDOW=6;
	
    public boolean isOpen=false;
    public Object obj;
    public int what;
    public WindowEvent(){}
    public WindowEvent(int what,boolean isOpen){
        this.what=what;
        this.isOpen=isOpen;
    }
    public WindowEvent(int what,Object obj)
    {
        this.obj = obj;
        this.what = what;
    }

    public WindowEvent(int what)
    {
        this.what = what;
    }

    public WindowEvent(int what,Object obj,boolean isOpen)
    {
        this.isOpen = isOpen;
        this.obj = obj;
        this.what = what;
    }
}
