package com.moe.dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class AlertDialog extends Dialog implements View.OnClickListener
{
	private Button b1,b2;
	private TextView msg;
	private String m;
	public AlertDialog(Context context){
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(LayoutInflater.from(getContext()).inflate(R.layout.alertdialog_view,null));
		msg=(TextView)findViewById(R.id.alertdialogview_message);
		if(m!=null)msg.setText(m);
		b1=(Button)findViewById(R.id.alertdialogview_sure);
		b2=(Button)findViewById(R.id.alertdialogview_cancel);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		b1.setId(0);b2.setId(1);
	}
public void setMessage(String msg){
	if(this.msg!=null)
	this.msg.setText(msg);
	else this.m=msg;
}
	@Override
	public void onClick(View p1)
	{
		if(ocl!=null)ocl.onClick(p1);
		dismiss();
	}

	
	public void setOnClickListener(OnClickListener o){
		ocl=o;
	}
	private OnClickListener ocl;
	public abstract interface OnClickListener{
		void onClick(View v);
	}
}
