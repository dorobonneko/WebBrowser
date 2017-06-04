package com.moe.dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.view.View;
import com.moe.database.BookMarks;
import com.moe.database.Sqlite;

public class AddFolderDialog extends Dialog implements View.OnClickListener
{
	private EditText et;
	private TextInputLayout til;
	private BookMarks bm;
	private String dir=null;
	public AddFolderDialog(Context context){
		super(context);
		bm=Sqlite.getInstance(context,BookMarks.class);
	}

	public void show(Object get)
	{
		dir=get.toString();
		super.show();
		til.setErrorEnabled(false);
		et.setText(dir);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setTitle("新建文件夹");
		setContentView(LayoutInflater.from(getContext()).inflate(R.layout.add_folder_view,null));
		findViewById(R.id.addfolderview_sure).setOnClickListener(this);
		findViewById(R.id.addfolderview_cancel).setOnClickListener(this);
		et=(EditText)findViewById(R.id.addfolderview_name);
		til=(TextInputLayout)findViewById(R.id.addfolderview_error);
		til.setCounterEnabled(true);
		til.setCounterMaxLength(10);
		til.setErrorEnabled(true);
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.addfolderview_sure:
				String str=et.getText().toString().trim();
				if(str.length()==0){
					til.setError("内容不能为空");
					}else if(str.length()<=10){
						if(dir==null)
						bm.createFolder(str);
						else
						bm.changeFolder(str,dir);
						if(osl!=null)osl.OnSuccess(str);
						dismiss();
					}
				break;
			case R.id.addfolderview_cancel:
				dismiss();
				break;
		}
	}

	@Override
	public void show()
	{
		dir=null;
		super.show();
		til.setError(null);
		et.setText("");
	}
public void setOnSuccessListener(OnSuccessListener o){
	osl=o;
}
private OnSuccessListener osl;
	public abstract interface OnSuccessListener{
		void OnSuccess(String name);
	}
}
