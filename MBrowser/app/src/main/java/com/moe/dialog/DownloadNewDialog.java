package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import com.moe.entity.TaskInfo;
import com.moe.database.Download;
import com.moe.database.DataBase;
import com.moe.bean.DownloadItem;
import com.moe.database.Download.State;

public class DownloadNewDialog extends Dialog implements View.OnClickListener,Download.Callback
{
	private TextInputLayout t_name,t_url;
	private EditText name,url;
	private Button add,cancel;
	private Callback call;
	public DownloadNewDialog(Context context,Callback call){
		super(context,R.style.searchDialog);
		this.call=call;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_add_view);
		t_name=(TextInputLayout)findViewById(R.id.dialogaddview_sitename);
        t_url=(TextInputLayout)findViewById(R.id.dialogaddview_siteurl);
        name=(EditText)findViewById(R.id.dialogaddview_sitename_content);
        url=(EditText)findViewById(R.id.dialogaddview_siteurl_content);
        add=(Button)findViewById(R.id.dialogaddview_add);
		add.setOnClickListener(this);
        cancel=(Button)findViewById(R.id.dialogaddview_cancel);
		cancel.setOnClickListener(this);
		t_name.setHint("文件名称");
		t_url.setHint("下载地址");
		add.setText("添加");
		setTitle("新建任务");
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.dialogaddview_add:
				boolean flag = true;
				if(name.getText().toString().trim().isEmpty()){
					t_name.setError(null);
					flag=false;
				}else t_name.setErrorEnabled(false);
				if(url.getText().toString().trim().isEmpty()){
					t_url.setError(null);
					flag=false;
				}else t_url.setErrorEnabled(false);
				if(flag){
					TaskInfo ti=new TaskInfo();
					ti.setTaskname(name.getText().toString().trim());
					ti.setTaskurl(url.getText().toString().trim());
					ti.setDir(getContext().getSharedPreferences("download",0).getString(Download.Setting.DIR,Download.Setting.DIR_DEFAULT));
					DataBase.getInstance(getContext()).addTaskInfo(ti,this);
					dismiss();
				}
				break;
			case R.id.dialogaddview_cancel:
				dismiss();
				break;
		}
	}

	@Override
	public void callback(TaskInfo ti,Download.State state)
	{
		if(call!=null)call.Added(ti);
	}


	public static abstract interface Callback{
		void Added(TaskInfo ti);
	}
}
