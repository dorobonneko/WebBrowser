package com.moe.dialog;
import android.widget.Spinner;
import android.support.design.widget.TextInputLayout;
import android.widget.Button;
import android.widget.EditText;
import com.moe.database.BookMarks;
import android.widget.ArrayAdapter;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import android.view.View.OnClickListener;
import android.view.View;
import com.moe.database.Sqlite;

public class BookmarkAddDialog extends Dialog implements OnClickListener
{
	private Spinner spinner;
	private TextInputLayout name_l,url_l;
	private EditText name,url;
	private Button sure,cancel;
	private BookMarks bm;
	private ArrayAdapter aa;
	private List list=new ArrayList();
	//private String currenturl;
	public BookmarkAddDialog(Context context){
		super(context);
		bm=Sqlite.getInstance(context,BookMarks.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("添加书签");
		setContentView(R.layout.bookmark_edit_dialog);
		spinner=(Spinner)findViewById(R.id.bookmark_edit_dialog_folder);
		name_l=(TextInputLayout)findViewById(R.id.dialogaddview_sitename);
		name=(EditText)findViewById(R.id.dialogaddview_sitename_content);
		url_l=(TextInputLayout)findViewById(R.id.dialogaddview_siteurl);
		url=(EditText)findViewById(R.id.dialogaddview_siteurl_content);
		sure=(Button)findViewById(R.id.dialogaddview_add);
		cancel=(Button)findViewById(R.id.dialogaddview_cancel);
		sure.setText("添加");
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
		name_l.setErrorEnabled(true);
		url_l.setErrorEnabled(true);
		spinner.setAdapter(aa=new ArrayAdapter(getContext(),android.R.layout.simple_spinner_dropdown_item,list));
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.dialogaddview_add:
				boolean flag=true;
				if(name.getText().toString().isEmpty()){
					name_l.setError("名称不能为空");
					flag=false;
				}
				if(url.getText().toString().isEmpty()){
					url_l.setError("网址不能为空");
					flag=false;
				}
				if(flag){
					bm.insertBookmark(url.getText().toString().trim(),name.getText().toString().trim(),list.get(spinner.getSelectedItemPosition()).toString());
					dismiss();
				}
				break;
			case R.id.dialogaddview_cancel:
				dismiss();
				break;
		}
	}


	public void show(String url,String title){
		super.show();
		name_l.setError(null);
		url_l.setError(null);
		name.setText(title);
		this.url.setText(url);
		list.clear();
		list.addAll(bm.getAllBookmarkGroup());
		aa.notifyDataSetChanged();
		spinner.setSelection(0);
	}

}
