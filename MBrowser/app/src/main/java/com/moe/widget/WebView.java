package com.moe.widget;

import android.content.Context;
import android.view.View;
import android.view.MotionEvent;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.gesture.Gesture;
import android.view.GestureDetector;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.ContextMenu;
import android.os.Message;
import de.greenrobot.event.EventBus;
import com.moe.bean.WindowEvent;
import android.view.Menu;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.view.Gravity;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import com.moe.dialog.OutProgramWindow;
import com.moe.database.WebHistory;
import com.moe.database.BlackList;
import com.moe.dialog.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.app.Activity;
import com.moe.utils.ImageDraw;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import java.io.IOException;
import com.moe.database.HomePage;
import com.moe.dialog.AddDialog;
import com.moe.view.PopupWindow;
import android.content.SharedPreferences;
import com.moe.bean.DownloadItem;
import com.moe.database.Download;
import android.view.ViewGroup;
import android.os.Bundle;
import java.util.ArrayList;
import android.webkit.DownloadListener;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.webkit.WebResourceResponse;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.webkit.JsResult;
import android.webkit.JsPromptResult;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.ValueCallback;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.CookieManager;
import android.webkit.WebView;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import com.moe.database.Sqlite;
import java.util.List;
import com.moe.database.JavaScript;
import android.text.TextUtils;
import com.moe.database.AdBlockDatabase;
import com.moe.utils.Convert;
import com.moe.utils.UrlBlock;
import android.webkit.WebChromeClient.CustomViewCallback;
import java.io.ByteArrayInputStream;

public class WebView extends WebView implements NestedScrollingChild,GestureDetector.OnGestureListener,SharedPreferences.OnSharedPreferenceChangeListener,DownloadListener,AlertDialog.OnClickListener,View.OnTouchListener
{


