package com.moe.m3u;
import java.io.InputStream;
import java.net.URI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.moe.m3u.tag.M3uTag;
import java.util.List;
import com.moe.m3u.tag.M3uXStreamInfTag;
import com.moe.m3u.tag.M3uXEndListTag;
import java.nio.charset.Charset;
import android.text.TextUtils;

public class M3uParser
{
	private M3uList ml;
	private URI uri;
	private M3uParser(InputStream is,URI uri) throws IOException{
		this.uri=uri;
			final InputStreamReader isr=new InputStreamReader(is);
			final BufferedReader br=new BufferedReader(isr);
			final String head=br.readLine();
		if(!head.trim().equals("#EXTM3U"))throw new M3uFormatException("format is not m3u");
		String line=null;
		ml=new M3uList();
		List<M3uTag> lmt=ml.getList();
		while((line=br.readLine())!=null){
			if(TextUtils.isEmpty(line))continue;
			M3uTag mt=TagParser.parse(line.trim(),uri,br);
			if(mt==null)continue;
			if(mt instanceof M3uXStreamInfTag)
				ml.setType(M3uList.Type.MASTER);
			else if(mt instanceof M3uXEndListTag)
				ml.setLive(false);
				lmt.add(mt);
		}
		br.close();
		isr.close();
	}
	public static M3uParser parse(InputStream is) throws IOException{
		return parse(is,null);
	}
	public static M3uParser parse(File file) throws FileNotFoundException, IOException{
		return parse(new FileInputStream(file));
	}
	public static M3uParser parse(InputStream is,URI host) throws IOException{
		return new M3uParser(is,host);
	}
	public M3uList getList(){
		return ml;
	}
}
