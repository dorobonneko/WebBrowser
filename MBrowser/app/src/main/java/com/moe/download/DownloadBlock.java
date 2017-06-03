package com.moe.download;
import com.moe.entity.DownloadInfo;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.InputStream;
import com.moe.database.Download;
import java.io.File;
import com.moe.Mbrowser.R;
import de.greenrobot.event.EventBus;

public class DownloadBlock extends Thread
{
	private DownloadTask dt;
	private DownloadInfo di;
	private boolean success;
	private Response response;
	private RandomAccessFile raf;
	private InputStream is;
	private boolean pause=false;
	public DownloadBlock(DownloadTask dt,DownloadInfo di){
		this.dt=dt;
		this.di=di;
		setPriority(Thread.MIN_PRIORITY);
		start();
	}
	public boolean isSuccess(){
		return success;
	}
	@Override
	public void run()
	{
		if(di.getCurrent()<di.getEnd()){
		Request.Builder request=new Request.Builder();
		if (dt.getTaskInfo().getCookie() != null)
			request.addHeader("Cookie", dt.getTaskInfo().getCookie());
		if (dt.getTaskInfo().getUserAgent() != null)
			request.addHeader("User-Agent", dt.getTaskInfo().getUserAgent());
		if(dt.getTaskInfo().getSourceUrl()!=null)
				request.addHeader("Referer",dt.getTaskInfo().getSourceUrl());
		request.addHeader("Range","bytes="+di.getCurrent()+"-"+di.getEnd());
		request.url(dt.getTaskInfo().getTaskurl());
		try
		{
			response = dt.getOkHttpCliebt().newCall(request.build()).execute();
			is=response.body().byteStream();
			byte[] b=new byte[Integer.parseInt(dt.getContext().getResources().getTextArray(R.array.buffer)[dt.getSharedPreferences().getInt(Download.Setting.BUFFER,Download.Setting.BUFFER_DEFAULT)].toString())];
			raf=new RandomAccessFile(new File(dt.getTaskInfo().getDir(),dt.getTaskInfo().getTaskname()),"rw");
			int len;
			switch(response.code()){
				case 200:
					raf.seek(di.getCurrent());
					is.skip(di.getCurrent());
					break;
				case 206:
					raf.seek(di.getCurrent());
					break;
				default:
				raf.close();
				is.close();
				new IOException();
				break;
			}
			
			while((len=is.read(b))!=-1){
				raf.write(b,0,len);
					di.setCurrent(di.getCurrent()+len);
					dt.getDownload().updateDownloadInfo(di);
					EventBus.getDefault().post(dt.getTaskInfo());
				if(pause==true)return;
			}
		}
		catch (IOException e)
		{
			if(!pause)
			dt.itemFinish(this);
			
			return;
		}finally{
			try
			{
				if(raf!=null)
				raf.close();
			}
			catch (IOException e)
			{}
			try
			{
				if(is!=null)
					is.close();
			}
			catch (IOException e1)
			{}
			if(response!=null)
				response.close();
		}
		}
		success=true;
		dt.itemFinish(this);
	}
	public void pause(){
		pause=true;
		if(!success){
			try
			{
				if (raf != null)
					raf.close();
			}
			catch (IOException e)
			{}
			
		}
	}
}
