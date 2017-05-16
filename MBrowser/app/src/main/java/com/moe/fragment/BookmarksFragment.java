package com.moe.fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import com.moe.Mbrowser.R;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import com.moe.adapter.ViewPagerAdapter;
import java.util.ArrayList;
import android.support.v7.widget.RecyclerView;
import com.moe.adapter.BookmarksAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.database.BookMarks;
import com.moe.database.WebHistory;
import com.moe.database.DataBase;
import com.moe.utils.CustomDecoration;
import android.widget.Button;
import com.moe.dialog.AddFolderDialog;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.RecyclerView.ViewHolder;
import com.moe.dialog.AlertDialog;
import de.greenrobot.event.EventBus;
import com.moe.bean.MenuOptions;
import com.moe.bean.WindowEvent;
import android.os.Vibrator;
import android.content.Context;
import com.moe.dialog.AddDialog;

public class BookmarksFragment extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener,AddFolderDialog.OnSuccessListener,AlertDialog.OnClickListener,BookmarksAdapter.OnItemClickListener,BookmarksAdapter.OnItemLongClickListener,AddDialog.OnAddListener
{
	private ViewPager vp;
	private ViewPagerAdapter vpa;
	private ArrayList<RecyclerView> av=new ArrayList<>();
	private ArrayList bookmark_data=new ArrayList(),history_data=new ArrayList();
	private BookmarksAdapter bookmark,history;
	private BookMarks bm;
	private WebHistory wh;
	private Button folder,edit;
	private AddFolderDialog afd;
	private boolean drag=false;
	private AlertDialog ad;
	private AddDialog aid;
	private Vibrator vibrator;
	private String dir;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view=inflater.inflate(R.layout.bookmarks_view, container, false);
		TabLayout tl=(TabLayout)view.findViewById(R.id.bookmarks_tablayout);
		vp = (ViewPager)view.findViewById(R.id.bookmarks_viewpager);
		vp.setAdapter(vpa = new ViewPagerAdapter(av));
		av.add(new RecyclerView(getActivity()));
		av.add(new RecyclerView(getActivity()));
		av.get(0).setTag("书签");
		av.get(1).setTag("历史");
		vp.setOffscreenPageLimit(1);
		vpa.notifyDataSetChanged();
		tl.setupWithViewPager(vp);
		tl.setElevation(2f);
		av.get(0).setAdapter(bookmark = new BookmarksAdapter(getActivity(), bookmark_data));
		bookmark.setMode(BookmarksAdapter.Mode.FOLDER);
		av.get(0).setItemAnimator(new DefaultItemAnimator());
		av.get(0).setLayoutManager(new LinearLayoutManager(getActivity()));
		av.get(0).addItemDecoration(new CustomDecoration(2));
		av.get(1).setAdapter(history = new BookmarksAdapter(getActivity(), history_data));
		history.setMode(BookmarksAdapter.Mode.HISTORY);
		av.get(1).setItemAnimator(new DefaultItemAnimator());
		av.get(1).setLayoutManager(new LinearLayoutManager(getActivity()));
		//av.get(1).addItemDecoration(new CustomDecoration(5, 0x00000000));
		folder = (Button)view.findViewById(R.id.bookmarksview_newfolder);
		edit = (Button)view.findViewById(R.id.bookmarksview_edit);
		vp.setOnPageChangeListener(this);
		folder.setOnClickListener(this);
		edit.setOnClickListener(this);
		ItemTouchHelper ith=new ItemTouchHelper(new ItemTouch());
		ith.attachToRecyclerView(av.get(0));
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		vibrator=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
		bm = DataBase.getInstance(getActivity());
		wh = DataBase.getInstance(getActivity());
		afd=new AddFolderDialog(getActivity());
		aid=new AddDialog(getActivity());
		afd.setOnSuccessListener(this);
		aid.setOnAddLostener(this);
		super.onActivityCreated(savedInstanceState);
		loadFolder();
		history_data.addAll(wh.getWebHistory());
		history.notifyDataSetChanged();
		ad=new AlertDialog(getActivity());
		ad.setTitle("是否清空历史记录？");
		ad.setOnClickListener(this);
		history.setOnItemClickListener(this);
		//history.setOnItemLongClickListener(this);
		bookmark.setOnItemClickListener(this);
		bookmark.setOnItemLongClickListener(this);
		aid.setTitle("添加书签");
		aid.setButtonText(0,"添加");
	}
	@Override
	public void onPageScrolled(int p1, float p2, int p3)
	{
		switch(p1){
			case 0:
				folder.setVisibility(View.VISIBLE);
				edit.setText("编辑");
				break;
			case 1:
				folder.setVisibility(View.INVISIBLE);
				edit.setText("清空");
				break;
		}
	}

	@Override
	public void onPageSelected(int p1)
	{
		
	}

	@Override
	public void onPageScrollStateChanged(int p1)
	{
		
	}

	@Override
	public void OnItemClick(BookmarksAdapter ba, com.moe.adapter.BookmarksAdapter.ViewHolder vh)
	{
		if(ba==history){
			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW,((String[])history_data.get(vh.getPosition()))[0]));
			EventBus.getDefault().post(MenuOptions.HOME);
		}else{
			if(drag){
				//编辑模式
			}else{
				if(bookmark.getMode()==BookmarksAdapter.Mode.FOLDER){
					loadBookmarks(bookmark_data.get(vh.getPosition()).toString());
				}else{
				EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW,((String[])bookmark_data.get(vh.getPosition()))[0]));
				EventBus.getDefault().post(MenuOptions.HOME);
				}
			}
		}
	}

	@Override
	public boolean OnItemLongClick(BookmarksAdapter ba, com.moe.adapter.BookmarksAdapter.ViewHolder vh)
	{
		return drag;
	}

	@Override
	public void onAdd(String url, String title, String dir)
	{
		bm.insertBookmark(url,title,dir);
		loadBookmarks(dir);
	}

	
