package com.moe.webkit;
import android.webkit.JavascriptInterface;
import de.greenrobot.event.EventBus;
import com.moe.bean.WindowEvent;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import com.moe.utils.ImageDraw;
import android.graphics.Bitmap;
import android.text.TextUtils;
import java.net.MalformedURLException;
import com.moe.database.Sqlite;
import com.moe.database.AdBlockDatabase;
import android.net.Uri;
import com.moe.database.HomePage;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import com.moe.Mbrowser.R;
public class JavascriptInterface implements DialogInterface.OnClickListener
{
	private AlertDialog dd;
	private HomePage hp;
	private WebView wv;
	public JavascriptInterface(WebView wv){
		this.wv=wv;
		hp=Sqlite.getInstance(wv.getContext(),HomePage.class);
		dd = new AlertDialog.Builder(wv.getContext()).setMessage("确认删除导航？")
		.setNegativeButton("确定",this).setPositiveButton("取消",null).create();
	}
	@JavascriptInterface
	public void cancelFullscreen()
	{
		wv.post(new Runnable(){

				@Override
				public void run()
				{
					if (wv.getTag(R.id.webview_callback) != null)
						((WebChromeClient.CustomViewCallback)wv.getTag(R.id.webview_callback)).onCustomViewHidden();
				}
			});
	}
	@JavascriptInterface
	public void source(final String data)
	{
		EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_DATA_NEW_WINDOW, data));
	}
	
	@JavascriptInterface
	public void getElement(final String tagName, final String id, final String className)
	{


		wv.post(new Runnable(){

				@Override
				public void run()
				{
					String data="";
					if (!TextUtils.isEmpty(id))
						data = "#" + id;
					else
						data = tagName + "." + className;
					wv.loadUrl("javascript:var item=document.querySelector('" + data + "');item.parentNode.removeChild(item);");
					String host=Uri.parse(wv.getUrl()).getHost();
					if(host!=null)
					Sqlite.getInstance(wv.getContext(), AdBlockDatabase.class).add(host, data);
				}
			});			
		}
	@JavascriptInterface
	public void delete(final String url)
	{
		
		wv.post(new Runnable(){public void run()
				{
					wv.setTag(R.id.webview_url,url);
					dd.show();
					}});
		//dd.show();
	}
	@JavascriptInterface
	public String getHomePageData()
	{
		return hp.getJsonData();
	}
	/**@JavascriptInterface
	public void refresh()
	{
		loadUrl(wv.getTag().toString());
	}*/
	//主页获取图标
	@JavascriptInterface
	public String getIcon(String str)
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		Bitmap b=ImageDraw.TextImage(str.charAt(0), true);
		b.compress(Bitmap.CompressFormat.PNG, 100, baos);
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
	public void onClick(DialogInterface p1, int p2)
	{
		hp.deleteItem(wv.getTag(R.id.webview_url).toString());
		wv.reload();
	}

	
}
