package com.moe.dialog;
import android.content.Context;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import android.widget.Spinner;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import com.moe.database.BookMarks;
import com.moe.database.DataBase;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;
import com.moe.fragment.BookmarksFragment;

public class BookmarkEditDialog extends Dialog implements OnClickListener
{
	private Spinner spinner;
	private TextInputLayout name_l,url_l;
	private EditText name,url;
	private Button sure,cancel;
	private BookMarks bm;
	private ArrayAdapter aa;
	private List list=new ArrayList();
	private String currenturl;
	private BookmarksFragment bf;
	public BookmarkEditDialog(BookmarksFragment context){
		super(context.getActivity());
		this.bf=context;
		bm=DataBase.getInstance(context.getContext());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("编辑书签");
		setContentView(R.layout.bookmark_edit_dialog);
		spinner=(Spinner)findViewById(R.id.bookmark_edit_dialog_folder);
		name_l=(TextInputLayout)findViewById(R.id.dialogaddview_sitename);
		name=(EditText)findViewById(R.id.dialogaddview_sitename_content);
		url_l=(TextInputLayout)findViewById(R.id.dialogaddview_siteurl);
		url=(EditText)findViewById(R.id.dialogaddview_siteurl_content);
		sure=(Button)findViewById(R.id.dialogaddview_add);
		cancel=(Button)findViewById(R.id.dialogaddview_cancel);
		sure.setText("确认更改");
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
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
					url.setError("网址不能为空");
					flag=false;
				}
				if(flag){
					bm.updataBookmark(url.getText().toString(),name.getText().toString(),spinner.getSelectedItem().toString(),currenturl);
					bf.loadBookmarks();
					dismiss();
				}
				break;
			case R.id.dialogaddview_cancel:
				dismiss();
				break;
		}
	}

	
	public void show(String url){
		super.show();
		String[] data=bm.getBookmark(url);
		name_l.setErrorEnabled(false);
		url_l.setErrorEnabled(false);
		name.setText(data[1]);
		this.url.setText(data[0]);
		list.clear();
		list.addAll(bm.getAllBookmarkGroup());
		aa.notifyDataSetChanged();
		spinner.setSelection(list.indexOf(data[2]));
		currenturl=data[0];
	}
	
}
