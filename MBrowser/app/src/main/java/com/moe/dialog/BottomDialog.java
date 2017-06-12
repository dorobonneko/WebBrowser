package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.content.res.TypedArray;
import android.view.View.OnLongClickListener;

public class BottomDialog extends android.app.Dialog
{
	private List<View> lv=new ArrayList<>();
	private BottomDialog(Context context){
		super(context,R.style.Dialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.PopupWindowAnim);
		super.onCreate(savedInstanceState);
		ScrollView sv=new ScrollView(getContext());
		sv.setBackgroundColor(0xffffffff);
		LinearLayout ll=new LinearLayout(getContext());
		setContentView(sv,new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT));
		ll.setOrientation(ll.VERTICAL);
		sv.addView(ll,new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.MATCH_PARENT));
		LinearLayout.LayoutParams param=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.selectableItemBackground});
		for(View v:lv){
		ll.addView(v,param);
		v.setBackgroundDrawable(ta.getDrawable(0));
		}
		ta.recycle();
	}
	
	public static class Builder implements View.OnClickListener,View.OnLongClickListener{
		private BottomDialog bd;
		private DialogInterface.OnClickListener doc;
		private View.OnLongClickListener vo;
		public Builder(Context context){
			bd=new BottomDialog(context);
		}

		@Override
		public void onClick(View p1)
		{
			doc.onClick(bd,p1.getId());
		}

		@Override
		public boolean onLongClick(View p1)
		{
			if(vo!=null)return vo.onLongClick(p1);
			return false;
		}


		
		public Builder addArrayItem(String... title,final DialogInterface.OnClickListener doc,View.OnLongClickListener vo){
			this.doc=doc;this.vo=vo;
			for(String msg:title){
				Button b=new Button(bd.getContext());
				b.setAllCaps(false);
				b.setText(msg);
				if(doc!=null)
					b.setOnClickListener(this);
				if(vo!=null)
					b.setOnLongClickListener(this);
				b.setId(bd.lv.size());
				bd.lv.add(b);
			}
			
			return this;
		}
		public BottomDialog create(){
			return bd;
		}
		public void show(){
			bd.show();
		}
	}
}
