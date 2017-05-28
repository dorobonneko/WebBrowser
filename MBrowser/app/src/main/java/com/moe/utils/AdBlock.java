package com.moe.utils;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.net.MalformedURLException;
import android.os.PatternMatcher;
import android.util.Patterns;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.text.TextUtils;

public class AdBlock
{
	private HashMap<String,List<String>> data=new HashMap<String,List<String>>();
	private static AdBlock ab;
	private static AdBlock adbreak;
	private int mode=0;
	private AdBlock(int mode){this(mode,null);}
	private AdBlock(int mode,InputStream is){
		this.mode=mode;
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
					String host=head.substring(0,head.indexOf("{"));
					String[] body=head.substring(head.indexOf("{")+1,head.lastIndexOf("}")).split(",");
					for(String rule:body){
						if(TextUtils.isEmpty(rule.trim()))continue;
						add(host,rule.trim());
					}
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
	public static AdBlock getAdBlock(InputStream is){
		if(ab==null)
		ab=new AdBlock(0,is);
		return ab;
	}
	public static AdBlock getAdBreak(){
		if(adbreak==null)
			adbreak=new AdBlock(1);
		return adbreak;
	}
	public boolean add(String host,String rule){
		if(data.containsKey(host)){
			return data.get(host).add(rule);
		}else{
			final ArrayList<String> as=new ArrayList<>();
			as.add(rule);
			data.put(host,as);
			return true;
		}
	}
	public boolean isHostExists(String url){
		try
		{
			return data.containsKey(new URL(url).getHost());
		}
		catch (MalformedURLException e)
		{}
		return false;
	}
	public boolean isUrlExists(String host,String url){
		try
		{
			List<String> da=data.get(new URL(host).getHost());
			switch(mode){
				case 0:
					for(String str:da){
					if(url.indexOf(str)!=-1)
						return true;
					}
					return false;
			}
		}
		catch (MalformedURLException e)
		{}
		return false;
		
	}
}
