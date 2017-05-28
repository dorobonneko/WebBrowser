package com.moe.utils;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.text.TextUtils;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class VideoBlock
{
	private static VideoBlock vb;
private ArrayList<Pattern> video=new ArrayList<>(); 
private VideoBlock(InputStream is){
		if(is!=null){
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			byte[] b=new byte[8192];
			int len;
			try
			{
				while ((len = is.read(b)) != -1)
				{
					baos.write(b, 0, len);
				}
				baos.flush();
				String[] str=baos.toString("utf-8").split(";");
				for(String head:str){
					if(TextUtils.isEmpty(head.trim()))continue;
					video.add(Pattern.compile(head.trim()));
					
				}
			}
			catch (IOException e)
			{}finally{
				try
				{
					baos.close();
					is.close();
				}
				catch (IOException e)
				{}
			}
		}
}

public boolean isExists(String url)
{
	for(Pattern s:video)
	while(s.matcher(url).find())return true;
	return false;
}
	
	public static VideoBlock getInstance(InputStream is){
		try
		{
			if (vb == null)
				vb = new VideoBlock(is);
			else
				is.close();
		}
		catch (IOException e)
		{}
		return vb;
	}
	
}
