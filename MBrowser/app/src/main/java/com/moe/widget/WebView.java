package com.moe.widget;

import android.content.Context;
import android.view.View;
import android.view.MotionEvent;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.gesture.Gesture;
import android.view.GestureDetector;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.view.KeyEvent;
import android.view.ContextMenu;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.JsResult;
import android.os.Message;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.GeolocationPermissions;
import de.greenrobot.event.EventBus;
import com.moe.bean.WindowEvent;
import android.view.Menu;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.view.Gravity;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.webkit.WebResourceError;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.ValueCallback;
import com.moe.dialog.OutProgramWindow;
import com.moe.database.WebHistory;
import com.moe.database.DataBase;
import com.moe.database.BlackList;
import com.moe.dialog.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.app.Activity;
import android.webkit.JavascriptInterface;
import com.moe.utils.ImageDraw;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import java.io.IOException;
import com.moe.database.HomePage;
import com.moe.dialog.AddDialog;
import com.moe.view.PopupWindow;
import android.content.SharedPreferences;
import android.webkit.DownloadListener;
import com.moe.bean.DownloadItem;
import android.webkit.CookieManager;


public class WebView extends WebView implements NestedScrollingChild,GestureDetector.OnGestureListener,SharedPreferences.OnSharedPreferenceChangeListener,DownloadListener,AlertDialog.OnClickListener
{


    private boolean canScroll=false;
    private NestedScrollingChildHelper childHelper = new NestedScrollingChildHelper(this);
    //private GestureDetector gesture=new GestureDetector(this);
    private OnStateListener osl;
    private PopupWindow pop;
	private boolean state=false;
	private final String homepage="file:///android_asset/homepage.html";
	private WebHistory wh;
	//private GestureDetector gd=new GestureDetector(this);
	private BlackList bl;
	private AlertDialog gps;
	private HomePage hp;
	private AddDialog ad;
	private SharedPreferences shared;
	private AlertDialog dd;
	private String homepageurl;
    public WebView(Context context,AddDialog ad)
	{
        super(context);
		shared=context.getSharedPreferences("webview",0);
		shared.registerOnSharedPreferenceChangeListener(this);
		this.ad=ad;
		gps = new AlertDialog(context);
		gps.setTitle("网页请求定位权限");
		dd=new AlertDialog(context);
		dd.setTitle("确认删除导航？");
		dd.setOnClickListener(this);
		bl = DataBase.getInstance(context);
		wh = DataBase.getInstance(context);
		hp=DataBase.getInstance(context);
        //setBackgroundColor(0xffff0000);
        setWebViewClient(wvc);
        setWebChromeClient(wcc);
		setScrollBarSize(1);
		setScrollbarFadingEnabled(true);
		setScrollBarFadeDuration(100);
		setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		initWebViewSettings();
		//setOnTouchListener(this);
		setNestedScrollingEnabled(true);
		pop=PopupWindow.getInstance(getContext());
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		addJavascriptInterface(this,"moe");
		loadUrl(homepage);
		setDownloadListener(this);
    }

	@Override
	public void onClick(View v)
	{
		if(v.getId()==0){
			hp.deleteItem(homepageurl);
			reload();
		}
	}

	
	@JavascriptInterface
	public void delete(String url){
		this.homepageurl=url;
		post(new Runnable(){public void run(){dd.show();}});
		//dd.show();
			}
	@JavascriptInterface
	public String getHomePageData(){
		return hp.getJsonData();
	}
	@JavascriptInterface
	public void refresh(){
		loadUrl(getTag().toString());
	}
	@JavascriptInterface
	public String getIcon(String str){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		Bitmap b=ImageDraw.TextImage(str.charAt(0),true);
		b.compress(Bitmap.CompressFormat.PNG,100,baos);
		b.recycle();b=null;
		String data=new String(Base64.encode(baos.toByteArray(),Base64.DEFAULT));
		try
		{
			baos.close();
		}
		catch (IOException e)
		{}
		return "data:image/png;base64,"+data;
	}

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
public String getCookie(String url){
	CookieManager cm=CookieManager.getInstance();
	return cm.getCookie(url);
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

		return state;
	}
    public void setOnStateListener(OnStateListener osl)
	{
        this.osl = osl;
    }
	
