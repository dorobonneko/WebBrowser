package com.moe.webkit;
import android.webkit.WebViewClient;
import com.moe.bean.DownloadItem;
import android.net.Uri;
import com.moe.utils.Convert;
import de.greenrobot.event.EventBus;
import com.moe.dialog.OutProgramWindow;
import android.content.Intent;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.graphics.Bitmap;
import java.util.List;
import android.webkit.WebResourceResponse;
import com.moe.database.Sqlite;
import com.moe.database.JavaScript;
import java.util.ArrayList;
import com.moe.internal.UrlBlock;
import com.moe.database.BlackList;
import com.moe.database.AdBlockDatabase;
import java.net.MalformedURLException;
import java.net.URL;
import com.moe.Mbrowser.R;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import android.webkit.CookieManager;
import com.moe.fragment.NetworkLogFragment;
import com.moe.utils.MediaUtils;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import com.moe.net.OkHttp;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.LinkedHashMap;
import android.text.TextUtils;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

public class WebViewClient extends WebViewClient
{
	private BlackList bl;
	private WebView wv;
	private List<String> javascript,video,block;
	private JavaScript javaScript;
	private UrlBlock urlBlock;
	private AdBlockDatabase adb;
	private WebViewManagerView wvmv;
	public WebViewClient(final WebView wv, WebViewManagerView wvmv)
	{
		this.wv = wv;
		this.wvmv = wvmv;
		this.video = wv.getVideo();
		this.block = wv.getBlock();
		javaScript = Sqlite.getInstance(wv.getContext(), JavaScript.class);
		bl = Sqlite.getInstance(wv.getContext(), BlackList.class);
		adb = Sqlite.getInstance(wv.getContext(), AdBlockDatabase.class);
		new Thread(){
			public void run()
			{
				urlBlock = UrlBlock.getInstance(wv.getContext());
			}
		}.start();
	}
	private List<String> getType(NetworkLogFragment.Type type)
	{
		List<String> ls=wv.getNetworkLog().getKey(type);
		if (ls == null)
		{
			ls = new ArrayList<>();
			wv.getNetworkLog().put(type, ls);
		}
		return ls;
	}
	private void urlParse(String url)
	{
		switch (Uri.parse(url).getScheme())
		{
			case "http":
			case "https":
			case "file":
			case "content":break;
			case "Flashget":
			case "thunder":
			case "qqdl":
				DownloadItem di=new DownloadItem();
				di.setUrl(Convert.parse(url));
				di.setLength(0);
				di.setSourceUrl(wv.getUrl());
				EventBus.getDefault().post(di);
				break;
			default:
				switch (bl.isBlackOrWhiteUrl(url))
				{
					case BlackList.UNKNOW:
						OutProgramWindow.getInstance(wv.getContext()).show(url);
						break;
					case BlackList.WHITE:
						Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						try
						{
							wv.getContext().startActivity(intent);
						}
						catch (Exception e)
						{}
						break;
				}
				break;
		}


	}
	@Override
	public boolean shouldOverrideUrlLoading(android.webkit.WebView p1, String url)
	{
		Uri uri=Uri.parse(url);
		switch (uri.getScheme())
		{
			case "http":
			case "https":
			case "file":
			case "content":
				if (wv.getSharedPreferences().getInt(WebSettings.Setting.MULTIVIEW, 0) == 1)
				{
					if(p1.getHitTestResult().getType()==0)return false;
					try
					{
						/*String path=uri.getPath();
						int index=path.indexOf("?");
						if (index != -1)path = path.substring(0, index);
						if (wv.getUrl().indexOf(path) == -1)
						{*/
							WebView webview=new WebView(wvmv);
							webview.loadUrl(url);
							p1.stopLoading();
							wvmv.addWebView(webview);
							return true;
						//}
					}
					catch (Exception e)
					{}
				}
				p1.pauseTimers();
				//p1.loadUrl(url);
				return false;
			case "moe":
				wvmv.getHomePageAdd().show();
				break;
			default:
				urlParse(url);
				break;

		}
		return true;
		//return super.shouldOverrideUrlLoading(p1,url);  
	}



	@Override
	public void onReceivedSslError(android.webkit.WebView p1, SslErrorHandler p2, SslError p3)
	{
		p2.proceed();//支持https
	}

	@Override
	public void onReceivedError(android.webkit.WebView p1, int p2, String p3, String url)
	{
		switch (Uri.parse(url).getScheme())
		{
			case "http":
			case "https":
			case "file":
			case "content":
				break;
			case "moe":
				wvmv.getHomePageAdd().show();
				break;
			default:
				urlParse(url);
				break;
		}

	}

	@Override
	public void onReceivedError(android.webkit.WebView p1, WebResourceRequest p2, WebResourceError p3)
	{
		onReceivedError(p1, p3.getErrorCode(), p3.getDescription().toString(), p2.getUrl().toString());
	}

