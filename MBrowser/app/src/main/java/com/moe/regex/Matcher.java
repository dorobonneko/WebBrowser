package com.moe.regex;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Matcher
{
	private Pattern pattern;
	private Mode mode;
	private Matcher(String regex){
		if(regex.startsWith("[H]")){
			mode=Mode.HOST;
			regex=regex.substring(3);
			}else if(regex.startsWith("[P]")){
				mode=Mode.PATH;
				regex=regex.substring(3);
				}else mode=Mode.ALL;
			try{
			pattern=Pattern.compile(regex);
			}catch(PatternSyntaxException e){
				throw new PatternSyntaxException(null,regex,e.getIndex()+(mode==Mode.HOST||mode==Mode.PATH?3:0));
			}
	}
	public static Matcher compile(String regex){
		return new Matcher(regex);
	}
	public boolean matches(String data){
		while(pattern.matcher(data).find())
			return true;
			return false;
	}
	public Mode getMode(){
		return mode;
	}
	public enum Mode{
		HOST,PATH,ALL;
	}
}
