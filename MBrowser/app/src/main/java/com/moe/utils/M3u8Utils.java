package com.moe.utils;
import java.util.List;
import java.util.ArrayList;
import android.text.TextUtils;

public class M3u8Utils
{
	public static List<String> pauser(String data){
		ArrayList<String> as=new ArrayList<>();
		for(String str:data.split("\n")){
			String line=str.trim();
			if(line.startsWith("#")||TextUtils.isEmpty(str))continue;
			as.add(line);
		}
		return as;
	}
}
