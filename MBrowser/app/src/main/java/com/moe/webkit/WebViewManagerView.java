package com.moe.webkit;

import android.view.GestureDetector;
import java.util.ArrayList;
import android.webkit.DownloadListener;
import com.moe.view.PopupWindow;
import com.moe.dialog.AddDialog;
import android.content.Context;
import android.content.SharedPreferences;
import de.greenrobot.event.EventBus;
import android.webkit.CookieManager;
import com.moe.database.Download;
import android.view.MotionEvent;
import android.view.Gravity;
import com.moe.bean.DownloadItem;
import android.view.View;
import com.moe.utils.LinkedListMap;
import com.moe.fragment.NetworkLogFragment.Type;
import java.util.List;
import com.moe.fragment.NetworkLogFragment;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.text.TextUtils;
import android.webkit.WebView;

public class WebViewManagerView extends FrameLayout implements GestureDetector.OnGestureListener,DownloadListener,com.moe.webkit.WebView.OnStateListener,View.OnTouchListener
{
	  private OnStateListener osl;
    private PopupWindow pop;
	//private boolean state=false;
	private final String homepage="file:///android_asset/homepage.html";
//	private final String ajaxhook="file:///android_asset/ajaxhook.js";
	private GestureDetector gd=new GestureDetector(this);
	private SharedPreferences shared;
	private WebSettings ws;
	private AddDialog homePageAdd;
	private com.moe.webkit.WebView current;
   private History history;
	public WebViewManagerView(final Context context, AddDialog ad)
	{
		super(context);
		history=new History();
		shared = context.getSharedPreferences("webview", 0);
		homePageAdd = ad;
		pop = PopupWindow.getInstance(context);
		current=new com.moe.webkit.WebView(this);
		current.loadUrl(homepage);
		addWebView(current);
		
	}

	public com.moe.webkit.WebView getCurrent()
	{
		return current;
	}

	public void loadDataWithBaseURL(String url, String toString, String p2, String p3, String p4)
	{
		current.loadDataWithBaseURL(url,toString,p2,p3,p4);
	}

	public void findAllAsync(String toString)
	{
		current.findAllAsync(toString);
	}

	public int findAll(String toString)
	{
		return current.findAll(toString);
	}

	public void setFindListener(WebView.FindListener p0)
	{
		current.setFindListener(p0);
	}

	public void clearMatches()
	{
		current.clearMatches();
	}

	public void stopLoading()
	{
		current.stopLoading();
	}

	public void reload()
	{
		current.reload();
	}

	public void findNext(boolean p0)
	{
		current.findNext(p0);
	}

	public int getProgress()
	{
		return current.getProgress();
	}

	public boolean canGoBack()
	{
		return history.canBack();
	}

	public boolean canGoForward()
	{
		return history.canNext();
	}

	public void destroy()
	{
		history.destory();
	}

	public String getTitle()
	{
		return current.getTitle();
	}
	public void addWebView(WebView wv){
		history.add(wv);
		addView(wv);
	}
	

	public void loadUrl(String url)
	{
		stopLoading();
		if(shared.getInt(WebSettings.Setting.MULTIVIEW,0)==1&&!url.startsWith("javascript:"))
		{
			WebView webview=new com.moe.webkit.WebView(this);
			webview.loadUrl(url);
			addWebView(webview);
		}else
		current.loadUrl(url);
	}

	
	