    WebViewClient wvc=new WebViewClient(){
		private void urlParse(String url)
		{
			switch (bl.isBlackOrWhiteUrl(url))
			{
				case BlackList.UNKNOW:
					OutProgramWindow.getInstance(getContext()).show(url);
					break;
				case BlackList.WHITE:
					Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					try
					{
						getContext().startActivity(intent);
					}
					catch (Exception e)
					{}
					break;
			}

		}
        @Override
        public boolean shouldOverrideUrlLoading(WebView p1, String url)
        {
			if (url.startsWith("file:") || url.startsWith("http:") || url.startsWith("https"))
			{
				return super.shouldOverrideUrlLoading(p1, url);
            }
			else if(url.startsWith("moe:")){
				ad.show();
			}else
			{
				urlParse(url);
				
			}
			return true;
			//return super.shouldOverrideUrlLoading(p1,url);  
		}

        @Override
        public void onReceivedSslError(WebView p1, SslErrorHandler p2, SslError p3)
        {
            // TODO: Implement this method
            //super.onReceivedSslError(p1, p2, p3);
            p2.proceed();//支持https
        }

        @Override
        public void onReceivedError(WebView p1, int p2, String p3, String url)
        {
            // TODO: Implement this method
            //super.onReceivedError(p1, p2, p3, url);
			if (url.startsWith("file:") || url.startsWith("http:") || url.startsWith("https:"))
			{
				setTag(url);
				p1.loadUrl("javascript:document.body.innerHTML=\"" + "访问出错" + "\"");				//显示出错页面
				//if (osl != null)
				//	osl.onEnd(p3, p1.getTitle());
            }
			else
			{
				urlParse(url);
			}

        }

		

		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
		{
			// TODO: Implement this method
			super.onReceivedHttpError(view, request, errorResponse);
			if (osl != null)
				osl.onEnd("", errorResponse.getReasonPhrase());

		}

        @Override
        public void onPageStarted(WebView p1, String p2, Bitmap p3)
        {
            // TODO: Implement this method
            super.onPageStarted(p1, p2, p3);
            if (osl != null)
                osl.onStart(p2);
			state = true;
        }

        @Override
        public void onPageFinished(WebView p1, String p2)
        { if (osl != null)
				osl.onEnd(p2, p1.getTitle());
            	state = false;
			//p1.loadUrl("javascript:document.body.style.marginTop=\""+getResources().getDimension(R.dimen.actionBarSize)+"px\";");
	        //transParent();
		}

		@Override
		public void onLoadResource(WebView view, String url)
		{
			//处理广告拦截
			super.onLoadResource(view, url);
		}

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload)
        {
            // 历史记录,不采用，标题获取不到
			super.doUpdateVisitedHistory(view, url, isReload);
        }

		


    };
	private void transParent()
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

	}
    WebChromeClient wcc=new WebChromeClient(){

		@Override
		public View getVideoLoadingProgressView()
		{
			// TODO: Implement this method
			return super.getVideoLoadingProgressView();
		}

		@Override
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
		{
			// TODO: Implement this method
			super.onShowCustomView(view, callback);
		}

		@Override
		public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback)
		{
			// TODO: Implement this method
			super.onShowCustomView(view, requestedOrientation, callback);
		}

		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
		{
			// TODO: Implement this method
			return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
		}

        @Override
        public void onReceivedTitle(WebView p1, String p2)
        {
            // TODO: Implement this method
            super.onReceivedTitle(p1, p2);
            if (osl != null)osl.onReceiverTitle(p2);
			wh.insertOrUpdateWebHistory(p1.getUrl(), p2);
		}

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            //js提示窗口
//			AlertDialog ad=new AlertDialog(getContext());
//			ad.setTitle("网页提示");
//			ad.setMessage(message);
//			ad.show();
			if(shared.getBoolean(Setting.ALERTDIALOG,false))
            result.confirm();
			else
			result.cancel();
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg)
        {

            EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_JS_NEW_WINDOW, resultMsg, true));
            return true;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback)
        {
            //是否允许定位
			gps.show();
			gps.setOnClickListener(new AlertDialog.OnClickListener(){

					@Override
					public void onClick(View v)
					{
						if (v.getId() == 0)
						{
							if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
							{ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 49);
							EventBus.getDefault().post(new com.moe.bean.Message(49,new Object[]{origin,callback}));
							}else
								callback.invoke(origin, true, false);
						}
					}
				});            
			// super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onProgressChanged(WebView p1, int p2)
        {
            // TODO: Implement this method
            super.onProgressChanged(p1, p2);
            if (osl != null)osl.onProgress(p2);
			
        }

    };
    private void initWebViewSettings()
	{
        WebSettings webSetting = this.getSettings();
        //webSetting.setOffscreenPreRaster(false);
        //切回前台不重写绘制
        //webSetting.setDefaultTextEncodingName("utf-8");
        //默认编码
        webSetting.setAppCachePath(getContext().getExternalCacheDir().getAbsolutePath());
        //设置缓存路径
        webSetting.setLoadsImagesAutomatically(true);
        webSetting.setBlockNetworkImage(shared.getBoolean(Setting.BLOCKIMAGES,false));
        //禁止加载图片
        webSetting.setJavaScriptEnabled(shared.getBoolean(Setting.JAVASCRIPT,true));
        //启用js
        webSetting.setJavaScriptCanOpenWindowsAutomatically(shared.getBoolean(Setting.NEWWINDOW,false));
        //允许js打开新窗口
        webSetting.setAllowFileAccess(true);
        //允许访问文件
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        //自动调整布局
        webSetting.setSupportZoom(true);
        //是否允许缩放
        webSetting.setBuiltInZoomControls(true);
		//自由缩放
		webSetting.setDisplayZoomControls(false);
        //显示缩放控制器
        webSetting.setUseWideViewPort(shared.getBoolean(Setting.WIDEVIEW,true));
        //自动调整图片大小
        webSetting.setSupportMultipleWindows(shared.getBoolean(Setting.MULTIWINDOWS,true));
        //支持多窗口
		webSetting.setLoadWithOverviewMode(shared.getBoolean(Setting.OVERVIEW,true));
		//预览模式
        webSetting.setAppCacheEnabled(true);
        //启用缓存
        webSetting.setMediaPlaybackRequiresUserGesture(true);
        //媒体手势播放
        webSetting.setTextZoom(shared.getInt(Setting.TEXTSIZE,50)+50);
        //设置文字缩放
		webSetting.setPluginState(WebSettings.PluginState.ON);
        //启用flash插件
		webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        //webSetting.setGeolocationDatabasePath(getContext().getDir("database", Context.MODE_PRIVATE).getPath());
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        //webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setSaveFormData(true);
        //保存表单
        webSetting.setSavePassword(true);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
		webSetting.setUserAgentString(shared.getString(Setting.USERAGENT,webSetting.getUserAgentString()));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		if(key.equals(Setting.TEXTSIZE))
			getSettings().setTextZoom(shared.getInt(Setting.TEXTSIZE,50)+50);
		else if(key.equals(Setting.JAVASCRIPT))
			getSettings().setJavaScriptEnabled(shared.getBoolean(Setting.JAVASCRIPT,true));
		else if(key.equals(Setting.MULTIWINDOWS))
			getSettings().setSupportMultipleWindows(shared.getBoolean(Setting.MULTIWINDOWS,true));
		else if(key.equals(Setting.NEWWINDOW))
			getSettings().setJavaScriptCanOpenWindowsAutomatically(shared.getBoolean(Setting.NEWWINDOW,false));
		else if(key.equals(Setting.OVERVIEW))
			getSettings().setLoadWithOverviewMode(shared.getBoolean(Setting.OVERVIEW,true));
		else if(key.equals(Setting.WIDEVIEW))
			getSettings().setUseWideViewPort(shared.getBoolean(Setting.WIDEVIEW,true));
		else if(key.equals(Setting.BLOCKIMAGES))
			getSettings().setBlockNetworkImage(shared.getBoolean(Setting.BLOCKIMAGES,false));
		else if(key.equals(Setting.USERAGENT))
			getSettings().setUserAgentString(shared.getString(Setting.USERAGENT,getSettings().getUserAgentString()));
		
		
	}


  /**  @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return gd.onTouchEvent(event) == true ?true: super.onTouchEvent(event);
    }*/
	@Override
	public boolean onDown(MotionEvent p1)
	{
		if (getContentHeight() * getScale() > getHeight() - getResources().getDimension(R.dimen.actionBarSize))
			canScroll = startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
		y = (int)p1.getRawY();
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
		if (canScroll)
			if (dispatchNestedPreScroll(0, (int)(y - p2.getRawY()), consumed, offset))
				return true;

		return false;
	}

	@Override
	public void onLongPress(MotionEvent p1)
	{
//		if(p1.getEventTime()-p1.getDownTime()>450){
//			if(Math.abs(y-p1.getY())<20)
//				onLongClick(p1);
//		}
		if(getUrl().isEmpty())return;
		switch (getHitTestResult().getType())
		{
            case HitTestResult.ANCHOR_TYPE:
			case HitTestResult.SRC_ANCHOR_TYPE:
				//链接
            case HitTestResult.IMAGE_ANCHOR_TYPE:
			case HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				//图片
				break;
            case HitTestResult.EDIT_TEXT_TYPE:
				return;
			case HitTestResult.EMAIL_TYPE:
			case HitTestResult.GEO_TYPE:
			case HitTestResult.PHONE_TYPE:
				break;
            case HitTestResult.UNKNOWN_TYPE:
			default:
				return;
        }
		//KeyEvent shiftPressEvent = new KeyEvent(0, 0,KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
		//shiftPressEvent.dispatch(this);
		pop.setHitTestResult(getHitTestResult());
        pop.showAtLocation(this, Gravity.TOP | Gravity.LEFT,p1);
	}

	@Override
	public boolean onFling(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		if (canScroll)
			stopNestedScroll();
		return false;
	}


    public boolean onLongClick(MotionEvent event)
    {
		return true;
    }

