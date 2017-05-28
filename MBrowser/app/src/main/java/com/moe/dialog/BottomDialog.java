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

public class BottomDialog extends android.app.Dialog
{
	private List<View> lv=new ArrayList<>();
	private BottomDialog(Context context){
		super(context,R.style.searchDialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.PopupWindowAnim);
		super.onCreate(savedInstanceState);
		ScrollView sv=new ScrollView(getContext());
		LinearLayout ll=new LinearLayout(getContext());
		setContentView(sv,new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT));
		ll.setOrientation(ll.VERTICAL);
		sv.addView(ll,new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.MATCH_PARENT));
		for(View v:lv)
		ll.addView(v);
	}
	
	public static class Builder{
		private BottomDialog bd;
		public Builder(Context context){
			bd=new BottomDialog(context);
		}
		public Builder addArrayItem(String... title,final DialogInterface.OnClickListener doc){
			for(String msg:title){
				Button b=new Button(bd.getContext());
				b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
				b.setAllCaps(false);
				b.setText(msg);
				if(doc!=null)
					b.setOnClickListener(new View.OnClickListener(){

							@Override
							public void onClick(View p1)
							{
								doc.onClick(bd,p1.getId());
							}
						});
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
