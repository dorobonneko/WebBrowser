package com.moe.dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.view.View;
import android.content.DialogInterface;
import de.greenrobot.event.EventBus;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.text.TextUtils;
import com.moe.database.Sqlite;
import com.moe.database.JavaScript;
import java.lang.reflect.Field;
import com.moe.utils.DialogUtils;

public class JavaScriptAddDialog implements AlertDialog.OnDismissListener,DialogInterface.OnClickListener
{
	private AlertDialog ad;
	private TextInputLayout name_l;
	private EditText name,content;
	private Object[] obj;
	public JavaScriptAddDialog(Context context){
		View v=LayoutInflater.from(context).inflate(R.layout.javascript_add_view,null);
		ad=new AlertDialog.Builder(context).setView(v)
			.setTitle("脚本")
			.setPositiveButton("取消",this)
			.setNegativeButton("确定",this)
			.setNeutralButton("删除",this).create();
		ad.setOnDismissListener(this);
		ad.setCancelable(true);
		name_l=(TextInputLayout)v.findViewById(R.id.javascript_add_view_name_layout);
		name=(EditText)v.findViewById(R.id.javascript_add_view_name);
		content=(EditText)v.findViewById(R.id.javascript_add_view_content);
		name_l.setErrorEnabled(true);
		name_l.setHint("all表示对所有网站启用");
	}
	public void show(){
		show(null);
	}
	public void show(Object[] o){
		ad.show();
		DialogUtils.changeState(ad,false);
		obj=o;
		name_l.setError(null);
		if(o==null){
			name.setText(null);
			content.setText(null);
		}else{
			name.setText(o[1].toString());
			content.setText(o[2].toString().substring(11));
		}
	}
	@Override
	public void onDismiss(DialogInterface p1)
	{
		EventBus.getDefault().post(JavaScriptDialog.SHOW);
	}

	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		switch(p2){
			case AlertDialog.BUTTON_NEGATIVE:
				String name=this.name.getText().toString().trim();
				if(TextUtils.isEmpty(name)){
					name_l.setError("名称不能为空");
				}else{
					if(obj==null)
						Sqlite.getInstance(ad.getContext(),JavaScript.class).addScript(name,"javascript:"+content.getText().toString());
					else
						Sqlite.getInstance(ad.getContext(),JavaScript.class).updateScript((Integer)obj[0],name,"javascript:"+content.getText().toString());
					DialogUtils.changeState(ad,true);
						p1.dismiss();
				}
				break;
			case AlertDialog.BUTTON_NEUTRAL:
				if(obj!=null){
					Sqlite.getInstance(((AlertDialog)p1).getContext(),JavaScript.class).deleteScript((Integer)obj[0]);
					DialogUtils.changeState(ad,true);
					p1.dismiss();
				}
				break;
			case AlertDialog.BUTTON_POSITIVE:
				DialogUtils.changeState(ad,true);
				p1.dismiss();
				break;
		}
		
	}


	
}
