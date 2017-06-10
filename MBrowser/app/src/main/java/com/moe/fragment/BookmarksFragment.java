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
import com.moe.database.HomePage;
import com.moe.dialog.SendToHomepageDialog;
import android.content.res.TypedArray;
import com.moe.utils.Theme;
import com.moe.database.Sqlite;
import com.moe.entity.Bookmark;
import android.widget.ViewFlipper;
import android.view.animation.AnimationUtils;
import java.util.Collections;
import android.content.DialogInterface;

public class BookmarksFragment extends Fragment implements View.OnClickListener,AddFolderDialog.OnSuccessListener,AlertDialog.OnClickListener,BookmarksAdapter.OnItemClickListener,BookmarksAdapter.OnItemLongClickListener
{
	private ViewPager vp;
	private ViewPagerAdapter vpa;
	//数据
	private ArrayList<View> av=new ArrayList<>();
	private ArrayList history_data=new ArrayList();
	private ArrayList<Bookmark> bookmark_data=new ArrayList<>();
	private ArrayList<Integer> selected=new ArrayList<>();
	//适配器
	private BookmarksAdapter bookmark,history;
	//数据库
	private BookMarks bm;
	private WebHistory wh;
	//新建文件夹对话框
	private AddFolderDialog afd;
	//历史记录清空提示
	private AlertDialog ad;
	//震动
	private Vibrator vibrator;
	//长按弹出窗口
	private PopupBookmarkMenu pbm;
	//发送至桌面前改名dialog
	private SendToHomepageDialog sthd;
	private Bookmark folder;
	private ViewFlipper toolbar;
	private BookmarkEditFragment bef;
	public void delete(int index)
	{
		bm.delete(bookmark_data.remove(index));
		bookmark.notifyItemRemoved(index);
		for (int i=0;i < index;i++)
		{
			Bookmark b=bookmark_data.get(i);
			b.setNo(b.getNo() - 1);
		}
		bookmark.notifyItemRangeChanged(0, index);
	}

	public void edit(int index)
	{
		if (bef == null)bef = new BookmarkEditFragment();
		bef.setArguments(folder, bookmark_data.get(index));
		showEdit();
	}

	public void setCurrent(int p0)
	{
		vp.setCurrentItem(p0);
	}
	public void sendToHomepage(boolean isBookmark, int index)
	{
		if (isBookmark)
		{
			String[] data=new String[2];
			Bookmark b=bookmark_data.get(index);
			data[0] = b.getSummary();
			data[1] = b.getTitle();
			sthd.show(data);
		}
		else
			sthd.show((String[])history_data.get(index));
	}



