package com.moe.webkit;

import android.webkit.WebView;
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
public class WebView extends WebView implements GestureDetector.OnGestureListener,DownloadListener
{
	private ArrayList<String> video=new ArrayList<>(),block=new ArrayList<>();;
    private OnStateListener osl;
    private PopupWindow pop;
	//private boolean state=false;
	private final String homepage="file:///android_asset/homepage.html";
//	private final String ajaxhook="file:///android_asset/ajaxhook.js";
	private GestureDetector gd=new GestureDetector(this);
	private SharedPreferences shared;
	private WebSettings ws;
	private AddDialog homePageAdd;
    public WebView(final Context context, AddDialog ad)
	{
        super(context);
		shared = context.getSharedPreferences("webview", 0);
		homePageAdd = ad;
		ws=new WebSettings(this);
		setWebViewClient(new WebViewClient(this,video,block));
        setWebChromeClient(new WebChromeClient(this));
		setScrollBarSize(1);
		setScrollbarFadingEnabled(true);
		setScrollBarFadeDuration(100);
		setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		//setOnTouchListener(this);
		pop = PopupWindow.getInstance(getContext());
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
		addJavascriptInterface(new JavascriptInterface(this),"moe");
		loadUrl(homepage);
		setDownloadListener(this);
		//setOnTouchListener(this);
		
	}

	

	public SharedPreferences getSharedPreferences(){
		return shared;
	}
	public void watchSource()
	{
		loadUrl("javascript:moe.source('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
	}
	public AddDialog getHomePageAdd(){
		return homePageAdd;
	}
	public OnStateListener getListener(){
		return osl;
	}
	
	public void videoFind()
	{
		//视频嗅探
		EventBus.getDefault().post(new com.moe.bean.Message(6, video));

		//loadUrl("javascript:var url='';var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){url=url+video[i].src+';';var source=video[i].getElementsByTagName('source');for(var n=0;n<source.length;n++){url=url+source[n].src+';'}}video=document.getElementsByTagName('iframe');for(var i=0;i<video.length;i++){url=url+video[i].src+';';}video=document.getElementsByTagName('embed');for(var i=0;i<video.length;i++){url=url+video[i].src+';';}moe.result(url);");

	}
	public void blockUrl()
	{
		EventBus.getDefault().post(new com.moe.bean.Message(6, block));

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
		super.saveWebArchive(Download.Setting.DIR_DEFAULT + "/" + getTitle() + ".mht");
	}

	public void goHome()
	{
		if (!homepage.equals(super.getUrl()))
			loadUrl(homepage);
	}

	@Override
	public String getUrl()
	{
		if (super.getUrl() != null && super.getUrl().equals(homepage))
			return "";
		return super.getUrl();
	}

	public boolean getState()
	{

		return getProgress() < 100 ?true: false;
	}
    public void setOnStateListener(OnStateListener osl)
	{
        this.osl = osl;
    }
    public OnStateListener getOnStateListener(){
		return osl;
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
    


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return gd.onTouchEvent(event) == true ?true: super.onTouchEvent(event);
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
		if (getUrl().isEmpty())return;
		switch (getHitTestResult().getType())
		{

            case HitTestResult.EDIT_TEXT_TYPE:
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
    public abstract interface OnStateListener
    {

        public void onProgress(int p2);

        void onStart(String url);
        void onEnd(String url, String title);
        void onReceiverTitle(String title);
    }
	
}
