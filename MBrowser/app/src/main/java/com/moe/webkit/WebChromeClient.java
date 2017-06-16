package com.moe.webkit;
import android.webkit.WebChromeClient;
import android.view.View;
import android.widget.FrameLayout;
import de.greenrobot.event.EventBus;
import com.moe.widget.ProgressBar;
import android.webkit.ValueCallback;
import android.webkit.JsResult;
import android.webkit.JsPromptResult;
import com.moe.bean.WindowEvent;
import android.os.Message;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.graphics.Bitmap;
import android.net.Uri;
import com.moe.Mbrowser.R;
import android.webkit.GeolocationPermissions;
import android.app.Activity;
import com.moe.database.WebHistory;
import com.moe.database.Sqlite;
import android.os.Build;

public class WebChromeClient extends WebChromeClient
{
	private WebView wv;
	private WebHistory wh;
	public WebChromeClient(WebView wv){
		this.wv=wv;
		wh=Sqlite.getInstance(wv.getContext(),WebHistory.class);
	}
	@Override
	public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback)
	{
		onShowCustomView(view, callback);
	}

	@Override
	public void onShowCustomView(View p1, final WebChromeClient.CustomViewCallback p2)
	{
		wv.post(new Runnable(){

				@Override
				public void run()
				{
					wv.setTag(R.id.webview_callback, p2);
				}
			}); 
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
		return new ProgressBar(wv.getContext());
	}

	@Override
	public boolean onShowFileChooser(android.webkit.WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
	{
		EventBus.getDefault().post(new com.moe.bean.Message(2888, new Object[]{filePathCallback,fileChooserParams}));
		return true;
	}

	@Override
	public boolean onJsConfirm(android.webkit.WebView view, String url, String message, JsResult result)
	{
		if (wv.getSharedPreferences().getBoolean(WebSettings.Setting.ALERTDIALOG, false))
		{
			result.confirm();
			return super.onJsConfirm(view, url, message, result);
		}
		else
			result.cancel();
		return true;
	}

	@Override
	public boolean onJsPrompt(android.webkit.WebView view, String url, String message, String defaultValue, JsPromptResult result)
	{
		if (wv.getSharedPreferences().getBoolean(WebSettings.Setting.ALERTDIALOG, false))
		{
			result.confirm();
			return super.onJsPrompt(view, url, message, defaultValue, result);
		}
		else
			result.cancel();
		return true;
	}

	@Override
	public void onCloseWindow(android.webkit.WebView window)
	{

		EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_JS_CLOSE_WINDOW, this));
	}

	@Override
	public void onReceivedTitle(final android.webkit.WebView p1, final String p2)
	{
		if (wv.getListener() != null)wv.getListener().onReceiverTitle(wv,p2);
		if (!wv.getSharedPreferences().getBoolean(WebSettings.Setting.PRIVATE, false))
		{
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
	public boolean onJsAlert(android.webkit.WebView view, String url, String message, JsResult result)
	{
		if (wv.getSharedPreferences().getBoolean(WebSettings.Setting.ALERTDIALOG, false))
		{
			result.confirm();
			return super.onJsAlert(view, url, message, result);
		}
		else
			result.cancel();
		return true;
	}

	@Override
	public boolean onCreateWindow(android.webkit.WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg)
	{

		EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_JS_NEW_WINDOW, resultMsg, true));
		return true;
	}
	//视频默认海报
	@Override
	public Bitmap getDefaultVideoPoster()
	{
		// TODO: Implement this method
		return ((BitmapDrawable)wv.getResources().getDrawable(R.drawable.poster)).getBitmap();
	}

	@Override
	public void onGeolocationPermissionsShowPrompt(final String p1, final GeolocationPermissions.Callback p2)
	{
		if (wv.getSharedPreferences().getBoolean(WebSettings.Setting.GPS, false))
		{
			if (Build.VERSION.SDK_INT>16&&ContextCompat.checkSelfPermission(wv.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(wv.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{ActivityCompat.requestPermissions((Activity)wv.getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 49);
				EventBus.getDefault().post(new com.moe.bean.Message(49, new Object[]{p1,p2}));
			}
			else
				p2.invoke(p1, true, false);
		}
		else
			p2.invoke(p1, false, false);
	}



	@Override
	public void onProgressChanged(android.webkit.WebView p1, int p2)
	{
		if (wv.getListener() != null)wv.getListener().onProgress(wv,p2);
		if (wv.getTag(R.id.webview_adblock)!= null)
			for (String js:wv.getTag(R.id.webview_adblock).toString().split(","))
			{
				p1.loadUrl("javascript:var item=document.querySelector('" + js + "');item.parentNode.removeChild(item);");
			}
	}
	
}
