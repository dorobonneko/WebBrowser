package com.moe.webkit;

import android.webkit.WebView;
import android.content.Context;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import com.moe.utils.LinkedListMap;
import com.moe.fragment.NetworkLogFragment;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import android.os.Build;
public class WebView extends WebView
{
	private OnTouchListener otl;
	private OnStateListener osl;
	private SharedPreferences shared;
	private LinkedListMap<NetworkLogFragment.Type, List<String>> llm=new LinkedListMap<>();
	private ArrayList<String> video=new ArrayList<>(),block=new ArrayList<>();
	public WebView(WebViewManagerView context)
	{
		super(context.getContext());
		shared = context.getContext().getSharedPreferences("webview", 0);
		setWebViewClient(new WebViewClient(this, context));
        setWebChromeClient(new WebChromeClient(this));
		WebSettings ws= new WebSettings(this);
		setTag(ws);
		setScrollBarSize(1);
		setScrollbarFadingEnabled(true);
		setScrollBarFadeDuration(100);
		setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		addJavascriptInterface(new JavascriptInterface(this), "moe");
		setDownloadListener(context);
	}

	
	public List<String> getVideo()
	{
		return video;
	}
	public List<String> getBlock()
	{
		return block;
	}
	public SharedPreferences getSharedPreferences()
	{
		return shared;
	}

	
	
	@Override
	public void destroy()
	{
		otl=null;
		osl=null;
		video.clear();
		video=null;
		block.clear();
		block=null;
		llm.clear();
		llm=null;
		setVisibility(View.GONE);
		stopLoading();
		releaseAllWebViewCallback();
		shared.unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener)getTag());
		super.destroy();
		System.gc();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return otl == null ?super.onTouchEvent(event): otl.onTouch(this, event) == true ?true: super.onTouchEvent(event);
	}
	public void releaseAllWebViewCallback()
	{
		if (android.os.Build.VERSION.SDK_INT < 16)
		{
			try
			{
				Field field = WebView.class.getDeclaredField("mWebViewCore");
				field = field.getType().getDeclaredField("mBrowserFrame");
				field = field.getType().getDeclaredField("sConfigCallback");
				field.setAccessible(true);
				field.set(null, null);
			}
			catch (NoSuchFieldException e)
			{

			}
			catch (IllegalAccessException e)
			{

			}
		}
		else
		{
			try
			{
				Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
				if (sConfigCallback != null)
				{
					sConfigCallback.setAccessible(true);
					sConfigCallback.set(null, null);
				}
			}
			catch (NoSuchFieldException e)
			{

			}
			catch (ClassNotFoundException e)
			{

			}
			catch (IllegalAccessException e)
			{

			}
		}
	}
	public LinkedListMap<NetworkLogFragment.Type, List<String>> getNetworkLog()
	{
		return llm;
	}
	public void setOnStateListener(OnStateListener osl)
	{
        this.osl = osl;
    }
	public OnStateListener getListener()
	{
		return osl;
	}
	public void setOnTouchListener(OnTouchListener o)
	{
		otl = o;
	}
	public abstract interface OnStateListener
    {

        void onProgress(WebView wv, int p2);
        void onStart(WebView wv, String url);
        void onEnd(WebView wv, String url, String title);
        void onReceiverTitle(WebView wv, String title);
    }
}