	public void watchSource()
	{
		current.loadUrl("javascript:moe.source('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
	}
	public AddDialog getHomePageAdd(){
		return homePageAdd;
	}
	
	public void videoFind()
	{
		//视频嗅探
		EventBus.getDefault().post(new com.moe.bean.Message(6, current.getVideo()));

		//loadUrl("javascript:var url='';var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){url=url+video[i].src+';';var source=video[i].getElementsByTagName('source');for(var n=0;n<source.length;n++){url=url+source[n].src+';'}}video=document.getElementsByTagName('iframe');for(var i=0;i<video.length;i++){url=url+video[i].src+';';}video=document.getElementsByTagName('embed');for(var i=0;i<video.length;i++){url=url+video[i].src+';';}moe.result(url);");

	}
	public void blockUrl()
	{
		EventBus.getDefault().post(new com.moe.bean.Message(6, current.getBlock()));

	}
	
	/**视频嗅探原规则，已抛弃
	 @JavascriptInterface
	 public void result(String data){
	 EventBus.getDefault().post(new com.moe.bean.Message(6,data));
	 }*/

	

	
	@Override
	public void onDownloadStart(String url, String useragent, String name, String type, long length)
	{
		DownloadItem di=new DownloadItem();
		di.setUrl(url);
		di.setUserAgent(useragent);
		di.setContentDisposition(name);
		di.setMime(type);
		di.setLength(length);
		di.setCookie(getCookie(url));
		di.setSourceUrl(getUrl());
		EventBus.getDefault().post(di);
	}
	private String getCookie(String url)
	{
		CookieManager cm=CookieManager.getInstance();
		return cm.getCookie(url);
	}

	public void saveWebArchive()
	{
		current.saveWebArchive(Download.Setting.DIR_DEFAULT + "/" + getTitle() + ".mht");
	}

	

	public void goHome()
	{
		if (!homepage.equals(current.getUrl()))
			loadUrl(homepage);
	}

	
	public String getUrl()
	{
		if (current.getUrl() != null && current.getUrl().equals(homepage))
			return "";
		return current.getUrl();
	}

	public boolean getState()
	{

		return current.getProgress() < 100 ?true: false;
	}
    public void setOnStateListener(OnStateListener osl)
	{
        this.osl = osl;
    }
  
	/**private void transParent()
	 {
	 loadUrl("javascript:" +
	 "function loopChild(node){" +
	 "if(node.hasChildNodes()){" +
	 "for(var i=0;i<node.children.length;i++){" +
	 "loopChild(node.children[i]);" +
	 "}}" +
	 "if(node.nodeName!=\"script\")node.style.backgroundColor=\"transparent\";" +
	 "};" +
	 "loopChild(document.body);");

	 }*/
/*
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return gd.onTouchEvent(event) == true ?true: super.onTouchEvent(event);
    }
*/
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		return gd.onTouchEvent(p2);
	}

	
	@Override
	public boolean onDown(MotionEvent p1)
	{
		//if (getContentHeight() * getScale() > getHeight() - getResources().getDimension(R.dimen.actionBarSize))
		//	canScroll = startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
		//y = (int)p1.getRawY();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent p1)
	{

	}

	@Override
	public boolean onSingleTapUp(MotionEvent p1)
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		return false;
	}

	@Override
	public void onLongPress(MotionEvent p1)
	{
//		if(p1.getEventTime()-p1.getDownTime()>450){
//			if(Math.abs(y-p1.getY())<20)
//				onLongClick(p1);
//		}
		if (TextUtils.isEmpty(getUrl()))return;
		switch (current.getHitTestResult().getType())
		{

            case WebView.HitTestResult.EDIT_TEXT_TYPE:
				return;
			default:
				break;
        }
		//KeyEvent shiftPressEvent = new KeyEvent(0, 0,KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
		//shiftPressEvent.dispatch(this);
		pop.showAtLocation(this, Gravity.TOP | Gravity.LEFT, p1);
	}

	@Override
	public boolean onFling(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		return false;
	}
	@Override
	protected void onDetachedFromWindow()
	{
		shared.unregisterOnSharedPreferenceChangeListener(ws);
		
		super.onDetachedFromWindow();
	}

	public void goBack()
	{
		stopLoading();
		if(canGoBack()){
			removeAllViews();
		addView(history.back());
		osl.onLoad(this);
		}
	}

	public void goForward()
	{
		stopLoading();
	if(canGoForward()){
		removeAllViews();
		addView(history.next());
		osl.onLoad(this);
		}
	}

	@Override
	public void addView(View child)
	{
		current.onPause();
		((com.moe.webkit.WebView)child).setOnStateListener(this);
		current=(com.moe.webkit.WebView)child;
		current.onResume();
		child.setOnTouchListener(this);
		super.addView(child);
	}

	@Override
	public void onProgress(WebView wv, int p2)
	{
		if(osl!=null&&current==wv)osl.onProgress(p2);
	}

	@Override
	public void onReceiverTitle(WebView wv, String title)
	{
		if(osl!=null&&current==wv)osl.onReceiverTitle(title);
	}

	@Override
	public void onStart(WebView wv, String url)
	{
		if(osl!=null&&current==wv)osl.onStart(url);
	}

	@Override
	public void onEnd(WebView wv, String url, String title)
	{
		if(osl!=null&&current==wv)osl.onEnd(url,title);
	}



	

	
	
    public abstract interface OnStateListener
    {

        void onProgress(int p2);
        void onStart(String url);
        void onEnd(String url, String title);
        void onReceiverTitle(String title);
		void onLoad(WebViewManagerView wvmv);
    }
	
}
