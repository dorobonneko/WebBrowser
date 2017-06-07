package com.moe.utils;
import android.net.Uri;
import android.util.Base64;

public class Convert
{

	public static String parse(String url)
	{
		switch(Uri.parse(url).getScheme()){
			case "flashget":
				return flashgetDecode(url);
			case "thunder":
				return thunderDecode(url);
			case "qqdl":
				return qqDecode(url);
				}
		return null;
	}
	public static String thunderDecode(String input){
	input=input.replace("thunder://","");
	input=new String(Base64.decode(input,Base64.DEFAULT)).trim();
	return input.substring(2,input.length()-2);
	}
	public static String thunderEncode(String input){
		StringBuffer sb=new StringBuffer();
		sb.append("thunder://").append(new String(Base64.encode(("AA"+input+"ZZ").getBytes(),Base64.NO_WRAP)).trim());
		return sb.toString();
	}
	public static String flashgetEncode(String input){
		StringBuffer sb=new StringBuffer();
		sb.append("flashget://").append(new String(Base64.encode(("[FLASHGET]"+input+"[FLASHGET]").getBytes(),Base64.NO_WRAP)).trim()).append("&moe");
		return sb.toString();
	}
	public static String flashgetDecode(String input){
		int i=input.indexOf("&");
		if(i!=-1)
			input=input.substring(0,i);
		input=input.replace("flashget://","");
		input=new String(Base64.decode(input,Base64.DEFAULT)).trim();
		return input.substring(10,input.length()-10);
	}
	public static String qqEncode(String input){
		return "qqdl://"+new String(Base64.encode(input.getBytes(),Base64.NO_WRAP)).trim();
	}
	public static String qqDecode(String input){
		input=input.replace("qqdl://","");
		return new String(Base64.decode(input,Base64.DEFAULT)).trim();
	}
    
    
}
