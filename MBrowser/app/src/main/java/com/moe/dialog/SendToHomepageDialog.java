package com.moe.dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.view.View;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import com.moe.fragment.BookmarksFragment;
import com.moe.database.HomePage;
import com.moe.database.Sqlite;

public class SendToHomepageDialog extends Dialog implements View.OnClickListener
{
	private TextInputLayout til;
	private EditText et;
	private BookmarksFragment bf;
	private HomePage hp;
	private String[] data;
	public SendToHomepageDialog(BookmarksFragment context){
		super(context.getActivity());
		this.bf=context;
		hp=Sqlite.getInstance(context.getContext(),HomePage.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setTitle("名称编辑");
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
				if(et.getText().toString().isEmpty()){
					til.setError("名称不能为空");
					break;
				}
				if(et.getText().toString().length()>10)break;
				hp.insertItem(data[0],et.getText().toString());
				dismiss();
			break;
			case R.id.addfolderview_cancel:
				dismiss();
				break;
		}
	}

	public void show(String[] data)
	{
		super.show();
		this.data=data;
		et.setText(data[1]);
		til.setError(null);
	}

	
}