	@Override
	public void onReceivedHttpError(android.webkit.WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
	{
	}

	@Override
	public void onPageStarted(android.webkit.WebView p1, String p2, Bitmap p3)
	{
		String host=Uri.parse(p2).getHost();
		if (host != null)
		{
			javascript = javaScript.getAllScript(host);
			wv.setTag(R.id.webview_adblock, adb.getData(host));
		}
		if (wv.getListener() != null)
			wv.getListener().onStart(wv, p2);
		//加载自动执行的脚本和广告拦截数据
		video.clear();
		block.clear();
		wv.getNetworkLog().clear();
		//p1.pauseTimers();
		super.onPageStarted(p1,p2,p3);
	}

	@Override
	public void onPageFinished(android.webkit.WebView p1, String p2)
	{
		p1.resumeTimers();
		if (wv.getListener() != null)
			wv.getListener().onEnd(wv, p2, p1.getTitle());		
		try
		{
			URL url=new URL(p2);
			inith5Video(url);
		}
		catch (MalformedURLException e)
		{}
		if (javascript != null)
			for (String js:javascript)
				p1.loadUrl(js);
		if (wv.getSharedPreferences().getBoolean(WebSettings.Setting.FORCESCALE, false))
			p1.loadUrl("javascript:var meta=document.querySelector('meta[name=viewport]');if(meta)meta.setAttribute('content','width=device-width');");

		//p1.getSettings().setBlockNetworkImage(false);

	}

	@Override
	public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, final String url)
	{
		
		Uri uri=Uri.parse(url);
		switch (uri.getScheme())
		{
			case "http":
			case "https":
				if (urlBlock == null || !urlBlock.matches(uri.getHost(), uri.getPath(), url))
				{
					new Thread(){
						public void run()
						{
							if (MediaUtils.isFormat(url, MediaUtils.Type.AUDIO))
								getType(NetworkLogFragment.Type.AUDIO).add(url);
							else if (MediaUtils.isFormat(url, MediaUtils.Type.CSS))
								getType(NetworkLogFragment.Type.CSS).add(url);
							else if (MediaUtils.isFormat(url, MediaUtils.Type.JS))
								getType(NetworkLogFragment.Type.JS).add(url);
							else if (MediaUtils.isFormat(url, MediaUtils.Type.VIDEO))
								getType(NetworkLogFragment.Type.VIDEO).add(url);
							else if (MediaUtils.isFormat(url, MediaUtils.Type.IMAGE))
								getType(NetworkLogFragment.Type.IMAGE).add(url);
							else getType(NetworkLogFragment.Type.OTHER).add(url);

						}
					}.start();
					return super.shouldInterceptRequest(view, url);
				}
				break;
			case "file":
			case "content":
				return null;
			default:break;
		}
		block.add(url);
		return super.shouldInterceptRequest(view, (String)null);

	}


