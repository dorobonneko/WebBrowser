package com.moe.webkit;
import java.util.Stack;
import android.webkit.WebView;
public class History
{
	private Stack<WebView> history=new Stack<>(),next=new Stack<>();
	private WebView current;

	
	public void destory()
	{
		while(!next.isEmpty())
			next.pop().destroy();
		while(!history.isEmpty())
			history.pop().destroy();
		if(current!=null)current.destroy();
	}
	public void add(WebView add){
		while(!next.isEmpty())
			next.pop().destroy();
		if(current!=null&&current!=add)
		history.push(current);
		current=add;
	}
	public WebView back(){
		if(current.canGoBack())
			current.goBack();
		else{
		next.push(current);
		current=history.pop();
		}
		return current;
	}
	public WebView next(){
		if(current.canGoForward())
			current.goForward();
		else{
		history.push(current);
		current=next.pop();
		}
		return current;
	}
	public boolean canBack(){
		return (current!=null&&current.canGoBack())||!history.isEmpty();
	}
	public boolean canNext(){
		return (current!=null&&current.canGoForward())||!next.isEmpty();
	}
}