	public void openInBackground(boolean isBookmark, int index)
	{
		if (isBookmark)
		{
			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW, bookmark_data.get(index).getSummary()));
		}
		else
		{
			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW, ((String[])history_data.get(index))[0]));
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.bookmarks_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		TabLayout tl=(TabLayout)view.findViewById(R.id.bookmarks_tablayout);
		vp = (ViewPager)view.findViewById(R.id.bookmarks_viewpager);
		vp.setAdapter(vpa = new ViewPagerAdapter(av));
		av.clear();
		av.add(LayoutInflater.from(getContext()).inflate(R.layout.bookmark_view, vp, false));
		av.add(LayoutInflater.from(getContext()).inflate(R.layout.history_view, vp, false));
		av.get(0).setTag("书签");
		av.get(1).setTag("历史");
		vp.setOffscreenPageLimit(1);
		vpa.notifyDataSetChanged();
		tl.setupWithViewPager(vp);
		tl.setElevation(2f);
		View view1=av.get(0);
		RecyclerView rv1=(RecyclerView)view1.findViewById(R.id.bookmark_view_recyclerview);
		rv1.setAdapter(bookmark = new BookmarksAdapter(getActivity(), bookmark_data, BookmarksAdapter.Type.BOOKMARK, selected));
		rv1.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv1.addItemDecoration(new CustomDecoration(2));
		//av.get(1).addItemDecoration(new CustomDecoration(5, 0x00000000));
		view1.findViewById(R.id.bookmarksview_newfolder).setOnClickListener(this);
		view1.findViewById(R.id.bookmarksview_edit).setOnClickListener(this);
		ItemTouchHelper ith=new ItemTouchHelper(new ItemTouch());
		ith.attachToRecyclerView(rv1);
		view1.findViewById(R.id.bookmarksview_newBookmark).setOnClickListener(this);
		view1.findViewById(R.id.bookmarksview_newfolder);
		view1.findViewById(R.id.bookmarksview_edit);
		toolbar = (ViewFlipper)view1.findViewById(R.id.bookmarks_view_toolbar);
		toolbar.setAnimation(AnimationUtils.loadAnimation(toolbar.getContext(), R.anim.right_in));
		view1.findViewById(R.id.bookmarks_view_cancel).setOnClickListener(this);
		view1.findViewById(R.id.bookmarks_view_delete).setOnClickListener(this);
		View view2=av.get(1);
		RecyclerView rv2=(RecyclerView)view2.findViewById(R.id.history_view_recyclerview);
		rv2.setAdapter(history = new BookmarksAdapter(getActivity(), history_data, BookmarksAdapter.Type.HISTORY, null));
		rv2.setLayoutManager(new LinearLayoutManager(getActivity()));
		view2.findViewById(R.id.history_view_clear).setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
		bm = Sqlite.getInstance(getActivity(), BookMarks.class);
		wh = Sqlite.getInstance(getActivity(), WebHistory.class);
		afd = new AddFolderDialog(getActivity());
		afd.setOnSuccessListener(this);
		super.onActivityCreated(savedInstanceState);
		loadBookmarksSon(bm.getRoot());
		history_data.addAll(wh.getWebHistory());
		history.notifyDataSetChanged();
		ad = new AlertDialog(getActivity());
		ad.setTitle("是否清空历史记录？");
		ad.setOnClickListener(this);
		history.setOnItemClickListener(this);
		history.setOnItemLongClickListener(this);
		bookmark.setOnItemClickListener(this);
		bookmark.setOnItemLongClickListener(this);
		pbm = new PopupBookmarkMenu(this);
		sthd = new SendToHomepageDialog(this);
		Bundle b=getArguments();
		if (b != null && b.getInt("index") == 1)
			vp.setCurrentItem(1);
	}

	@Override
	public void OnItemClick(BookmarksAdapter ba, com.moe.adapter.BookmarksAdapter.ViewHolder vh)
	{
		if (ba == history)
		{
			EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW, ((String[])history_data.get(vh.getPosition()))[0]));
			EventBus.getDefault().post(MenuOptions.HOME);
		}
		else
		{
			if (toolbar.getDisplayedChild() == 1)
			{
				//编辑模式
				if (selected.indexOf(vh.getAdapterPosition()) == -1)
					selected.add(vh.getAdapterPosition());
				else
					selected.remove((Integer)vh.getAdapterPosition());
				bookmark.notifyItemChanged(vh.getAdapterPosition());
			}
			else
			{
				Bookmark b=bookmark_data.get(vh.getPosition());
				if (b.getType() == 0)
				{
					loadBookmarksSon(bookmark_data.get(vh.getPosition()));
				}
				else
				{
					EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW, b.getSummary()));
					EventBus.getDefault().post(MenuOptions.HOME);
				}
			}
		}
	}

	private void loadBookmarksSon(Bookmark b)
	{
		folder = b;
		bookmark_data.clear();
		bookmark_data.addAll(bm.query(b));
		bookmark.notifyDataSetChanged();
	}

	@Override
	public boolean OnItemLongClick(BookmarksAdapter ba, com.moe.adapter.BookmarksAdapter.ViewHolder vh)
	{
		if (toolbar.getDisplayedChild() == 0)
		{
			int type=0;
			if (vp.getCurrentItem() == 0)
				type = bookmark_data.get(vh.getPosition()).getType();
			pbm.show(vh.itemView, vp.getCurrentItem(), type, vh.getPosition());
		}
		return toolbar.getDisplayedChild() == 0;
	}






	@Override
	public void onClick(View p1)
	{
		switch (p1.getId())
		{
			case R.id.bookmarksview_newfolder:
				afd.show(folder);
				break;
			case R.id.bookmarksview_newBookmark:
				//新建书签
				if (bef == null)bef = new BookmarkEditFragment();
				bef.setArguments(folder, null);
				showEdit();
				break;
			case R.id.bookmarksview_edit:
				toolbar.setDisplayedChild(1);
				break;
			case R.id.bookmarks_view_cancel:
				toolbar.setDisplayedChild(0);
				selected.clear();
				bookmark.notifyDataSetChanged();
				break;
			case R.id.bookmarks_view_delete:
				if(!selected.isEmpty())
				new android.support.v7.app.AlertDialog.Builder(getActivity()).setMessage("确认删除所选书签和文件夹？")
				.setPositiveButton("取消",null)
					.setNegativeButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							for(Integer ii:selected){
							bm.delete(bookmark_data.remove(ii.intValue()));
							}
							selected.clear();
							bookmark.notifyDataSetChanged();
						}
					}).show();
				break;
			case R.id.bookmarks_view_export:
				break;
			case R.id.bookmarks_view_import:
				break;
			case R.id.history_view_clear:
				if (history_data.size() != 0)
					ad.show();
				break;
			case 0:
				//清空历史记录
				wh.clearWebHistory();
				history_data.clear();
				history.notifyDataSetChanged();
				break;
		}
	}

	private void showEdit()
	{
		if (!bef.isAdded())
			getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).add(R.id.bookmarks_view_float, bef).commit();
		else
			getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_in, 0).show(bef).commit();

	}
	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if (hidden)
		{
			if (toolbar.getDisplayedChild() == 1)
			{
				toolbar.setDisplayedChild(0);
				selected.clear();
				bookmark.notifyDataSetChanged();
			}
			vp.setCurrentItem(0);
		}
		else
		{
			loadBookmarksSon(folder);
			history_data.clear();
			history_data.addAll(wh.getWebHistory());
			history.notifyDataSetChanged();
		}
		super.onHiddenChanged(hidden);
	}
	public void refresh()
	{
		loadBookmarksSon(folder);
	}
	@Override
	public void OnSuccess(Bookmark name)
	{
		loadBookmarksSon(folder);
	}

	@Override
	public boolean onBackPressed()
	{
		if (!isHidden())
		{
			if (bef != null && !bef.isHidden() && bef.onBackPressed())
			{
				return true;
			}
			switch (vp.getCurrentItem())
			{
				case 0:

					if (toolbar.getDisplayedChild() == 1)
					{
						toolbar.setDisplayedChild(0);
						selected.clear();
						bookmark.notifyDataSetChanged();
						return true;
					}
					else
					{
						if (folder.getParent() == bm.getRoot().getParent())
						{
							return false;
						}
						else
						{
							Bookmark b=bm.queryWithSon(folder.getParent());
							if (b == null)b = bm.getRoot();
							loadBookmarksSon(b);
							return true;
						}
					}
				case 1:
					return false;
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
			return toolbar.getDisplayedChild() == 1 && selected.isEmpty();
		}

		@Override
		public int getMovementFlags(RecyclerView p1, RecyclerView.ViewHolder p2)
		{
			return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
		}

		@Override
		public boolean onMove(RecyclerView p1, RecyclerView.ViewHolder p2, RecyclerView.ViewHolder p3)
		{//no交换
			int fromPos=p2.getAdapterPosition(),toPos=p3.getAdapterPosition();
			Bookmark src=bookmark_data.get(fromPos);
			Bookmark dsc=bookmark_data.get(toPos);
			int no=dsc.getNo();
			dsc.setNo(src.getNo());
			src.setNo(no);
			bm.trimNo(src);
			bm.trimNo(dsc);
			Collections.swap(bookmark_data, fromPos, toPos);
			bookmark.notifyItemMoved(fromPos, toPos);
			return true;
		}

		@Override
		public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y)
		{
		}
		@Override
		public void onSwiped(RecyclerView.ViewHolder p1, int p2)
		{

		}
	}
}