    private boolean canScroll=false;
    private NestedScrollingChildHelper childHelper = new NestedScrollingChildHelper(this);
    //private GestureDetector gesture=new GestureDetector(this);
    private OnStateListener osl;
    private PopupWindow pop;
	//private boolean state=false;
	private final String homepage="file:///android_asset/homepage.html";
//	private final String ajaxhook="file:///android_asset/ajaxhook.js";
	private WebHistory wh;
	private GestureDetector gd=new GestureDetector(this);
	private BlackList bl;
	private HomePage hp;
	private AddDialog ad;
	private SharedPreferences shared;
	private AlertDialog dd;
	private String homepageurl;
	//private DomElement de;
	private ArrayList<String> video=new ArrayList<>();
	private UrlBlock urlBlock;
	private WebChromeClient.CustomViewCallback callback;
	private List<String> javascript;
	private float scale;
	private String adblock;
	private AdBlockDatabase abd;
    public WebView(final Context context, AddDialog ad)
	{
        super(context);
		abd=Sqlite.getInstance(context,AdBlockDatabase.class);
		shared = context.getSharedPreferences("webview", 0);
		shared.registerOnSharedPreferenceChangeListener(this);
		this.ad = ad;
		dd = new AlertDialog(context);
		dd.setTitle("确认删除导航？");
		dd.setOnClickListener(this);
		bl = Sqlite.getInstance(context, BlackList.class);
		wh = Sqlite.getInstance(context, WebHistory.class);
		hp = Sqlite.getInstance(context, HomePage.class);
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
		pop = PopupWindow.getInstance(getContext());
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
		addJavascriptInterface(this, "moe");
		loadUrl(homepage);
		setDownloadListener(this);
		//setOnTouchListener(this);
		new Thread(){
			public void run(){
				urlBlock=UrlBlock.getInstance(context);
			}
		}.start();
	    }
	@JavascriptInterface
	public void cancelFullscreen()
	{
		post(new Runnable(){

				@Override
				public void run()
				{
					if (callback != null)
						callback.onCustomViewHidden();
					//wcc.onHideCustomView();
				}
			});
	}
	@JavascriptInterface
	public void source(final String data)
	{
		EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_DATA_NEW_WINDOW, data));
	}
	public void watchSource()
	{
		loadUrl("javascript:moe.source('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
	}


	@JavascriptInterface
	public void getElement(final String tagName, final String id, final String className)
	{


		post(new Runnable(){

				@Override
				public void run()
				{
					String data="";
					if (!TextUtils.isEmpty(id))
						data = "#" + id;
					else
						data = tagName + "." + className;
					loadUrl("javascript:var item=document.querySelector('" + data + "');item.parentNode.removeChild(item);");
					try
					{
						Sqlite.getInstance(getContext(), AdBlockDatabase.class).add(new URL(getUrl()).getHost(), data);
					}
					catch (MalformedURLException e)
					{}

				}
			});			

		/**de=new DomElement();
		 de.setTag(tagName);
		 de.set_Class(className);
		 de.setHref(href);
		 de.setId(id);
		 de.setValue(value);
		 de.setSrc(src);*/
	}
	public void videoFind()
	{
		//视频嗅探
		EventBus.getDefault().post(new com.moe.bean.Message(6, video));

		//loadUrl("javascript:var url='';var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){url=url+video[i].src+';';var source=video[i].getElementsByTagName('source');for(var n=0;n<source.length;n++){url=url+source[n].src+';'}}video=document.getElementsByTagName('iframe');for(var i=0;i<video.length;i++){url=url+video[i].src+';';}video=document.getElementsByTagName('embed');for(var i=0;i<video.length;i++){url=url+video[i].src+';';}moe.result(url);");

	}

	/**视频嗅探原规则，已抛弃
	 @JavascriptInterface
	 public void result(String data){
	 EventBus.getDefault().post(new com.moe.bean.Message(6,data));
	 }*/

	//删除主页条目
	@Override
	public void onClick(View v)
	{
		if (v.getId() == 0)
		{
			hp.deleteItem(homepageurl);
			reload();
		}
	}


	@JavascriptInterface
	public void delete(String url)
	{
		this.homepageurl = url;
		post(new Runnable(){public void run()
				{dd.show();}});
		//dd.show();
	}
	@JavascriptInterface
	public String getHomePageData()
	{
		return hp.getJsonData();
	}
	@JavascriptInterface
	public void refresh()
	{
		loadUrl(getTag().toString());
	}
	//主页获取图标
	@JavascriptInterface
	public String getIcon(String str)
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		Bitmap b=ImageDraw.TextImage(str.charAt(0), true);
		b.compress(Bitmap.CompressFormat.PNG, 100, baos);
		b.recycle();b = null;
		String data=new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		try
		{
			baos.close();
		}
		catch (IOException e)
		{}
		return "data:image/png;base64," + data;
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
    WebViewClient wvc=new WebViewClient(){
		private void urlParse(String url)
		{
			switch(Uri.parse(url).getScheme()){
				case "Flashget":
				case "thunder":
				case "qqdl":
					DownloadItem di=new DownloadItem();
					di.setUrl(Convert.parse(url));
					di.setLength(0);
					di.setSourceUrl(getUrl());
					EventBus.getDefault().post(di);
					break;
				default:
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
				break;
			}
			

		}
        @Override
        public boolean shouldOverrideUrlLoading(WebView p1, String url)
        {
			if (url.startsWith("file://") || url.startsWith("http://") || url.startsWith("https://"))
			{
				return super.shouldOverrideUrlLoading(p1, url);
            }
			else if (url.startsWith("moe:"))
			{
				ad.show();
			}
			else
			{
				urlParse(url);

			}
			return true;
			//return super.shouldOverrideUrlLoading(p1,url);  
		}



        @Override
        public void onReceivedSslError(WebView p1, SslErrorHandler p2, SslError p3)
        {
			p2.proceed();//支持https
        }

        @Override
        public void onReceivedError(WebView p1, int p2, String p3, String url)
        {
			if (p2 == ERROR_UNSUPPORTED_SCHEME || p2 == ERROR_UNSUPPORTED_AUTH_SCHEME)
				urlParse(url);
			else
				setTag(url);

        }

		@Override
		public void onReceivedError(WebView p1, WebResourceRequest p2, WebResourceError p3)
		{
			onReceivedError(p1, p3.getErrorCode(), p3.getDescription().toString(), p2.getUrl().toString());
		}

		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
		{
			super.onReceivedHttpError(view,request,errorResponse);
		}

        @Override
        public void onPageStarted(WebView p1, String p2, Bitmap p3)
        {
            if (osl != null)
                osl.onStart(p2);
			video.clear();
            super.onPageStarted(p1, p2, p3);
			//加载自动执行的脚本和广告拦截数据
			try
			{
				String host=new URL(p2).getHost();
				javascript = Sqlite.getInstance(getContext(), JavaScript.class).getAllScript(host);
				adblock = abd.getData(host);
			}
			catch (MalformedURLException e)
			{}

        }

        @Override
        public void onPageFinished(WebView p1, String p2)
        {
			if (osl != null)
				osl.onEnd(p2, p1.getTitle());		
			try
			{
				inith5Video(new URL(p2));
				if (javascript != null)
					for (String js:javascript)
						loadUrl(js);
				if (adblock != null)
					for (String js:adblock.split(","))
					{
						loadUrl("javascript:var item=document.querySelector('" + js + "');item.parentNode.removeChild(item);");
					}
			}
			catch (MalformedURLException e)
			{urlParse(p2);}
		}


		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest p2)
		{
			if(urlBlock.isExists(p2.getUrl().toString())){
				WebResourceResponse wr=new WebResourceResponse("*/*","unicode",null);
				wr.setStatusCodeAndReasonPhrase(403,"HTTP/1.1 403");
				return wr;
			}else{
			String data=p2.getRequestHeaders().get("Accept");
			if ((data != null && data.indexOf("video/") != -1) || p2.getRequestHeaders().get("Range") != null)
			{
				video.add(p2.getUrl().toString());
			}
//			if(videoBlock.isExists(p2.getUrl().toString())){
//				p2=null;
//			}
			}
			return null;
			//return super.shouldInterceptRequest(view, p2);
		}
		@Override
		public void onLoadResource(WebView view, String url)
		{
			/*if(ad!=null&&ab.isHostExists(view.getUrl()))
			 if(ab.isUrlExists(view.getUrl(),p2.getUrl().toString()))
			 return new WebResourceResponse(null,null,null);
			 */
			try
			{
				if (video.contains(url))
					inith5Video(new URL(view.getUrl()));
			}
			catch (MalformedURLException e)
			{}
		}

		@Override
		public void onScaleChanged(WebView view, float oldScale, float newScale)
		{
			scale = newScale;
			super.onScaleChanged(view, oldScale, newScale);
		}


		/**    @Override
		 public void doUpdateVisitedHistory(WebView view, String url, boolean isReload)
		 {
		 // 历史记录,不采用，标题获取不到
		 super.doUpdateVisitedHistory(view, url, isReload);
		 }*/

    };

	@Override
	public float getScale()
	{
		return scale;
	}

	public void inith5Video(URL url)
	{
		switch (url.getHost())
		{
			case "live.bilibili.com":
				loadUrl("javascript:document.querySelector('video').addEventListener('canplay',function(){var button=document.querySelector('.playwrap').lastChild;button.addEventListener('click',function(){var full=document.querySelector('.player-wrap');if(button.value=='bind'){moe.cancelFullscreen();button.value='';}else{full.webkitRequestFullscreen();button.value='bind';}},false);},false);");
				break;
			case "m.bilibili.com":
				loadUrl("javascript:document.querySelector('video').addEventListener('canplay',function(){var button=document.querySelector('.btn-widescreen');button.onclick=function(){if(this.id=='bind'){moe.cancelFullscreen();this.id='';}else{this.id='bind';document.querySelector('.player-container').webkitRequestFullscreen();}}},false);");
				break;
			case "bangumi.bilibili.com":
				loadUrl("javascript:document.querySelector('video').addEventListener('canplay',function(){var button=document.querySelector('.btn-widescreen');button.onclick=function(){if(this.id=='bind'){moe.cancelFullscreen();this.id='';}else{this.id='bind';document.querySelector('.player-container').webkitRequestFullscreen();}}},false);");
				break;
			case "m.le.com":
				loadUrl("javascript:var video=document.querySelector('video');video.setAttribute('controls','true');if(video.value!='bind'){video.value='bind';video.addEventListener('canplay',function(){var child=this.parentNode.nextSibling; if(child)child.parentNode.removeChild(child);},false); };");
				break;
			/**case "m.tv.sohu.com":
				loadUrl("javascript:var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){if(video[i].value=='bind')continue;video[i].value='bind'; video[i].setAttribute('controls','true'); video[i].setAttribute('playsinline','false'); video[i].setAttribute('webkit-playsinline','false');   video[i].addEventListener('play',function(){if(moe.isVideoBlock(this.src)){this.currentTime=150;}; },false); };");
				break;
			case "m.youku.com":
				loadUrl("javascript:var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){if(video[i].value=='bind')continue;video[i].value='bind'; " +
						"video[i].addEventListener('loadstart',function(){" +
						"if(this.src&&moe.isVideoBlock(this.src)){this.src='';}" +
						"},false); " +
						"};");
				break;*/
			case "m.v.qq.com":
				loadUrl("javascript:var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){if(video[i].value=='bind')continue;video[i].value='bind'; " +
						"video[i].setAttribute('controls','true');" +
						"video[i].setAttribute('playsinline','false');" +
						"video[i].setAttribute('webkit-playsinline','false'); " +
						"video[i].addEventListener('loadstart',function(){" +
						//"if(this.src&&moe.isVideoBlock(this.src)){this.src='';}else{" +
						"var parent=document.querySelector('.site_player');var ts=parent.querySelector('video');if(ts){parent.innerHTML=''; parent.appendChild(ts);}" +
						//"}+"
						"},false); " +
						"};"
						//"var mircol=document.querySelector('#2016_mini');alert(mircol);mircol.innerHTML='';"
						);
				break;
			default:
				loadUrl("javascript:var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){"+
				"video[i].setAttribute('controls','true');" +
						"video[i].setAttribute('playsinline','false');" +
						"video[i].setAttribute('webkit-playsinline','false'); " +
						//"video[i].addEventListener('loadstart',function(){" +
						//"if(this.src&&moe.isVideoBlock(this.src)){this.src='';}" +
						//"var source=document.getElementsByTagName('source');" +
						//"if(source)for(var s=0;s<source.length;s++){" +
						//"if(moe.isVideoBlock(source[s].src)){source[s]='';}" +
						//"}" +
						//"},false); " +
						"};");
				//loadUrl("javascript:var video=document.getElementsByTagName('source');for(var i=0;i<video.length;i++){if(video[i].value=='bind')continue;video[i].value='bind';video[i].addEventListener('loadstart',function(){if(moe.isVideoBlock(this.src)){this.src='';}; },false); }");
				loadUrl("javascript:var video=document.getElementsByTagName('iframe');for(var i=0;i<video.length;i++){video[i].setAttribute('allowfullscreen','true');video[i].setAttribute('allowTransparency','true');"+
				//"video[i].addEventListener('load',function(){if(moe.isVideoBlock(this.src)){this.src='';};"+
				"}");
				break;
		}

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
    WebChromeClient wcc=new WebChromeClient(){

		@Override
		public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback)
		{
			onShowCustomView(view,callback);
		}
		
		@Override
		public void onShowCustomView(View p1, WebChromeClient.CustomViewCallback p2)
		{
			callback = p2;
			if (p1 instanceof FrameLayout)
			{
				p1.setBackgroundColor(0xff000000);
			}
			p1.setTag(p2);
			EventBus.getDefault().post(p1);
		}

		@Override
		public void onHideCustomView()
		{
			EventBus.getDefault().post("hide");
		}



		@Override
		public View getVideoLoadingProgressView()
		{
			return new ProgressBar(getContext());
		}


		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
		{
			EventBus.getDefault().post(new com.moe.bean.Message(2888, new Object[]{filePathCallback,fileChooserParams}));
			return true;
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result)
		{
			if (shared.getBoolean(Setting.ALERTDIALOG, false))
			{
				result.confirm();
				return super.onJsConfirm(view, url, message, result);
			}
			else
				result.cancel();
			return true;
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result)
		{
			if (shared.getBoolean(Setting.ALERTDIALOG, false))
			{
				result.confirm();
				return super.onJsPrompt(view, url, message, defaultValue, result);
			}
			else
				result.cancel();
			return true;
		}

		@Override
		public void onCloseWindow(WebView window)
		{

			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_JS_CLOSE_WINDOW, this));
		}

        @Override
        public void onReceivedTitle(final WebView p1, final String p2)
        {
             if (osl != null)osl.onReceiverTitle(p2);
			if(!shared.getBoolean(Setting.PRIVATE,false)){
			final String url=p1.getUrl();
			new Thread(){
				public void run()
				{
					wh.insertOrUpdateWebHistory(url, p2);
				}
			}.start();
			}
		}

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            //js提示窗口
//			AlertDialog ad=new AlertDialog(getContext());
//			ad.setTitle("网页提示");
//			ad.setMessage(message);
//			ad.show();
			if (shared.getBoolean(Setting.ALERTDIALOG, false))
			{
				result.confirm();
				return super.onJsAlert(view, url, message, result);
			}
			else
				result.cancel();
            return true;
		}

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg)
        {

            EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_JS_NEW_WINDOW, resultMsg, true));
            return true;
        }
		//视频默认海报
		@Override
		public Bitmap getDefaultVideoPoster()
		{
			// TODO: Implement this method
			return ((BitmapDrawable)getResources().getDrawable(R.drawable.poster)).getBitmap();
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(final String p1, final GeolocationPermissions.Callback p2)
		{
			if(shared.getBoolean(Setting.GPS,false)){
				if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				{ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 49);
					EventBus.getDefault().post(new com.moe.bean.Message(49, new Object[]{p1,p2}));
				}
				else
					p2.invoke(p1, true, false);
			}else
			p2.invoke(p1,false,false);
		}



        @Override
        public void onProgressChanged(WebView p1, int p2)
        {
           if (osl != null)osl.onProgress(p2);
			if (adblock != null)
				for (String js:adblock.split(","))
				{
					loadUrl("javascript:var item=document.querySelector('" + js + "');item.parentNode.removeChild(item);");
				}
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
        webSetting.setBlockNetworkImage(shared.getBoolean(Setting.BLOCKIMAGES, false));
        //禁止加载图片
        webSetting.setJavaScriptEnabled(shared.getBoolean(Setting.JAVASCRIPT, true));
        //启用js
        webSetting.setJavaScriptCanOpenWindowsAutomatically(shared.getBoolean(Setting.NEWWINDOW, false));
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
        webSetting.setUseWideViewPort(shared.getBoolean(Setting.WIDEVIEW, true));
        //自动调整图片大小
        webSetting.setSupportMultipleWindows(shared.getBoolean(Setting.MULTIWINDOWS, true));
        //支持多窗口
		webSetting.setLoadWithOverviewMode(shared.getBoolean(Setting.OVERVIEW, true));
		//预览模式
        webSetting.setAppCacheEnabled(true);
        //启用缓存
        webSetting.setMediaPlaybackRequiresUserGesture(true);
        //媒体手动播放
        webSetting.setTextZoom(shared.getInt(Setting.TEXTSIZE, 50) + 50);
        //设置文字缩放
		webSetting.setPluginState(WebSettings.PluginState.ON);
        //启用flash插件
		webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setGeolocationDatabasePath(getContext().getExternalFilesDir("database").getPath());
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
		webSetting.setUserAgentString(shared.getBoolean(Setting.DESKTOP, false) == true ?getResources().getTextArray(R.array.uavalue)[1].toString(): shared.getString(Setting.USERAGENT, webSetting.getUserAgentString()));
		webSetting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		//加载不安全的视图模式

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		if (key.equals(Setting.TEXTSIZE))
			getSettings().setTextZoom(shared.getInt(Setting.TEXTSIZE, 50) + 50);
		else if (key.equals(Setting.JAVASCRIPT))
			getSettings().setJavaScriptEnabled(shared.getBoolean(Setting.JAVASCRIPT, true));
		else if (key.equals(Setting.MULTIWINDOWS))
			getSettings().setSupportMultipleWindows(shared.getBoolean(Setting.MULTIWINDOWS, true));
		else if (key.equals(Setting.NEWWINDOW))
			getSettings().setJavaScriptCanOpenWindowsAutomatically(shared.getBoolean(Setting.NEWWINDOW, false));
		else if (key.equals(Setting.OVERVIEW))
			getSettings().setLoadWithOverviewMode(shared.getBoolean(Setting.OVERVIEW, true));
		else if (key.equals(Setting.WIDEVIEW))
			getSettings().setUseWideViewPort(shared.getBoolean(Setting.WIDEVIEW, true));
		else if (key.equals(Setting.BLOCKIMAGES))
			getSettings().setBlockNetworkImage(shared.getBoolean(Setting.BLOCKIMAGES, false));
		else if (key.equals(Setting.USERAGENT) || key.equals(Setting.DESKTOP))
			getSettings().setUserAgentString(shared.getBoolean(Setting.DESKTOP, false) == true ?getResources().getTextArray(R.array.uavalue)[1].toString(): shared.getString(Setting.USERAGENT, getSettings().getUserAgentString()));



	}

	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		return onTouchEvent(p2);
	}



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
		if (canScroll)
			stopNestedScroll();
		return false;
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
	public static class Setting
	{
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
		//桌面模式
		public final static String DESKTOP="desktop";
		//gps
		public final static String GPS="gps";
		//无痕浏览
		public final static String PRIVATE="private";
	}
}
