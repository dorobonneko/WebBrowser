package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import com.moe.widget.CircleImageView;
import com.moe.fragment.SkinFragment;
import android.content.SharedPreferences;
import android.graphics.Color;

public class SkinAdapter extends RecyclerView.Adapter<SkinAdapter.ViewHolder>
{
	private SkinFragment skinFragment;
	private CharSequence[] color;
	private SharedPreferences shared;
public SkinAdapter(SkinFragment skinfragment){
	this.skinFragment=skinfragment;
	color=skinfragment.getResources().getTextArray(R.array.skin_color);
	shared=skinfragment.getContext().getSharedPreferences("moe",0);
}

public int getColor(int position)
{
	return Color.parseColor(color[position].toString());
}
	@Override
	public SkinAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(new CircleImageView(p1.getContext()));
	}

	@Override
	public void onBindViewHolder(SkinAdapter.ViewHolder p1, int p2)
	{
		p1.civ.setBackgroundColor(Color.parseColor(color[p2].toString()));
		if(shared.getInt("color",0)==p2)
			p1.civ.setSelected(true);
		else
		p1.civ.setSelected(false);
	}
	

	
	@Override
	public int getItemCount()
	{
		return color.length;
	}
public class ViewHolder extends RecyclerView.ViewHolder{
	public CircleImageView civ;
	public ViewHolder(View v){
		super(v);
		civ=(CircleImageView)v;
		v.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if(oisl!=null)oisl.onItemSelected(getPosition());
				}
			});
	}
}
public void setOnItemSelectedListener(OnItemSelectedListener o){
	oisl=o;
}
private OnItemSelectedListener oisl;
	public abstract interface OnItemSelectedListener{
		void onItemSelected(int position);
	}
}
