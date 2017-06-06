package com.moe.m3u;
import com.moe.m3u.tag.M3uTag;
import java.io.BufferedReader;
import com.moe.m3u.tag.M3uInfTag;
import java.net.URI;
import java.io.IOException;
import android.text.TextUtils;
import com.moe.m3u.tag.M3uXStreamInfTag;
import com.moe.m3u.tag.M3uXAllowCacheTag;
import com.moe.m3u.tag.M3uXEndListTag;
import com.moe.m3u.tag.M3uXDiscontinuityTag;
import com.moe.m3u.tag.M3uXKeyTag;
import java.net.URL;
import java.io.InputStream;
import com.moe.m3u.tag.M3uXMediaSequenceTag;
import com.moe.m3u.tag.M3uXProgramDataTimeTag;
import java.util.Calendar;
import java.util.Date;
import com.moe.m3u.tag.M3uXTagGetDurationTag;
import com.moe.m3u.tag.M3uXVersionTag;
import com.moe.m3u.tag.M3uXMediaTag;
class TagParser
{
	public static M3uTag parse(String tag,URI uri,BufferedReader br) throws IOException{
		if(tag.startsWith("#EXTINF")||tag.startsWith("#EXT-X-STREAM-INF"))
			return dataTag(tag,uri,br);
			else
			return tag(tag);
	}
	private static M3uTag dataTag(String tag,URI uri,BufferedReader br) throws IOException{
		String url=br.readLine();
		if(url.startsWith("/"))
			url=uri.getScheme()+"://"+uri.getHost()+url;
		
		int index=tag.indexOf(":");
		if(index==-1)index=tag.length();
		String value=null;
		String[] item=null;
		try{
			value=tag.substring(index+1).trim();
			item=value.split(",");}catch(Exception e){}
		switch(tag.substring(0,index)){
			case "#EXTINF":
				M3uInfTag mit=new M3uInfTag();
				for(int i=0;i<item.length;i++){
					if(TextUtils.isEmpty(item[i]))continue;
					switch(i){
						case 0:
							mit.setDuration(Double.parseDouble(item[i]));
							break;
						case 1:
							mit.setTitle(item[i]);
							break;
					}
				}
				mit.setUrl(url);
				return mit;
			case "#EXT-X-STREAM-INF":
				M3uXStreamInfTag mxsi=new M3uXStreamInfTag();
				for(int i=0;i<item.length;i++){
					if(TextUtils.isEmpty(item[i]))continue;
					String[] son=item[i].split("=");
					switch(son[0]){
						case "PROGRAM-ID":
							mxsi.setId(Integer.parseInt(son[1]));
							break;
						case "BANDWIDTH":
							mxsi.setBandwidth(Integer.parseInt(son[1]));
							break;
						case "CODECS":
							mxsi.setCodecs(son[1]);
							break;
						case "RESOLUTION":
							String[] ss=son[1].trim().split("x");
							mxsi.setWidth(Integer.parseInt(ss[0]));
							mxsi.setHeight(Integer.parseInt(ss[1]));
							break;
					}
				}
				mxsi.setUrl(url);
				return mxsi;
		}
		return null;
	}
	private static M3uTag tag(String tag) throws IOException{
		int index=tag.indexOf(":");
		if(index==-1)index=tag.length();
		String value=null;
		String[] item=null;
		try{
		value=tag.substring(index+1).trim();
		item=value.split(",");}catch(Exception e){}
		switch(tag.substring(0,index)){
			case "#EXT-X-ALLOW-CACHE":
				M3uXAllowCacheTag mxact=new M3uXAllowCacheTag();
				mxact.setAllow(value.equals("YES"));
				return mxact;
			case "#EXT-X-ENDLIST":
				return new M3uXEndListTag();
			case "#EXT-X-DISCONTINUITY":
				return new M3uXDiscontinuityTag();
			case "EXT-X-KEY":
				M3uXKeyTag mxkt=new M3uXKeyTag();
				for(int i=0;i<item.length;i++){
					String[] son=item[i].split("=");
					switch(son[0]){
						case "METHOD":
							switch(son[1]){
								case "NONE":
									mxkt.setMethod(M3uXKeyTag.Method.NONE);
									break;
								case "AES-128":
									mxkt.setMethod(M3uXKeyTag.Method.AES128);
									break;
							}
							break;
						case "URI":
							mxkt.setUri(son[1].replaceAll("\"",""));
							byte[] b=new byte[16];
							InputStream is=new URL(mxkt.getUri()).openStream();
							is.read(b,0,16);
							is.close();
							mxkt.setKey(new String(b));
						break;
					}
				}
				return mxkt;
			case "#EXT-X-MEDIA-SEQUENCE":
				M3uXMediaSequenceTag mxmst=new M3uXMediaSequenceTag();
				mxmst.setSequence(Long.parseLong(value));
				return mxmst;
			case "#EXT-X-MEDIA":
				M3uXMediaTag mxmt=new M3uXMediaTag();
				for(int i=0;i<item.length;i++){
					String[] son=item[i].split("=");
					switch(son[0]){
						case "URI":
							mxmt.setUri(son[1].replace("\"",""));
							break;
						case "TYPE":
							mxmt.setType(son[1].equals("AUDIO")?M3uXMediaTag.Type.AUDIO:M3uXMediaTag.Type.VIDEO);
							break;
						case "GROUP-ID":
							mxmt.setGroupId(Long.parseLong(son[1]));
							break;
						case "LANGUAGE":
							mxmt.setLanguage(son[1]);
							break;
						case "NAME":
							mxmt.setName(son[1]);
							break;
						case "DEFAULT":
							mxmt.setDefault(son[1].equals("YES"));
							break;
						case "AUTOSELECT":
							mxmt.setAutuSelected(son[1].equals("YES"));
							break;
					}
				}
				return mxmt;
			case "#EXT-X-PROGRAM-DATA-TIME":
				M3uXProgramDataTimeTag mxpdtt=new M3uXProgramDataTimeTag();
				Calendar c=Calendar.getInstance();
				c.setTime(new Date(value));
				mxpdtt.setDate(c);
				return mxpdtt;
			case "#EXT-X-TAGGETDURATION":
				M3uXTagGetDurationTag mxtgdt=new M3uXTagGetDurationTag();
				mxtgdt.setTime(Long.parseLong(value));
				return mxtgdt;
			case "#EXT-X-VERSION":
				M3uXVersionTag mxvt=new M3uXVersionTag();
				mxvt.setVersion(Integer.parseInt(value));
				return mxvt;
			case "#EXT-X-PLAYLIST-TYPE":
				return null;
			case "#EXT-X-BYTERANGE":
				return null;
			
		}
		return null;
	}
}