	@Override
	public WebResourceResponse shouldInterceptRequest(final android.webkit.WebView view, final WebResourceRequest p2)
	{
		if (urlBlock == null)return null;
		try
		{
			final String url=p2.getUrl().toString();
			final String mimeType=p2.getRequestHeaders().get("Accept");
			switch (p2.getUrl().getScheme())
			{
				case "http":
				case "https":
					if (url.matches("^http?://.*?http.*?") || !urlBlock.matches(p2.getUrl().getHost(), p2.getUrl().getPath(), url))
					{
						new Thread(){
							public void run()
							{
								if (mimeType == null)
									getType(NetworkLogFragment.Type.OTHER).add(url);
								else
								{
									if (mimeType.indexOf("audio/") != -1 || MediaUtils.isFormat(url, MediaUtils.Type.AUDIO))
										getType(NetworkLogFragment.Type.AUDIO).add(url);
									else if (mimeType.indexOf("text/css") != -1 || MediaUtils.isFormat(url, MediaUtils.Type.CSS))
										getType(NetworkLogFragment.Type.CSS).add(url);
									else if (mimeType.indexOf("text/javascript") != -1 || MediaUtils.isFormat(url, MediaUtils.Type.JS))
										getType(NetworkLogFragment.Type.JS).add(url);
									else if (mimeType.indexOf("video/") != -1 || MediaUtils.isFormat(url, MediaUtils.Type.VIDEO))
										getType(NetworkLogFragment.Type.VIDEO).add(url);
									else if (mimeType.indexOf("image/") != -1 || MediaUtils.isFormat(url, MediaUtils.Type.IMAGE))
										getType(NetworkLogFragment.Type.IMAGE).add(url);
									else getType(NetworkLogFragment.Type.OTHER).add(url);
								}
								synchronized (video)
								{
									if ((mimeType != null && mimeType.indexOf("video/") != -1) || p2.getRequestHeaders().get("Range") != null)
									{
										video.add(url);
									}
								}
							}
						}.start();
						if (p2.getUrl().getHost().matches("(?i)^(?:pan|wangpan|yun).baidu.com$")&& p2.getMethod().equalsIgnoreCase("get"))
						{
							HttpURLConnection huc=(HttpURLConnection)new URL(p2.getUrl().toString()).openConnection();
							//huc.setRequestMethod(p2.getMethod());
							huc.setConnectTimeout(1000);
							huc.addRequestProperty("Cookie", CookieManager.getInstance().getCookie(p2.getUrl().toString()));
							huc.addRequestProperty("Accept-Encoding", "gzip");
							huc.addRequestProperty("Connection", "Keep-Alive");
							huc.addRequestProperty("X-Requested-With", view.getContext().getPackageName());
							huc.addRequestProperty("Accept-Language", "zh-CN,en_US");
							
							for (String key:p2.getRequestHeaders().keySet())
							{
								huc.setRequestProperty(key, p2.getRequestHeaders().get(key));
							}
							huc.setRequestProperty("User-Agent", "Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/20.0.019; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/525 (KHTML, like Gecko) BrowserNG/7.1.18124");
							if (huc instanceof HttpsURLConnection)
								((HttpsURLConnection)huc).setSSLSocketFactory(OkHttp.getSslSocketFactory());
							if (huc.getResponseCode() == 200)
							{
								Map<String,List<String>> map=huc.getHeaderFields();
								Map<String,String> header=new LinkedHashMap<>();
								for (String key:map.keySet())
									header.put(key, map.get(key).get(0));
								WebResourceResponse wrr=new WebResourceResponse(header.get("Content-Type"), huc.getContentEncoding(), new GZIPInputStream(huc.getInputStream()));
								wrr.setResponseHeaders(header);
								wrr.setStatusCodeAndReasonPhrase(huc.getResponseCode(),header.get(null));
								return wrr;
							}
							else return null;
						}
						return CacheManager.getInstance(wv.getContext()).shouldInterceptRequest(view, p2);
					}
					break;
				case "file":
				case "content":
					return null;
				default:break;
			}
			wv.post(new Runnable(){

					@Override
					public void run()
					{
						synchronized (block)
						{
							urlParse(url);
							block.add(url);
						}
					}
				});

			WebResourceResponse wr=new WebResourceResponse(mimeType == null ?"*/*": mimeType, "utf-8", null);
			wr.setStatusCodeAndReasonPhrase(403, "HTTP/1.1 403");
			return wr;
		}
		catch (Exception E)
		{}
		return null;
	}
	@Override
	public void onLoadResource(final android.webkit.WebView view, final String url)
	{
		view.post(new Runnable(){

				@Override
				public void run()
				{
					try
					{
						if (video.contains(url))
							inith5Video(new URL(view.getUrl()));
					}
					catch (MalformedURLException e)
					{}
				}
			});

	}
	public void inith5Video(URL url)
	{
		switch (url.getHost())
		{
			case "live.bilibili.com":
				wv.loadUrl("javascript:var video=document.querySelector('video');if(video)video.addEventListener('canplay',function(){" +
						   "var button=document.querySelector('.playwrap').lastChild;" +
						   "button.onclick=function(){" +
						   "if(this.id=='bind'){moe.cancelFullscreen();this.id='';}else{document.querySelector('.player-wrap').webkitRequestFullscreen();this.id='bind';}" +
						   "}" +
						   "},false);");
				break;
			case "bangumi.bilibili.com":
			case "m.bilibili.com":
				wv.loadUrl("javascript:document.querySelector('video').addEventListener('canplay',function(){var button=document.querySelector('.btn-widescreen');button.onclick=function(){if(this.id=='bind'){moe.cancelFullscreen();this.id='';}else{this.id='bind';document.querySelector('.player-container').webkitRequestFullscreen();}}},false);");
				break;
			case "m.le.com":
				wv.loadUrl("javascript:var video=document.querySelector('video');video.setAttribute('controls','true');if(video.value!='bind'){video.value='bind';video.addEventListener('canplay',function(){var child=this.parentNode.nextSibling; if(child)child.parentNode.removeChild(child);},false); };");
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
				wv.loadUrl("javascript:var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){if(video[i].value=='bind')continue;video[i].value='bind'; " +
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
				wv.loadUrl("javascript:var video=document.getElementsByTagName('video');for(var i=0;i<video.length;i++){" +
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
				wv.loadUrl("javascript:var video=document.getElementsByTagName('iframe');for(var i=0;i<video.length;i++){video[i].setAttribute('allowfullscreen','true');video[i].setAttribute('allowTransparency','true');" +
						   //"video[i].addEventListener('load',function(){if(moe.isVideoBlock(this.src)){this.src='';};"+
						   "}");
				break;
		}

	}



}
