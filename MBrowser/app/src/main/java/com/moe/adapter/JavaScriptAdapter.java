package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.CompoundButton;
import com.moe.database.Sqlite;
import com.moe.database.JavaScript;
import android.content.res.TypedArray;

public class JavaScriptAdapter extends RecyclerView.Adapter<JavaScriptAdapter.ViewHolder>
{

	@Override
	public JavaScriptAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(LayoutInflater.from(p1.getContext()).inflate(R.layout.javascript_item_view,null));
	}

	@Override
	public void onBindViewHolder(JavaScriptAdapter.ViewHolder p1, int p2)
	{
		p1.name.setText(list.get(p2)[1].toString());
		p1._switch.setChecked(list.get(p2)[3]);
	}
	
	private List<Object[]> list;
	public JavaScriptAdapter(List<Object[]> list){
		this.list=list;
	}
	

	@Override
	public int getItemCount()
	{
		return list.size();
	}
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,Switch.OnCheckedChangeListener{
		private TextView name;
		private Switch _switch;
		public ViewHolder(View v){
			super(v);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.listPreferredItemHeightSmall});
			v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
			ta.recycle();
			name=(TextView)v.findViewById(R.id.javascript_item_view_name);
			v.findViewById(R.id.javascript_item_view_edit).setOnClickListener(this);
			v.findViewById(R.id.javascript_item_viewImage_run).setOnClickListener(this);
			_switch=(Switch)v.findViewById(R.id.javascript_item_view_autorun);
			_switch.setOnCheckedChangeListener(this);
		}

		@Override
		public void onClick(View p1)
		{
			switch(p1.getId()){
				case R.id.javascript_item_view_edit:
					if(ocl!=null)ocl.onClick(getPosition(),0);
					break;
				case R.id.javascript_item_viewImage_run:
					if(ocl!=null)ocl.onClick(getPosition(),1);
					break;
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2)
		{
			Sqlite.getInstance(p1.getContext(),JavaScript.class).updateScriptState(list.get(getPosition())[0],p2);
		}


		
	}
	private OnClickListener ocl;
	public void setOnClickalistener(OnClickListener o){
		ocl=o;
	}
	public abstract interface OnClickListener{
		void onClick(int position,int type);
	}
}
