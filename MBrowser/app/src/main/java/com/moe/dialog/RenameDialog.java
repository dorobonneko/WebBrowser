package com.moe.dialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AlertDialog;
import com.moe.utils.DialogUtils;
import com.moe.entity.TaskInfo;
import android.content.DialogInterface;
import android.text.TextUtils;
import com.moe.database.Sqlite;
import com.moe.database.Download;
import java.io.File;
public class RenameDialog implements AlertDialog.OnClickListener
{
	private AlertDialog ad;
	private TextInputLayout til;
	private EditText msg;
	private TaskInfo ti;
	private Callback call;
	private int position;
	public RenameDialog(Context context){
		til = new TextInputLayout(context);
		msg = new EditText(context);
		til.addView(msg);
		til.setErrorEnabled(true);
		ad=new AlertDialog.Builder(context).setView(til)
			.setTitle("重命名")
			.setPositiveButton("取消",this)
			.setNegativeButton("确定",this).create();
		til.setErrorEnabled(true);
	}
	public void show(TaskInfo o,Callback call,int position){
		this.ti=o;
		this.call=call;
		this.position=position;
		ad.show();
		DialogUtils.changeState(ad,false);
		til.setError(null);
		msg.setText(o.getTaskname());
	}

	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		switch(p2){
			case AlertDialog.BUTTON_NEGATIVE:
				String name=this.msg.getText().toString().trim();
				if(TextUtils.isEmpty(name)){
					msg.setError("名称不能为空");
				}else{
					new File(ti.getDir(),ti.getTaskname()).renameTo(new File(ti.getDir(),msg.getText().toString()));
					ti.setTaskname(msg.getText().toString().trim());
					Sqlite.getInstance(ad.getContext(),Download.class).renameTask(ti.getId(),ti.getTaskname());
					DialogUtils.changeState(ad,true);
					call.success(position);
					p1.dismiss();
				}
				break;
			case AlertDialog.BUTTON_POSITIVE:
				DialogUtils.changeState(ad,true);
				p1.dismiss();
				break;
		}

	}

public abstract interface Callback{
	void success(int position);
}
	
}
