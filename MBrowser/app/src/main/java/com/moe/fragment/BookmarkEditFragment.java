package com.moe.fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import com.moe.Mbrowser.R;
import android.support.design.widget.TextInputLayout;
import com.moe.entity.Bookmark;
import android.widget.TextView;
import android.text.TextUtils;
import com.moe.database.BookMarks;
import com.moe.database.Sqlite;

public class BookmarkEditFragment extends Fragment implements View.OnClickListener
{
	private BookMarks bm;
	private Bookmark bookmark,parent,folder_;
	private TextInputLayout title_l,summary_l;
	private TextView bigTitle,title,summary,folder;
	private Fragment folderselect;
	public void setArguments(Bookmark get)
	{
		this.folder_=get;
		folder.setText("位置："+get.getTitle());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.bookmarkedit_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		view.findViewById(R.id.bookmarkedit_view_cancel).setOnClickListener(this);
		view.findViewById(R.id.bookmarkedit_view_sure).setOnClickListener(this);
		bigTitle = (TextView)view.findViewById(R.id.bookmarkedit_view_bigtitle);
		title_l = (TextInputLayout)view.findViewById(R.id.bookmarkedit_view_title);
		summary_l = (TextInputLayout)view.findViewById(R.id.bookmarkedit_view_summary);
		title = (TextView)title_l.getChildAt(0);
		summary = (TextView)summary_l.getChildAt(0);
		title_l.setErrorEnabled(true);
		summary_l.setErrorEnabled(true);
		folder=(TextView)view.findViewById(R.id.bookmarkedit_view_folder);
		folder.setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		bm = Sqlite.getInstance(getContext(), BookMarks.class);
		super.onActivityCreated(savedInstanceState);
		check();
	}
	private void check()
	{
		folder_=null;
		if (bookmark == null)
		{
			bigTitle.setText("新建书签");
			summary_l.setVisibility(View.VISIBLE);
			title.setText(null);
			summary.setText(null);
			folder.setVisibility(View.GONE);
		}
		else
		{
			folder.setVisibility(View.VISIBLE);
			title.setText(bookmark.getTitle());
			folder.setText("位置："+parent.getTitle());
			switch (bookmark.getType())
			{
				case 0:
					bigTitle.setText("编辑文件夹");
					summary_l.setVisibility(View.GONE);
					break;
				case 1:
					bigTitle.setText("编辑书签");
					summary_l.setVisibility(View.VISIBLE);
					summary.setText(bookmark.getSummary());
					break;
			}

		}
	}
	@Override
	public void onHiddenChanged(boolean hidden)
	{
		super.onHiddenChanged(hidden);
		if (!hidden)
			check();
	}


	public void setArguments(Bookmark parent, Bookmark args)
	{
		this.parent = parent;
		this.bookmark = args;
	}

	@Override
	public void onClick(View p1)
	{
		switch (p1.getId())
		{
			case R.id.bookmarkedit_view_sure:
				boolean flag=true;
				if (bookmark == null)
				{
					if (TextUtils.isEmpty(title.getText()))
					{
						title_l.setError("内容不能为空");
						flag = false;
					}
					if (TextUtils.isEmpty(summary.getText()))
					{
						summary_l.setError("内容不能为空");
						flag = false;
					}
					if (flag)
					{
						Bookmark b=new Bookmark();
						b.setParent(parent.getSon());
						b.setTitle(title.getText().toString());
						b.setSummary(summary.getText().toString());
						b.setType(BookMarks.Type.BOOKMARK);
						bm.insert(b);
						finish();
					}
				}
				else
				{
					switch (bookmark.getType())
					{
						case 0:
							if (TextUtils.isEmpty(title.getText()))
							{
								title_l.setError("内容不能为空");
								flag = false;
							}
							if(flag){
								Bookmark b=bookmark;
								if(folder_!=null)
								b.setParent(folder_.getSon());
								else
								b.setParent(parent.getSon());
								b.setTitle(title.getText().toString());
								bm.update(b);
								finish();
							}
							break;
						case 1:
							if (TextUtils.isEmpty(title.getText()))
							{
								title_l.setError("内容不能为空");
								flag = false;
							}
							if (TextUtils.isEmpty(summary.getText()))
							{
								summary_l.setError("内容不能为空");
								flag = false;
							}
							if (flag)
							{
								Bookmark b=bookmark;
								if(folder_!=null)
									b.setParent(folder_.getSon());
								else
								b.setParent(parent.getSon());
								b.setTitle(title.getText().toString());
								b.setSummary(summary.getText().toString());
								bm.update(b);
								finish();
							}
							break;
					}
				}
				break;
			case R.id.bookmarkedit_view_cancel:
				finish();
				break;
			case R.id.bookmarkedit_view_folder:
				if(folderselect==null)
					folderselect=new FolderSelectFragment();
					if(folderselect.isAdded())
						getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in,0).show(folderselect).commit();
						else
						getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in,0).add(R.id.bookmarkedit_view_float,folderselect).commit();
				break;
		}
	}


private void finish(){
	getFragmentManager().beginTransaction().setCustomAnimations(0, R.anim.right_out).hide(this).commit();
	((BookmarksFragment)getParentFragment()).refresh();
}
	@Override
	public boolean onBackPressed()
	{
		if(!isHidden()){
			if(folderselect!=null&&!folderselect.isHidden()&&folderselect.onBackPressed())
				;
				else{
				getFragmentManager().beginTransaction().setCustomAnimations(0, R.anim.right_out).hide(this).commit();
				}
				return true;
		}
		return false;
	}


}