//    public void startTextSelection()
//	{
//        try
//		{
//            WebView.class.getMethod("selectText").invoke(this);
//        }
//		catch (Exception e)
//		{
//            try
//			{
//                WebView.class.getMethod("emulateShiftHeld").invoke(this);
//            }
//			catch (Exception e1)
//			{
//
//			}
//        }
//    }
//
	@Override
	protected void onDetachedFromWindow()
	{
		shared.unregisterOnSharedPreferenceChangeListener(this);
		super.onDetachedFromWindow();
	}



    int y;
	int[] consumed =new int[2];
	int[] offset=new int[2];
    public  void setNestedScrollingEnabled(boolean p1)
	{
        childHelper.setNestedScrollingEnabled(p1);
    }

    public  boolean isNestedScrollingEnabled()
	{
        return childHelper.isNestedScrollingEnabled();
    }

    public  boolean startNestedScroll(int p1)
	{
        return childHelper.startNestedScroll(p1);
    }

    public  void stopNestedScroll()
	{
        childHelper.stopNestedScroll();
    }

    public  boolean hasNestedScrollingParent()
	{
        return childHelper.hasNestedScrollingParent();
    }

    public  boolean dispatchNestedScroll(int p1, int p2, int p3, int p4, int[] p5)
	{
        return childHelper.dispatchNestedScroll(p1, p2, p3, p4, p5);
    }

    public  boolean dispatchNestedPreScroll(int p1, int p2, int[] p3, int[] p4)
	{
        return childHelper.dispatchNestedPreScroll(p1, p2, p3, p4);
    }


    public  boolean dispatchNestedFling(float p1, float p2, boolean p3)
	{
        return childHelper.dispatchNestedFling(p1, p2, p3);
    }

    public  boolean dispatchNestedPreFling(float p1, float p2)
	{
        return childHelper.dispatchNestedPreFling(p1, p2);
    }
    public abstract interface OnStateListener
    {

        public void onProgress(int p2);

        void onStart(String url);
        void onEnd(String url, String title);
        void onReceiverTitle(String title);
    }
	public static class Setting{
		//文字大小
		public final static String TEXTSIZE="textSize";
		//启用js
		public final static String JAVASCRIPT="javaScript";
		//启用多窗口
		public final static String MULTIWINDOWS="multiWindows";
		//链接打开方式(自动/本页)
		public final static String NEWWINDOW="newWindow";
		//预览模式
		public final static String OVERVIEW="overView";
		//自适应布局
		public final static String WIDEVIEW="wideView";
		//禁止加载图片
		public final static String BLOCKIMAGES="blockImages";
		//设置ua
		public final static String USERAGENT="userAgent";
		//是否允许弹出对话框
		public final static String ALERTDIALOG="alertDialog";
	}
}
