package com.moe.utils;
import java.util.regex.Pattern;

public class MediaUtils
{
	private static Pattern audio=Pattern.compile("(?i).*[\\.](mp3|m4a|aac|flac|mid|wav|wma|ogg|ape|acc|tta)([\\?].*|$)");
	private static Pattern video=Pattern.compile("(?i).*[\\.](mp4|rmvb|rm|avi|flv|3gp|mpeg|mpg|ts|mkv|mov)([\\?].*|$)");
	private static Pattern image=Pattern.compile("(?i).*[\\.](jpg|png|bmp|webp|gif|fav|ico|tga|mif|miff)([\\?].*|$)");
	private static Pattern css=Pattern.compile("(?i).*[\\.](css)([\\?].*|$)");
	private static Pattern js=Pattern.compile("(?i).*[\\.](js)([\\?].*|$)");
	
	public static boolean isFormat(String url,Type type){
		Pattern rule = null;
		switch(type){
			case AUDIO:
				rule=audio;
				break;
			case VIDEO:
				rule=video;
				break;
			case IMAGE:
				rule=image;
				break;
			case CSS:
				rule=css;
				break;
			case JS:
				rule=js;
				break;
		}
		if(rule.matcher(url).find())
			return true;
		return false;
	}
	public enum Type{
		AUDIO,VIDEO,IMAGE,CSS,JS;
	}
}
