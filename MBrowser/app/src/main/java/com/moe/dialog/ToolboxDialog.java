package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import com.moe.adapter.ToolboxAdapter;

public class ToolboxDialog extends android.app.Dialog
{
	private RecyclerView rv;
	private ToolboxAdapter ta;
	public ToolboxDialog(Context context){
		super(context,R.style.searchDialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.PopupWindowAnim);
		super.onCreate(savedInstanceState);
		setContentView(rv=new RecyclerView(getContext()),new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT));
		GridLayoutManager glm=new GridLayoutManager(getContext(),5);
		glm.setAutoMeasureEnabled(true);
		rv.setLayoutManager(glm);
		rv.setAdapter(ta=new ToolboxAdapter());
		ta.notifyDataSetChanged();
	}
}
