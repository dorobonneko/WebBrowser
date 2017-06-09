package com.moe.view;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import com.moe.fragment.BookmarksFragment;
import android.view.Gravity;
import android.graphics.drawable.BitmapDrawable;

public class PopupBookmarkMenu extends android.widget.PopupWindow implements View.OnClickListener
{
	private int item_height=42;
	private BookmarksFragment context;
	private View open,edit,send,delete;
	private boolean isBookmark;
	private int index;
	public PopupBookmarkMenu(BookmarksFragment bf){
		super(bf.getActivity());
		this.context=bf;
		setWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62, context.getResources().getDisplayMetrics()));
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new BitmapDrawable());
		View v=LayoutInflater.from(context.getActivity()).inflate(R.layout.bookmark_menu,null);
		setContentView(v);
		open=v.findViewById(R.id.bookmark_menu_openInBackground);
		edit=v.findViewById(R.id.bookmark_menu_edit);
		send=v.findViewById(R.id.bookmark_menu_sendToHomepage);
		delete=v.findViewById(R.id.bookmark_menu_deleteBookmark);
		open.setOnClickListener(this);
		edit.setOnClickListener(this);
		send.setOnClickListener(this);
		delete.setOnClickListener(this);
	}
	public void show(View v,int isBookMark,int type,int index){
		this.isBookmark=isBookMark==0;
		this.index=index;
		if(this.isBookmark){
			if(type==0)
			{toggle(0);
				setHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item_height*2, context.getResources().getDisplayMetrics()));
				
			}else{
			toggle(2);
			setHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item_height*4, context.getResources().getDisplayMetrics()));
		}}else{
			toggle(1);
			setHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item_height*2, context.getResources().getDisplayMetrics()));
		}
		showAsDropDown(v,v.getWidth()/2,-v.getHeight());
		}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.bookmark_menu_openInBackground:
				context.openInBackground(isBookmark,index);
				break;
			case R.id.bookmark_menu_edit:
				context.edit(index);
				break;
			case R.id.bookmark_menu_deleteBookmark:
				context.delete(index);
				break;
			case R.id.bookmark_menu_sendToHomepage:
				context.sendToHomepage(isBookmark,index);
				break;
		}
		dismiss();
	}
private void toggle(int mode){
	switch(mode){
		case 0://文件夹
			this.open.setVisibility(View.GONE);
			this.send.setVisibility(View.GONE);
			this.delete.setVisibility(View.VISIBLE);
			this.edit.setVisibility(View.VISIBLE);
			break;
		case 1://历史记录
			this.open.setVisibility(View.VISIBLE);
			this.delete.setVisibility(View.GONE);
			this.send.setVisibility(View.VISIBLE);
			this.edit.setVisibility(View.GONE);
			break;
		case 2:
			this.open.setVisibility(View.VISIBLE);
			this.delete.setVisibility(View.VISIBLE);
			this.send.setVisibility(View.VISIBLE);
			this.edit.setVisibility(View.VISIBLE);
			break;
	}
}
	
}
