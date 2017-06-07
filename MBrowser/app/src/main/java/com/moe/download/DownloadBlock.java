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
import android.text.TextUtils;
import okhttp3.ResponseBody;
import java.util.zip.GZIPInputStream;

public class DownloadBlock extends Thread
{
	private DownloadTask dt;
	private DownloadInfo di;
	private Response response;
	private RandomAccessFile raf;
	private InputStream is;
	private boolean pause=false;
	private int errorsize=0;
	public DownloadBlock(DownloadTask dt, DownloadInfo di)
	{
		this.dt = dt;
		this.di = di;
		setPriority(Thread.MIN_PRIORITY);
	}

	public void setErrorsize(int errorsize)
	{
		this.errorsize = errorsize;
	}

	public int getErrorsize()
	{
		return errorsize;
	}
	public boolean isSuccess()
	{
		return di.isSuccess();
	}
	@Override
	public void run()
	{
		if (!isSuccess())
		{
			Request.Builder request=new Request.Builder();
			if (!TextUtils.isEmpty(dt.getTaskInfo().getCookie()))
				request.addHeader("Cookie", dt.getTaskInfo().getCookie());
			if (!TextUtils.isEmpty(dt.getTaskInfo().getUserAgent()))
				request.addHeader("User-Agent", dt.getTaskInfo().getUserAgent());
			//if(ti.getSourceUrl()!=null)
			if(!dt.getTaskInfo().isForbidden())
			request.addHeader("Referer",dt.getTaskInfo().getTaskurl());
			request.addHeader("Accept", "*/*");
			request.addHeader("Connection", "Keep-Alive");
			//request.addHeader("Icy-MetaData", "1");
			request.addHeader("Accept-Encoding","gzip");
			request.addHeader("Range", "bytes=" + di.getCurrent() + "-" + (di.getEnd() < 1 ?"": di.getEnd()));
			if(!dt.getTaskInfo().getM3u8())di.setUrl(dt.getTaskInfo().getTaskurl());
			try
			{
				response = dt.getOkHttpCliebt().newCall(request.url(di.getUrl()).build()).execute();
				ResponseBody rb=response.body();
				is = rb.byteStream();
				if("gzip".equalsIgnoreCase(response.header("Content-Encoding")))
					is=new GZIPInputStream(is);
				if (di.getEnd() == 0)
				{
					long length=0;
					try
					{
						length = Long.parseLong(response.header("Content-Length"));
					}
					catch (Exception e)
					{length = rb.contentLength();}
					di.setEnd(length);
				}
				dt.getDownload().updateDownloadInfoWithData(di);
				byte[] b=new byte[Integer.parseInt(dt.getContext().getResources().getTextArray(R.array.buffer)[dt.getSharedPreferences().getInt(Download.Setting.BUFFER, Download.Setting.BUFFER_DEFAULT)].toString())];
				File file=new File(dt.getTaskInfo().getDir(), dt.getTaskInfo().getTaskname());
				if (dt.getTaskInfo().getM3u8())
				{
					if (file.exists())file.delete();
					file.mkdirs();
					file = new File(file, di.getNo() + "");}
				raf = new RandomAccessFile(file, "rw");
				if (dt.getTaskInfo().getM3u8() && !file.exists())
					raf.setLength(di.getEnd());
				int len;
				switch (response.code())
				{
					case 200:
						raf.seek(di.getCurrent());
						is.skip(di.getCurrent());
						break;
					case 206:
						raf.seek(di.getCurrent());
						break;
					case 403://禁止访问
					if(dt.getTaskInfo().isForbidden())throw new IOException("资源禁止访问");
					dt.getTaskInfo().setForbidden(true);
					start();
					return;
					default:
						throw new IOException(response.code()+"错误");
				}

				while ((len = is.read(b)) != -1)
				{
					if (pause == true)throw new IOException("已停止");
					raf.write(b, 0, len);
					di.setCurrent(di.getCurrent() + len);
					dt.getDownload().updateDownloadInfo(di);
					}
				di.setSuccess(true);
				dt.getDownload().updateDownloadInfoWithData(di);
			}
			catch (Exception e)
			{
				if (!pause)
					dt.itemFinish(this);
				return;
			}
			finally
			{
				try
				{
					if (raf != null)
						raf.close();
				}
				catch (IOException e)
				{}
				try
				{
					if (is != null)
						is.close();
				}
				catch (IOException e1)
				{}
				if (response != null)
					response.close();
			}
		} 
		dt.itemFinish(this);
	}
	public void pause()
	{
		pause = true;
	}
}
