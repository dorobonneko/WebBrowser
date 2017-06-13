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

public class WebViewClient extends WebViewClient
{
	private BlackList bl;
	private WebView wv;
	private List<String> javascript,video,block;
	private JavaScript javaScript;
	private UrlBlock urlBlock;
	private AdBlockDatabase adb;
	public WebViewClient(final WebView wv,List<String> video,List<String> block){
		this.wv=wv;
		this.video=video;
		this.block=block;
		javaScript=Sqlite.getInstance(wv.getContext(), JavaScript.class);
		bl=Sqlite.getInstance(wv.getContext(),BlackList.class);
		adb=Sqlite.getInstance(wv.getContext(),AdBlockDatabase.class);
		new Thread(){
			public void run()
			{
				urlBlock = UrlBlock.getInstance(wv.getContext());
			}
		}.start();
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
		switch (Uri.parse(url).getScheme())
		{
			case "http":
			case "https":
			case "file":
			case "content":
				return super.shouldOverrideUrlLoading(p1, url);
			case "moe":
				wv.getHomePageAdd().show();
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
				wv.getHomePageAdd().show();
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
			wv.setTag(R.id.webview_adblock,adb.getData(host));
			}
		if (wv.getListener() != null)
			wv.getListener().onStart(p2);
		video.clear();
		block.clear();
		//加载自动执行的脚本和广告拦截数据
	}

	@Override
	public void onPageFinished(android.webkit.WebView p1, String p2)
	{
		if (wv.getListener() != null)
			wv.getListener().onEnd(p2, p1.getTitle());		
		try
		{
			inith5Video(new URL(p2));
		}
		catch (MalformedURLException e)
		{}
		if (javascript != null)
			for (String js:javascript)
				p1.loadUrl(js);
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, String url)
	{
		switch (Uri.parse(url).getScheme())
		{
			case "http":
			case "https":
				if (urlBlock==null|| !urlBlock.isExists(url))
					return super.shouldInterceptRequest(view, url);
				break;
			default:break;
		}
		block.add(url);
		return super.shouldInterceptRequest(view, (String)null);

	}


	@Override
	public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, final WebResourceRequest p2)
	{
		if(urlBlock==null)return null;
		final String url=p2.getUrl().toString();
		switch (p2.getUrl().getScheme())
		{
			case "http":
			case "https":
				if (!urlBlock.isExists(url))
				{
					new Thread(){
						public void run(){
							synchronized(video){
								String data=p2.getRequestHeaders().get("Accept");
								if ((data != null && data.indexOf("video/") != -1) || p2.getRequestHeaders().get("Range") != null)
								{
									video.add(url);
								}
							}
						}
					}.start();

					return null;
				}
				break;
			default:break;
		}
		wv.post(new Runnable(){

				@Override
				public void run()
				{
					synchronized(block){
						urlParse(url);
						block.add(url);
					}
				}
			});
		WebResourceResponse wr=new WebResourceResponse("*/*", "unicode", null);
		wr.setStatusCodeAndReasonPhrase(403, "HTTP/1.1 403");
		return wr;
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
				wv.loadUrl("javascript:var video=document.querySelector('video');if(video)video.addEventListener('canplay',function(){"+
				"var button=document.querySelector('.playwrap').lastChild;"+
				"button.onclick=function(){"+
				"if(this.id=='bind'){moe.cancelFullscreen();this.id='';}else{document.querySelector('.player-wrap').webkitRequestFullscreen();this.id='bind';}"+
				"}"+
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