private void loadFolder(){
	bookmark_data.clear();
	bookmark_data.addAll(bm.getAllBookmarkGroup());
	bookmark.setMode(BookmarksAdapter.Mode.FOLDER);
	bookmark.notifyDataSetChanged();
	folder.setText("新建文件夹");
}
private void loadBookmarks(String dir){
	this.dir=dir;
	bookmark_data.clear();
	bookmark_data.addAll(bm.queryBookmark(dir));
	bookmark.setMode(BookmarksAdapter.Mode.ITEM);
	bookmark.notifyDataSetChanged();
	folder.setText("新建书签");
}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.bookmarksview_newfolder:
				if(bookmark.getMode()==BookmarksAdapter.Mode.FOLDER)
				afd.show();
				else
				aid.show(dir);
				break;
			case R.id.bookmarksview_edit:
				switch(vp.getCurrentItem()){
					case 0:
						toggleMode(!drag);
						break;
					case 1:
						if(history_data.size()!=0)
						ad.show();
						break;
				}
				break;
			case 0:
				//清空历史记录
				wh.clearWebHistory();
				history_data.clear();
				history.notifyDataSetChanged();
				break;
		}
	}
	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if (hidden)vp.setCurrentItem(0);
		else
		{
			history_data.clear();
			history_data.addAll(wh.getWebHistory());
			history.notifyDataSetChanged();
		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void OnSuccess(String name)
	{
		loadFolder();
	}
	private void toggleMode(boolean f){
		if(f){
			drag=true;
			edit.setText("取消");
		}else{
			drag=false;
			edit.setText("编辑");
		}
	}

	@Override
	public boolean onBackPressed()
	{
		if(drag){
			toggleMode(false);
			return true;
		}else if(bookmark.getMode()==BookmarksAdapter.Mode.ITEM){
			loadFolder();
			return true;
		}
		return false;
	}
	public class ItemTouch extends ItemTouchHelper.Callback
	{

		@Override
		public boolean isLongPressDragEnabled()
		{
			vibrator.vibrate(50);
			return !drag;
		}
		
		@Override
		public int getMovementFlags(RecyclerView p1, RecyclerView.ViewHolder p2)
		{
			return makeMovementFlags(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0);
		}

		@Override
		public boolean onMove(RecyclerView p1, RecyclerView.ViewHolder p2, RecyclerView.ViewHolder p3)
		{
			if(bookmark.getMode()==BookmarksAdapter.Mode.FOLDER)
				bm.moveGroupToIndex(bookmark_data.get(p2.getPosition()).toString(),p3.getPosition());
				else
				bm.moveToIndex(((String[])bookmark_data.get(p2.getPosition()))[0],p3.getPosition());
			bookmark_data.add(p3.getPosition(),bookmark_data.remove(p2.getPosition()));
			bookmark.notifyItemMoved(p2.getPosition(),p3.getPosition());
							return false;
		}


		@Override
		public void onSwiped(RecyclerView.ViewHolder p1, int p2)
		{
			// TODO: Implement this method
		}
	}
}
