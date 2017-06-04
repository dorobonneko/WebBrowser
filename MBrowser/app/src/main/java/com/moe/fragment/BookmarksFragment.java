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
import com.moe.view.PopupBookmarkMenu;
import com.moe.dialog.BookmarkEditDialog;
import com.moe.database.HomePage;
import com.moe.dialog.SendToHomepageDialog;
import android.content.res.TypedArray;
import com.moe.utils.Theme;
import com.moe.database.Sqlite;

public class BookmarksFragment extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener,AddFolderDialog.OnSuccessListener,AlertDialog.OnClickListener,BookmarksAdapter.OnItemClickListener,BookmarksAdapter.OnItemLongClickListener,AddDialog.OnAddListener
{
	private ViewPager vp;
	private ViewPagerAdapter vpa;
	//数据
	private ArrayList<RecyclerView> av=new ArrayList<>();
	private ArrayList bookmark_data=new ArrayList(),history_data=new ArrayList();
	//适配器
	private BookmarksAdapter bookmark,history;
	//数据库
	private BookMarks bm;
	private WebHistory wh;
	//下方按钮
	private Button folder,edit;
	//新建文件夹对话框
	private AddFolderDialog afd;
	//是否编辑模式
	private boolean drag=false;
	//历史记录清空提示
	private AlertDialog ad;
	//添加书签
	private AddDialog aid;
	//震动
	private Vibrator vibrator;
	//当前打开的文件夹
	private String dir;
	//长按弹出窗口
	private PopupBookmarkMenu pbm;
	//编辑书签
	private BookmarkEditDialog bed;
	//发送至桌面前改名dialog
	private SendToHomepageDialog sthd;

	public void setCurrent(int p0)
	{
		vp.setCurrentItem(p0);
	}
	public void sendToHomepage(boolean isBookmark, int index)
	{
		if(isBookmark)
			sthd.show((String[])bookmark_data.get(index));
			else
			sthd.show((String[])history_data.get(index));
	}
	public void delete(int index)
	{
		if(bookmark.getMode()==BookmarksAdapter.Mode.FOLDER){
			if(!bookmark_data.get(index).equals("默认"))
				bm.deleteFolder(bookmark_data.get(index).toString());
				loadFolder();
		}else{
			bm.deleteBookmark(((String[])bookmark_data.get(index))[0]);
			loadBookmarks(dir);
		}
	}

	public void edit(int index)
	{
		if(bookmark.getMode()==BookmarksAdapter.Mode.FOLDER){
			if(!bookmark_data.get(index).equals("默认"))
			afd.show(bookmark_data.get(index));
		}else{
			bed.show(((String[])bookmark_data.get(index))[0]);
		}
	}
	public void openInBackground(boolean isBookmark, int index)
	{
		if(isBookmark){
			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW,((String[])bookmark_data.get(index))[0]));
		}else{
			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW,((String[])history_data.get(index))[0]));
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view=inflater.inflate(R.layout.bookmarks_view, container, false);
				return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		/**TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.colorPrimaryDark});
		view.setBackgroundColor(ta.getColor(0,R.color.primary_dark));
		ta.recycle();*/
		Theme.registerBackground(view);
		TabLayout tl=(TabLayout)view.findViewById(R.id.bookmarks_tablayout);
		vp = (ViewPager)view.findViewById(R.id.bookmarks_viewpager);
		vp.setAdapter(vpa = new ViewPagerAdapter(av));
		av.clear();
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
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		vibrator=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
		bm = Sqlite.getInstance(getActivity(),BookMarks.class);
		wh = Sqlite.getInstance(getActivity(),WebHistory.class);
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
		history.setOnItemLongClickListener(this);
		bookmark.setOnItemClickListener(this);
		bookmark.setOnItemLongClickListener(this);
		aid.setTitle("添加书签");
		aid.setButtonText(0,"添加");
		pbm=new PopupBookmarkMenu(this);
		bed=new BookmarkEditDialog(this);
		sthd=new SendToHomepageDialog(this);
		Bundle b=getArguments();
		if(b!=null&&b.getInt("index")==1)
			vp.setCurrentItem(1);
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
		if(!drag){
			pbm.show(vh.itemView,ba==bookmark,bookmark.getMode()==BookmarksAdapter.Mode.FOLDER,vh.getPosition());
		}
		return !drag;
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
public void loadBookmarks(){
	loadBookmarks(this.dir);
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
		if(!isHidden()){
		if(drag){
			toggleMode(false);
			return true;
		}else if(bookmark.getMode()==BookmarksAdapter.Mode.ITEM){
			loadFolder();
			return true;
		}
		}
		return false;
	}
	public class ItemTouch extends ItemTouchHelper.Callback
	{

		@Override
		public boolean isLongPressDragEnabled()
		{
			vibrator.vibrate(50);
			return drag;
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
