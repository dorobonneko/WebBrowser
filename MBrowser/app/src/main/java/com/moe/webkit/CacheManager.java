package com.moe.webkit;
import android.content.Context;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;
import java.io.File;
import com.moe.utils.MediaUtils;
import java.net.HttpURLConnection;
import android.webkit.CookieManager;
import java.net.URL;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;
import com.moe.net.OkHttp;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;
import com.moe.utils.StringUtils;

public class CacheManager
{
	private static CacheManager cm;
	private File cache;
	private CacheManager(Context context){
		cache=context.getExternalCacheDir();
		if(!cache.exists())cache.mkdirs();
	}
	public static CacheManager getInstance(Context context){
		if(cm==null)cm=new CacheManager(context);
		return cm;
	}
	public WebResourceResponse shouldInterceptRequest(final android.webkit.WebView view, final WebResourceRequest p2) throws IOException
	{
		String url=p2.getUrl().toString();
		Type type;
		//if ( MediaUtils.isFormat(url, MediaUtils.Type.CSS))
		//	type=Type.CSS;
		//else
			if (MediaUtils.isFormat(url, MediaUtils.Type.JS))
			type=Type.JS;
		else 
			if (MediaUtils.isFormat(url, MediaUtils.Type.IMAGE))
				type=Type.IMAGE;
		else
			if(MediaUtils.isFormat(url,MediaUtils.Type.CSS))	
		type=Type.CSS;
		else
			type=Type.OTHER;
		if(type!=Type.OTHER){
			File file=new File(cache,p2.getUrl().toString().hashCode()+"");
			File contenttype=new File(cache,p2.getUrl().toString().hashCode()+(type==Type.IMAGE?".img":type==Type.JS?".js":".css"));
			InputStream is=null;
			String contentType="*/*";
			try{
			if(file.exists()){
				try
				{
					contentType=StringUtils.newString(new FileInputStream(contenttype));
				}
				catch (IOException e)
				{}
				String[] time=null;
				if((time=contentType.split("#")).length==2){
					if(type==Type.JS&&System.currentTimeMillis()>Long.parseLong(time[1].trim())+file.lastModified())
						file.delete();
						else
					is=new GZIPInputStream(new FileInputStream(file));
				}
				
			}
			if(is==null){
				HttpURLConnection huc=get(p2);
				OutputStream os=null;
				if (huc.getResponseCode()== 200){
					is=huc.getInputStream();
					os=new FileOutputStream(file);
					if(!"gzip".equalsIgnoreCase(huc.getContentEncoding()))
						os=new GZIPOutputStream(os);
				}else{
				return new WebResourceResponse("*/*","utf-8",null);
				}
				if(huc.getContentType().indexOf("text/htm")!=-1)
					throw new IllegalAccessException();
				contentType=huc.getContentType();
				byte[] b=new byte[20480];
				int len=0;
				while((len=is.read(b))!=-1){
					os.write(b,0,len);
				}
				os.flush();
				os.close();
				is.close();
				os=new FileOutputStream(contenttype);
				//os.write(p2.getRequestHeaders().get("Accept").getBytes());
				os.write(contentType.getBytes());
				os.write('#');
				String cache=huc.getHeaderField("Cache-Control");
				os.write((cache==null?"0":cache.startsWith("max-age")?cache.split("=")[1].trim()+"000":"0").getBytes());
				os.flush();
				os.close();
				huc.disconnect();
				is=new GZIPInputStream(new FileInputStream(file));
			}
			}catch(Exception e){
				try{if(is!=null)is.close();}catch(IOException e1){}
				is=null;
				file.delete();
			}
			if(is!=null)
				return new WebResourceResponse(contentType.split("#")[0], "utf-8", is);
		}
		return null;
		}
	private HttpURLConnection get(WebResourceRequest wrr) throws IOException{
		HttpURLConnection huc=(HttpURLConnection)new URL(wrr.getUrl().toString()).openConnection();
		huc.setRequestMethod(wrr.getMethod());
		huc.setConnectTimeout(500);
		huc.setReadTimeout(500);
		for (String key:wrr.getRequestHeaders().keySet())
		{
			huc.addRequestProperty(key, wrr.getRequestHeaders().get(key));
		}
		huc.addRequestProperty("Cookie", CookieManager.getInstance().getCookie(wrr.getUrl().toString()));
		if(huc instanceof HttpsURLConnection)
			((HttpsURLConnection)huc).setSSLSocketFactory(OkHttp.getSslSocketFactory());
		return huc;
	}
	public enum Type{
		IMAGE,CSS,JS,OTHER;
	}
}
