package com.moe.fragment.block;
import com.moe.fragment.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import com.moe.Mbrowser.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;
import com.moe.adapter.UrlBlockAdapter;
import java.util.List;
import java.util.ArrayList;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.text.TextUtils;
import com.moe.utils.DialogUtils;
import com.moe.database.UrlBlockDatabase;
import com.moe.database.Sqlite;
import android.widget.Toast;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import android.content.Intent;
import java.io.File;
import com.moe.database.Download;
import java.util.Calendar;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.Activity;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.moe.utils.UrlBlock;

public class UrlBlockFragment extends PreferenceFragment implements View.OnClickListener,DialogInterface.OnClickListener,UrlBlockAdapter.OnItemClickListener
{
private UrlBlockAdapter uba;
private List<String> list;
	private AlertDialog add;
	private TextInputLayout til;
	private EditText msg;
	private String data;
	private UrlBlockDatabase ubd;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.urlblock_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		RecyclerView rv=(RecyclerView)view.findViewById(R.id.urlblock_view_list);
		view.findViewById(R.id.urlblock_view_add).setOnClickListener(this);
		view.findViewById(R.id.urlblock_view_recycler).setOnClickListener(this);
		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(uba=new UrlBlockAdapter(list=new ArrayList<>()));
		uba.setOnItemClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		ubd=Sqlite.getInstance(getContext(),UrlBlockDatabase.class);
		super.onActivityCreated(savedInstanceState);
		list.addAll(ubd.query());
		uba.notifyDataSetChanged();
	}
	
	private void createAdd()
	{
		til = new TextInputLayout(getContext());
		msg = new EditText(getContext());
		msg.setSingleLine(true);
		til.addView(msg);
		til.setErrorEnabled(true);
		add = new AlertDialog.Builder(getActivity()).setView(til)
			.setNeutralButton("删除", this)
			.setNegativeButton("确定", this)
			.setPositiveButton("取消", this).create();

	}
	private int error=0;
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data)
	{
		if(requestCode==666){
			if(resultCode==Activity.RESULT_OK){
				new Thread(){
					public void run(){
						BufferedReader br = null;
						error=0;
						try
						{
							br = new BufferedReader(new InputStreamReader(getContext().getContentResolver().openInputStream(data.getData())));
							String line=null;
							while ((line = br.readLine()) != null)
							{
								if (TextUtils.isEmpty(line))continue;
								try
								{
									Pattern.compile(line);
									ubd.insert(line);
								}
								catch (PatternSyntaxException e)
								{error++;}
							}
						}
						catch (IOException e)
						{}
						finally{
							try
							{
								if (br != null)br.close();
							}
							catch (IOException e)
							{}
						}
						getView().post(new Runnable(){

								@Override
								public void run()
								{
									list.clear();
									list.addAll(ubd.query());
									uba.notifyDataSetChanged();
									UrlBlock.getInstance(getContext(),list);
									if(error>0)
										Toast.makeText(getActivity(),"已忽略"+error+"条错误规则",Toast.LENGTH_SHORT).show();
										else
										Toast.makeText(getActivity(),"导入成功，无错误",Toast.LENGTH_SHORT).show();
									
								}
							});
					}
				}.start();
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.urlblock_view_add:
				show(null);
				break;
			case R.id.urlblock_view_recycler:
				if(list.size()>0){
					new AlertDialog.Builder(getActivity()).setMessage("确认清空数据？")
						.setNegativeButton("确定", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								list.clear();
								uba.notifyDataSetChanged();
								ubd.clear();
								UrlBlock.getInstance(getContext()).clear();
							}
						})
					.setPositiveButton("取消",null).show();
				}
				break;
		}
	}
	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		switch (p2)
		{
			case AlertDialog.BUTTON_NEUTRAL:
				if(data!=null){
					int index=list.indexOf(data);
					ubd.delete(list.remove(index));
					uba.notifyItemRemoved(index+2);
					DialogUtils.changeState(add,true);
					UrlBlock.getInstance(getContext()).delete(data);
				}
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				boolean flag=true;
				String dd=msg.getText().toString().trim();
				if (TextUtils.isEmpty(dd))
				{
					flag=false;
					til.setError("内容不能为空");
				}
				try{
					Pattern.compile(dd);
				}catch(PatternSyntaxException e){
					til.setError("语法不符合规范：第"+e.getIndex()+"位错误");
					flag=false;
				}
				if(flag){
					if(data==null){
						if(ubd.insert(dd)){
							list.add(dd);
							uba.notifyItemInserted(list.size()+1);
							UrlBlock.getInstance(getContext()).insert(dd);
						}else{
							Toast.makeText(getActivity(),"存在相同规则",Toast.LENGTH_SHORT).show();
						}
						
					}else{
						ubd.update(data,dd);
						int index=list.indexOf(data);
						list.remove(index);
						list.add(index,dd);
						uba.notifyItemChanged(index+2);
						UrlBlock.getInstance(getContext()).delete(data);
						UrlBlock.getInstance(getContext()).insert(dd);
					}
					DialogUtils.changeState(add,true);
					}
					
				break;
			case AlertDialog.BUTTON_POSITIVE:
				DialogUtils.changeState(add,true);
				break;
		}
	}

	@Override
	public void onItemClick(int pos)
	{
		if(pos<2){
			if(pos==1){
				Calendar c=Calendar.getInstance();
				File dir=new File(Download.Setting.DIR_DEFAULT+"/adblock");
				if(dir.isFile())
					dir.delete();
				if(!dir.exists())
					dir.mkdirs();
				File file=new File(dir,"广告拦截规则"+(c.get(c.MONTH)+1)+"."+c.get(c.DAY_OF_MONTH)+"-"+c.get(c.HOUR_OF_DAY)+":"+c.get(c.MINUTE)+".ini");
				try
				{
					FileOutputStream fos=new FileOutputStream(file);
					for(String s:list){
						fos.write(s.getBytes());
						fos.write("\r\n".getBytes());
					}
					fos.flush();
					fos.close();
					Toast.makeText(getActivity(),file.getAbsolutePath(),Toast.LENGTH_LONG).show();
					
				}
				catch (IOException e)
				{
					Toast.makeText(getActivity(),"导出失败",Toast.LENGTH_SHORT).show();
				}
			}
			else{
				Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("text/*");
				try{getActivity().startActivityForResult(intent,666);}catch(Exception e){
					Toast.makeText(getActivity(),"无可用程序",Toast.LENGTH_SHORT).show();
				}
			}
		}else{
			show(list.get(pos-2));
		}
	}

	
private void show(String data){
	this.data=data;
	if(add==null)createAdd();
	if(data==null){
		add.setTitle("新增规则");
		msg.setText(null);
		}
		else{
		add.setTitle("编辑条目");
		msg.setText(data);
		}
	add.show();
	til.setError(null);
	DialogUtils.changeState(add,false);
	
}
	@Override
	public boolean onBackPressed()
	{
		if(!isHidden()){
			getFragmentManager().beginTransaction().hide(this).commit();
			return true;
			}
		return false;
	}
	
}
