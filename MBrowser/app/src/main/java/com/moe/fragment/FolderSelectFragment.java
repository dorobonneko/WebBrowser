package com.moe.fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import com.moe.Mbrowser.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.adapter.FolderSelectAdapter;
import com.moe.entity.Bookmark;
import java.util.List;
import java.util.ArrayList;
import com.moe.database.BookMarks;
import com.moe.database.Sqlite;
import android.support.v7.widget.RecyclerView.Adapter;
import com.moe.utils.CustomDecoration;

public class FolderSelectFragment extends Fragment implements FolderSelectAdapter.OnItemClickListener
{

	private FolderSelectAdapter fsa;
	private List<Bookmark> lb;
	private BookMarks bm;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.selectfolder_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		RecyclerView rv=(RecyclerView)view.findViewById(R.id.selectfolder_view_list);
		rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
		rv.setAdapter(fsa=new FolderSelectAdapter(lb=new ArrayList<>()));
		rv.addItemDecoration(new CustomDecoration(1));
		fsa.setOnItemClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		bm=Sqlite.getInstance(getContext(),BookMarks.class);
		super.onActivityCreated(savedInstanceState);
		lb.addAll(bm.loop(bm.getRoot()));
		fsa.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(RecyclerView.Adapter ra, int pos)
	{
		((BookmarkEditFragment)getParentFragment()).setArguments(lb.get(pos));
		getFragmentManager().beginTransaction().setCustomAnimations(0,R.anim.right_out).hide(this).commit();
	}


	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if(!hidden){
			lb.clear();
			lb.addAll(bm.loop(bm.getRoot()));
			fsa.notifyDataSetChanged();
		}
		super.onHiddenChanged(hidden);
	}
	
	@Override
	public boolean onBackPressed()
	{
		if(!isHidden())
		{
			getFragmentManager().beginTransaction().setCustomAnimations(0,R.anim.right_out).hide(this).commit();
		return true;
		}
		return false;
	}

	
}
