package com.moe.Mbrowser;
import android.app.Activity;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import android.widget.ListView;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import android.widget.AdapterView;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import com.moe.adapter.FolderAdapter;
import java.io.InputStreamReader;
import android.os.storage.StorageManager;
import com.moe.utils.StorageHelper;
public class FileExplorer extends Activity implements View.OnClickListener,ListView.OnItemClickListener{
private ArrayList<String> list=new ArrayList<>();
	private HashMap<String,File> folder=new HashMap<>();
	private ListView lv;
	private ViewGroup.LayoutParams vl=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
	private LinearLayout.LayoutParams ll=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT,1.0f);
	private FolderAdapter fa;
	private File currentFile;
	private ArrayList<String> index=new ArrayList<>();
	private boolean isindex=false;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		lv=new ListView(this);
		LinearLayout l=new LinearLayout(this);
		l.setFitsSystemWindows(true);
		l.setOrientation(LinearLayout.VERTICAL);
		l.addView(lv,ll);
		LinearLayout l1=new LinearLayout(this);
		l1.setOrientation(LinearLayout.HORIZONTAL);
		l.addView(l1,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
		Button b1=new Button(this);
		Button b2=new Button(this);
		l1.addView(b1,ll);
		l1.addView(b2,ll);
		b1.setText("取消");
		b2.setText("确定");
		setContentView(l,vl);
		fa=new FolderAdapter(this,list);
		lv.setAdapter(fa);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		lv.setOnItemClickListener(this);
		b1.setId(464978);
		b2.setId(467676);
		index.addAll(StorageHelper.getAllPath(this));
		String s=getIntent().getStringExtra("path");
		if(s!=null)
			Folder(new File(s));
			else
		openIndex();
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case 464978:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case 467676:
				if(currentFile!=null&&currentFile.canWrite()){
					Intent intent=new Intent();
					intent.putExtra("dir",currentFile.getPath());
					setResult(RESULT_OK,intent);
					finish();
				}else
				Toast.makeText(this,"当前目录禁止访问，请另选目录！",Toast.LENGTH_LONG).show();
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		if(list.get(p3).equals("返回首页")){
			openIndex();
			return;
		}else if(list.get(p3).equals("..")){
			for(String f:index){
				if(f.equals(currentFile.getAbsolutePath())){
					openIndex();
					return;}
			}
		}
        Folder(folder.get(list.get(p3)));
	
	}

	@Override
	public void onBackPressed()
	{
		if(isindex)
			finish();
		if(currentFile!=null){
		for(String f:index){
			if(f.equals(currentFile.getAbsolutePath())){
				openIndex();
				return;}
		}
			Folder(currentFile.getParentFile());
			return;
		}
	}
	
private void openIndex(){
		folder.clear();
		list.clear();
		for(String f:index){
			folder.put(new File(f).getName(),new File(f));
			list.add(new File(f).getName());
		}
		isindex=true;
		currentFile=null;
		fa.notifyDataSetChanged();
}
//private void index(){
//	try
//	{
//		Process p=Runtime.getRuntime().exec("sh");
//		OutputStream os=p.getOutputStream();
//		os.write("mount|grep sdcardfs\n echo end\nexit".getBytes());
//		os.flush();
//		BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
//		do{
//			String s=br.readLine();
//			if(s.contains("end"))
//				break;
//			String[] line=s.split(" ");
//			index.add(new File(line[1]));
//			}while(true);
//		br.close();
//		os.close();
//		p.destroy();
//	}
//	catch (IOException e)
//	{}
//}
	
private void loadFolder(){
	list.clear();
	list.addAll(folder.keySet());
	if(list.size()>1)
		Collections.sort(list, new Comparator<String>(){

				@Override
				public int compare(String p1, String p2)
				{
					// TODO: Implement this method
					return p1.compareTo(p2);
				}

		});
	folder.put("返回首页",new File("/sdcard"));
	folder.put("..",currentFile.getParentFile());
	list.add(0,"..");
	list.add(0,"返回首页");
	isindex=false;
	fa.notifyDataSetChanged();
		}
	
private void Folder(File f){
	if(!f.exists())f.mkdirs();
	if(f.isDirectory()&&f.canRead()){
		currentFile=f;
		folder.clear();
		File[] ff=f.listFiles();
		for(File temp:ff){
			if(temp.isDirectory()){
				folder.put(temp.getName(),temp);
			}
		}
        loadFolder();
	}
   
   }
}

