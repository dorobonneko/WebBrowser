package com.moe.utils;
import java.util.List;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStreamReader;
import android.os.storage.StorageManager;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.content.Context;

public class StorageHelper
{
	public static List<String> getAllPath(Context context)
	{
		ArrayList<String> index=new ArrayList<>();
			StorageManager storageManager = (StorageManager) context.getSystemService(Context
																							.STORAGE_SERVICE);
			try {
				Class<?>[] paramClasses = {};
				Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
				getVolumePathsMethod.setAccessible(true);
				String[] invoke = (String[])getVolumePathsMethod.invoke(storageManager, new Object[]{});
				for(String s:invoke){
					index.add(s);
				}
			} catch (Exception e1) {
				try
				{
					Process p=Runtime.getRuntime().exec("sh");
					OutputStream os=p.getOutputStream();
					os.write("mount|grep sdcardfs\n echo end\nexit".getBytes());
					os.flush();
					BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
					do{
						String s=br.readLine();
						if (s.contains("end"))
							break;
						String[] line=s.split(" ");
						index.add(line[1]);
					}while(true);
					br.close();
					os.close();
					p.destroy();
				}
				catch (IOException e)
				{}
			} 
		return index;
	}
}
