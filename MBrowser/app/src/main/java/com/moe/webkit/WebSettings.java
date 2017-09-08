package com.moe.webkit;
import android.webkit.WebSettings;
import android.os.Build;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebSettings.PluginState;
import com.moe.Mbrowser.R;
import android.content.SharedPreferences;

public class WebSettings implements SharedPreferences.OnSharedPreferenceChangeListener
{
private WebSettings setting;
private String userAgent;
private WebView wv;
	public WebSettings(WebView wv){
		this.wv=wv;
		//切回前台不重写绘制
        //webSetting.setDefaultTextEncodingName("utf-8");
        //默认编码
		wv.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		setting=wv.getSettings();
		setting.setOffscreenPreRaster(false);
        
		userAgent=setting.getUserAgentString();
        setting.setAppCachePath(wv.getContext().getExternalCacheDir().getAbsolutePath());
        //设置缓存路径
        setting.setLoadsImagesAutomatically(!wv.getSharedPreferences().getBoolean(Setting.BLOCKIMAGES, false));
        //禁止加载图片
        setting.setJavaScriptEnabled(wv.getSharedPreferences().getBoolean(Setting.JAVASCRIPT, true));
        //启用js
        setting.setJavaScriptCanOpenWindowsAutomatically(wv.getSharedPreferences().getBoolean(Setting.NEWWINDOW, false));
        //允许js打开新窗口
        setting.setAllowFileAccess(true);
        //允许访问文件
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //自动调整布局
        setting.setSupportZoom(true);
        //是否允许缩放
        setting.setBuiltInZoomControls(true);
		//自由缩放
		setting.setDisplayZoomControls(false);
        //显示缩放控制器
        setting.setUseWideViewPort(wv.getSharedPreferences().getBoolean(Setting.WIDEVIEW, true));
        //自动调整图片大小
        setting.setSupportMultipleWindows(wv.getSharedPreferences().getBoolean(Setting.MULTIWINDOWS, true));
        //支持多窗口
		setting.setLoadWithOverviewMode(wv.getSharedPreferences().getBoolean(Setting.OVERVIEW, true));
		//预览模式
        setting.setAppCacheEnabled(true);
        //启用缓存
		if (Build.VERSION.SDK_INT > 16)
			setting.setMediaPlaybackRequiresUserGesture(true);
        //媒体手动播放
        setting.setTextZoom(wv.getSharedPreferences().getInt(Setting.TEXTSIZE, 30) + 50);
        //设置文字缩放
		setting.setPluginState(WebSettings.PluginState.ON);
        //启用flash插件
		setting.setDatabaseEnabled(true);
        setting.setDomStorageEnabled(true);
        setting.setGeolocationEnabled(true);
        setting.setGeolocationDatabasePath(wv.getContext().getExternalFilesDir("database").getPath());
        setting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        //webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);
        setting.setSaveFormData(true);
        //保存表单
        //setting.setSavePassword(true);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
		setting.setUserAgentString(wv.getSharedPreferences().getBoolean(Setting.DESKTOP, false) == true ?wv.getResources().getTextArray(R.array.uavalue)[1].toString(): wv.getSharedPreferences().getString(Setting.USERAGENT, userAgent));
		if (Build.VERSION.SDK_INT > 20)
			setting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		//加载不安全的视图模式
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		if (key.equals(Setting.TEXTSIZE))
			setting.setTextZoom(p1.getInt(Setting.TEXTSIZE, 30) + 50);
		else if (key.equals(Setting.JAVASCRIPT))
			setting.setJavaScriptEnabled(p1.getBoolean(Setting.JAVASCRIPT, true));
		else if (key.equals(Setting.MULTIWINDOWS))
			setting.setSupportMultipleWindows(p1.getBoolean(Setting.MULTIWINDOWS, true));
		else if (key.equals(Setting.NEWWINDOW))
			setting.setJavaScriptCanOpenWindowsAutomatically(p1.getBoolean(Setting.NEWWINDOW, false));
		else if (key.equals(Setting.OVERVIEW))
			setting.setLoadWithOverviewMode(p1.getBoolean(Setting.OVERVIEW, true));
		else if (key.equals(Setting.WIDEVIEW))
			setting.setUseWideViewPort(p1.getBoolean(Setting.WIDEVIEW, true));
		else if (key.equals(Setting.BLOCKIMAGES))
			setting.setLoadsImagesAutomatically(!p1.getBoolean(Setting.BLOCKIMAGES, false));
		else if (key.equals(Setting.USERAGENT) || key.equals(Setting.DESKTOP))
			setting.setUserAgentString(p1.getBoolean(Setting.DESKTOP, false) == true ?wv.getResources().getTextArray(R.array.uavalue)[1].toString(): p1.getString(Setting.USERAGENT, userAgent));



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
		//强制缩放
		public final static String FORCESCALE="forceScale";
		//多webview
		public final static String MULTIVIEW="multiView";
	}
}
